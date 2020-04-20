package com.intel.xml.rss.util.rexec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intel.xml.rss.util.DateTimeRoutine;
import com.intel.xml.rss.util.Routine;

public class RServer {
    static {
        System.loadLibrary("jobimpl");
    }

    public static final String VERSION_STRING = "Rserver version 1.0.0 (Jul. 27, 2009 16:40)";

    // 30 seconds
    public static final int RSERVER_HEARTBEAT_INTERVAL = 30000;

    public static final int SERVER_PORT = 7878;

    private Logger logger = Logger.getLogger(getClass().getName());

    private ExecutorService executorService = null;

    private ConcurrentHashMap<String, JobInfo> runningJobMap = new ConcurrentHashMap<String, JobInfo>();

    private IdGen idGen = new IdGen();

    private boolean terminateServer = false;

    private Thread serverThread = null;

    class ServerRuntimeInfo {

        final String version = VERSION_STRING;

        long startupTime;

        int acceptedConnections = 0;

        AtomicLong executedAdminInstructions = new AtomicLong(0);

        AtomicLong executedJobs = new AtomicLong(0);

        public String toDetailString(String prefix, String postfix) {
            if (prefix == null) {
                prefix = "";
            }
            if (postfix == null) {
                postfix = "";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(prefix).append("              Version: ").append(version)
                    .append(postfix).append("\n");
            sb.append(prefix).append("       Server started: ").append(
                    DateTimeRoutine.millisToStdTimeString(startupTime)).append(
                    postfix).append("\n");
            sb.append(prefix).append("           Server age: ").append(
                    DateTimeRoutine.millisToDuration(System.currentTimeMillis()
                            - startupTime)).append(postfix).append("\n");
            sb.append(prefix).append("  Connection accepted: ").append(
                    acceptedConnections).append(postfix).append("\n");
            sb.append(prefix).append("         Job executed: ").append(
                    executedJobs).append(postfix).append("\n");
            return sb.toString();
        }

        public String toDetailString(String prefix) {
            return toDetailString(prefix, null);
        }

        @Override
        public String toString() {
            return toDetailString(null, null);
        }
    }

    ServerRuntimeInfo runtimeInfo = new ServerRuntimeInfo();

    public RServer() {

    }

    public void shutdown() {
        terminateServer = true;
        logger.info("Shutting down server ...");

        synchronized (runningJobMap) {
            for (JobInfo jobInfo : runningJobMap.values()) {
                killJob(jobInfo.jobId);
            }
        }
        executorService.shutdown();
        serverThread.interrupt();
    }

    public void killJob(String jobId) {
        JobInfo jobToKill = runningJobMap.get(jobId);
        if (jobToKill != null) {
            logger.info("Killing job: " + jobId);
            jobToKill.theJob.destroy();
            jobToKill.terminateReason = "external request";
            logger.info("Job " + jobId + " has been killed successfully!");
        }
    }

    public void start() {
        runtimeInfo.startupTime = System.currentTimeMillis();
        executorService = Executors.newCachedThreadPool();
        try {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ServerSocket serverSocket = ssc.socket();
            serverSocket.setReceiveBufferSize(1024 * 64);
            // System.out.println( "Receive buffer size=" +
            // serverSocket.getReceiveBufferSize() );
            serverSocket.bind(new InetSocketAddress(SERVER_PORT));
            ssc.configureBlocking(false);

            serverThread = Thread.currentThread();
            System.err.println(VERSION_STRING);
            System.err.println("");
            logger.info("Server started on port " + SERVER_PORT + " ...");
            while (!terminateServer) {
                SocketChannel sc = ssc.accept();
                if (sc == null) {
                    try {
                        // should investigate how much the sleeping value should
                        // be
                        Thread.sleep(50);
                    } catch (InterruptedException ie) {
                        break;
                    }
                } else {
                    if (!terminateServer) {
                        ++runtimeInfo.acceptedConnections;
                        executorService.execute(new SocketHandler(sc.socket()));
                    }
                }
            }
        } catch (Exception e) {
            logger.severe(Routine.getExceptionLogInfo(
                    "unhandled exception, please investigate", e));
        }
    }

    class SocketHandler implements Runnable {

        private final Socket socket;

        private JobInfo jobInfo;

        SocketHandler(Socket socket) {
            this.socket = socket;
        }

        Pattern killCmdPattern = Pattern.compile("^!!!kill:(.*)!!!$");

        public void run() {
            String host = socket.getInetAddress().getHostName();
            logger.info("Accepted connection from " + host + ".");
            try {
                InputStream ins = socket.getInputStream();
                OutputStream ous = socket.getOutputStream();
                ObjectInputStream oins = new ObjectInputStream(ins);
                Object obj = null;
                obj = oins.readObject();
                String[] cmd = (String[]) obj;
                if (cmd.length == 1 && cmd[0].startsWith("!!!")) {
                    runtimeInfo.executedAdminInstructions.incrementAndGet();
                    logger.info("Executing admin instruction: " + cmd[0]);
                    BufferedWriter bwriter = new BufferedWriter(
                            new OutputStreamWriter(ous));
                    if (cmd[0].equals("!!!shutdown!!!")) {
                        try {
                            bwriter.write("Server about to shutdown.");
                            bwriter.newLine();
                            bwriter.write("admin command succeeded");
                            bwriter.newLine();
                            bwriter.flush();
                        } catch (Exception e) {
                            // in case server dies
                        }
                        RServer.this.shutdown();
                    } else if (cmd[0].equals("!!!list!!!")) {
                        synchronized (runningJobMap) {
                            try {
                                for (JobInfo ji : runningJobMap.values()) {
                                    bwriter.write(ji.toDetailedString("+"));
                                    bwriter.newLine();
                                }
                                bwriter.write("admin command succeeded");
                                bwriter.newLine();
                            } catch (Exception e) {
                                // in case host dies
                            }
                        }
                    } else if (cmd[0].equals("!!!status!!!")) {
                        try {
                            bwriter.write(runtimeInfo.toString());
                            bwriter.newLine();
                            bwriter.write("admin command succeeded");
                            bwriter.newLine();
                            bwriter.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        Matcher matcher = killCmdPattern.matcher(cmd[0]);
                        if (matcher.matches()) {
                            String killJobId = matcher.group(1);

                            if (runningJobMap.get(killJobId) == null) {
                                try {
                                    bwriter.write(String.format(
                                            "Job - %s not found.", killJobId));
                                    bwriter.newLine();
                                    bwriter.write("admin command failed");
                                    bwriter.newLine();
                                    bwriter.flush();
                                } catch (Exception e) {
                                    // in case host dies
                                }
                            } else {
                                try {
                                    bwriter.write("Job - " + killJobId
                                            + " about to be killed.");
                                    bwriter.newLine();
                                    bwriter.write("admin command succeeded");
                                    bwriter.newLine();
                                    bwriter.flush();
                                    killJob(killJobId);
                                } catch (Exception e) {
                                    // in case host dies
                                }
                            }
                        }
                    }
                    try {
                        bwriter.close();
                    } catch (IOException ioe) {
                    }
                } else {
                    runtimeInfo.executedJobs.incrementAndGet();
                    String jobId = idGen.gen();
                    int timeout = -1;
                    String workingDir = null;
                    LinkedList<String> realCmd = new LinkedList<String>();
                    int len = cmd.length;
                    Pattern optionPattern = Pattern
                            .compile("^!!!([a-zA-Z0-9_-]+):(.*)!!!$");
                    boolean optionEnd = false;
                    for (int i = 0; i < len; ++i) {
                        Matcher matcher = optionPattern.matcher(cmd[i]);
                        if (!optionEnd && matcher.matches()) {
                            String optionName = matcher.group(1);
                            String optionValue = matcher.group(2);
                            if ("jobId".equals(optionName)) {
                                jobId = optionValue;
                            } else if ("timeout".equalsIgnoreCase(optionName)) {
                                timeout = Integer.parseInt(optionValue);
                            } else if ("workingdir"
                                    .equalsIgnoreCase(optionName)) {
                                workingDir = optionValue;
                            }
                        } else {
                            optionEnd = true;
                            realCmd.addLast(cmd[i]);
                        }
                    }
                    String[] realCmdArray = realCmd.toArray(new String[0]);
                    if (runningJobMap.containsKey(jobId)) {
                        Enumeration<String> ee = runningJobMap.keys();
                        System.out.println("Existed JobID are : ");
                        while (ee.hasMoreElements()) {
                            System.out.println(ee.nextElement());
                        }

                        BufferedWriter bwriter = null;
                        try {
                            bwriter = new BufferedWriter(
                                    new OutputStreamWriter(ous));
                            bwriter.write(String.format(
                                    "Job \"%s\" exists, abort. ", jobId));
                            bwriter.newLine();
                            bwriter.close();
                        } catch (Exception e) {
                            // client is dead.
                        }
                        try {
                            if (bwriter != null) {
                                bwriter.close();
                            }
                        } catch (Exception e) {
                        }

                    } else {
                        jobInfo = new JobInfo();
                        jobInfo.jobId = jobId;
                        jobInfo.jobHost = host;
                        jobInfo.timeout = timeout;
                        jobInfo.submitTime = new Date(System
                                .currentTimeMillis());
                        jobInfo.commandString = Routine.join(realCmdArray, " ");
                        jobInfo.workingDir = workingDir;
                        try {
                            Future<Integer> future = executorService
                                    .submit(new Worker(realCmdArray, ous));
                            if (timeout > 0)
                                jobInfo.exitValue = future.get(timeout,
                                        TimeUnit.SECONDS);
                            else
                                jobInfo.exitValue = future.get();
                        } catch (TimeoutException e) {
                            killJob(jobInfo.jobId);
                            jobInfo.terminateReason = "time out";
                            jobInfo.endTime = new Date(System
                                    .currentTimeMillis());
                            try {
                                BufferedWriter bwriter = new BufferedWriter(
                                        new OutputStreamWriter(ous));
                                bwriter.newLine();
                                bwriter.write(jobInfo.toDetailedString("+"));
                                bwriter.newLine();

                                bwriter.newLine();
                                bwriter.flush();
                                bwriter.close();
                            } catch (Exception e1) {
                                // this is due to the host is die, so just
                                // ignore
                                // the write to host exception
                            }
                            runningJobMap.remove(jobInfo.jobId);
                            logger.info("Finished executing job:\n"
                                    + jobInfo.toDetailedString("  "));
                        } catch (Exception e) {
                            jobInfo.terminateReason = e.getMessage();
                            jobInfo.endTime = new Date(System
                                    .currentTimeMillis());
                            try {
                                BufferedWriter bwriter = new BufferedWriter(
                                        new OutputStreamWriter(ous));
                                bwriter.newLine();
                                bwriter.write(jobInfo.toDetailedString("+"));
                                bwriter.newLine();

                                bwriter.newLine();
                                bwriter.flush();
                                bwriter.close();
                            } catch (Exception e1) {
                                // this is due to the host is die, so just
                                // ignore
                                // the write to host exception
                            }
                            runningJobMap.remove(jobInfo.jobId);
                            logger.info("Finished executing job:\n"
                                    + jobInfo.toDetailedString("  "));
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("SocketHandler encounted exception: "
                        + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                logger.info("Connection from " + host + " finished.");
            }

        }

        class Worker implements Callable<Integer> {

            private String[] command;

            private OutputStream outputStream;

            public Worker(String[] command, OutputStream ous) {
                this.command = command;
                this.outputStream = ous;
            }

            public Integer call() throws Exception {
                jobInfo.startTime = new Date(System.currentTimeMillis());

                JobBuilder pb = new JobBuilder();
                pb.redirectErrorStream(true);
                if (jobInfo.workingDir != null)
                    pb.directory(new File(jobInfo.workingDir));

                // real.addLast( jobMonitorExecutable );
                StringBuilder sb = new StringBuilder();
                for (String cmd : command) {
                    sb.append(cmd).append(" ");
                }

                int exitValue = 0;
                sb.deleteCharAt(sb.length() - 1);
                pb.command(sb.toString().split(" "));

                BufferedWriter bwriter = new BufferedWriter(
                        new OutputStreamWriter(outputStream));

                Job process = null;
                if (System.getProperty("os.name").contains("Windows"))
                    process = pb.start(jobInfo.jobId);
                else
                    process = pb.start();

                runningJobMap.put(jobInfo.jobId, jobInfo);

                Thread pumperThread = new Pumper(process.getInputStream(),
                        bwriter);
                pumperThread.start();

                logger.info("Start executing job:\n"
                        + jobInfo.toDetailedString("  "));

                jobInfo.theJob = process;

                exitValue = process.waitFor();

                // bug fix: we should wait for timely pumper
                // to exit before we can do the close
                try {
                    pumperThread.join();
                } catch (InterruptedException ie) {
                    // ignore
                }

                jobInfo.endTime = new Date(System.currentTimeMillis());
                jobInfo.exitValue = exitValue;

                try {
                    bwriter.newLine();
                    bwriter.write(jobInfo.toDetailedString("+"));
                    bwriter.newLine();

                    bwriter.newLine();
                    bwriter.flush();
                    bwriter.close();
                } catch (Exception e) {
                    // this is due to the host is die, so just ignore
                    // the write to host exception
                }

                runningJobMap.remove(jobInfo.jobId);
                logger.info("Finished executing job:\n"
                        + jobInfo.toDetailedString("  "));
                return exitValue;

            }

        }

        class Pumper extends Thread {

            BufferedWriter bufferedWriter = null;

            InputStream inputStream = null;

            Pumper(InputStream inputStream, BufferedWriter bwriter) {
                this.inputStream = inputStream;
                this.bufferedWriter = bwriter;
            }

            AtomicBoolean writtenHappened = new AtomicBoolean(false);

            class TimelyPumper extends Thread {

                boolean terminate = false;

                boolean terminated = false;

                @Override
                public void run() {
                    logger.info("Timely pumper started.");
                    while (!terminate) {
                        writtenHappened.set(false);
                        // 10 seconds
                        try {
                            Thread.sleep(RSERVER_HEARTBEAT_INTERVAL);
                        } catch (InterruptedException e) {
                            break;
                        }
                        if (writtenHappened.get()) {
                            continue;
                        }
                        try {
                            bufferedWriter
                                    .write("------- server heart beat ------\n");
                            bufferedWriter.flush();
                        } catch (Exception e) {
                            break;
                        }
                    }
                    terminated = true;
                    logger.info("Timely pumper finished.");
                }

                public void killMe() {
                    terminate = true;
                    this.interrupt();
                    while (!terminated) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                        }
                    }
                }

            }

            @Override
            public void run() {
                TimelyPumper tp = null;
                try {
                    char[] buffer = new char[2048];
                    InputStreamReader reader = new InputStreamReader(
                            inputStream);
                    int n;
                    tp = new TimelyPumper();
                    tp.start();
                    boolean doWriter = true;
                    while ((n = reader.read(buffer)) != -1) {
                        try {
                            if (doWriter) {
                                writtenHappened.set(true);
                                bufferedWriter.write(buffer, 0, n);
                                bufferedWriter.flush();
                            }
                        } catch (Exception e) {
                            // the host seems to dead
                            // the write fails, but we still have to pump the
                            // process's
                            // output,
                            // till it is dead.
                            doWriter = false;
                            logger
                                    .severe(String
                                            .format(
                                                    "Job host %s seems to be dead, job %s has to be killed.",
                                                    jobInfo.jobHost,
                                                    jobInfo.jobId));
                            killJob(jobInfo.jobId);
                        }
                    }

                } catch (Exception e) {
                } finally {
                    if (tp != null) {
                        tp.killMe();
                    }
                }
            }

        }
    }

    class JobInfo {

        String jobId;

        String jobHost;

        String commandString = null;

        String workingDir = null;

        Date submitTime = null;

        Date startTime = null;

        Date endTime = null;

        int exitValue = -987654321;

        int timeout = -1;

        Job theJob = null;

        String terminateReason = "normal exit";

        public String toDetailedString(String prefix) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix).append("Job - ").append(jobId).append("\n");
            if (workingDir != null) {
                sb.append(prefix).append(" workingdir: ").append(workingDir)
                        .append("\n");
            } else {
                sb.append(prefix).append(" workingdir: unspecified\n");
            }
            sb.append(prefix).append("    command: ").append(commandString)
                    .append("\n");
            if (timeout > 0) {
                sb.append(prefix).append("    timeout: ").append(timeout)
                        .append("\n");
            } else {
                sb.append(prefix).append("    timeout: unlimited\n");
            }
            sb.append(prefix).append("       host: ").append(jobHost).append(
                    "\n");
            if (submitTime != null) {
                sb.append(prefix).append("  submitted: ").append(
                        submitTime.toString()).append("\n");
            }
            if (startTime != null) {
                sb.append(prefix).append("    started: ").append(
                        startTime.toString()).append("\n");
            }
            if (endTime != null) {
                sb.append(prefix).append("   finished: ").append(
                        endTime.toString()).append("\n");
                sb.append(prefix).append("  exit code: ").append(exitValue)
                        .append("\n");
                sb.append("Reason of termination: ").append(terminateReason);
            }
            return sb.toString();
        }

    }

    class IdGen {

        private int offset = 19;

        synchronized public String gen() {
            String rv = String.format("%d%07d", System.currentTimeMillis(),
                    ++offset);
            if (offset >= 599999) {
                offset = 19;
            }
            return rv;
        }
    }

    public static void main(String[] args) {
        new RServer().start();
    }

}
