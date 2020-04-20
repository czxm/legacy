package com.intel.cedar.agent.runtime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.agent.impl.TaskRunnerStatus;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.impl.Result;
import com.intel.cedar.util.SubDirectory;
import com.intel.xml.rss.util.rexec.Job;
import com.intel.xml.rss.util.rexec.JobBuilder;

public class AgentRuntime {
    private static Logger LOG = LoggerFactory.getLogger(AgentRuntime.class);
    private static AgentRuntime singleton;

    public static AgentRuntime getInstance() {
        if (singleton == null) {
            singleton = new AgentRuntime();
        }
        return singleton;
    }

    class TaskItem {
        String taskId;
        String tasklet;
        String taskItem;
        String itemContent;
        int timeout;
        String cwd;
        String storageRoot;
        TaskRunnerStatus status;
        TaskRunner runner;
        String[] features;
        boolean queued;
        boolean persistent;
        String agentID;
        boolean debug;
    }

    class TaskItemRunner implements Runnable {
        private TaskItem item;

        public TaskItemRunner(TaskItem item) {
            this.item = item;
        }

        public void run() {
            try {
                item.runner = new TaskRunner(item);
                try {
                    if (item.timeout > 0) {
                        exe.submit(item.runner).get(item.timeout,
                                java.util.concurrent.TimeUnit.SECONDS);
                    } else {
                        exe.submit(item.runner).get();
                    }
                } catch (java.util.concurrent.TimeoutException e1) {
                    item.status = TaskRunnerStatus.Timeout;
                    item.runner.kill();
                    LOG.info("Timeout " + item.tasklet + " as " + item.taskId);
                }
            } catch (Exception e) {
            }
        }
    }

    class TaskQueueRunner implements Runnable {
        private ConcurrentLinkedQueue<TaskItem> queue = new ConcurrentLinkedQueue<TaskItem>();

        public TaskItem[] getQueuedItems() {
            return queue.toArray(new TaskItem[] {});
        }

        public void run() {
            while (true) {
                try {
                    TaskItem item = queue.poll();
                    if (item == null) {
                        Thread.sleep(1000);
                    } else {
                        item.runner = new TaskRunner(item);
                        try {
                            if (item.timeout > 0) {
                                exe.submit(item.runner).get(item.timeout,
                                        java.util.concurrent.TimeUnit.SECONDS);
                            } else {
                                exe.submit(item.runner).get();
                            }
                        } catch (java.util.concurrent.TimeoutException e1) {
                            item.runner.kill();
                            item.status = TaskRunnerStatus.Timeout;
                            LOG.info("Timeout " + item.tasklet + " as "
                                    + item.taskId);
                        }
                    }
                } catch (Exception e) {
                }
            }
        }

        public void offer(TaskItem item) {
            queue.offer(item);
        }

        public boolean cancel(String taskId) {
            // return true if the task is not running and removed from queue
            for (TaskItem item : queue) {
                if (item.taskId.equals(taskId)) {
                    return queue.remove(item);
                }
            }
            return false;
        }
    }

    class TaskRunner implements Runnable {
        private CircularByteBuffer buffer = new CircularByteBuffer(
                CircularByteBuffer.INFINITE_SIZE);
        private CircularByteBuffer taskLog = new CircularByteBuffer(
                CircularByteBuffer.INFINITE_SIZE);
        private StringWriter taskWriter = new StringWriter();
        private boolean logRequested = false;
        private StringWriter failureWriter = new StringWriter();
        private TimelyPumper tp;
        private Job process;
        private TaskItem item;
        private Result result = new Result();
        private OutputStream controlOutput;
        private String storageFile;

        public TaskRunner(TaskItem item) {
            this.item = item;
        }

        public InputStream getLogStream() {
            logRequested = true;
            if (item.status.equals(TaskRunnerStatus.Started)
                    || item.status.equals(TaskRunnerStatus.Submitted))
                return taskLog.getInputStream();
            else
                return new ByteArrayInputStream(taskWriter.toString()
                        .getBytes());
        }

        public InputStream getControlInputStream() {
            return buffer.getInputStream();
        }

        public OutputStream getControlOutputStream() {
            return controlOutput;
        }

        public void setStorageFile(String file) {
            this.storageFile = file;
        }

        public String getStorageFile() {
            return this.storageFile;
        }

        public void kill() {
            if (process != null) {
                if (item.status != TaskRunnerStatus.Timeout)
                    item.status = TaskRunnerStatus.Killed;
                process.destroy();
            }
            LOG.info("Task killed: " + item.taskId);
        }

        public Result getResult() {
            return result;
        }

        protected String getClassPath() {
            StringBuilder sb = new StringBuilder();
            sb.append(".");
            for (File file : new File(SubDirectory.FEATURES.toString())
                    .listFiles()) {
                if (file.isFile() && file.getName().endsWith(".jar")
                        && file.getName().startsWith("cedar")) {
                    sb.append(File.pathSeparator);
                    sb.append(file.getAbsolutePath());
                }
            }
            for (String feature : item.features) {
                File featureDir = new File(SubDirectory.FEATURES.toString()
                        + feature);
                if (featureDir.isDirectory()) {
                    for (File subFile : featureDir.listFiles()) {
                        if (subFile.isFile()) {
                            if (subFile.getName().endsWith(".jar")) {
                                sb.append(File.pathSeparator);
                                sb.append(subFile.getAbsolutePath());
                            }
                        } else {
                            if (subFile.getName().equalsIgnoreCase("lib")) {
                                for (File libFile : subFile.listFiles()) {
                                    if (libFile.isFile()
                                            && libFile.getName().endsWith(
                                                    ".jar")) {
                                        sb.append(File.pathSeparator);
                                        sb.append(libFile.getAbsolutePath());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (String file : new File(SubDirectory.LIBS.toString()).list()) {
                sb.append(File.pathSeparator);
                sb.append(SubDirectory.LIBS.toString() + file);
            }
            return sb.toString();
        }

        public void run() {
            long startTime = System.currentTimeMillis();
            HeartBeat hb = null;
            try {
                item.status = TaskRunnerStatus.Started;
                LOG.info("Started " + item.tasklet + " as " + item.taskId);
                JobBuilder pb = new JobBuilder();
                pb.redirectErrorStream(false);

                List<String> params = new ArrayList<String>();
                params.add(System.getProperty("java.cmd"));
                String debugPort = "8002";
                if (System.getProperty("sandbox.debug.port") != null) {
                    debugPort = System.getProperty("sandbox.debug.port");
                }
                if (item.debug) {
                    params.add("-Xdebug");
                    params
                            .add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address="
                                    + debugPort);
                }
                params.add("-cp");
                params.add(getClassPath());
                params.add("com.intel.cedar.agent.runtime.Sandbox");
                params.add(item.tasklet);
                params.add(item.taskItem);
                params.add(String.format("%d",
                        item.itemContent.getBytes().length));
                params.add(item.agentID);
                params.add(Boolean.toString(item.persistent));
                if (item.storageRoot != null)
                    params.add(item.storageRoot);

                pb.command(params);
                File workingDir = new File(item.cwd);
                if (workingDir.exists())
                    pb.directory(workingDir);

                BufferedWriter bwriter = new BufferedWriter(
                        new OutputStreamWriter(buffer.getOutputStream()));

                if (System.getProperty("os.name").contains("Windows")) {
                    process = pb.start(item.taskId);
                } else {
                    process = pb.start();
                }

                controlOutput = process.getOutputStream();
                controlOutput.write(item.itemContent.getBytes());
                controlOutput.flush();

                tp = new TimelyPumper(new InputStreamReader(process
                        .getErrorStream()), bwriter);
                tp.start();

                String line = null;
                BufferedReader reader = new BufferedReader(new InputStreamReader(process
                        .getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(taskLog
                        .getOutputStream()));
                AtomicBoolean writtenHappened = new AtomicBoolean(false);
                hb = new HeartBeat(writer, writtenHappened);
                hb.start();
                while ((line = reader.readLine()) != null) {
                    writtenHappened.set(true);
                    if (!logRequested) {
                        taskWriter.write(line);
                        taskWriter.write("\n");
                        taskWriter.flush();
                    }
                    writer.write(line);
                    writer.newLine();
                    writer.flush();
                    writtenHappened.set(false);
                }
                reader.close();
                writer.close();
            } catch (Throwable e) {
                LOG.error("", e);
            } finally {
                try {
                    if (process != null)
                        process.waitFor();
                    if (item.status.equals(TaskRunnerStatus.Started)) {
                        item.status = TaskRunnerStatus.Finished;
                        LOG.info("Finished " + item.tasklet + " as "
                                + item.taskId);
                    }
                    if (tp != null)
                        tp.kill();
                    if (hb != null) {
                        hb.shutdown();
                    }
                    buffer.getOutputStream().close();
                    taskLog.getOutputStream().close();
                } catch (Exception e) {
                }
            }
            ServerRuntimeInfo.getInstance().addExecutedTime(item.tasklet,
                    System.currentTimeMillis() - startTime);
            ServerRuntimeInfo.getInstance().addExecutedTask(item.tasklet);
        }

        class TimelyPumper extends Thread {
            private boolean started = false;
            private volatile boolean terminate = false;
            private volatile boolean terminated = false;
            private BufferedWriter bufferedWriter;
            private BufferedReader jobReader;
            private HeartBeat heartbeat;
            private AtomicBoolean writtenHappened = new AtomicBoolean(false);

            public TimelyPumper(InputStreamReader jobReader,
                    BufferedWriter writer) {
                bufferedWriter = writer;
                this.jobReader = new BufferedReader(jobReader);
            }

            public String readData(String endTag) {
                StringBuilder sb = new StringBuilder();
                String line = "";
                try {
                    while ((line = jobReader.readLine()) != null) {
                        if (line.equals(endTag))
                            break;
                        sb.append(line);
                    }
                } catch (Exception e) {
                }
                return sb.toString();
            }

            @Override
            public void run() {
                LOG.info("Timely pumper started for " + item.tasklet + " as "
                        + item.taskId);
                started = true;
                heartbeat = new HeartBeat(bufferedWriter, writtenHappened);
                heartbeat.start();
                try {
                    String line = "";
                    boolean processCedarMessages = false;
                    while ((line = jobReader.readLine()) != null) {
                        writtenHappened.set(true);
                        if (line.equals("@@CedarResult")) {
                            String content = readData("CedarResult@@");
                            result.setID(ResultID.valueOf(content));
                            writtenHappened.set(false);
                            break;
                        } else if (line.equals("@@CedarStorageFile")) {
                            bufferedWriter.write("@@CedarHeartBeat@@");
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                            storageFile = readData("CedarStorageFile@@");
                        } else if (line.startsWith("@@Cedar")) {
                            processCedarMessages = true;
                            bufferedWriter.write(line);
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                        } else if (processCedarMessages) {
                            bufferedWriter.write(line);
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                            if (line.endsWith("@@"))
                                processCedarMessages = false;
                            else
                                continue;
                        } else {
                            bufferedWriter.write("@@CedarHeartBeat@@");
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                            // TODO: don't collect the whole stderr of the
                            // running task
                            if (failureWriter.getBuffer().length() < 8192) {
                                failureWriter.write(line);
                                failureWriter.write("\n");
                                failureWriter.flush();
                            }
                        }
                        writtenHappened.set(false);
                    }
                    while (!terminate) {
                        Thread.sleep(0);
                    }
                } catch (Throwable e) {
                    LOG.error("", e);
                } finally {
                    if (item.status.equals(TaskRunnerStatus.Killed))
                        result.setID(ResultID.Killed);
                    else if (item.status.equals(TaskRunnerStatus.Timeout))
                        result.setID(ResultID.Timeout);
                    if (!failureWriter.toString().equals("")) {
                        result.setFailureMessage(StringEscapeUtils
                                .escapeXml(failureWriter.toString()));
                    }
                    if (heartbeat != null) {
                        heartbeat.shutdown();
                    }
                    try {
                        bufferedWriter.write("@@CedarFinish@@");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    } catch (Exception e1) {
                    }
                }
                terminated = true;
                LOG.info("Timely pumper finished for " + item.tasklet + " as "
                        + item.taskId);
            }

            public void kill() {
                if (started) {
                    terminate = true;
                    while (!terminated) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    }

    private ExecutorService exe = Executors.newCachedThreadPool();
    private ConcurrentHashMap<String, TaskQueueRunner> queues = new ConcurrentHashMap<String, TaskQueueRunner>();
    private ConcurrentHashMap<String, TaskItem> tasks = new ConcurrentHashMap<String, TaskItem>();

    private AgentRuntime() {
    }

    public String submit(boolean queued, String[] features, String tasklet,
            String taskItem, String itemContent, int timeout, String cwd,
            String storageRoot, boolean isPersist, String agentID, boolean debug) {
        TaskItem item = new TaskItem();
        item.taskId = UUID.randomUUID().toString();
        item.tasklet = tasklet;
        item.taskItem = taskItem;
        item.itemContent = itemContent;
        item.timeout = timeout;
        item.cwd = cwd;
        item.storageRoot = storageRoot;
        item.status = TaskRunnerStatus.Submitted;
        item.runner = null;
        item.features = features;
        item.queued = queued;
        item.persistent = isPersist;
        item.agentID = agentID;
        item.debug = debug;
        tasks.put(item.taskId, item);
        if (queued) {
            synchronized (AgentRuntime.class) {
                if (!queues.containsKey(tasklet)) {
                    TaskQueueRunner runner = new TaskQueueRunner();
                    queues.put(tasklet, runner);
                    exe.submit(runner);
                }
            }
            TaskQueueRunner queue = queues.get(tasklet);
            queue.offer(item);
        } else {
            TaskItemRunner runner = new TaskItemRunner(item);
            exe.submit(runner);
        }
        LOG.info("Submitted new tasklet: " + item.tasklet + " as "
                + item.taskId);
        return item.taskId;
    }

    public void kill(String taskId) {
        TaskItem item = tasks.get(taskId);
        if (item != null) {
            if (item.runner != null) {
                // it's already running
                item.runner.kill();
            } else {
                String tasklet = item.tasklet;
                TaskQueueRunner queue = queues.get(tasklet);
                if (queue != null && queue.cancel(taskId)) {
                    LOG.info("Task cancelled: {}", taskId);
                    purgeTask(taskId);
                } else {
                    // wait for a while to see if this item is scheduled
                    try {
                        Thread.sleep(5000);
                        if (item.runner != null) {
                            // it's already running
                            item.runner.kill();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public void purgeTask(String taskId) {
        tasks.remove(taskId);
    }

    public String getStorageFile(String taskId) {
        TaskItem item = tasks.get(taskId);
        if (item != null) {
            // wait for the item being scheduled
            while (item.runner == null) {
                try {
                    if (!tasks.containsKey(taskId))
                        return null;
                    Thread.sleep(30);
                } catch (Exception e) {
                }
            }
            return item.runner.getStorageFile();
        }
        return null;
    }

    public OutputStream getControlOutputStream(String taskId) {
        TaskItem item = tasks.get(taskId);
        if (item != null) {
            // wait for the item being scheduled
            while (item.runner == null) {
                try {
                    if (!tasks.containsKey(taskId))
                        return null;
                    Thread.sleep(30);
                } catch (Exception e) {
                }
            }
            return item.runner.getControlOutputStream();
        }
        return null;
    }

    public InputStream getControlInputStream(String taskId) {
        TaskItem item = tasks.get(taskId);
        if (item != null && item.runner != null) {
            return item.runner.getControlInputStream();
        }
        return null;
    }

    public InputStream getTaskLogStream(String taskId) {
        TaskItem item = tasks.get(taskId);
        if (item != null && item.runner != null) {
            return item.runner.getLogStream();
        }
        return null;
    }

    public Result getResult(String taskId) {
        TaskItem item = tasks.get(taskId);
        if (item != null) {
            if (item.runner != null)
                return item.runner.getResult();
        }
        return new Result(); // it's queued, canceled or evicted
    }

    public TaskRunnerStatus getStatus(String taskId) {
        TaskItem item = tasks.get(taskId);
        if (item != null) {
            return item.status;
        }
        return TaskRunnerStatus.Evicted;
    }

    public List<String> listTasks() {
        List<String> result = new ArrayList<String>();
        HashMap<String, List<String>> collected = new HashMap<String, List<String>>();
        // collect all tasklet queue, and its running taskrunner
        for (TaskItem item : tasks.values()) {
            List<String> l = collected.get(item.tasklet);
            if (l == null) {
                l = new ArrayList<String>();
                collected.put(item.tasklet, l);
            }
            if (item.status.equals(TaskRunnerStatus.Started)) {
                l.add(String
                        .format("%s -- %s", item.taskId, item.status.name()));
                // l.add(item.itemContent);
            }
        }
        // collect all queued taskrunners
        for (String tasklet : collected.keySet()) {
            TaskQueueRunner queueRunner = queues.get(tasklet);
            if (queueRunner != null) {
                TaskItem[] items = queueRunner.getQueuedItems();
                List<String> l = collected.get(tasklet);
                if (l != null) {
                    for (TaskItem item : items) {
                        l.add(String.format("%s -- %s", item.taskId,
                                item.status.name()));
                        // l.add(item.itemContent);
                    }
                }
            }
        }
        for (String key : collected.keySet()) {
            result.add(key + " Queue:");
            result.addAll(collected.get(key));
            result.add("\n");
        }
        return result;
    }
}
