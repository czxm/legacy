package com.intel.cedar.tasklet;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.feature.util.GitClient;
import com.intel.cedar.feature.util.SVNClient;
import com.intel.cedar.feature.util.SVNHistory;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.util.Utils;

public abstract class AbstractTaskRunner implements Serializable, ITaskRunner,
        ITaskItemProvider, IProgressProvider {
    private static final long serialVersionUID = -6726864868974687437L;
    protected String curLine;

    protected void createZipBundle(String zipFileName, String folder) {
        if (new File(folder).isDirectory()) {
            try {
                FileUtils.zip(zipFileName, new File(folder));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void createZipBundle(String zipFileName, List<File> files) {
        try {
            FileUtils.zip(zipFileName, files);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void extractZipBundle(IFile zipFile, String folder) {
        try {
            FileUtils.unzip(zipFile.getContents(), new File(folder));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void extractZipBundle(String zipFile, String folder) {
        try {
            FileUtils.unzip(new FileInputStream(zipFile), new File(folder));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void svnCheckOut(String url, String user, String passwd,
            String rev, String dst) throws Exception {
        SVNClient client = new SVNClient(url, user, passwd);
        client.doCheckout(rev, dst);
    }

    protected void svnUpdate(String url, String user, String passwd,
            String rev, String dst) throws Exception {
        SVNClient client = new SVNClient(url, user, passwd);
        client.doUpdate(rev, dst);
    }
    
    protected String svnGetLatestRevision(String svn_url,
            String username, String passwd) throws Exception {
        return new SVNHistory(svn_url, username, passwd, svn_url
                .startsWith("svn")).getLatestModificationRevision();
    }
    
    protected GitClient getGitClient(String url, String username, String password, 
            String privatekey, String localrepo, 
            String proxy, int port) throws Exception{
        GitClient git = null;
        try{
            if(password != null){
                git = new GitClient(url, localrepo, username, password);
            }
            else if(privatekey != null){
                git = new GitClient(url, localrepo, username, Utils.decodeBase64(privatekey));
            }
            if(git != null){
                git.setProxy(proxy, port);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return git;
    }
    
    protected void gitCheckOut(String url, String branch, String user, String passwd, String privatekey,
            String rev, String dst, String proxy, int port) throws Exception {
        GitClient client = null;
        try{
            client = getGitClient(url, user, passwd, privatekey, dst, proxy, port);
            if(client != null && client.openRepository()){
                client.checkout(branch, rev);
            }
        }
        finally{
            if(client != null)
                client.closeRepository();
        }
    }
    
    protected String gitGetLatestRevision(String url, String branch, 
            String user, String passwd, String privatekey, 
            String dst, String proxy, int port) throws Exception {
        GitClient client = null;
        try{
            client = getGitClient(url, user, passwd, privatekey, dst, proxy, port);
            if(client != null && client.openRepository()){
                client.checkout(branch);
                return client.getHeadCommit().getName();
            }
            return null;
        }finally{
            if(client != null)
                client.closeRepository();
        }
    }

    public List<ITaskItem> getTaskItems(Environment env) {
        return new ArrayList<ITaskItem>();
    }

    public void encounterLine(String line) {
        curLine = line;
    }

    public String getProgress() {
        return curLine;
    }

    public void onError(Throwable e, Environment env) {
    }

    public void onFinish(Environment env) {
    }

    public void onStart(Environment env) {
    }
}
