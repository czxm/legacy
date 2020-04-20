package com.intel.cedar.feature.util;

/*
 * ====================================================================
 * Copyright (c) 2004-2008 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;


/*
 * The following example program demonstrates how you can use SVNRepository to
 * obtain a history for a range of revisions including (for each revision): all
 * changed paths, log message, the author of the commit, the timestamp when the 
 * commit was made. It is similar to the "svn log" command supported by the 
 * Subversion client library.
 * 
 * As an example here's a part of one of the program layouts (for the default
 * values):
 * 
 * ---------------------------------------------
 * revision: 1240
 * author: alex
 * date: Tue Aug 02 19:52:49 NOVST 2005
 * log message: 0.9.0 is now trunk
 *
 * changed paths:
 *  A  /trunk (from /branches/0.9.0 revision 1239)
 * ---------------------------------------------
 * revision: 1263
 * author: sa
 * date: Wed Aug 03 21:19:55 NOVST 2005
 * log message: updated examples, javadoc files
 *
 * changed paths:
 *  M  /trunk/doc/javadoc-files/javadoc.css
 *  M  /trunk/doc/javadoc-files/overview.html
 *  M  /trunk/doc/examples/src/org/tmatesoft/svn/examples/wc/StatusHandler.java
 * ...
 * 
 */
public class SVNHistory {
    private static Logger LOG = LoggerFactory.getLogger(SVNHistory.class);
    String url;
    String name = "lab_xmldev";
    String password = "qnn8S*NP";
    boolean svnProtocol = false;

    public SVNHistory(String _url) {
        this.url = _url;
    }

    public SVNHistory(String _url, boolean _svnProtocol) {
        this.url = _url;
        this.svnProtocol = _svnProtocol;
    }

    public SVNHistory(String _url, String _name, String _password,
            boolean _svnProtocol) {
        this.url = _url;
        if(_name != null)
            this.name = _name;
        if(_password != null)
            this.password = _password;
        this.svnProtocol = _svnProtocol;
    }

    public String getLatestModificationRevision() {
        setupLibrary(svnProtocol);

        SVNRepository repository = null;
        int index = url.lastIndexOf("/");
        String path = "";
        String url0 = "";
        try {
            path = url.substring(index + 1);
            url0 = url.substring(0, index);
            repository = SVNRepositoryFactory.create(SVNURL
                    .parseURIEncoded(url0));
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }

        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(name, password);
        repository.setAuthenticationManager(authManager);
        String fullPath = URI.create(url).getPath();
        long endRevision = -1;
        long headRevision = -1;
        try {
            headRevision = repository.getLatestRevision();
            Collection entriesList = repository.getDir(".", headRevision, null,
                    (Collection) null);
            for (Iterator entries = entriesList.iterator(); entries.hasNext();) {
                SVNDirEntry entry = (SVNDirEntry) entries.next();
                if (fullPath.equals(entry.getURL().getPath())) {
                    endRevision = entry.getRevision();
                }
            }
        } catch (SVNException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }

        if (endRevision < 0)
            return null;
        else
            return Long.toString(endRevision);
    }

    public ArrayList<SCMChangeSet> getLatestRevisions(int atMost, long endRevision) {
        setupLibrary(svnProtocol);
        SVNRepository repository = null;
        int index = url.lastIndexOf("/");
        String path = "";
        String url0 = "";
        try {
            path = url.substring(index + 1);
            url0 = url.substring(0, index);
            repository = SVNRepositoryFactory.create(SVNURL
                    .parseURIEncoded(url0));
        }catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }

        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(name, password);
        repository.setAuthenticationManager(authManager);

        /*
         * Gets the latest revision number of the repository
         */
        if (endRevision == -1) {
            try {
                long headRevision = repository.getLatestRevision();
                Collection entriesList = repository.getDir(".", headRevision,
                        null, (Collection) null);
                for (Iterator entries = entriesList.iterator(); entries
                        .hasNext();) {
                    SVNDirEntry entry = (SVNDirEntry) entries.next();
                    if (endRevision < entry.getRevision()) {
                        endRevision = entry.getRevision();
                    }
                }
            } catch (SVNException e) {
                LOG.error(e.getMessage(), e);
                return null;
            }
        }
        if (endRevision < 0)
            return null;
        ArrayList<SCMChangeSet> infoList = new ArrayList<SCMChangeSet>();
        Stack<SCMChangeSet> infoStack = new Stack<SCMChangeSet>();
        long end = endRevision;
        long start = endRevision - 2 * atMost;
        String[] paths = new String[] { path };
        while (infoList.size() < atMost) {
            Collection logEntries = null;
            try {
                logEntries = repository.log(paths, null, start, end, false,
                        true);
            } catch (SVNException e) {
                LOG.error(e.getMessage(), e);
                return infoList;
            }
            for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
                SVNLogEntry logEntry = (SVNLogEntry) entries.next();
                SVNChangeSet cedarLogEntry = new SVNChangeSet();
                cedarLogEntry.setUser(logEntry.getAuthor());
                cedarLogEntry.setDateTime(logEntry.getDate().toString());
                cedarLogEntry.setRev(String.valueOf(logEntry.getRevision()));

                infoStack.push(cedarLogEntry);
            }
            while (!infoStack.empty() && infoList.size() < atMost) {
                infoList.add(infoStack.pop());
            }
            end = start;
            start = end - 2 * atMost;
            if (end <= 0)
                break;
        }
        return infoList;
    }


    public ArrayList<SCMChangeSet> getRevisionLogs(long startRevision,
            long endRevision) {
        setupLibrary(svnProtocol);
        SVNRepository repository = null;
        int index = url.lastIndexOf("/");
        String path = "";
        String url0 = "";
        try {
            path = url.substring(index + 1);
            url0 = url.substring(0, index);
            repository = SVNRepositoryFactory.create(SVNURL
                    .parseURIEncoded(url0));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }

        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(name, password);
        repository.setAuthenticationManager(authManager);
        if (endRevision <= 0) {
            endRevision = Long.parseLong(getLatestModificationRevision());
        }
        if (startRevision > endRevision) {
            startRevision = endRevision;
        }
        ArrayList<SCMChangeSet> infoList = new ArrayList<SCMChangeSet>();
        long start = startRevision;
        long end = endRevision;
        String[] paths = new String[] { path };
        Collection logEntries = null;
        try {
            logEntries = repository.log(paths, null, start, end, true, true);
        } catch (SVNException e) {
            LOG.error(e.getMessage(), e);
            return infoList;
        }
        for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
            SVNChangeSet cedarLogEntry = new SVNChangeSet();
            cedarLogEntry.setUser(logEntry.getAuthor());
            cedarLogEntry.setDateTime(logEntry.getDate().toString());
            cedarLogEntry.setRev(String.valueOf(logEntry.getRevision()));
            cedarLogEntry.setChangeItems(logEntry.getChangedPaths());
            cedarLogEntry.setLogMsg(logEntry.getMessage());
            infoList.add(cedarLogEntry);
        }
        return infoList;
    }

    public String getRevisionLog(String rev) {
        setupLibrary(svnProtocol);

        SVNRepository repository = null;
        int index = url.lastIndexOf("/");
        String path = "";
        String url0 = "";
        try {
            path = url.substring(index + 1);
            url0 = url.substring(0, index);
            repository = SVNRepositoryFactory.create(SVNURL
                    .parseURIEncoded(url0));
        } catch (SVNException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }

        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(name, password);
        repository.setAuthenticationManager(authManager);

        int end = Integer.parseInt(rev);
        int start = end;
        Collection logEntries = null;
        String[] paths = new String[] { path };
        try {
            logEntries = repository.log(paths, null, start, end, true, true);
        } catch (SVNException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
        if (logEntries.size() != 1) {
            System.out.println("Very Strange Error!!!");
            new Exception().printStackTrace();
        }
        for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
            // add changed path as detailed log
            StringBuilder sb = new StringBuilder();
            for(Object o : logEntry.getChangedPaths().values()){
                if(o instanceof SVNLogEntryPath){
                    SVNLogEntryPath ep = (SVNLogEntryPath)o;
                    sb.append(ep.getType());
                    sb.append("    ");
                    sb.append(ep.getPath());
                    sb.append("\n\n");
                }
            }
            return logEntry.getMessage() + "\n\n" + sb.toString();
        }
        return "";
    }

    public static void main(String[] args) {
        SVNHistory history = new SVNHistory(
                "https://sh-ssvn.sh.intel.com/ssg_repos/svn_hadoop/hadoop/hadoop/IDH/trunk/IDH2");
        String rev = history.getLatestModificationRevision();
        System.out.println("latestModificationRevision: " + rev);

        ArrayList<SCMChangeSet> res = history.getLatestRevisions(1, -1);
        for (SCMChangeSet str : res) {
            System.out.println(str);
        }

        System.out.println(history.getRevisionLog("11275"));
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
