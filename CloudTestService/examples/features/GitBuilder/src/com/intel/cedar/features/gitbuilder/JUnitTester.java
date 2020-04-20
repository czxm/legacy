package com.intel.cedar.features.gitbuilder;

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
	
	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
        List<ITaskItem> items = new ArrayList<ITaskItem>();
        try{
            String junit = env.getVariable("junit").getValue().toLowerCase();
            SimpleTaskItem item = new SimpleTaskItem();
            item.setValue("junit " + junit);
            items.add(item);
        }
        catch(Exception e){         
        }
        return items;
	}
	
	@Override
	protected InputStream getTaskRunnerConfig(Environment env){
	    try{
            String url = env.getVariable("testtask_url").getValue();
            IFile file = env.getStorageRoot().getFile(URI.create(url));
            if(file.exist()){
                return file.getContents();
            }
            else{
                env.extractResource("conf/testtask.xml");
                return new FileInputStream("testtask.xml");
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
    public void onStart(Environment env) {
        try{                            
            String url = env.getVariable("git_url").getValue();
            String rev = env.getVariable("git_rev").getValue();
            String branch = env.getVariable("git_branch").getValue();
            Variable refspecVar = env.getVariable("gerrit_refspec");
            String refspec = null;
            if(refspecVar.getValue() != null){
            	refspec =refspecVar.getValue();
            }
            String project_src = env.getVariable("project_src").getValue();
            File projectFolder = new File(project_src);
            if(!projectFolder.exists()){
                GitBuilderFeature.doCheckout(url, branch, project_src, rev, refspec, env);
                IFile patch = null;
                try{
                    String uri = env.getVariable("project_patch").getValue();
                    if(uri != null && uri.length() > 0){
                        patch = env.getStorageRoot().getFile(URI.create(uri));
                    }
                }
                catch(Exception e){                
                }
                GitBuilderFeature.doApplyPatch(patch, 1, project_src, env);
                GitBuilderFeature.doChangeOwner(project_src, "user", "user", env);
                super.onStart(env);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
   
	@Override
	protected boolean doTask(SimpleTaskItem item, final Environment env) throws Exception{	
        String resultFolder = env.getVariable("project_src").getValue();
	    IFolder root = env.getStorageRoot();
	    List<File> results = getJUnitResults(resultFolder, item.getProperty("filter"));
	    IFolder targetFolder = root.getFolder("junit");
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
		return succeeded;
	}
	
    @Override
    protected void doFinish(SimpleTaskItem[] pendingItems, Environment env) throws Exception{
        String resultFolder = env.getVariable("project_src").getValue();
        IFolder root = env.getStorageRoot();
        for(SimpleTaskItem item : pendingItems){
            List<File> results = getJUnitResults(resultFolder, item.getProperty("filter"));
            IFolder targetFolder = root.getFolder("junit");
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
    }
}
