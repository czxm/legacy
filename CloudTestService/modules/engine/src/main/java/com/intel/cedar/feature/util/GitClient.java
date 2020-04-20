package com.intel.cedar.feature.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ListTagCommand;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.impl.LocalFolder;
import com.intel.xml.rss.util.DateTimeRoutine;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.thoughtworks.xstream.core.util.Base64Encoder;

public class GitClient{
    private static Logger LOG = LoggerFactory.getLogger(GitClient.class);
    
    //TODO remove this global lock
    private static Lock lock = new ReentrantLock();
    private String url;
    private File localrepo;
    private String proxy;
    private int port;
    private CredentialsProvider credential;
    
    public GitClient(String url, String localrepo, String username, String password){
        this.url = url;
        this.localrepo = new File(localrepo);
        this.credential = new UsernamePasswordCredentialsProvider(username, password);
    }
    
    public GitClient(String url, File localrepo, String username, String password){
        this.url = url;
        this.localrepo = localrepo;
        this.credential = new UsernamePasswordCredentialsProvider(username, password);
    }
    
    private void configSSH(final String username, final byte[] privatekey){
        SshSessionFactory.setInstance(new JschConfigSessionFactory(){
            @Override
            protected Session createSession(Host hc, String user, String host,
                    int port, FS fs) throws JSchException {
                return super.createSession(hc, username, host, port, fs);
            }

            @Override
            protected JSch getJSch(Host arg0, FS arg1) throws JSchException {
                JSch jsch = super.getJSch(arg0, arg1);
                jsch.addIdentity(username, privatekey, null, null);
                return jsch;
            }

            @Override
            protected void configure(Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");                
            }
        });
    }
    
    public GitClient(String url, String localrepo, final String username, final byte[] privatekey){
        this.url = url;
        this.localrepo = new File(localrepo);
        configSSH(username, privatekey);
    }
    
    public GitClient(String url, IFolder localrepo, String username, String password){
        if(!(localrepo instanceof LocalFolder)){
            throw new RuntimeException("Not supported!");
        }
        this.url = url;
        this.localrepo = ((LocalFolder)localrepo).toFile();
        this.credential = new UsernamePasswordCredentialsProvider(username, password);
    }
    
    public GitClient(String url, IFolder localrepo, String username, byte[] privatekey){
        if(!(localrepo instanceof LocalFolder)){
            throw new RuntimeException("Not supported!");
        }
        this.url = url;
        this.localrepo = ((LocalFolder)localrepo).toFile();
        configSSH(username, privatekey);
    }

    public void setProxy(String proxy, int port){
        this.proxy = proxy;
        this.port = port;
    }
    
    public boolean openRepository(){
        lock.lock();
        ProxySelector def = ProxySelector.getDefault();
        try{
            if(proxy != null && port > 0){
                ProxySelector.setDefault(new CedarProxySelector(proxy, port));
            }
            if(!localrepo.exists()){
                CloneCommand cc = Git.cloneRepository();
                if(credential != null)
                    cc.setCredentialsProvider(credential);
                cc.setDirectory(localrepo);
                cc.setRemote("origin");
                cc.setURI(url);
                //cc.setProgressMonitor(new TextProgressMonitor());
                Git git = cc.call();
                StoredConfig config = git.getRepository().getConfig();
                config.save();
                LOG.info(url + " is cloned to " + localrepo.getAbsolutePath() + " successfully!");
            }
            else{
                boolean hasgit = false;
                for(File f : localrepo.listFiles()){
                    if(f.getName().equals(".git") && f.isDirectory()){
                        hasgit = true;
                        break;
                    }
                }
                if(!hasgit){
                    LOG.error(localrepo.getAbsolutePath() + " is not a Git repository!");
                }
            }
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
            return false;
        }
        finally{
            ProxySelector.setDefault(def);
        }
        return true;
    }
    
    public void closeRepository(){
        lock.unlock();
    }
    
    public void addFile(File file){
        try {
            AddCommand ac = Git.open(file.getParentFile()).add();
            ac.addFilepattern(file.getName());
            ac.call();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
    public void addFiles(File dir, boolean recursive){
        try{
            for(File f : dir.listFiles()){
                AddCommand ac = Git.open(dir).add();
                if(!f.isDirectory())
                    ac.addFilepattern(f.getName());
                else if(!f.getName().equals(".git") && recursive){
                    addFiles(f, recursive);
                }
                ac.call();
            }
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
    }
    
    public void addFiles(){
        addFiles(localrepo, true);
    }
    
    public void commit(File dir, String name, String email, String comment){
        ProxySelector def = ProxySelector.getDefault();
        try {
            if(proxy != null && port > 0){
                ProxySelector.setDefault(new CedarProxySelector(proxy, port));
            }
            Git git = Git.open(dir);
            CommitCommand commit = git.commit();
            commit.setCommitter(name, email).setMessage(comment);
            commit.call();
            PushCommand pc = git.push();
            if(credential != null)                
                pc.setCredentialsProvider(credential);
            pc.setForce(true);
            pc.setPushAll();
            try {
                Iterator<PushResult> it = pc.call().iterator();
                if(it.hasNext()){
                    for(RemoteRefUpdate rru : it.next().getRemoteUpdates()){
                        LOG.info(rru.getRemoteName() + ":" + rru.getMessage());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            ProxySelector.setDefault(def);
        }
    }
    
    public void commit(String name, String email, String comment){
        commit(localrepo, name, email, comment);
    }
    
    public List<String> listBranches(){
        return listBranches(false);
    }
    
    public List<String> listBranches(boolean all){
        List<String> branches = new ArrayList<String>();
        try{
            Git git = Git.open(localrepo);
            ListBranchCommand c = git.branchList();
            c.setListMode(all ? ListMode.ALL : ListMode.REMOTE);
            for(Ref r : c.call()){
                branches.add(r.getName());
            }
            
            ListTagCommand tc = git.tagList();
            for(Ref r : tc.call()){
                branches.add(r.getName());
            }
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return branches;
    }
    
    public void checkout(String branch, String cset){
        boolean branch_created = false;
        boolean isTag = false;
        branch = branch.substring(branch.lastIndexOf("/") + 1);
        for(String b : listBranches(true)){
            if(b.endsWith("heads/" + branch) || b.startsWith("FETCH_HEAD")){
                branch_created = true;
            }
            if(b.endsWith("tags/" + branch)){
                isTag = true;
                branch_created = true;
            }
        }
        ProxySelector def = ProxySelector.getDefault();
        try{
            if(proxy != null && port > 0){
                ProxySelector.setDefault(new CedarProxySelector(proxy, port));
            }
            Git git = Git.open(localrepo);
            if(!branch_created){
                CreateBranchCommand cb = git.branchCreate();
                cb.setForce(true);
                cb.setName(branch);
                cb.setStartPoint("origin/" + branch);
                //cb.setUpstreamMode(SetupUpstreamMode.TRACK);
                cb.call();
            }
            CheckoutCommand c = git.checkout();
            c.setForce(true);
            c.setCreateBranch(false);
            if(isTag)
                c.setName("tags/" + branch);
            else
                c.setName(branch);
            c.call();
            if(cset != null && cset.length() > 0){
                ResetCommand rc = git.reset();
                rc.setRef(cset);
                rc.setMode(ResetType.HARD);
                rc.call();
            }
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        finally{
            ProxySelector.setDefault(def);
        }
    }
    
    public void checkout(String branch){
        checkout(branch, null);
    }

    public void update(){
        ProxySelector def = ProxySelector.getDefault();
        try {
            if(proxy != null && port > 0){
                ProxySelector.setDefault(new CedarProxySelector(proxy, port));
            }
            Git git = Git.open(localrepo);
            PullCommand c = git.pull();
            if(credential != null)
                c.setCredentialsProvider(credential);
            c.setTimeout(300);
            c.setProgressMonitor(new TextProgressMonitor());
            PullResult pr = c.call();
            for(TrackingRefUpdate tru : pr.getFetchResult().getTrackingRefUpdates()){
                LOG.info(tru.getRemoteName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            ProxySelector.setDefault(def);
        }
    }
    
    public void checkoutPatchSet(String refspec){
        ProxySelector def = ProxySelector.getDefault();
        try {
            if(proxy != null && port > 0){
                ProxySelector.setDefault(new CedarProxySelector(proxy, port));
            }
            Git git = Git.open(localrepo);
            FetchCommand c = git.fetch();
            if(credential != null)
                c.setCredentialsProvider(credential);
            c.setTimeout(300);
            RefSpec rs = new RefSpec(refspec);
            c.setRefSpecs(rs);
            c.setProgressMonitor(new TextProgressMonitor());
            FetchResult pr = c.call();
            System.out.println(pr.getMessages());
            CheckoutCommand cc = git.checkout();
            cc.setForce(true);
            cc.setCreateBranch(false);
            cc.setName("FETCH_HEAD");
            cc.call();
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            ProxySelector.setDefault(def);
        }        
    }
    
    public RevCommit getHeadCommit(){
        try{
            Git git = Git.open(localrepo);
            Repository repo = git.getRepository();
            ObjectId lastCommit = repo.resolve("HEAD");
            RevWalk rw = new RevWalk(repo);
            return rw.parseCommit(lastCommit);            
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
    
    public RevCommit getCommitByName(String name){
        try{
            Git git = Git.open(localrepo);
            Repository repo = git.getRepository();
            ObjectId lastCommit = repo.resolve(name);
            RevWalk rw = new RevWalk(repo);
            return rw.parseCommit(lastCommit);            
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
    
    public List<RevCommit> getCommitHistory(int max){
        return getCommitHistory(null, null, max);
    }
    
    public List<RevCommit> getCommitHistory(RevCommit since, RevCommit until, int max){
        List<RevCommit> commits = new ArrayList<RevCommit>();
        try{
            Git git = Git.open(localrepo);
            LogCommand c = git.log();
            c.setMaxCount(max);
            if(until != null)
                c.add(until);
            for(RevCommit r : c.call()){
                commits.add(r);
                if(since != null && since.equals(r))
                    break;
            }
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return commits;
    }
    
    public SCMChangeSet getChangeSet(RevCommit commit){
        GitChangeSet cs = new GitChangeSet();
        try{
            Git git = Git.open(localrepo);
            RevWalk walk = new RevWalk(git.getRepository());
            if(commit.getParentCount() > 0){
                RevCommit parent = walk.parseCommit(commit.getParent(0).getId());
                DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
                df.setRepository(git.getRepository());
                df.setDiffComparator(RawTextComparator.DEFAULT);
                df.setDetectRenames(true);
                cs.setChangeItems(df.scan(parent.getTree(), commit.getTree()));
            }
            PersonIdent id = commit.getAuthorIdent();
            cs.setUser(id.getName() + "(" + id.getEmailAddress() + ")");
            cs.setDateTime(DateTimeRoutine.dateToStdTimeString(id.getWhen()));
            cs.setLogMsg(commit.getFullMessage());
            cs.setRev(commit.getName());
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return cs;
    }

    public static void Base64Encode(String file) throws Exception{
        Base64Encoder encoder =  new Base64Encoder();
        System.out.println(encoder.encode(loadFile(file)));
    }
    
    public static byte[] loadFile(String file) throws Exception{
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = -1;
        while((len = fis.read(buf)) > 0){
            bos.write(buf, 0, len);
        }
        fis.close();
        bos.close();
        return bos.toByteArray();
    }
    
    
    public static void main(String args[]) throws Exception{
        String url = "ssh://git-ccr-1.devtools.intel.com:29418/idh-manager";
        String localrepo = "/tmp/idh-manager";
        String privatekey = "/root/.ssh/itgit_key";
        GitClient client = new GitClient(url, localrepo, "xzhan27", loadFile(privatekey));
        client.openRepository();
        for(String b : client.listBranches(false)){
            System.out.println(b);
        }
        
        client.checkout("milestone1", null);
        
        RevCommit r1 = client.getCommitByName("969546017ba95133e27994b96e4d23b6e1b9ad88");
        RevCommit r2 = client.getCommitByName("2e0118a43fe5687d27f4e7b2fd6a1b96ebb71d86");
        
        System.out.println(DateTimeRoutine.dateToStdTimeString(r1.getAuthorIdent().getWhen()));
        System.out.println(r1.getCommitTime() < r2.getCommitTime());
        System.out.println(client.getHeadCommit().getName());
        
        for(RevCommit r : client.getCommitHistory(r1, r2, 100)){
            System.out.println(client.getChangeSet(r).getLogMsg());            
        }
        
        /*
        for(RevCommit c : client.getCommitHistory(10)){
            System.out.println(c.getName());
        }        
        client.update();        
        client.checkoutPatchSet("refs/changes/75/3875/1");
        System.out.println(client.getHeadCommit().getName());
        */
        client.closeRepository();
    }
}