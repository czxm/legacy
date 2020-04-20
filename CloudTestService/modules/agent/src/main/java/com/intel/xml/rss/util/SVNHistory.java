package com.intel.xml.rss.util;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

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
    String url;
    String name;
    String password;
    long startRevision;
    long endRevision;// HEAD (the latest) revision
    boolean svnProtocol = false;

    public SVNHistory(String _url, String _name, String _password,
            long startRev, long endRev) {
        /*
         * Default values:
         */
        url = _url;
        name = _name;
        password = _password;
        startRevision = startRev;
        endRevision = endRev;
    }

    public SVNHistory(String _url, String _name, String _password,
            long startRev, long endRev, boolean svnProtocol) {
        /*
         * Default values:
         */
        url = _url;
        name = _name;
        password = _password;
        startRevision = startRev;
        endRevision = endRev;
        this.svnProtocol = svnProtocol;
    }

    public SVNHistory(long startRev, long endRev) {
        /*
         * Default values:
         */
        url = "http://sh-svn.sh.intel.com/ssg_repos/svn_xmldev/xmldev/penngrove/features/new_java_dom";
        name = "lab_xmldev";
        password = "qnn8S*NP";
        startRevision = startRev;
        endRevision = endRev;
    }

    public SVNHistory() {
        /*
         * Default values:
         */
        url = "http://sh-svn.sh.intel.com/ssg_repos/svn_xmldev/xmldev/penngrove/features/new_java_dom";
        name = "lab_xmldev";
        password = "qnn8S*NP";
        startRevision = 9000;
        endRevision = -1;
    }

    public String[] getUpdateRevisions() {

        /*
         * Initializes the library (it must be done before ever using the
         * library itself)
         */
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
        } catch (SVNException svne) {
            /*
             * Perhaps a malformed URL is the cause of this exception.
             */
            // svne.printStackTrace();
            return null;
        } catch (Exception e) {
            return null;
        }

        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(name, password);
        repository.setAuthenticationManager(authManager);

        /*
         * Gets the latest revision number of the repository
         */
        endRevision = -1;
        try {
            long headRevision = repository.getLatestRevision();
            Collection entriesList = repository.getDir(".", headRevision, null,
                    (Collection) null);
            for (Iterator entries = entriesList.iterator(); entries.hasNext();) {
                SVNDirEntry entry = (SVNDirEntry) entries.next();
                /*
                 * System.out.println("entry: " + entry.getName());
                 * System.out.println("last modified at revision: " +
                 * entry.getRevision() + " by " + entry.getAuthor());
                 */
                if (entry.getName().compareTo(path) == 0) {
                    endRevision = entry.getRevision();
                    break;
                }
            }
        } catch (SVNException svne) {
            // svne.printStackTrace();
            return null;
        }

        Collection logEntries = null;
        try {
            logEntries = repository.log(new String[] { "" }, null,
                    startRevision, endRevision, true, true);
        } catch (SVNException svne) {
            // svne.printStackTrace();
            return null;
        }
        String[] revList = new String[logEntries.size()];
        int i = 0;
        for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
            /*
             * gets a next SVNLogEntry
             */
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
            revList[i++] = String.valueOf(logEntry.getRevision());
        }
        return revList;
    }

    public String getLatestRevision() {

        /*
         * Initializes the library (it must be done before ever using the
         * library itself)
         */
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
        } catch (SVNException svne) {
            /*
             * Perhaps a malformed URL is the cause of this exception.
             */
            // svne.printStackTrace();
            return null;
        } catch (Exception e) {
            return null;
        }

        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(name, password);
        repository.setAuthenticationManager(authManager);

        /*
         * Gets the latest revision number of the repository
         */
        endRevision = -1;
        try {
            long headRevision = repository.getLatestRevision();
            Collection entriesList = repository.getDir(".", headRevision, null,
                    (Collection) null);
            for (Iterator entries = entriesList.iterator(); entries.hasNext();) {
                SVNDirEntry entry = (SVNDirEntry) entries.next();
                /*
                 * System.out.println("entry: " + entry.getName());
                 * System.out.println("last modified at revision: " +
                 * entry.getRevision() + " by " + entry.getAuthor());
                 */
                // if(entry.getName().compareTo(path)==0)
                if (endRevision < entry.getRevision()) {
                    endRevision = entry.getRevision();
                    // break;
                }
            }
        } catch (SVNException svne) {
            // svne.printStackTrace();
            return null;
        }
        if (endRevision < 0)
            return null;
        else
            return Long.toString(endRevision);
    }

    public ArrayList<String> getLatestRevisions(int atMost) {

        /*
         * Initializes the library (it must be done before ever using the
         * library itself)
         */
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
        } catch (SVNException svne) {
            /*
             * Perhaps a malformed URL is the cause of this exception.
             */
            // svne.printStackTrace();
            return null;
        } catch (Exception e) {
            return null;
        }

        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(name, password);
        repository.setAuthenticationManager(authManager);

        /*
         * Gets the latest revision number of the repository
         */
        endRevision = -1;
        try {
            long headRevision = repository.getLatestRevision();
            Collection entriesList = repository.getDir(".", headRevision, null,
                    (Collection) null);
            for (Iterator entries = entriesList.iterator(); entries.hasNext();) {
                SVNDirEntry entry = (SVNDirEntry) entries.next();
                /*
                 * System.out.println("entry: " + entry.getName());
                 * System.out.println("last modified at revision: " +
                 * entry.getRevision() + " by " + entry.getAuthor());
                 */
                // if(entry.getName().compareTo(path)==0)
                if (endRevision < entry.getRevision()) {
                    endRevision = entry.getRevision();
                    // break;
                }
            }
        } catch (SVNException svne) {
            // svne.printStackTrace();
            return null;
        }
        if (endRevision < 0)
            return null;
        ArrayList<String> infoList = new ArrayList<String>();
        Stack<String> infoStack = new Stack<String>();
        long end = endRevision;
        long start = endRevision - 2 * atMost;
        String[] paths = new String[] { path };
        while (infoList.size() < atMost) {
            Collection logEntries = null;
            try {
                logEntries = repository.log(paths, null, start, end, false,
                        true);
            } catch (SVNException svne) {
                // svne.printStackTrace();
                return infoList;
            }
            int i = 0;
            String rev = "";
            for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
                /*
                 * gets a next SVNLogEntry
                 */
                SVNLogEntry logEntry = (SVNLogEntry) entries.next();
                rev = String.valueOf(logEntry.getRevision());
                infoStack.push(rev);
            }
            while (!infoStack.empty() && infoList.size() < atMost) {
                infoList.add(infoStack.pop());
            }
            end = start;
            start = end - 2 * atMost;
        }
        return infoList;
    }

    public String getRevisionLog(String rev) {

        /*
         * Initializes the library (it must be done before ever using the
         * library itself)
         */
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
        } catch (SVNException svne) {
            /*
             * Perhaps a malformed URL is the cause of this exception.
             */
            // svne.printStackTrace();
            return null;
        }

        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(name, password);
        repository.setAuthenticationManager(authManager);

        /*
         * Gets the latest revision number of the repository
         */
        int end = Integer.parseInt(rev);
        int start = end;
        Collection logEntries = null;
        String[] paths = new String[] { path };
        try {
            logEntries = repository.log(paths, null, start, end, false, true);
        } catch (SVNException svne) {
            // svne.printStackTrace();
            return null;
        }
        if (logEntries.size() != 1) {
            System.out.println("Very Strange Error!!!");
            new Exception().printStackTrace();
        }
        for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
            /*
             * gets a next SVNLogEntry
             */
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
            return "[" + logEntry.getAuthor() + " | " + logEntry.getDate()
                    + "] " + logEntry.getMessage();
        }
        return "";
    }

    public static void main(String[] args) {
        /*
         * SVNHistory svnHis=newSVNHistory(
         * "http://sh-svn.sh.intel.com/ssg_repos/svn_xmldev/xmldev/penngrove/tags/NIGHTLY_TRUNK_TAG_20091104/xmlcore-src"
         * , "lab_xmldev","qnn8S*NP",1,-1); ArrayList<String>
         * revList=svnHis.getLatestRevisions(2); for(String rev:revList)
         * System.out.println(rev); System.out.println(""); svnHis=new
         * SVNHistory
         * ("http://sh-svn.sh.intel.com/ssg_repos/svn_xmldev/xmldev/penngrove/trunk"
         * , "lab_xmldev","qnn8S*NP",1,-1);
         * revList=svnHis.getLatestRevisions(30); for(String rev:revList)
         * System.out.println(rev);
         */
        SVNHistory svnHis = new SVNHistory(
                "http://sh-svn.sh.intel.com/ssg_repos/svn_xmldev/xmldev/penngrove/tags/NIGHTLY_TRUNK_TAG_20091104/xmlcore-src",
                "lab_xmldev", "qnn8S*NP", 1, -1);
        String rev = svnHis.getLatestRevision();
        System.out.println(rev);
    }

    /*
     * args parameter is used to obtain a repository location URL, a start
     * revision number, an end revision number, user's account name & password
     * to authenticate him to the server.
     */
    public static void main1(String[] args) {
        /*
         * Default values:
         */
        SVNHistory obj = new SVNHistory();
        String url = obj.url;
        String name = obj.name;
        String password = obj.password;
        long startRevision = obj.startRevision;
        long endRevision = obj.endRevision;
        /*
         * Initializes the library (it must be done before ever using the
         * library itself)
         */
        setupLibrary(false);

        if (args != null) {
            /*
             * Obtains a repository location URL
             */
            url = (args.length >= 1) ? args[0] : url;
            /*
             * Obtains the start point of the revisions range
             */
            startRevision = (args.length >= 2) ? Long.parseLong(args[1])
                    : startRevision;
            /*
             * Obtains the end point of the revisions range
             */
            endRevision = (args.length >= 3) ? Long.parseLong(args[2])
                    : endRevision;
            /*
             * Obtains an account name (will be used to authenticate the user to
             * the server)
             */
            name = (args.length >= 4) ? args[3] : name;
            /*
             * Obtains a password
             */
            password = (args.length >= 5) ? args[4] : password;
        }

        SVNRepository repository = null;

        try {
            /*
             * Creates an instance of SVNRepository to work with the repository.
             * All user's requests to the repository are relative to the
             * repository location used to create this SVNRepository. SVNURL is
             * a wrapper for URL strings that refer to repository locations.
             */
            repository = SVNRepositoryFactory.create(SVNURL
                    .parseURIEncoded(url));
        } catch (SVNException svne) {
            /*
             * Perhaps a malformed URL is the cause of this exception.
             */
            System.err
                    .println("error while creating an SVNRepository for the location '"
                            + url + "': " + svne.getMessage());
            System.exit(1);
        }

        /*
         * User's authentication information (name/password) is provided via an
         * ISVNAuthenticationManager instance. SVNWCUtil creates a default
         * authentication manager given user's name and password.
         * 
         * Default authentication manager first attempts to use provided user
         * name and password and then falls back to the credentials stored in
         * the default Subversion credentials storage that is located in
         * Subversion configuration area. If you'd like to use provided user
         * name and password only you may use BasicAuthenticationManager class
         * instead of default authentication manager:
         * 
         * authManager = new BasicAuthenticationsManager(userName,
         * userPassword);
         * 
         * You may also skip this point - anonymous access will be used.
         */
        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(name, password);
        repository.setAuthenticationManager(authManager);

        /*
         * Gets the latest revision number of the repository
         */
        try {
            endRevision = repository.getLatestRevision();
        } catch (SVNException svne) {
            System.err
                    .println("error while fetching the latest repository revision: "
                            + svne.getMessage());
            System.exit(1);
        }

        Collection logEntries = null;
        try {
            /*
             * Collects SVNLogEntry objects for all revisions in the range
             * defined by its start and end points [startRevision, endRevision].
             * For each revision commit information is represented by
             * SVNLogEntry.
             * 
             * the 1st parameter (targetPaths - an array of path strings) is set
             * when restricting the [startRevision, endRevision] range to only
             * those revisions when the paths in targetPaths were changed.
             * 
             * the 2nd parameter if non-null - is a user's Collection that will
             * be filled up with found SVNLogEntry objects; it's just another
             * way to reach the scope.
             * 
             * startRevision, endRevision - to define a range of revisions you
             * are interested in; by default in this program - startRevision=0,
             * endRevision= the latest (HEAD) revision of the repository.
             * 
             * the 5th parameter - a boolean flag changedPath - if true then for
             * each revision a corresponding SVNLogEntry will contain a map of
             * all paths which were changed in that revision.
             * 
             * the 6th parameter - a boolean flag strictNode - if false and a
             * changed path is a copy (branch) of an existing one in the
             * repository then the history for its origin will be traversed; it
             * means the history of changes of the target URL (and all that
             * there's in that URL) will include the history of the origin
             * path(s). Otherwise if strictNode is true then the origin path
             * history won't be included.
             * 
             * The return value is a Collection filled up with SVNLogEntry
             * Objects.
             */
            logEntries = repository.log(new String[] { "" }, null,
                    startRevision, endRevision, true, true);

        } catch (SVNException svne) {
            System.out.println("error while collecting log information for '"
                    + url + "': " + svne.getMessage());
            System.exit(1);
        }
        for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
            /*
             * gets a next SVNLogEntry
             */
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
            System.out.println("---------------------------------------------");
            /*
             * gets the revision number
             */
            System.out.println("revision: " + logEntry.getRevision());
            /*
             * gets the author of the changes made in that revision
             */
            System.out.println("author: " + logEntry.getAuthor());
            /*
             * gets the time moment when the changes were committed
             */
            System.out.println("date: " + logEntry.getDate());
            /*
             * gets the commit log message
             */
            System.out.println("log message: " + logEntry.getMessage());
            /*
             * displaying all paths that were changed in that revision; cahnged
             * path information is represented by SVNLogEntryPath.
             */
            if (logEntry.getChangedPaths().size() > 0) {
                System.out.println();
                System.out.println("changed paths:");
                /*
                 * keys are changed paths
                 */
                Set changedPathsSet = logEntry.getChangedPaths().keySet();

                for (Iterator changedPaths = changedPathsSet.iterator(); changedPaths
                        .hasNext();) {
                    /*
                     * obtains a next SVNLogEntryPath
                     */
                    SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry
                            .getChangedPaths().get(changedPaths.next());
                    /*
                     * SVNLogEntryPath.getPath returns the changed path itself;
                     * 
                     * SVNLogEntryPath.getType returns a charecter describing
                     * how the path was changed ('A' - added, 'D' - deleted or
                     * 'M' - modified);
                     * 
                     * If the path was copied from another one (branched) then
                     * SVNLogEntryPath.getCopyPath &
                     * SVNLogEntryPath.getCopyRevision tells where it was copied
                     * from and what revision the origin path was at.
                     */
                    System.out.println(" "
                            + entryPath.getType()
                            + "	"
                            + entryPath.getPath()
                            + ((entryPath.getCopyPath() != null) ? " (from "
                                    + entryPath.getCopyPath() + " revision "
                                    + entryPath.getCopyRevision() + ")" : ""));
                }
            }
        }
    }

    /*
     * Initializes the library to work with a repository via different
     * protocols.
     */
    private static void setupLibrary(boolean svnProtocol) {
        /*
         * For using over http:// and https://
         */
        if (!svnProtocol)
            DAVRepositoryFactory.setup();
        /*
         * For using over svn:// and svn+xxx://
         */
        else
            SVNRepositoryFactoryImpl.setup();

        /*
         * For using over file:///
         */
        // FSRepositoryFactory.setup();
    }
}
