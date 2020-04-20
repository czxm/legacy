package com.intel.cedar.features.cppConf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.feature.IFeature;
import com.intel.cedar.feature.SVNRegressionTestFeature;
import com.intel.cedar.feature.TaskSummaryItem;
import com.intel.cedar.feature.util.SVNHistory;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ITaskItemProvider;

public class CppConformanceTestFeature extends SVNRegressionTestFeature{
	private String taskId;
	
	@Override
	protected boolean isBuildCompleted(Environment env) {
        IFile packageFile = env.getStorageRoot().getFile("package.zip");
        return packageFile.exist();
	}
	
	@Override
	protected String getTestName(){
		return "XML Library C++ Conformance Test";
	}
	
	@Override
	protected List<TaskSummaryItem> getSummaryItems(Environment env){
		List<TaskSummaryItem> items = super.getSummaryItems(env);
		if(isBuildCompleted(env)){
			TaskSummaryItem item = new TaskSummaryItem();
			item.setName("Built Package");
			item.setHyperLink(true);
			item.setUrl(env.getHyperlink(env.getStorageRoot().getFile("package.zip")));
			item.setValue("package.zip");
			items.add(item);
		}
		return items;
	}
	
	private String getDBFriendlyId(String id){
		Long timestamp = System.currentTimeMillis();
	    return id.substring(0,id.indexOf("-")) + Long.toString(timestamp);
	}
	
	public CppConformanceTestFeature(){
		 taskId = getDBFriendlyId(UUID.randomUUID().toString());
	}
	
	private void issueHttpCommand(String command) throws Exception{
	    HttpClient client = new HttpClient();
	    HostConfiguration hc = new HostConfiguration();
	    hc.setHost( "xmlqa-c2d2.sh.intel.com" , 80 , "http" );
	    GetMethod gm = new GetMethod(
	        String
	            .format(
	                "/%s" ,
	                command) );
	    BufferedReader br;
		client.executeMethod( hc , gm );
	    HttpState hs = client.getState();
		br = new BufferedReader( new InputStreamReader( gm
			    .getResponseBodyAsStream() ) );
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while( ( line = br.readLine() ) != null ){
	        sb.append( line ).append( "\n" );
	    }
	}
	
	@Override
	protected String getFeatureReport(Environment env){
		StringBuilder sb = new StringBuilder();
		FTPClient ftp=new FTPClient();
	    try {
	      int reply;
	      ftp.connect("xmlqa-c2d2.sh.intel.com");
	      ftp.login("repserver", "repserver");

	      // After connection attempt, you should check the reply code to verify
	      // success.
	      reply = ftp.getReplyCode();

	      if(!FTPReply.isPositiveCompletion(reply)) {
	        ftp.disconnect();
	        return null;
	      }
	      ftp.changeWorkingDirectory("xml-report-server_dev/siteroot/report/jobs");
	      /*
	      String[] files=ftp.listNames();
	      for(String name:files)
	    	  System.out.println(name);
	      */
	      int waitCnt=60;
	      int i=0;
	      InputStream is=null;
	      while(i<waitCnt)
	      {
	    	  is=ftp.retrieveFileStream(taskId+".html");
	    	  if(is==null)
	    	  {
	    		  try{
	    			  Thread.sleep(10000);
	    		  }
	    		  catch(InterruptedException e)
	    		  {	    			  
	    		  }
	    	  }
	    	  else
	    		  break;
	    	  i++;
	      }
	      if(is!=null)	    	  
	      {
	    	  InputStreamReader reader=new InputStreamReader(is);
		      BufferedReader br=new BufferedReader(reader);
		      String line;
		      while((line=br.readLine())!=null)
		    	  sb.append(line);
		      br.close();
	      }
	      ftp.logout();
	    } catch(IOException e) {
	    } 
	    finally 
	    {
	      if(ftp.isConnected()) {
	        try {
	          ftp.disconnect();
	        } catch(IOException ioe) {     	
	        }
	      }
	    }		
	    return sb.toString();
	}	
	
	@Override
	public void onInit(Environment env) throws Exception{
		super.onInit(env);
		env.getVariable("taskId").addValue(taskId);
		issueHttpCommand("jobstart.php?job="+taskId);
	}

	@Override
	public void onFinalize(Environment env) throws Exception{
		super.onFinalize(env);
		issueHttpCommand("jobcomplete.php?job="+taskId);
	}
	
	public static String genTestingPlf(String buildTargetPlf) {
		if(buildTargetPlf.equalsIgnoreCase("win32")){
			return "windows";
		}else if(buildTargetPlf.equalsIgnoreCase("win64")){
			return "windows64";
		}else if(buildTargetPlf.equalsIgnoreCase("as5")){
			return "linux";
		}else if(buildTargetPlf.equalsIgnoreCase("as5_64")){
			return "em64";
		}else{ // Invalid buildTargetPlf, use "em64" as default value here.
			return "em64";
		}
	}
	
	public static String genBuildTargetPlf(String os, String arch) {
		if(os.equalsIgnoreCase("windows") && arch.equalsIgnoreCase("x86")){
			return "win32";
		}else if(os.equalsIgnoreCase("windows") && arch.equalsIgnoreCase("x86_64")){
			return "win64";
		}else if(os.equalsIgnoreCase("linux") && arch.equalsIgnoreCase("x86")){
			return "as5";
		}else if(os.equalsIgnoreCase("linux") && arch.equalsIgnoreCase("x86_64")){
			return "as5_64";
		}else{ // Invalid os or arch, use "as5_64" as default value here.
			return "as5_64";
		}
	}
	
	// only can be called by tasklet!
	public static void updateICCLicense(Environment env) throws Exception{
		String licPath = null;
		File licFile = null;
		String licName = null;
		if(env.getOSName().contains("Windows")){
			licName = "COM___TXD4-9K5FC752.lic";
			licPath = "C:\\Program Files\\Common Files\\Intel\\Licenses\\";
			if(env.getArchitecture().equals("x86_64")){
				licPath = "C:\\Program Files (x86)\\Common Files\\Intel\\Licenses\\";
			}
			licFile = new File(licPath + licName);
			if(licFile.exists())
				return;
		}
		else{
			licName = "COM_L_CMP_CPP_TSCN-9WF6CCZ8.lic";
			licPath = "/opt/intel/licenses/";
			licFile = new File(licPath + licName);
			if(licFile.exists())
				return;
		}
		new File(licPath).mkdir();
		if(licFile.createNewFile()){
			InputStream is = CppConformanceTestFeature.class.getClassLoader().getResourceAsStream("resource/" + licName);
			FileOutputStream ous = new FileOutputStream(licFile);
			byte[] buf = new byte[1024];
			int len = 0;
			while((len = is.read(buf)) > 0){
				ous.write(buf, 0, len);
			}
			ous.close();
			is.close();
		}
	}
	
	public static void addSSLCertToSVN(Environment env, String cert) throws Exception{
		List<String> certPaths = new ArrayList<String>();
		if(env.getOSName().contains("Windows")){
			certPaths.add("C:\\Documents and Settings\\Administrator\\Application Data\\Subversion\\auth\\svn.ssl.server\\");
			certPaths.add("C:\\Documents and Settings\\Default User\\Application Data\\Subversion\\auth\\svn.ssl.server\\");
		}
		else{
			certPaths.add("/.subversion/auth/svn.ssl.server/");
			certPaths.add("/root/.subversion/auth/svn.ssl.server/");
		}
		for(String certPath : certPaths){
			File certFile = new File(certPath + cert);
			if(certFile.exists())
				continue;
			if(certFile.createNewFile()){
				InputStream is = CppConformanceTestFeature.class.getClassLoader().getResourceAsStream("resource/" + cert);
				FileOutputStream ous = new FileOutputStream(certFile);
				byte[] buf = new byte[1024];
				int len = 0;
				while((len = is.read(buf)) > 0){
					ous.write(buf, 0, len);
				}
				ous.close();
				is.close();
			}
		}
	}
	
	public static void addSSLCertsToSVN(Environment env) throws Exception{
		addSSLCertToSVN(env, "c319527a66a8f45c4fe9dfac50df7c1a");
	}
}
