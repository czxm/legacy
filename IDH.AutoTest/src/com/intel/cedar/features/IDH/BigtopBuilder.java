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
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.SimpleTaskItem;
import com.intel.cedar.tasklet.SimpleTaskRunner;

public class BigtopBuilder extends SimpleTaskRunner {
	Logger LOG = LoggerFactory.getLogger(BigtopBuilder.class);
	
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
            List<String> components = AutoTestFeature.loadComponents(env);
            for(String c : components){
                SimpleTaskItem item = new SimpleTaskItem();
                item.setValue(c + " package");
                item.setProperty("basename", AutoTestFeature.getBaseName(c));
                item.setProperty("component", c);
                items.add(item);
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
            url = env.getVariable("svn_url").getValue();
            rev = env.getVariable("svn_rev").getValue();
            
            resultFolder = env.getVariable("resultFolder").getValue();
            
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
                    //new File("/home/user/" + theFolder).renameTo(new File(resultFolder));
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
            
            super.onStart(env);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    @Override
    protected InputStream getTaskRunnerConfig(Environment env){
        try{
            String url = env.getVariable("buildtask_url").getValue();
            IFile file = env.getStorageRoot().getFile(URI.create(url));
            if(file.exist()){
                return file.getContents();
            }
            else{
                env.extractResource("conf/buildtask.xml");
                return new FileInputStream("buildtask.xml");
            }
        }
        catch(Exception e){         
        }
        return null;
    }
	
	@Override
	protected boolean doTask(SimpleTaskItem item, final Environment env) throws Exception {	
        IFolder packageRoot = env.getStorageRoot().getFolder("output");
        IFolder packageFolder = packageRoot.getFolder(item.getProperty("basename"));
        if(!packageFolder.exist()){
            packageFolder.create();
        }
        boolean succeeded = false;
        for(File f : new File(resultFolder + File.separator + 
                              "output" + File.separator + 
                              item.getProperty("basename")).listFiles()){
            IFile df = packageFolder.getFile(f.getName());
            if(!df.exist()){
                df.create();
                df.setContents(new FileInputStream(f));
                succeeded = true;
            }
        }
		return succeeded;
	}
}
