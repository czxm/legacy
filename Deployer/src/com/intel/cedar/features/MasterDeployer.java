package com.intel.cedar.features;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.SimpleTaskItem;
import com.intel.cedar.tasklet.SimpleTaskRunner;

public class MasterDeployer extends SimpleTaskRunner {
    private static final long serialVersionUID = 1L;
    private String resultFolder;
    private String username;
	private String password;
	private String bigtop_url;
	private String bigtop_rev;
	private String dply_scrpts_url;
	private String dply_scrpts_rev;
	
	@Override
    public List<ITaskItem> getTaskItems(Environment env) {
        List<ITaskItem> items = new ArrayList<ITaskItem>();
        SimpleTaskItem i = new SimpleTaskItem();
        i.setValue("First Task");
        items.add(i);
        return items;
    }
    
	@Override
    protected InputStream getTaskRunnerConfig(Environment env){
        try{
            env.extractResource("conf/masterdeploy.xml");
            return new FileInputStream("masterdeploy.xml");
        }
        catch(Exception e){         
        }
        return null;
    }
    
    @Override
    public void onStart(Environment env) {
    	try{
    		doCheckoutBigtop(env);
	    }
	    catch(Exception e){
	        e.printStackTrace();
	    }
    }
    

	@Override
	protected boolean doTask(SimpleTaskItem item, final Environment env) throws Exception{       
    	return true;
	}
	
	 public void doCheckoutBigtop(Environment env) throws Exception {  
		 	Variable master = env.getVariable("master");
	        master.setValue(env.getHostName());
	        env.setVariable(master);
	        Variable ttl_hour = env.getVariable("cluster_TTL");
	        int ttl_seconds = Integer.parseInt(ttl_hour.getValue()) * 3600;
	        Variable ttl_seconds_v = env.getVariable("TTL_Seconds");
	        ttl_seconds_v.setValue(ttl_seconds + "");
	        env.setVariable(ttl_seconds_v);
	        username = "lab_xmldev";
	        password = "qnn8S*NP";       
	        bigtop_url = env.getVariable("bigtop_svn_url").getValue();
	        bigtop_rev = env.getVariable("bigtop_svn_rev").getValue();      
	        resultFolder = env.getVariable("resultFolder").getValue();    
	        try{
	            this.svnUpdate(bigtop_url, username, password, bigtop_rev, resultFolder);
	        }
	        catch(Exception e){
	            e.printStackTrace();
	            File rf = new File(resultFolder);
	            if(rf.exists()){
	                env.execute("rm -rf " + resultFolder);
	            }
	            this.svnCheckOut(bigtop_url, username, password, bigtop_rev, resultFolder);
	        }
	        dply_scrpts_url = env.getVariable("deploy_scripts_svn_url").getValue();
	        dply_scrpts_rev = env.getVariable("deploy_scripts_svn_rev").getValue();
	        String dply_scrpts_folder = "deploy_scripts";
	        try{
	            this.svnUpdate(dply_scrpts_url, username, password, dply_scrpts_rev, dply_scrpts_folder);
	        }
	        catch(Exception e){
	            e.printStackTrace();
	            File rf = new File(dply_scrpts_folder);
	            if(rf.exists()){
	                env.execute("rm -rf " + dply_scrpts_folder);
	            }
	            this.svnCheckOut(dply_scrpts_url, username, password, dply_scrpts_rev, dply_scrpts_folder);
	        }
	        env.execute("mv " + resultFolder + " " + dply_scrpts_folder);
	    }
}
