package com.intel.xml.rss.util.rexec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.Security;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intel.xml.rss.util.DateTimeRoutine;
import com.intel.xml.rss.util.NullWriter;

public class RExec {
    public static final String VERSION_STRING = "Rexec version 2.1.0 (Oct. 9, 2006 09:56)";

    // every 180 second to check heartbeat
    public static final int CHECK_RSERVER_HEARTBEAT_INTERVAL = 180000;

    String host = null;

    private static final String NEW_LINE = System.getProperty("line.separator");

    private boolean printVersionInfo = false;

    static {
        /*
         * Indicates the caching policy for successful name lookups from the
         * name service. The value is specified as as integer to indicate the
         * number of seconds to cache the successful lookup.
         * 
         * A value of -1 indicates "cache forever".
         */
        Security
                .setProperty("networkaddress.cache.ttl", String.valueOf(60) /*
                                                                             * refresh
                                                                             * per
                                                                             * minute
                                                                             */);
    }

    public RExec() {
    }

    public boolean isPrintVersionInfo() {
        return printVersionInfo;
    }

    public void setPrintVersionInfo(boolean printVersionInfo) {
        this.printVersionInfo = printVersionInfo;
    }

    public void printHelp() {
        System.out.println("usage:");
        System.out.println("    rexec -h");
        System.out.println("  or");
        System.out
                .println("    rexec rserver_address [--kill job_name] [--list] [--shutdown] [--status]");
        System.out.println("  or");
        System.out
                .println("    rexec rserver_address [--workingdir dir] [--timeout seconds]");
        System.out
                .println("                   --jobId job_name cmd_name [cmd_options]...");
        System.out.println("");
        System.out.println("options:");
        System.out
                .println("  -k, --kill job_name           kill a job by name");
        System.out
                .println("  -l, --list                    list all the running jobs");
        System.out
                .println("  -s, --shutdown                shutdown the rserver");
        System.out
                .println("  --status                      query the rserver status");
        System.out
                .println("  -d, --workingdir dir          specify the working directory of");
        System.out
                .println("                                  the created process.");
        System.out
                .println("  -t, --timeout seconds         specify the time out value for");
        System.out.println("                                  this process");
        System.out
                .println("  -j, --jobId job_name          create a new job using the name");
        System.out
                .println("  -h, --help                    print this help message");
    }

    public class QueryServerResult {

        public String version = "";

        public long startMillis = 0;

        public int connectionAccepted = 0;

        public int jobExecuted = 0;
    }

    // @TODO refine this
    public QueryServerResult queryServer(String serverName) {
        if (serverName == null || serverName.length() == 0) {
            return null;
        }
        QueryServerResult result = null;
        StringWriter sw = new StringWriter();

        try {
            exec(sw, new String[] { serverName, "--status" });
        } catch (RException re) {
            return null;
        }

        Pattern p1 = Pattern
                .compile("\\s*(Version|Server started|Connection accepted|Job executed): (.*)$");
        try {
            result = new QueryServerResult();
            BufferedReader breader = new BufferedReader(new StringReader(sw
                    .getBuffer().toString()));
            String line = breader.readLine();
            while (line != null) {
                Matcher m1 = p1.matcher(line);
                if (m1.matches()) {
                    String p = m1.group(1);
                    String v = m1.group(2);
                    if (p.equals("Version")) {
                        result.version = v;
                    } else if (p.equals("Server started")) {
                        try {
                            result.startMillis = DateTimeRoutine
                                    .stdTimeStringToMillis(v);
                        } catch (ParseException e) {
                        }
                    } else if (p.equals("Connection accepted")) {
                        result.connectionAccepted = Integer.parseInt(v);
                    } else if (p.equals("Job executed")) {
                        result.jobExecuted = Integer.parseInt(v);
                    } else {
                        throw new RuntimeException("Impossible, please check!");
                    }
                }
                line = breader.readLine();
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int exec(Writer writer, String[] args)
            throws RServerUnreachableException, RJobTimedOutException,
            RJobKilledException {
        String terminationReason = null;

        if (printVersionInfo) {
            System.err.println(VERSION_STRING);
            System.err.println("");
        }

        if (args.length < 1) {
            System.err
                    .println("Missing required arguments. Using '-h' or '--help' to get the help.");
            System.exit(10);
        } else if (args[0].equals("-h") || args[0].equals("--help")) {
            printHelp();
            return 0;
        } else if (args.length < 2) {
            System.err
                    .println("Missing required arguments. Using '-h' or '--help' to get the help.");
            System.exit(10);
        }

        host = args[0];
        Socket socket = null;
        try {
            socket = new Socket(host, RServer.SERVER_PORT);
            socket.setSoTimeout(CHECK_RSERVER_HEARTBEAT_INTERVAL);
            InputStream ins = socket.getInputStream();
            OutputStream ous = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(ous);

            boolean isAdminCommand = true;

            if (args[1].equals("-s") || args[1].equals("--shutdown")) {
                oos.writeObject(new String[] { "!!!shutdown!!!" });
            } else if (args[1].equals("-l") || args[1].equals("--list")) {
                oos.writeObject(new String[] { "!!!list!!!" });
            } else if (args[1].equals("-k") || args[1].equals("--kill")) {
                if (args.length < 3) {
                    System.err.println("Missing job id for " + args[1]
                            + " option.");
                    System.exit(11);
                }
                String jobId = args[2];
                oos.writeObject(new String[] { String.format("!!!kill:%s!!!",
                        jobId) });
            } else if (args[1].equals("--status")) {
                oos.writeObject(new String[] { "!!!status!!!" });
            } else {
                isAdminCommand = false;
                String jobId = null;
                LinkedList<String> cl = new LinkedList<String>();
                int i = 1;
                boolean optionEnd = false;
                while (i < args.length) {
                    if (!optionEnd && "-j".equals(args[i])
                            || "--jobId".equalsIgnoreCase(args[i])) {
                        if (args.length < i + 2) {
                            System.err
                                    .println("Missing arguments for --jobId.");
                            System.exit(12);
                        }
                        jobId = args[i + 1];
                        cl.addLast(String.format("!!!jobId:%s!!!", jobId));
                        i += 2;
                    } else if (!optionEnd && "-t".equals(args[i])
                            || "--timeout".equalsIgnoreCase(args[i])) {
                        if (args.length < i + 2) {
                            System.err
                                    .println("Missing arguments for --timeout.");
                            System.exit(13);
                        }
                        int timeout = Integer.parseInt(args[i + 1]);
                        cl.addLast(String.format("!!!timeout:%d!!!", timeout));
                        i += 2;
                    } else if (!optionEnd && "-d".equals(args[i])
                            || "--workingdir".equalsIgnoreCase(args[i])) {
                        if (args.length < i + 2) {
                            System.err
                                    .println("Missing arguments for --workingdir.");
                            System.exit(14);
                        }
                        String workingDir = args[i + 1];
                        cl.addLast(String.format("!!!workingdir:%s!!!",
                                workingDir));
                        i += 2;
                    } else {
                        optionEnd = true;
                        cl.addLast(args[i]);
                        ++i;
                    }
                }
                oos.writeObject(cl.toArray(new String[0]));
            }
            BufferedReader breader = new BufferedReader(new InputStreamReader(
                    ins));
            String line = breader.readLine();
            int exitCode = -137;
            if (isAdminCommand) {
                while (line != null) {
                    if ("admin command succeeded".equals(line)) {
                        exitCode = 0;
                    } else if ("admin command failed".equals(line)) {
                        exitCode = 2;
                    } else {
                        writer.write(line);
                        writer.write(NEW_LINE);
                        writer.flush();
                    }
                    line = breader.readLine();
                }
            } else {
                // + exit code: 0
                // private final Pattern exitCodePattern = Pattern
                // .compile( "^\\+\\s+exit code:\\s+(\\d+)(\\(.*\\))?$" );
                String exitCodePrefix = "+  exit code: ";
                while (line != null) {
                    writer.write(line);
                    writer.write(NEW_LINE);
                    writer.flush();

                    if (line.startsWith(exitCodePrefix)) {
                        exitCode = Integer.parseInt(line
                                .substring(exitCodePrefix.length()));
                    } else if (line.startsWith("Reason of termination: ")) {
                        terminationReason = line
                                .substring("Reason of termination: ".length());
                    }
                    line = breader.readLine();
                }
            }

            writer.close();

            return exitCode;

        } catch (SocketTimeoutException se) {
            throw new RServerUnreachableException(host,
                    "reading socket timed out ", se);
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
            throw new RServerUnreachableException(host, String.format(
                    "know nothing about '%s'", host), uhe);
        } catch (IOException e) {
            try {
                writer.write(String.format(
                        "IOException happened between server and client: %s\n",
                        e.getMessage()));
            } catch (IOException doNotUseThisException) {
                // in case the exception e is from writer, we will have to print
                // it
                // to stderr
                System.err
                        .println("IOException happened between server and client: "
                                + e.getMessage());
            }
            throw new RServerUnreachableException(host,
                    "IOException happened while connecting", e);
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                } catch (IOException e) {
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
            if ("time out".equals(terminationReason)) {
                throw new RJobTimedOutException(host);
            } else if ("external request".equals(terminationReason)) {
                throw new RJobKilledException(host);
            }
            // assert ( "normal exit".equals( terminationReason ) );
        }
    }

    public void killJobById(String rserver, String jobId)
            throws RServerUnreachableException, RJobNotFoundException {
        try {
            int exitCode = exec(new NullWriter(), new String[] { rserver, "-k",
                    jobId });
            if (exitCode != 0) {
                throw new RJobNotFoundException(rserver, jobId);
            }
        } catch (RJobTimedOutException e) {
            System.err.println("*** impossible, please check ***");
        } catch (RJobKilledException e) {
            System.err.println("*** impossible, please check ***");
        }
    }

    public static void main(String[] args) {
        try {
            RExec rexec = new RExec();
            rexec.setPrintVersionInfo(true);
            System.exit(rexec.exec(new PrintWriter(System.out), args));
        } catch (RJobKilledException e) {
            System.err.println("Job killed");
        } catch (RJobTimedOutException e) {
            System.err.println("Job timed out");
        } catch (RServerUnreachableException e) {
            System.err.println("Rserver unreachable");
        }
    }

}
