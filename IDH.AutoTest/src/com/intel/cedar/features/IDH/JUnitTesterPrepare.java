package com.intel.cedar.features.IDH;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.tasklet.AbstractTaskRunner;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.SimpleTaskItem;

public class JUnitTesterPrepare extends AbstractTaskRunner {
	Logger LOG = LoggerFactory.getLogger(JUnitTesterPrepare.class);
	
	private static final long serialVersionUID = 733863306707750102L;
	
	private String username;
	private String password;
	private String url;
	private String rev;
	private String resultFolder;
	
	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
		List<ITaskItem> items = new ArrayList<ITaskItem>();
		// we should know the IDH source layout for junit tests if required
		// Otherwise, Builder task will generate source list for bigtop task
	    try{
	        String junitTest = env.getVariable("junitTest").getValue().toLowerCase();
	        if(!junitTest.equals("skip"))
	            items.add(new SimpleTaskItem());
	    }
	    catch(Exception e){	        
	    }
		return items;
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
        
        // firstly we check if there's an archive on shared storage
        resultFolder = url.substring(url.lastIndexOf("/") + 1);
        
        // update the resultFolder variable for other Tasklets
        Variable v = env.getVariable("resultFolder");
        v.setValue(resultFolder);
        env.setVariable(v);
        
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
                // don't use the local copy, as hadoop is moved to another repository
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
    
    @Override
    public void onStart(Environment env){
        try {
            doCheckout(env);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
	
	@Override
	public ResultID run(ITaskItem ti, final Writer output, final Environment env) {	
		try {
			IFile sources = env.getStorageRoot().getFile("sources.txt");
			sources.create();
			StringWriter sw = new StringWriter();
			for(String s : new File(resultFolder + File.separator + "sources").list()){
			    if(s.startsWith(".")) // skip .svn
			        continue;
			    sw.write(s);
			    sw.write("\n");
			}
			sw.close();
			ByteArrayInputStream is = new ByteArrayInputStream(sw.toString().getBytes());
			sources.setContents(is);
			is.close();
			return ResultID.Passed;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResultID.Failed;
	}
}
