package com.intel.cedar.features.gitbuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.feature.GitRegressionTestFeature;
import com.intel.cedar.feature.TaskSummaryItem;
import com.intel.cedar.feature.util.GitClient;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.util.Utils;

public class GitBuilderFeature extends GitRegressionTestFeature {
	private String target = "CentOS 6.2";
	private String product = "project";
	private String source = "Project";
	private IFolder comparedJob = null;
	
	private static class GroupPattern {
	    private String group;
	    private String pattern;	    
	    public GroupPattern(String g, String p){
	        group = g;
	        pattern = p;
	    }	    
	}
	
	protected List<GroupPattern> groupPatterns = new ArrayList<GroupPattern>();
	
	public GitBuilderFeature(){
	    try{
	        InputStream ins = GitBuilderFeature.class.getResourceAsStream("/conf/groups.conf");
	        BufferedReader br = new BufferedReader(new InputStreamReader(ins));
	        String line = null;
	        while((line = br.readLine()) != null){
	            String[] sp = line.trim().split("=");
	            if(sp.length == 2 && sp[0].length() > 0 && sp[1].length() > 0){
	                groupPatterns.add(new GroupPattern(sp[0], sp[1]));
	            }
	        }
	        br.close();     
	    }
	    catch(Exception e){
	        e.printStackTrace();
	    }
	}
	 
	public static GitClient getGitClient(String url, String dest, Environment env) throws Exception{
        String username = null;
        String password = null;
        String privatekey = null;
        String proxy = null;       
        int port = 0;
        
        try{
            Variable vu = env.getVariable("git_username");
            if(vu.getValue() != null && vu.getValue().length() > 0)
                username = vu.getValue();
        }
        catch(Exception e){            
        }
        
        try{
            Variable vu = env.getVariable("git_password");
            if(vu.getValue() != null && vu.getValue().length() > 0)
                password = vu.getValue();
        }
        catch(Exception e){           
        }
  
        try{
            Variable vu = env.getVariable("git_privatekey");
            if(vu.getValue() != null && vu.getValue().length() > 0)
                privatekey = vu.getValue();
        }
        catch(Exception e){            
        }
        
        try{
            Variable vu = env.getVariable("git_proxyhost");
            if(vu.getValue() != null && vu.getValue().length() > 0)
                proxy = vu.getValue();   
        }
        catch(Exception e){            
        }
        
        try{
            Variable vu = env.getVariable("git_proxyport");
            if(vu.getValue() != null && vu.getValue().length() > 0)
                port = Integer.parseInt(vu.getValue());
        }
        catch(Exception e){            
        }

        GitClient git = null;
        if(password != null){
            git = new GitClient(url, dest, username, password);
        }
        else if(privatekey != null){
            git = new GitClient(url, dest, username, Utils.decodeBase64(privatekey));
        }
        if(git != null){
            git.setProxy(proxy, port);
        }
        return git;
    }
	
	
    public static void doCheckout(String url, String branch, String dest, String rev, String refspec, Environment env) throws Exception {
        GitClient client = getGitClient(url, dest, env);
        if(client != null && client.openRepository()){
        	if(refspec != null){
        		client.checkoutPatchSet(refspec);
        	}
        	else{
        		client.checkout(branch, rev);
        	}
        }
    }
    
    public static void doChangeOwner(String dest, String user, String group, Environment env) throws Exception{
        env.execute("chown -R " + user + "." + group + " \"" + dest + "\"");
    }
    
    public static void doApplyPatch(IFile file, int level, String dest, Environment env) throws Exception {
        if(file != null && file.exist()){
            File patch = new File(file.getName());
            env.copyFile(file, patch);
            env.execute(new String[]{"cd " + dest, "patch -p" + level + " < " + patch.getAbsolutePath()});
        }
    }
    
    public static String doGetLatestRev(String url, String branch, String dest, Environment env) throws Exception {
        GitClient client = getGitClient(url, dest, env);
        if(client != null && client.openRepository()){
            client.checkout(branch);
            client.update();
            return client.getHeadCommit().getName();
        }
        return null;
    }
	
	@Override
	public void onInit(Environment env) throws Exception{
		super.onInit(env);
		product = getTrackedRepository().substring(getTrackedRepository().lastIndexOf("/") + 1);
		target = env.getVariable("target").getValue();
		comparedJob = getComparedJob(env);
		env.getStorageRoot().getFolder("junit").create();
		
		Variable v = env.getVariable("git_branch");
		String git_branch = v.getValue();
		String branch = git_branch.substring(git_branch.lastIndexOf("/") + 1);
		v.setValue(branch);
		
		String git_url = env.getVariable("git_url").getValue();
        source = git_url.substring(git_url.lastIndexOf("/") + 1);
        env.getVariable("project_src").setValue(source + "_" + branch);
	}

    protected IFolder getComparedJob(Environment env){
        try{
            Variable v = env.getVariable("compareResult");
            String l = null;
            if(v.getValue().toLowerCase().contains("checkin")){
                l = env.getFeatureProperty("last_checkin_job", product);
            }
            else if(v.getValue().toLowerCase().contains("nightly")){
                l = env.getFeatureProperty("last_nightly_job", product);
            }
            if(l != null){
                return env.getFolderByURI(URI.create(l));                        
            }
        }
        catch(Exception e){                
        }
        return null;
    }
	
	class ProjectJUnitReportConfig extends AbstractJUnitReportConfig{
	    protected Environment env;
	    public ProjectJUnitReportConfig(Environment env){
	        this.env = env;
	    }
	    
        @Override
        public String[] getGroups() {
            List<String> groups = new ArrayList<String>();
            for(GroupPattern gp : groupPatterns){
                groups.add(gp.group);
            }
            return groups.toArray(new String[]{});
        }
        @Override
        public String getGroupPattern(String group) {
            for(GroupPattern gp : groupPatterns){
                if(group.contains(gp.group))
                    return gp.pattern;
            }
            return null;
        }
        @Override
        public String getDefaultGroup() {
            return "Others";
        }
        @Override
        public IFolder getResultFolder(){
            return env.getStorageRoot().getFolder("junit");
        }
        @Override
        public IFolder getReportFolder(){
            return env.getStorageRoot().getFolder("junit_report");
        }
        @Override
        public IFile getDiffSource(){
            if(comparedJob != null && comparedJob.exist()){
                IFolder reportFolder = comparedJob.getFolder("junit_report");
                if(reportFolder.exist())
                    return reportFolder.getFile("report.xml");
            }
            return null;
        }
	}
	
	@Override
	protected List<JUnitReportConfig> getReportConfig(final Environment env) {
		List<JUnitReportConfig> results = new ArrayList<JUnitReportConfig>();
		results.add(new ProjectJUnitReportConfig(env));
		return results;
	}

    @Override
	protected String getTestName() {
		return "Git Project Test";
	}
	
	@Override
	protected boolean isBuildCompleted(Environment env) {
		IFile buildok = env.getStorageRoot().getFile("BUILD_OK");
		return buildok.exist();
	}
	
	
	@Override
	protected List<TaskSummaryItem> getSummaryItems(Environment env){
		List<TaskSummaryItem> items = super.getSummaryItems(env);
		for(TaskSummaryItem i : items){
			if(i.getName().equals("Build Result")){
				i.setHyperLink(true);
				i.setUrl(env.getHyperlink(env.getStorageRoot().getFolder("builder")));
			}else if(i.getName().equals("Platform")){
				i.setValue(target);				
			}
			continue;
		}
		try{
		    String url = env.getVariable("project_patch").getValue();
		    if(url != null && url.length() > 0){
		        IFile patch = env.getStorageRoot().getFile(URI.create(url));
		        TaskSummaryItem item = new TaskSummaryItem();
                item.setName("Uploaded patch");
                item.setHyperLink(true);
                item.setUrl(env.getHyperlink(patch));
                item.setValue(patch.getName());
                items.add(item);
		    }
		}
		catch(Exception e){		    
		}
		return items;
	}
	
    @Override
    public String getReportTitle(Environment env) throws Exception {
        boolean isNightly = false;
        try{
            if(Boolean.parseBoolean(env.getVariable("isNightly").getValue())){
                isNightly = true;
            }
        }
        catch(Exception e){            
        }
        StringBuilder sb = new StringBuilder();
        if (isCheckIn) {
            sb.append("[");
            sb.append(product);
            sb.append(":");
            sb.append(rev.substring(0,10));
            sb.append("...] ");
            sb.append(source);
            sb.append(" Checkin Test");         
        }
        else if(isNightly){
            sb.append("[");
            sb.append(product);
            sb.append("] ");
            sb.append(source);
            sb.append(" Nightly Test"); 
        }
        else if(gerrit_refspec != null){
            sb.append("Change in ");
            sb.append(source);
            sb.append("[");
            sb.append(product);
            sb.append("] ");    
        }
        else{
           return null;
        }
        return sb.toString();
    }

    @Override
    protected String getFeatureReport(Environment env) {
        StringBuilder sb = new StringBuilder();
        if(comparedJob != null && comparedJob.exist()){
            sb.append("The following result is compared against ");
            sb.append("<a href=\"");
            sb.append(env.getHyperlink(comparedJob.getFile("job.log")));
            sb.append("\">");
            try{
                Variable v = env.getVariable("compareResult");
                sb.append(v.getValue());
                sb.append("</a>");
            }
            catch(Exception e){
            }
        }
        try{
            if(isCheckIn){
                env.setFeatureProperty("last_checkin_job", env.getStorageRoot().getURI().toString(), product);
            }
            if(Boolean.parseBoolean(env.getVariable("isNightly").getValue())){
                env.setFeatureProperty("last_nightly_job", env.getStorageRoot().getURI().toString(), product);
            }
        }
        catch(Exception e){            
        }
        return sb.toString();
    }
}
