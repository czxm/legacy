package com.intel.cedar.features.IDH;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.SimpleTaskItem;
import com.intel.cedar.tasklet.SimpleTaskRunner;

public class JUnitTester extends SimpleTaskRunner {
	Logger LOG = LoggerFactory.getLogger(JUnitTester.class);
	
	private static final long serialVersionUID = 733863306707750102L;
	
	private String username;
    private String password;
    private String url;
    private String rev;
    private String resultFolder;
	
	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
        List<ITaskItem> items = new ArrayList<ITaskItem>();
	    try{
	        String utTestType = env.getVariable("junitTest").getValue().toLowerCase();
	        String mr1_version = "null";
	        String hadoop_version = "null";
	        String hbase_version = "null";
            List<String> testcomponents = env.getVariable("components").getValues();
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
            }
            
            //move hive, pig, and hadoop as the top items
            String hive = null;
            String pig = null;
            String hadoop = null;
            for(String c : components){
                if(c.contains("hive")){
                    hive = c;
                }
                if(c.contains("pig")){
                    pig = c;
                }
                if(c.contains("hadoop")){
                    hadoop = c;
                }
            }            
            if(pig != null){
                components.remove(pig);
                components.add(0, pig);
            }
            if(hive != null){
                components.remove(hive);
                components.add(0, hive);
            }
            if(hadoop != null){
                components.remove(hadoop);
                components.add(0, hadoop);
            }
            
            if(!utTestType.equals("skip")){
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
                        i.setValue(c + " " + utTestType);
                        i.setProperty("mr1_version", mr1_version);
                        i.setProperty("hadoop_version", hadoop_version);
                        i.setProperty("hbase_version", hbase_version);
                        i.setProperty("component", c);
                        items.add(i);
                    }
                }
            }
                        
            // add Nist cases
            // check if hive is selected
            boolean hiveSelected = false;
            if(hive != null){
                for(String t : testcomponents){
                    if(hive.contains(t))
                        hiveSelected = true;
                }
            }
            if(hiveSelected){
                List<String> cases = env.getVariable("nist").getValues();
                if(cases.size() > 0){
                    List<String> nist_test_config = env.getVariable("nist_test_config").getValues();
                    int batch = Integer.parseInt(nist_test_config.get(0));
                    int total = Integer.parseInt(nist_test_config.get(1));
                    List<String> options = new ArrayList<String>();
                    if(!cases.contains("All")){
                        if(cases.contains("Sub Query"))
                            options.add("sub");
                        if(cases.contains("Multi Table Query"))
                            options.add("mt");
                        if(cases.contains("TPC-H"))
                            options.add("tpch");                
                    }
                    
                    for(int i = 0; i < total; i+=batch){
                        SimpleTaskItem item = new SimpleTaskItem();
                        item.setValue("nist test[" + i + "-" + (i+batch) + "]");
                        StringBuilder sb = new StringBuilder();
                        sb.append(i);
                        sb.append(" ");
                        sb.append(batch);
                        sb.append(" ");
                        sb.append(total);
                        for(String o : options){
                            sb.append(" ");
                            sb.append(o);
                        }
                        item.setProperty("option", sb.toString());
                        item.setProperty("index", Integer.toString(i));
                        item.setProperty("group", "nist");
                        item.setProperty("hive_src", hive);
                        items.add(item);
                    }          
                }
            }
	    }
	    catch(Exception e){	        
	    }
		return items;
	}
	
	@Override
	protected InputStream getTaskRunnerConfig(Environment env){
	    try{
            String url = env.getVariable("junittask_url").getValue();
            IFile file = env.getStorageRoot().getFile(URI.create(url));
            if(file.exist()){
                return file.getContents();
            }
            else{
                env.extractResource("conf/junittask.xml");
                return new FileInputStream("junittask.xml");
            }
	    }
	    catch(Exception e){	        
	    }
	    return null;
	}
	
    public void doCheckout(Environment env) throws Exception {              
        username = "lab_xmldev";
        password = "qnn8S*NP";
        try{
            username = env.getVariable("svn_username").getValue();
            password = env.getVariable("svn_password").getValue();
        }
        catch(Exception e){         
        }
        url = env.getVariable("svn_url").getValue();
        rev = env.getVariable("svn_rev").getValue();
        
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
                if(url.endsWith(f)){
                    theFolder = f;
                    break;
                }
            }
            if(theFolder != null){
                //env.execute("cp -ar /home/user/" + theFolder + " .");
            }
        }
        
        try{
            this.svnUpdate(url, username, password, rev, resultFolder);
        }
        catch(Exception e){
            e.printStackTrace();
            File rf = new File(resultFolder);
            if(rf.exists()){
                env.execute("rm -rf " + resultFolder);
            }
            this.svnCheckOut(url, username, password, rev, resultFolder);
        }
        
        IFile patch_file = null;
        try{
            String patch = env.getVariable("IDH_patch").getValue();
            if(patch != null && patch.length() > 0){
                patch_file = env.getStorageRoot().getFile(URI.create(patch));
            }
        }
        catch(Exception e){                
        }
        if(patch_file != null && patch_file.exist()){
            File localpatch = new File("idh_patch");
            env.copyFile(patch_file, localpatch);
            env.execute(new String[]{"cd " + resultFolder, "patch -p0 < " + localpatch.getAbsolutePath()});
        }
        
        env.execute("chown -R user.user " + resultFolder);
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
    public void onStart(Environment env) {
        try{
            Variable v = env.getVariable("resultFolder");
            resultFolder = v.getValue();
            
            if(!new File(resultFolder).exists()){
                doCheckout(env);
            }
            
            // prepare Nist test framework
            List<String> cases = env.getVariable("nist").getValues();
            if(cases.size() > 0){
                env.extractResource("resource/maven-ant-tasks-2.1.3.jar");
                try{
                    String uri = env.getVariable("nist_diff_url").getValue();
                    if(uri != null && uri.length() > 0){
                        IFile rpatch = env.getStorageRoot().getFile(URI.create(uri));
                        if(rpatch.exist()){
                            File patch = new File(rpatch.getName());
                            env.copyFile(rpatch, patch);
                        }
                    }
                    
                    String nist_url = env.getVariable("nist_url").getValue();
                    String nist_branch = env.getVariable("nist_branch").getValue();
                    String nist_src = env.getVariable("nist_src").getValue();
                    String nist_rev = env.getVariable("nist_rev").getValue();
                    if(nist_rev == null || nist_rev.length() == 0){
                        nist_rev = AutoTestFeature.doGetLatestRev(nist_url, nist_branch, nist_src, env);
                        env.getVariable("nist_rev").setValue(nist_rev);
                    }
                    File nistFolder = new File(nist_src);
                    if(!nistFolder.exists()){
                        AutoTestFeature.doCheckout(nist_url, nist_branch, nist_src, nist_rev, env);
                    }
                    if(nistFolder.exists()){
                        AutoTestFeature.doChangeOwner(nist_src, "user", "user", env);
                    }   
                }
                catch(Exception e){
                }                        
            }
            super.onStart(env);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
   
    protected boolean collectResult(SimpleTaskItem item, Environment env) throws Exception {
        boolean succeeded = false;
        if(item.getProperty("component") != null){
            String resultFolder = env.getVariable("resultFolder").getValue();
            IFolder root = env.getStorageRoot();
            List<File> results = getJUnitResults(resultFolder + File.separator + "sources" + File.separator + item.getProperty("component"), item.getProperty("filter"));
            IFolder targetFolder = root.getFolder("junit").getFolder(item.getProperty("component"));
            if(!targetFolder.exist()){
                targetFolder.create();
            }
            
            for(File f : results){
                IFile t = targetFolder.getFile(f.getName());
                if(!t.exist()){
                    t.create();
                    t.setContents(new FileInputStream(f));
                    succeeded = true;
                }
            }
        }
        else{ // collect Nist case result
            String resultFolder = env.getVariable("nist_src").getValue();
            String hive_src = item.getProperty("hive_src");
            IFolder root = env.getStorageRoot();
            List<File> results = getJUnitResults(resultFolder + File.separator + hive_src, item.getProperty("filter"));
            IFolder targetFolder = root.getFolder("nist").getFolder(item.getProperty("group"));
            if(!targetFolder.exist()){
                targetFolder.create();
            }
            
            IFolder failureLogs = targetFolder.getFolder("failures");
            if(!failureLogs.exist()){
                failureLogs.create();
            }
            File localFailureLog = new File(resultFolder + File.separator + "failures.csv");
            if(localFailureLog.exists()){
                IFile failureLog = failureLogs.getFile("failure" + item.getProperty("index") + ".csv");
                env.copyFile(localFailureLog , failureLog);
            }
            
            for(File f : results){
                String fn = f.getName();
                fn = fn.replace(".xml", item.getProperty("index") + ".xml");
                IFile t = targetFolder.getFile(fn);
                if(!t.exist()){
                    t.create();
                    t.setContents(new FileInputStream(f));
                    succeeded = true;
                }
            }
        }
        return succeeded;
    }
    
	@Override
	protected boolean doTask(SimpleTaskItem item, final Environment env) throws Exception{
	    return collectResult(item, env);
	}
	
    @Override
    protected void doFinish(SimpleTaskItem[] pendingItems, Environment env) throws Exception{
        for(SimpleTaskItem item : pendingItems){
            collectResult(item, env);
        }
    }
}
