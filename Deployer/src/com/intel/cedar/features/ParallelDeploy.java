package com.intel.cedar.features;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.SimpleTaskItem;
import com.intel.cedar.tasklet.SimpleTaskRunner;

public class ParallelDeploy extends SimpleTaskRunner {
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
        String nodeNumber;
		try {
			nodeNumber = env.getVariable("node").getValue();
			SimpleTaskItem i;
	        int nodeNum = Integer.parseInt(nodeNumber);
	        for(int idx = 0; idx < nodeNum; idx ++){
	        	i = new SimpleTaskItem(); 
	        	i.setValue("Deploy Bigtop: " + env.getHostName());
	            items.add(i);
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		} 
        return items;
    }
    
    @Override
    protected InputStream getTaskRunnerConfig(Environment env){
        try{
            env.extractResource("conf/slavedeploy.xml");
            return new FileInputStream("slavedeploy.xml");
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
    	System.out.println(env.getHostName());
    	return true;
	}
    
    
    public void doCheckoutBigtop(Environment env) throws Exception {
    	String master = env.getVariable("master").getValue();
    	if(master.equals(env.getHostName())){
    		System.exit(0);
    	}
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
