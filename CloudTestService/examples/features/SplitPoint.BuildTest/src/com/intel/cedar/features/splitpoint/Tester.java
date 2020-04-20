package com.intel.cedar.features.splitpoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.features.splitpoint.sanity.SanityTester;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.tasklet.AbstractTaskRunner;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.SimpleTaskItem;

public class Tester extends AbstractTaskRunner {
	Logger LOG = LoggerFactory.getLogger(Tester.class);
	
	private static final long serialVersionUID = 733863306707750102L;
	private String resultFolder = null;
	private String buildLog = "build.log";
	private String startupLog = "startup.log";
	private String sanitytestLog = "sanitytest.log";
	private String packageZip = "package.zip";
	private String userdirZip = "userdir.zip";
	
	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
		List<ITaskItem> items = new ArrayList<ITaskItem>();
		try{
			String username = "lab_xmldev";
			String password = "qnn8S*NP";
			try{
				username = env.getVariable("svn_username").getValue();
				password = env.getVariable("svn_password").getValue();
			}
			catch(Exception e){			
			}
			String url = env.getVariable("svn_url").getValue();
			String rev = env.getVariable("svn_rev").getValue();
			SimpleTaskItem item = new SimpleTaskItem();
			item.setProperty("svn_url", url);
			item.setProperty("svn_rev", rev);
			item.setProperty("svn_username", username);
			item.setProperty("svn_password", password);
			items.add(item);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return items;
	}
	
	protected void limitGwtcWorkers(File file) throws Exception{
		File bakFile = new File(file.getName() + ".bak");
		file.renameTo(bakFile);
		FileOutputStream fos = new FileOutputStream(file);
		FileInputStream fis = new FileInputStream(bakFile);
		OutputStreamWriter writer = new OutputStreamWriter(fos);
		InputStreamReader reader = new InputStreamReader(fis);
		BufferedWriter wr = new BufferedWriter(writer);
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		while((line = br.readLine()) != null){
			if(line.contains("<arg value=\"8\"/>")){
				wr.write("<arg value=\"2\"/>");
			}
			else{
				wr.write(line);
			}
			wr.newLine();
		}
		br.close();
		wr.close();
	}

	protected void replaceURL(File file) throws Exception{
		File bakFile = new File(file.getName() + ".bak");
		file.renameTo(bakFile);
		FileOutputStream fos = new FileOutputStream(file);
		FileInputStream fis = new FileInputStream(bakFile);
		OutputStreamWriter writer = new OutputStreamWriter(fos);
		InputStreamReader reader = new InputStreamReader(fis);
		BufferedWriter wr = new BufferedWriter(writer);
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		while((line = br.readLine()) != null){
			if(line.startsWith("URL")){
				wr.write("URL=https://localhost:8443");
			}
			else{
				wr.write(line);
			}
			wr.newLine();
		}
		br.close();
		wr.close();
	}
	
	protected void findAndKillProcess(Environment env)  throws Exception{
		List<String> commands = new ArrayList<String>();
		if(!env.getOSName().contains("Windows")){
			commands.add("pid=`ps -ef | grep " + resultFolder + " | grep nobody  | awk -F' '  '{print $2}'`");
			commands.add("kill -9 $pid");
			env.execute(commands.toArray(new String[]{}));
		}
	}
	
	protected boolean waitForServerStartup(String hostname, int port) throws Exception{
		int count = 0;
		while(count < 60){
	        try {
	            Socket s = new Socket(hostname, port);
	            s.close();
	            return true;
	        } catch (Exception e) {
	        }
	        Thread.sleep(5000);
	        count++;
		}
		return false;
	}
	
	protected boolean waitForServerShutdown(String hostname, int port) throws Exception{
		int count = 0;
		while(count < 60){
	        try {
	            Socket s = new Socket(hostname, port);
	            s.close();
	        } catch (Exception e) {
	        	return true;
	        }
	        Thread.sleep(5000);
	        count++;
		}
		return false;
	}
	
	@Override
	public void onStart(Environment env){
		try {
			env.extractResource("resource/ant.zip");
			this.extractZipBundle("ant.zip", "ant");
			if(!env.getOSName().contains("Windows")){
				env.execute("chmod +x ant/bin/ant");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected boolean startECA(final Environment env) throws Exception{
		LOG.info("Starting ECA");
		if(!env.getOSName().contains("Windows")){
			env.asyncExec(new Runnable(){
				@Override
				public void run() {
					try{
						List<String> commands = new ArrayList<String>();
						if(new File("/opt/jdk").isDirectory())
							commands.add("export JAVA_HOME=/opt/jdk");
						else if(new File("/opt/java/jdk1.6.0_22").isDirectory())
							commands.add("export JAVA_HOME=/opt/java/jdk1.6.0_22");
						commands.add("export PATH=.:$JAVA_HOME/bin:$PATH");
						commands.add("cd " + resultFolder + java.io.File.separator + 
								"scr" + java.io.File.separator + 
								"bin");
						commands.add("sh service.sh chown");
						commands.add("sh eca360sso.sh start");
						IFile startupLogFile = env.getStorageRoot().getFile(startupLog);
						if(!startupLogFile.exist())
							startupLogFile.create();
						env.execute(commands.toArray(new String[]{}), startupLogFile);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}							
			});
		}
		else{
			env.asyncExec(new Runnable(){
				@Override
				public void run() {
					try{
						List<String> commands = new ArrayList<String>();
						commands.add("cd " + resultFolder + java.io.File.separator + 
								"scr" + java.io.File.separator + 
								"bin");
						commands.add("eca360sso.cmd start");
						IFile startupLogFile = env.getStorageRoot().getFile(startupLog);
						if(!startupLogFile.exist())
							startupLogFile.create();
						env.execute(commands.toArray(new String[]{}), startupLogFile);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}							
			});
		}
		Thread.sleep(60000);		
		if(waitForServerStartup("localhost", 8443)){
			LOG.info("ECA started");
			return true;
		}
		else{
			LOG.info("ECA failed to start");
			return false;
		}
	}
	
	protected boolean stopECA(final Environment env) throws Exception{
		LOG.info("Stopping ECA");
		if(!env.getOSName().contains("Windows")){
			env.asyncExec(new Runnable(){
				@Override
				public void run() {
					try{
						List<String> commands = new ArrayList<String>();
						if(new File("/opt/jdk").isDirectory())
							commands.add("export JAVA_HOME=/opt/jdk");
						else if(new File("/opt/java/jdk1.6.0_22").isDirectory())
							commands.add("export JAVA_HOME=/opt/java/jdk1.6.0_22");
						commands.add("export PATH=.:$JAVA_HOME/bin:$PATH");
						commands.add("cd " + resultFolder + java.io.File.separator + 
								"scr" + java.io.File.separator + 
								"bin");
						commands.add("sh eca360sso.sh stop");
						if(env.execute(commands.toArray(new String[]{})) != 0){
							LOG.info("Failed to stop ECA via CLI");
							findAndKillProcess(env);							
						}
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}							
			});
		}
		else{
			env.asyncExec(new Runnable(){
				@Override
				public void run() {
					try{
						List<String> commands = new ArrayList<String>();
						commands.add("cd " + resultFolder + java.io.File.separator + 
								"scr" + java.io.File.separator + 
								"bin");
						commands.add("eca360sso.cmd stop");
						if(env.execute(commands.toArray(new String[]{})) != 0){
							LOG.info("Failed to stop ECA via CLI");
						}
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}							
			});
		}
		Thread.sleep(60000);
		if(waitForServerShutdown("localhost", 8443)){
			LOG.info("ECA stopped");
			return true;
		}
		else{
			LOG.info("ECA failed to stop");
			return false;
		}
	}
	
	protected void startCurlTest(Environment env, Writer output) throws Exception{
		List<String> commands = new ArrayList<String>();
		if(new File("/opt/jdk").isDirectory())
			commands.add("export JAVA_HOME=/opt/jdk");
		else if(new File("/opt/java/jdk1.6.0_22").isDirectory())
			commands.add("export JAVA_HOME=/opt/java/jdk1.6.0_22");
		commands.add("export PATH=.:$JAVA_HOME/bin:$PATH");
		commands.add("cd " + resultFolder + java.io.File.separator + 
				"build" + java.io.File.separator + 
				"landmark_test");
		commands.add("sh run.sh -config=config/test.config -sumReport=lm_summary.xml");
		env.execute(commands.toArray(new String[]{}), output);
	}
	
	protected List<String> getRequiredCommands(Environment env){
		List<String> commands = new ArrayList<String>();
		if(!env.getOSName().contains("Windows")){
			if(new File("/opt/jdk").isDirectory())
				commands.add("export JAVA_HOME=/opt/jdk");
			else if(new File("/opt/java/jdk1.6.0_22").isDirectory())
				commands.add("export JAVA_HOME=/opt/java/jdk1.6.0_22");
			commands.add("export ANT_HOME=" + env.getCWD() + "/ant");
			commands.add("export PATH=.:$JAVA_HOME/bin:$ANT_HOME/bin:$PATH");
		}
		else{
			commands.add("set ANT_HOME=" + env.getCWD() + "\\ant");
			commands.add("set PATH=%ANT_HOME%\\bin;%PATH%");
		}
		return commands;
	}
	
	protected boolean importConfiguration(Environment env, String domainName, Writer output) throws Exception {
		String configFile = "SplitPointConfiguration.zip";
		try{
		    String url = env.getVariable(resultFolder + "_config_archive").getValue();
			IFile file = env.getFileByURI(URI.create(url));
			if(file.exist()){
				env.copyFile(file, new File(configFile));
			}
		}
		catch(Exception e){
			return false;
		}
		
		//call configuration importing tool command
		//cd %INSTALLDIR%\osgi
		//java -cp ..\webapps\splat.war\splat\WEB-INF\classes;..\webapps\splat.war\splat\WEB-INF\lib\gxt.jar;..\webapps\splat.war\splat\WEB-INF\lib\bcprov-ext-jdk15-1.40.jar;.\bundles\com.intel.splat.config_1.0.0.jar;.\bundles\com.intel.splat.xacml_1.0.0.jar;.\bundles\slf4j-logback.jar;..\..\codeCoverage\emma2.jar com.intel.splat.identityservice.configuration.ConfigurationManager file:\\C:\xhao1\trunk_test_verify\scr\osgi\management\configuration\ C:\nightly\QAFrame\cruiseNightly\testcases\EndToEndTest\configs\SplitPointConfiguration.zip
		String sproot = new File(resultFolder).getAbsolutePath();
		List<String> commands = getRequiredCommands(env);
		String directory = sproot + File.separator + "scr" + File.separator + "osgi";
    	String domainPath = "default";
    	if(domainName != null && domainName.length() > 0){
    		domainPath = domainName;
    	}
    	String absScrDirPath = sproot + "\\" + "scr";
    	String absDomainPath = sproot + "\\" + "userdir" + "\\" + domainPath;
		String cmdline = "java -Dcom.intel.soae.product=splat -cp ..\\webapps\\splat.war\\splat\\WEB-INF\\classes;..\\webapps\\splat.war\\splat\\WEB-INF\\lib\\gxt.jar;..\\bin\\cli.jar" + " com.intel.splat.identityservice.configuration.ConfigurationManager "+ absScrDirPath + " " + absDomainPath + " " + env.getCWD() + "\\" + configFile;
		cmdline = cmdline.replace('\\', File.separatorChar);
		cmdline = cmdline.replace(';', File.pathSeparatorChar);
		LOG.info(cmdline);
		 
		commands.add("cd " + directory);
		commands.add(cmdline);

		if(env.execute(commands.toArray(new String[]{}), output) != 0){
			LOG.error("Failed to import split point configuration!");
			return false;
		}
		return true;		
	}
	
	protected void fetchSource(Environment env, String url, String rev, String username, String password) throws Exception{
		String archive_url = null;
		try{
			archive_url = env.getVariable(resultFolder + "_archive").getValue();
		}
		catch(Exception e){			
		}
		if(archive_url != null){
			IFile archiveFile = env.getFileByURI(URI.create(archive_url));
			if(archiveFile.exist()){
				this.extractZipBundle(archiveFile, ".");
				this.svnUpdate(url, username, password, rev, resultFolder);
			}
		}
		else{
			this.svnCheckOut(url, username, password, rev, resultFolder);
		}
	}
	
	@Override
	public ResultID run(ITaskItem ti, final Writer output, final Environment env) {
		SimpleTaskItem item = (SimpleTaskItem)ti;
		String url = item.getProperty("svn_url");
		String rev = item.getProperty("svn_rev");
		String username = item.getProperty("svn_username");
		String password = item.getProperty("svn_password");
		int ret = 1;
		try {
			// check out source code
			resultFolder = url.substring(url.lastIndexOf("/") + 1);
			fetchSource(env, url, rev, username, password);
			
			// build
			limitGwtcWorkers(new File(resultFolder + java.io.File.separator + "webapps" + java.io.File.separator + "splat.war" + java.io.File.separator + "build.xml"));
			List<String> commands = getRequiredCommands(env);
			commands.add("cd " + resultFolder + java.io.File.separator + "build");
			if(resultFolder.contains("landmark")){
				commands.add("ant build.landmark");
			}
			else{
				commands.add("ant build");
			}
			IFile buildLogFile = env.getStorageRoot().getFile(buildLog);
			if(buildLogFile.create())
				ret = env.execute(commands.toArray(new String[]{}), buildLogFile);
			
			if(ret == 0){
				List<File> files = new ArrayList<File>();
				files.add(new File(resultFolder + java.io.File.separator + "scr"));
				files.add(new File(resultFolder + java.io.File.separator + "userdir"));
				files.add(new File(resultFolder + java.io.File.separator + "service"));
				this.createZipBundle(packageZip, files);
				
				if(new File(packageZip).exists()){
					IFile packageFile = env.getStorageRoot().getFile(packageZip);
					if(packageFile.create())
						packageFile.setContents(new FileInputStream(packageZip));
				}
				
				// test 
				commands = getRequiredCommands(env);
				commands.add("cd " + resultFolder + java.io.File.separator + "build");
				commands.add("ant test");
				env.execute(commands.toArray(new String[]{}), output);
				
				String junitFolderStr = resultFolder + java.io.File.separator + "junit";
				if(new File(junitFolderStr).exists()){
					this.createZipBundle("junit.zip", junitFolderStr);
					IFile junit = env.getStorageRoot().getFile("junit.zip");
					if(junit.create())
						junit.setContents(new FileInputStream("junit.zip"));
				}
				
				if(startECA(env)){
					// prepare userdir
					if(stopECA(env)){
						boolean canDoSanityTest = importConfiguration(env, null, output);
						if(startECA(env)){
							if(!env.getOSName().contains("Windows")){
								File lmTestDir = new File(resultFolder + java.io.File.separator + "build" + java.io.File.separator + 
										"landmark_test");
								if(lmTestDir.isDirectory()){
									File lmTestConfig = new File(lmTestDir, "env.sh");
									replaceURL(lmTestConfig);
									startCurlTest(env, output);
									
									String lmSumStr = resultFolder + java.io.File.separator + 
									"build" + java.io.File.separator + 
									"landmark_test" + java.io.File.separator + 
									"lm_summary.xml";
									if(new File(lmSumStr).exists()){
										IFile lmSum = env.getStorageRoot().getFile("lm_summary.xml");
										if(lmSum.create())
											lmSum.setContents(new FileInputStream(lmSumStr));
									}
								}
							}						
							
							if(canDoSanityTest){
								String sanity_config_url = null;
								InputStream config = null;
								try{
									sanity_config_url = env.getVariable(resultFolder + "_sanity_test_config").getValue();
									if(sanity_config_url != null){
										IFile sanityConfigFile = env.getFileByURI(URI.create(sanity_config_url));
										if(sanityConfigFile.exist())
											config = sanityConfigFile.getContents();
									}
								}
								catch(Exception e){			
								}
								if(new SanityTester().run(config)){
									LOG.info("Sanity Test Passed");
								}		
							}
							
							stopECA(env);
						}
					}
				}
				
				files = new ArrayList<File>();
				files.add(new File(resultFolder + java.io.File.separator + "userdir"));
				this.createZipBundle(userdirZip, files);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return ret == 0 ? ResultID.Passed : ResultID.Failed;
	}

	@Override
	public void onFinish(Environment env) {
		try{				
			if(new File(userdirZip).exists()){
				IFile userdirFile = env.getStorageRoot().getFile(userdirZip);
				if(userdirFile.create())
					userdirFile.setContents(new FileInputStream(userdirZip));
			}
			
			if(new File(sanitytestLog).exists()){
				IFile startupLogFile = env.getStorageRoot().getFile(sanitytestLog);
				if(startupLogFile.create())
					startupLogFile.setContents(new FileInputStream(sanitytestLog));
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
