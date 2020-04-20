package com.intel.cedar.engine.impl;

import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import com.intel.cedar.agent.IExtensiveAgent;
import com.intel.cedar.agent.impl.LocalExtensiveAgent;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.VolumeInfo;
import com.intel.cedar.engine.model.feature.Tasklet;
import com.intel.cedar.engine.model.feature.flow.TaskletFlow;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.tasklet.AbstractResult;
import com.intel.cedar.tasklet.SimpleTaskItem;
import com.intel.cedar.tasklet.IProgressProvider;
import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ITaskRunner;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.TaskletInfo;
import com.intel.cedar.tasklet.impl.GenericTaskItem;
import com.intel.cedar.tasklet.impl.OnFinishTaskItem;
import com.intel.cedar.tasklet.impl.OnStartTaskItem;
import com.intel.cedar.tasklet.impl.Result;
import com.intel.cedar.util.CedarConfiguration;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

class AgentRunner implements Runnable {
    private LogWriter logger;
    private IExtensiveAgent agent;
    private ITaskRunner runner;
    private ConcurrentLinkedQueue<ITaskItem> queue;
    private Object machine;
    private VolumeInfo volume;
    private CountDownLatch barrier;
    private TaskletRuntimeInfo tasklet;
    private String timeout;
    private String cwd;

    private volatile boolean killed;
    private volatile boolean earlyExit;
    private volatile boolean isWaiting;

    private ITaskItem curItem;
    private int accomplished;
    private IFolder storage;
    private String agentId;
    private String hostName;
    private TaskletFlow.FailurePolicy policy;

    public AgentRunner(IExtensiveAgent agent, ITaskRunner runner, LogWriter logger,
            TaskletFlow.FailurePolicy policy, TaskletRuntimeInfo tasklet, VolumeInfo volume, Object host,
            CountDownLatch barrier) {
        this.agent = agent;
        this.logger = logger;
        this.runner = runner;
        this.queue = tasklet.getWorkingQueue();
        this.volume = volume;
        this.machine = (host instanceof InstanceInfo) ? ((InstanceInfo) host)
                .getMachineInfo() : host;
        this.barrier = barrier;
        this.tasklet = tasklet;
        this.timeout = "0";
        this.cwd = "";
        this.accomplished = 0;
        this.hostName = agent.getServerInfo().getServer();
        this.agentId = agent.getAgentID() + "_" + hostName;
        this.policy = policy;
    }

    public IExtensiveAgent getAgent() {
        return agent;
    }

    public String getHostName() {
        return this.hostName;
    }

    public String getStatus() {
        if (isWaiting) {
            return "Server is starting up ...";
        } else {
            if (curItem != null)
                return (curItem.getValue());
            return agent.getStatus(runner).name();
        }
    }

    public String getProgress() {
        String progress = null;
        if (runner instanceof IProgressProvider) {
            progress = ((IProgressProvider) runner).getProgress();
        }
        if (progress == null)
            progress = "Waiting for response ...";
        return progress;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public void setWorkingDir(String cwd) {
        this.cwd = cwd;
    }

    public void setStorage(IFolder storage) {
        this.storage = storage;
    }

    public int getAccomplished() {
        return this.accomplished;
    }

    public void kill() {
        agent.kill(runner);
        killed = true;
    }

    public void earlyExit() {
        this.earlyExit = true;
    }

    public boolean isEarlyExit() {
        return this.earlyExit;
    }

    protected boolean shouldQuit() {
        return isEarlyExit() || killed;
    }

    protected IFile createFile(String parent, String name) {
        IFolder taskletFolder = storage.getFolder(tasklet.getTasklet()
                .getName());
        if (!taskletFolder.exist())
            taskletFolder.create();
        IFolder itemFolder = taskletFolder.getFolder(parent);
        if (!itemFolder.exist())
            itemFolder.create();
        IFile itemLog = itemFolder.getFile(name);
        if (!itemLog.exist())
            itemLog.create();
        return itemLog;
    }

    protected boolean waitForAgent() {
        try {
            isWaiting = true;
            int i = 0;
            int cnt = CedarConfiguration.getInstance().getAgentTimeout() / 10;
            while (!shouldQuit() && i < cnt && !agent.testConnection()) {
                TimeUnit.SECONDS.sleep(10);
                i++;
            }
            if (i == cnt)
                return false;
            else
                return true;
        } catch (Exception e) {
            return false;
        } finally {
            isWaiting = false;
        }
    }

    protected IResult executeItem(ITaskItem taskItem, String filename)
            throws Exception {
        String value = taskItem.getValue();
        if (value == null || value.length() == 0)
            value = taskItem.getClass().getCanonicalName();
        logger.append("Agent(" + agent.getAgentID() + "," + this.getHostName()
                + ") is scheduled to execute task item: " + value);
        IFile logFile = createFile(agentId, filename);
        tasklet.setItemStorage(taskItem, logFile.getURI().toString());
        tasklet.setItemVolume(taskItem, volume);
        tasklet.setItemMachine(taskItem, machine);

        LogWriter writer = new LogWriter(logFile);
        OutputStream output = writer.getOutputStream();
        agent.setOutputStream(runner, output);
        writer.start();
        if (!(agent instanceof LocalExtensiveAgent)) {
            agent.addPostParam(runner, "queued", Boolean.toString(!TaskletInfo
                    .load(tasklet.getTasklet().getID(), tasklet.getFeatureId())
                    .getSharable().equals(Tasklet.Sharable.full)));
        }
        // enable persistent TaskRunner always
        agent.addPostParam(runner, "persist", "true");
        agent.addPostParam(runner, "debug", Boolean.toString(tasklet
                .getTasklet().isDebug()));
        IResult r = new Result(ResultID.Unreachable);
        try {
            if (waitForAgent())
                r = agent.run(runner, taskItem, timeout, cwd);
            else {
                taskItem.setResult(r);
            }
            writer.finish();
            writer.join();
        } catch (Exception e) {
            logger.append(e);
        }
        return r;
    }
    
    protected void rescheduleItem(ITaskItem taskItem){
        if(shouldQuit())
            return;
        if (taskItem instanceof SimpleTaskItem) {
            int life = ((SimpleTaskItem) taskItem).getLife();
            if (life >= 1) {
                life--;
                queue.offer(taskItem);
                ((SimpleTaskItem) taskItem).setLife(life);
                ((SimpleTaskItem) taskItem).setResult(new AbstractResult(ResultID.NotAvailable));
                logger.append("rescheduled taskitem: "
                        + taskItem.getClass().getSimpleName() + "("
                        + taskItem.getValue() + ")");
            }
        } else {
            logger.append("rescheduled taskitem: "
                    + taskItem.getClass().getSimpleName() + "("
                    + taskItem.getValue() + ")");
            queue.offer(taskItem);
        }
    }

    protected boolean executeWithPolicy(ITaskItem taskItem, String logFileName)
            throws Exception {
        IResult r = executeItem(taskItem, logFileName);
        if (r.getFailureMessage() != null && r.getFailureMessage().length() > 0) {
            logger.append("Agent(" + agent.getAgentID() + ","
                    + this.getHostName() + ") returns FailureMessage for "
                    + taskItem.getClass().getSimpleName() + ":");
            logger.append(r.getFailureMessage());
        }
        if ((taskItem instanceof OnStartTaskItem || taskItem instanceof OnFinishTaskItem)) {
            if (r.getID().equals(ResultID.NotAvailable))
                return true;
            else {
                logger.append("Agent(" + agent.getAgentID() + ","
                        + this.getHostName() + ") executed ("
                        + r.getID().name() + ") for "
                        + taskItem.getClass().getSimpleName());
                if (agent.testConnection()) {
                    agent.kill(runner);
                }
                return false;
            }
        }
        if (!r.getID().isSucceeded()) {
            logger.append("Agent(" + agent.getAgentID() + ","
                    + this.getHostName() + ") executed (" + r.getID().name()
                    + ") for " + taskItem.getClass().getSimpleName() + "("
                    + taskItem.getValue() + ")");
            if (agent.testConnection()) {
                agent.kill(runner);
            }
            if (r.getID().equals(ResultID.Unreachable)){
                // reschedule the failed item due to network issue
                rescheduleItem(taskItem);
                return false;
            }
            if (policy.equals(TaskletFlow.FailurePolicy.Exit)) {
                logger.append("Agent(" + agent.getAgentID() + ","
                        + this.getHostName() + ") exiting");
                this.earlyExit();
                return false;
            } else if (policy.equals(TaskletFlow.FailurePolicy.Reschedule)) {
                rescheduleItem(taskItem);
                return false;
            }
        }
        // the item is consumed
        return true;
    }

    public void run() {
        try {
            int itemCount = 1;
            boolean onStartCalled = false;
            agent.setStorageRoot(runner, storage);
            while (!shouldQuit()) {
                if (!waitForAgent()){
                    // actually means this agent fails, and should skip onFinish
                    killed = true; 
                    break;
                }
                curItem = queue.poll();
                if (curItem == null)
                    break;
                if(!onStartCalled){
                    // one time initialization to call ITaskRunner.onStart
                    // don't continue execution if failed 
                    if (!executeWithPolicy(new OnStartTaskItem(), "start.log")){
                        // should reschedule the fetched curItem
                        rescheduleItem(curItem);
                        break;
                    }
                    onStartCalled = true;
                }
                if(shouldQuit())
                    break;
                String logFileName = String.format("%05d.log", itemCount++);
                if (executeWithPolicy(curItem, logFileName))
                    accomplished++;
            }
            if (!killed && onStartCalled)
                executeWithPolicy(new OnFinishTaskItem(), "stop.log");                
        } catch (Exception e) {
            logger.append(e);
        } finally {
            barrier.countDown();
        }
    }
}
