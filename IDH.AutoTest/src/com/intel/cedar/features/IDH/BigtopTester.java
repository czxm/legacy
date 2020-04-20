package com.intel.cedar.features.IDH;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.SimpleTaskItem;
import com.intel.cedar.tasklet.SimpleTaskRunner;

public class BigtopTester extends SimpleTaskRunner {
	Logger LOG = LoggerFactory.getLogger(BigtopTester.class);
	
	private static final long serialVersionUID = 733863306707750102L;
	
	private String username;
	private String password;
	private String test_url;
	private String test_rev;
	private String resultFolder = "test";
	
	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
        List<ITaskItem> items = new ArrayList<ITaskItem>();
        String imTestEnabledVar = checkEnvVar(env, "imTestEnabled", "false");
        Boolean imTestEnabled = Boolean.parseBoolean(imTestEnabledVar);   
	    try{
	        String bigtopTest = env.getVariable("bigtopTest").getValue().toLowerCase();
            String mr1_version = "null";
            String hadoop_version = "null";
            String hbase_version = "null";
            String pig_version = "null";
            String sqoop_version = "null";;
            List<String> testcomponents = env.getVariable("components").getValues();
            if(!bigtopTest.equals("skip")){
                List<String> components = AutoTestFeature.loadComponents(env);
                for(String c : components){
                    if(c.contains("mr1")){
                        mr1_version = c.replace("mr1-", "");
                    }
                    else if(c.contains("hadoop")){
                        hadoop_version = c.replace("hadoop-", "");
                    }
                    else if(c.contains("hbase")){
                        hbase_version = c.replace("hbase-", "");
                    }
                    else if(c.contains("pig")){
                        pig_version = c.replace("pig-", "");
                    }
                    else if(c.contains("sqoop")){
                        sqoop_version = c.replace("sqoop-", "");
                    }
                }
                if(imTestEnabled){
                    testcomponents.add("manager");
                }                
                for(String c : components){
                    boolean include = false;
                    for(String t : testcomponents){
                        if(c.contains(t)){
                            include = true;
                            break;
                        }
                    }
                    if(include){
                        SimpleTaskItem i = new SimpleTaskItem();
                        i.setValue(c + " bigtop");
                        i.setProperty("hadoop_version", hadoop_version);
                        i.setProperty("mr1_version", mr1_version);
                        i.setProperty("hbase_version", hbase_version);
                        i.setProperty("pig_version", pig_version);
                        i.setProperty("sqoop_version", sqoop_version);
                        i.setProperty("basename", AutoTestFeature.getBaseName(c));
                        i.setProperty("component", c);
                        i.setProperty("isCheckIn", Boolean.toString(bigtopTest.equals("commit")));
                        items.add(i);
                    }
                }
            }
	    }
	    catch(Exception e){	        
	    }
		return items;
	}
	
    @Override
    public void onStart(Environment env) {
        try{                
            username = "lab_xmldev";
            password = "qnn8S*NP";
            try{
                username = env.getVariable("svn_username").getValue();
                password = env.getVariable("svn_password").getValue();
            }
            catch(Exception e){         
            }
            test_url = env.getVariable("test_svn_url").getValue();
            if(test_url == null)
                test_url = "https://sh-ssvn.sh.intel.com/ssg_repos/svn_hadoop/hadoop/hadoop/QA/test";
            
            test_rev = env.getVariable("test_svn_rev").getValue();
            if (test_rev == null)
                test_rev = this.svnGetLatestRevision(test_url, username, password);
            
            String archive_url = null;
            IFile archiveFile = null;
            try{
                archive_url = env.getVariable(resultFolder + "_archive").getValue();
            }
            catch(Exception e){         
            }
            
            if(archive_url != null){
                archiveFile = env.getFileByURI(URI.create(archive_url));
                if(!archiveFile.exist()){
                    archiveFile = null;
                }
            }
            
            if(archiveFile != null){
                this.extractZipBundle(archiveFile, ".");
            }
            else{
                // next we check the VM's local storage
                String theFolder = null;
                for(String f : new File("/home/user").list()){
                    if(test_url.endsWith(f)){
                        theFolder = f;
                        break;
                    }
                }
                if(theFolder != null){
                    //new File("/home/user/" + theFolder).renameTo(new File(resultFolder));
                }
            }
            
            try{
                this.svnUpdate(test_url, username, password, test_rev, resultFolder);
            }
            catch(Exception e){
                e.printStackTrace();
                File rf = new File(resultFolder);
                if(rf.exists()){
                    env.execute("rm -rf " + resultFolder);
                }
                this.svnCheckOut(test_url, username, password, test_rev, resultFolder);
            }
            
            IFile patch_file = null;
            try{
                String patch = env.getVariable("Bigtop_patch").getValue();
                if(patch != null && patch.length() > 0){
                    patch_file = env.getStorageRoot().getFile(URI.create(patch));
                }
            }
            catch(Exception e){                
            }
            if(patch_file != null && patch_file.exist()){
                File localpatch = new File("bigtop_patch");
                env.copyFile(patch_file, localpatch);
                env.execute(new String[]{"cd " + resultFolder, "patch -p0 < " + localpatch.getAbsolutePath()});
            }
            
            env.execute("chown -R user.user " + resultFolder);
            
            //File thePackageFolder = new File("output");
            //thePackageFolder.mkdir();
            //IFolder packageRoot = env.getStorageRoot().getFolder("output");
            //env.copyFolder(packageRoot, thePackageFolder);
			////String release = checkEnvVar(env, "iso_release_version", "");
			////String product = release.startsWith(ConstHelper.ISO_RELEASE_VERSION_3)?"IM3":"IM2";
            ////String package_file_full_name=env.getFeatureProperty("package_file_full_name", product);
            String plf=checkEnvVar(env, "platform_info_for_repo", "AAAAAA");
            String package_file_full_name=checkEnvVar(env, "package_file_full_name", "BBBBBB");
            String compareResultEnvVarValue=checkEnvVar(env, "compareResult", "CCCCCC");
			//for debug
            StringBuilder ss=new StringBuilder();
            ss.append("platform_info_for_repo:" + plf);
            ss.append("\n");
            ss.append("package_file_full_name:" + package_file_full_name);
            ss.append("\n");  
            ss.append("compareResult:" + compareResultEnvVarValue);
            ss.append("\n"); 
            IFile envLogFile = env.getStorageRoot().getFile("env_var_log_for_DEBUG.txt");
			if(!envLogFile.exist()){
				if(!envLogFile.create())
					LOG.error("envLogFile (on shared storage) creation failed!");
			}
			envLogFile.setContents(new ByteArrayInputStream(ss.toString().getBytes()));
			
            if(package_file_full_name != null && !package_file_full_name.isEmpty()){
            	File dstFile = new File(package_file_full_name);
            	if(dstFile.createNewFile()){
                	IFile srcFile = env.getStorageRoot().getFile(package_file_full_name);
                	if(srcFile.exist()){
                		env.copyFile(srcFile, dstFile);
                	}else{
                		LOG.error("Could not find installation package on shared storage!!");
                	}
            	}
            }else{
            	LOG.error("Invalid env var 'package_file_full_name is'!!");
            }
            
			// check out silent install script
            String silent_install_script_name=checkEnvVar(env, "silent_install_script_name", "");
			File silentInstallScriptFile = new File(silent_install_script_name);
			if(silentInstallScriptFile.exists()){
				deleteFolderAndContents(silentInstallScriptFile);
			}
			String conf_tmpl_file_name=checkEnvVar(env, "conf_tmpl_file_name", "");
			File confTmplFile = new File(conf_tmpl_file_name);
			if(confTmplFile.exists()){
				deleteFolderAndContents(confTmplFile);
			}
			String install_script_zip_url=checkEnvVar(env, "install_script_zip_url", "");
			IFile installScriptZipFile = env.getFileByURI(URI.create(install_script_zip_url));
			this.extractZipBundle(installScriptZipFile, ".");
			env.execute("chmod +x " + silent_install_script_name);
			env.execute("chmod +x " + conf_tmpl_file_name);
            
            super.onStart(env);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
	
	@Override
	protected InputStream getTaskRunnerConfig(Environment env){
	    try{
            String url = env.getVariable("bigtoptask_url").getValue();
            IFile file = env.getStorageRoot().getFile(URI.create(url));
	        if(file.exist()){
	            return file.getContents();
	        }
            else{
                env.extractResource("conf/bigtoptask.xml");
                return new FileInputStream("bigtoptask.xml");
            }
	    }
	    catch(Exception e){	        
	    }
	    return null;
	}
	
   protected List<File> getJUnitResults(String folder, String filter){
        boolean directAdded = false;
        final List<File> files = new ArrayList<File>();
        for(File f : new File(folder).listFiles()){
            if(f.isDirectory()){
                files.addAll(getJUnitResults(f.getAbsolutePath(), filter));
            }
            else if(f.getName().startsWith("TEST-") && f.getName().endsWith(".xml") &&
                    (filter != null ? f.getName().contains(filter) : true)){
                directAdded = true;
                files.add(f);
            }
        }
        if(directAdded && files.size() > 0)
            LOG.info("Added " + files.size() + " results from " + folder);
        return files;
    }
	
	@Override
	protected boolean doTask(SimpleTaskItem item, final Environment env) throws Exception{
	    IFolder root = env.getStorageRoot();
	    // test is not run, as no such item is defined in bigtoptask.xml
	    String basename = item.getProperty("basename");
	    String failsafeTarget = resultFolder + "/bigtop-tests/test-execution/smokes/" + basename + "/target";
	    if(!new File(failsafeTarget).exists())
	        return true;
	    List<File> results = getJUnitResults(failsafeTarget , null);
	    IFolder targetFolder = root.getFolder("bigtop").getFolder(basename);
	    if(!targetFolder.exist()){
	        targetFolder.create();
	    }
	    boolean succeeded = false;
	    for(File f : results){
	        IFile t = targetFolder.getFile(f.getName());
	        if(!t.exist()){
	            t.create();
	            t.setContents(new FileInputStream(f));
	            succeeded = true;
	        }
	    }
	    //special check for IM test
        if(basename.equals("manager")){
            if(new File(failsafeTarget + "/failed").exists()){
                succeeded = false;
            }
        }
	    return succeeded;
	}

    @Override
    protected void doFinish(SimpleTaskItem[] pendingItems, Environment env) throws Exception{
        IFolder root = env.getStorageRoot();
        for(SimpleTaskItem item : pendingItems){
            List<File> results = getJUnitResults(resultFolder + "/bigtop-tests/test-execution/smokes/" + item.getProperty("basename") , null);
            IFolder targetFolder = root.getFolder("bigtop").getFolder(item.getProperty("basename"));
            if(!targetFolder.exist()){
                targetFolder.create();
            }
            
            for(File f : results){
                IFile t = targetFolder.getFile(f.getName());
                if(!t.exist()){
                    t.create();
                    try{
                        t.setContents(new FileInputStream(f));
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        
        String hostname = env.getHostName();
        IFolder logsFolder = root.getFolder("logs");
        IFolder hostLogsFolder = logsFolder.getFolder(hostname);
        if(!hostLogsFolder.exist())
            hostLogsFolder.create();
        if(new File("/var/log/hadoop").exists()){
            IFolder hadoopLogsFolder = hostLogsFolder.getFolder("hadoop");
            if(!hadoopLogsFolder.exist())
                hadoopLogsFolder.create();
            env.copyFolder(new File("/var/log/hadoop"), hadoopLogsFolder);
        }
        if(new File("/var/log/hadoop-hdfs").exists()){
            IFolder hadoopLogsFolder = hostLogsFolder.getFolder("hadoop-hdfs");
            if(!hadoopLogsFolder.exist())
                hadoopLogsFolder.create();
            env.copyFolder(new File("/var/log/hadoop-hdfs"), hadoopLogsFolder);
        }
        if(new File("/var/log/hadoop-httpfs").exists()){
            IFolder hadoopLogsFolder = hostLogsFolder.getFolder("hadoop-httpfs");
            if(!hadoopLogsFolder.exist())
                hadoopLogsFolder.create();
            env.copyFolder(new File("/var/log/hadoop-httpfs"), hadoopLogsFolder);
        }
        if(new File("/var/log/hadoop-mapreduce").exists()){
            IFolder hadoopLogsFolder = hostLogsFolder.getFolder("hadoop-mapreduce");
            if(!hadoopLogsFolder.exist())
                hadoopLogsFolder.create();
            env.copyFolder(new File("/var/log/hadoop-mapreduce"), hadoopLogsFolder);
        }
        if(new File("/var/log/hadoop-yarn").exists()){
            IFolder hadoopLogsFolder = hostLogsFolder.getFolder("hadoop-yarn");
            if(!hadoopLogsFolder.exist())
                hadoopLogsFolder.create();
            env.copyFolder(new File("/var/log/hadoop-yarn"), hadoopLogsFolder);
        }
        if(new File("/var/log/hbase").exists()){
            IFolder hbaseLogsFolder = hostLogsFolder.getFolder("hbase");
            if(!hbaseLogsFolder.exist())
                hbaseLogsFolder.create();
            env.copyFolder(new File("/var/log/hbase"), hbaseLogsFolder);
        }
        if(new File("/var/log/hadoop-hive").exists()){
            IFolder hiveLogsFolder = hostLogsFolder.getFolder("hive");
            if(!hiveLogsFolder.exist())
                hiveLogsFolder.create();
            env.copyFolder(new File("/var/log/hive"), hiveLogsFolder);
        }
        if(new File("/var/log/hadoop-zookeeper").exists()){
            IFolder zkLogsFolder = hostLogsFolder.getFolder("zookeeper");
            if(!zkLogsFolder.exist())
                zkLogsFolder.create();
            env.copyFolder(new File("/var/log/zookeeper"), zkLogsFolder);
        }
        if(new File("/var/log/intel-manager").exists()){
            IFolder managerLogsFolder = hostLogsFolder.getFolder("manager");
            if(!managerLogsFolder.exist())
            	managerLogsFolder.create();
            env.copyFolder(new File("/var/log/intel-manager"), managerLogsFolder);
        }
    }
    
	private String checkEnvVar(Environment env, String varName, String defaultValue) {
		String value = "";
		try {
			value = env.getVariable(varName).getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(value.isEmpty())
			return defaultValue;
		else
			return value;
	}
	
	private void deleteFolderContents(File folder) {
		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();
			for (File file : files) {
				if (!file.isDirectory()) {
					file.delete();
				} else {
					deleteFolderContents(file);
					file.delete();
				}
			}
		}
	}

	private void deleteFolderAndContents(File folder) {
		deleteFolderContents(folder);
		folder.delete();
	}
}
