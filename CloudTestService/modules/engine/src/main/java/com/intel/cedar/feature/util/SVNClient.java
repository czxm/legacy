package com.intel.cedar.feature.util;

import java.io.File;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class SVNClient {
    String url;
    String name = "lab_xmldev";
    String password = "qnn8S*NP";
    boolean svnProtocol = false;

    public SVNClient(String _url) {
        this.url = _url;
    }

    public SVNClient(String _url, String _name, String _password) {
        this.url = _url;
        this.name = _name;
        this.password = _password;
        this.svnProtocol = _url.startsWith("svn");
    }

    public void doCheckout(String rev) throws Exception {
        setupLibrary(svnProtocol);
        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(name, password);
        SVNURL u = SVNURL.parseURIEncoded(url);
        File dstPath = new File(u.getPath().substring(
                u.getPath().lastIndexOf("/") + 1));
        SVNClientManager cm = SVNClientManager.newInstance(null, authManager);
        SVNUpdateClient uc = cm.getUpdateClient();
        uc.doCheckout(u, dstPath, null, SVNRevision.parse(rev), SVNDepth
                .fromRecurse(true), true);
    }

    public void doCheckout(String rev, String dst) throws Exception {
        setupLibrary(svnProtocol);
        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(name, password);
        SVNURL u = SVNURL.parseURIEncoded(url);
        File dstPath = new File(dst);
        SVNClientManager cm = SVNClientManager.newInstance(null, authManager);
        SVNUpdateClient uc = cm.getUpdateClient();
        uc.doCheckout(u, dstPath, null, SVNRevision.parse(rev), SVNDepth
                .fromRecurse(true), true);
    }

    public void doUpdate(String rev, String dst) throws Exception {
        setupLibrary(svnProtocol);
        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(name, password);
        File dstPath = new File(dst);
        SVNClientManager cm = SVNClientManager.newInstance(null, authManager);
        SVNUpdateClient uc = cm.getUpdateClient();
        uc.doUpdate(dstPath, SVNRevision.parse(rev),
                SVNDepth.fromRecurse(true), true, true);
    }

    public static void main(String[] args) throws Exception {
        if(args.length == 0){
            System.out.println("Usage:");
            System.out.println("  svnclient checkout URL rev");
            System.out.println("  or");
            System.out.println("  svnclient update URL rev repository");
            System.exit(0);
        }
        String op = args[0];
        String url = args[1];
        SVNClient client = new SVNClient(url);
        if(op.equals("checkout") || op.equals("co")){
            client.doCheckout(args[2]);            
        }
        else if(op.equals("update") || op.equals("up")){
            client.doUpdate(args[2], args[3]);
        }
        else{
            System.out.println(op + " is not supported yet!");
        }
    }

    /*
     * Initializes the library to work with a repository via different
     * protocols.
     */
    private static void setupLibrary(boolean svnProtocol) {
        if (!svnProtocol)
            DAVRepositoryFactory.setup(); // for using over http:// and https://
        else
            SVNRepositoryFactoryImpl.setup(); // for using over svn:// and
                                              // svn+xxx://

    }
}
