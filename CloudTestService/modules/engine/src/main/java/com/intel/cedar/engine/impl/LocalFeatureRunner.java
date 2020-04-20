package com.intel.cedar.engine.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.collect.Lists;
import com.intel.cedar.agent.IExtensiveAgent;
import com.intel.cedar.agent.impl.AgentManager;
import com.intel.cedar.agent.impl.LocalExtensiveAgent;
import com.intel.cedar.core.entities.AbstractHostInfo;
import com.intel.cedar.core.entities.MachineInfo;
import com.intel.cedar.core.entities.PhysicalNodeInfo;
import com.intel.cedar.core.entities.VolumeInfo;
import com.intel.cedar.engine.FeatureJobInfo;
import com.intel.cedar.engine.FeatureStatus;
import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.engine.model.feature.FeatureDoc;
import com.intel.cedar.engine.model.feature.ResultMetaData;
import com.intel.cedar.engine.model.feature.Tasklet;
import com.intel.cedar.engine.model.feature.Tasklet.Sharable;
import com.intel.cedar.engine.model.feature.flow.FeatureFlow;
import com.intel.cedar.engine.model.feature.flow.Machine;
import com.intel.cedar.engine.model.feature.flow.MachineParameter;
import com.intel.cedar.engine.model.feature.flow.ParallelTasklets;
import com.intel.cedar.engine.model.feature.flow.SequenceTasklets;
import com.intel.cedar.engine.model.feature.flow.TaskletFlow;
import com.intel.cedar.engine.model.feature.flow.TaskletsFlow;
import com.intel.cedar.feature.FeatureEnvironment;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.feature.IFeature;
import com.intel.cedar.feature.util.FeatureUtil;
import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.pool.ComputeNode;
import com.intel.cedar.pool.Resource;
import com.intel.cedar.pool.ResourceItem;
import com.intel.cedar.pool.ResourcePool;
import com.intel.cedar.pool.ResourceRequest;
import com.intel.cedar.service.client.feature.model.VarValue;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ITaskItemProvider;
import com.intel.cedar.tasklet.ITaskRunner;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.impl.GenericTaskItem;
import com.intel.cedar.user.util.UserUtil;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.xml.rss.util.DateTimeRoutine;

public class LocalFeatureRunner implements Runnable {
    private LogWriter logWriter;
    private FeatureEnvironment env;
    private VariableManager vars;
    private FeaturePropsManager props;
    private Evaluator evaluator;
    private FeatureJobInfo info;
    private Feature feature;
    private List<TaskletRuntimeInfo> tasklets;
    ClassLoader featureClassLoader;
    private Object featureImpl;
    // private ResourcePool pool = ResourcePool.getPool();
    private ExecutorService exe = Executors.newCachedThreadPool();
    private volatile boolean killed = false;
    private volatile boolean earlyExit = false;

    private TaskletRuntimeInfo getTaskletRuntime(TaskletFlow f) {
        for (TaskletRuntimeInfo t : tasklets) {
            if (t.getTasklet().equals(f))
                return t;
        }
        return null;
    }

    private void findTaskRunner(TaskletFlow tasklet, TaskletRuntimeInfo rt) {
        ITaskRunner taskRunner = null;
        try {
            FeatureDoc doc = (FeatureDoc) tasklet.getDocument();
            List<Tasklet> tasklets = doc.getFeature().getTasklets()
                    .getModelChildren();
            String taskletId = tasklet.getID();
            String featureId = info.getFeatureId();

            for (Tasklet t : tasklets) {
                if (taskletId.equals(t.getID())) {
                    String clz = t.getProvider();
                    Object o = Class.forName(clz, true, featureClassLoader)
                            .newInstance();
                    if (o instanceof ITaskRunner) {
                        taskRunner = (ITaskRunner) o;
                        break;
                    }
                }
            }
            if (taskRunner == null) {
                // search DB to see if it's in another feature
                /*
                 * TaskletInfo t = TaskletInfo.load(taskletId, featureId); if(t
                 * != null){ featureId = t.getFeatureId(); String clz =
                 * t.getProvider(); Object o =
                 * Class.forName(clz,true,EngineFactory
                 * .getInstance().getEngine()
                 * .loadFeature(featureId).getFeatureClassLoader
                 * ()).newInstance(); if(o instanceof ITaskRunner){ taskRunner =
                 * (ITaskRunner)o; } }
                 */
            }
            rt.setTaskRunner(taskRunner);
            rt.setFeatureId(featureId);
        } catch (Exception e) {
            logWriter.append(e);
        }
    }

    public void kill() {
        this.killed = true;
        exe.shutdownNow();
    }

    public LocalFeatureRunner(FeatureJobInfo info, Feature feature,
            List<Variable> vars) {
        this.info = info;
        this.vars = new VariableManager(vars);
        this.evaluator = new Evaluator(this.vars);
        this.tasklets = new ArrayList<TaskletRuntimeInfo>();
        IFile logger = info.getStorage().getFile("job.log");
        if (logger.create() || logger.exist()) {
            this.logWriter = new LogWriter(logger);
            this.logWriter.start();
        }
        try {
            logWriter.append("Received variables:");
            for (Variable var : vars) {
                logWriter.append("    " + var.toString());
            }

            FeatureInfo fi = FeatureUtil
                    .getFeatureInfoById(info.getFeatureId());
            this.props = new FeaturePropsManager(fi.getName(), fi.getVersion());

            // feature =
            // EngineFactory.getInstance().getEngine().loadFeature(info.getFeatureId());
            this.feature = feature;
            featureClassLoader = feature.getClass().getClassLoader();
            String featureProvider = feature.getProvider();
            if (featureProvider != null && !featureProvider.equals("")) {
                logWriter.append("Constructing feature: " + featureProvider);
                featureImpl = Class.forName(featureProvider, true,
                        featureClassLoader).newInstance();
            }
            env = new FeatureEnvironment(featureClassLoader, info.getStorage(),
                    this.vars, this.props);
        } catch (Exception e) {
            logWriter.append(e);
            feature = null;
        }
    }

    @Override
    public void run() {
        StatusUpdater statusUpdater = null;
        if (feature == null) {
            info.setStatus(FeatureStatus.Failed);
            return;
        }
        try {
            try {
                if (featureImpl instanceof IFeature) {
                    ((IFeature) featureImpl).onInit(env);
                }
            } catch (Throwable e1) {
                logWriter.append("Executing onInit() failed");
                logWriter.append(e1);
                throw new RuntimeException(e1);
            }
            info.setStatus(FeatureStatus.Started);
            // need a fresh new feature flow model, as current runtime relies on
            // the model
            // FeatureFlow featureFlow = new
            // FeatureLoader().loadFeatureFlow(info.getFeatureId());
            FeatureFlow featureFlow = feature.getFeatureFlow();
            TaskletsFlow flow = featureFlow.getTasklets();
            logWriter.append("Collecting TaskItems");
            collectTaskItems(flow);
            // statusUpdater = new StatusUpdater(tasklets,info);
            // exe.submit(statusUpdater);
            logWriter.append("Executing task flow");
            executeFlow(flow);
            if (killed)
                info.setStatus(FeatureStatus.Cancelled);
            else
                info.setStatus(FeatureStatus.Finished);
        } catch (Throwable e) {
            if ("Job is killed".equals(e.getMessage()))
                info.setStatus(FeatureStatus.Cancelled);
            else {
                info.setStatus(FeatureStatus.Failed);
                logWriter.append("Execution failed");
                logWriter.append(e);
            }
        } finally {
            if (statusUpdater != null)
                statusUpdater.stop();
            try {
                if (featureImpl instanceof IFeature) {
                    ((IFeature) featureImpl).onFinalize(env);
                }
            } catch (Throwable e) {
                logWriter.append("Executing onFinalize() failed");
                logWriter.append(e);
            }
            try {
                info.setEndTime(System.currentTimeMillis());
                logWriter.append("Job was finished successfully");
                if (info.isSendReport()) {
                    sendReport();
                    logWriter.append("Report was sent successfully");
                }
                // appendHistory();
                logWriter.append("Job was appended to history successfully");
            } catch (Throwable e) {
                logWriter.append("Job finalize failed");
                logWriter.append(e);
            }
            if (logWriter != null) {
                try {
                    logWriter.finish();
                    logWriter.join();
                } catch (Exception e) {
                }
            }
            exe.shutdown();
        }
    }

    protected ResultMetaData generateMetaData() {
        return new ResultMetaData(null);
    }

    protected String getReport(ResultMetaData result) {
        List<ITaskItem> results = new ArrayList<ITaskItem>();
        StringBuilder sb = new StringBuilder();
        sb.append("TaskItems report:\n");
        for (TaskletRuntimeInfo t : tasklets) {
            sb.append(t.getTasklet().getID());
            sb.append("\n");
            for (ITaskItem item : t.getItems()) {
                if (item.getResult().getID().equals(ResultID.NotAvailable))
                    continue;
                sb.append(item.getValue());
                sb.append("(");
                sb.append(t.getItemVolume(item).getPath());
                sb.append("),");
                sb.append("(");
                Object m = t.getItemMachine(item);
                if (m instanceof MachineInfo)
                    sb.append(((MachineInfo) m).getImageId());
                else
                    sb.append(((PhysicalNodeInfo) m).getHost());
                sb.append(")");
                sb.append("    ");
                sb.append(item.getResult().getID().name());
                sb.append("   ");
                sb.append(item.getResult().getFailureMessage());
                sb.append("\n");
                sb.append(t.getItemStorage(item));
                results.add(item);
                sb.append("\n");
            }
            sb.append("\n");
        }

        IFile file = info.getStorage().getFile(info.getId() + ".txt");
        try {
            file
                    .setContents(new ByteArrayInputStream(sb.toString()
                            .getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String report = ((IFeature) featureImpl).getReportBody(env);
            sb.append(report);
        } catch (Throwable e) {
            logWriter.append("Executing getReport() failed");
            logWriter.append(e);
        }
        return sb.toString();
    }

    protected void sendReport() throws Exception {
        boolean important = false;
        boolean urgent = false;
        if (info.getStatus().equals(FeatureStatus.Cancelled)
                || info.getStatus().equals(FeatureStatus.Failed)) {
            important = true;
            urgent = true;
        }
        String subject = String.format("%s: %s", info.getStatus().name(),
                feature.getName());
        if (info.getDesc() != null && !info.getDesc().equals(""))
            subject = subject + " (" + info.getDesc() + ")";
        ResultMetaData result = generateMetaData();
        StringBuilder body = new StringBuilder();
        if (info.getStatus().equals(FeatureStatus.Finished)) {
            body
                    .append(
                            String
                                    .format(
                                            "<p>&nbsp;&nbsp;&nbsp;&nbsp;Total test time: <i><b>%s</b></i></p>",
                                            DateTimeRoutine
                                                    .millisToDuration(info
                                                            .getEndTime()
                                                            - info
                                                                    .getSubmitTime())))
                    .append("\n");
            body.append(getReport(result));
            body
                    .append("<p>&nbsp;&nbsp;&nbsp;&nbsp;For detailed logs, please click below link:</p>");
            String link = CedarConfiguration.getStorageServiceURL()
                    + "?cedarURL=" + info.getStorage().getURI().toString();
            body.append(String.format(
                    "<p>&nbsp;&nbsp;&nbsp;&nbsp;<a href='%s'>%s</a></p>", link,
                    info.getStorage().getURI().toString()));
            if (earlyExit) {
                body
                        .append(
                                String
                                        .format(
                                                "<p>&nbsp;&nbsp;&nbsp;&nbsp;*Some test results are not available, please contact %s for further troubleshooting.</p>",
                                                FeatureUtil.getFeatureInfoById(
                                                        info.getFeatureId())
                                                        .getContributer()))
                        .append("\n");
            }
        } else if (info.getStatus().equals(FeatureStatus.Failed)) {
            body
                    .append(
                            String
                                    .format(
                                            "<p>&nbsp;&nbsp;&nbsp;&nbsp;We're sorry that your %s has <font color='red'>failed</font>.</p><p>&nbsp;&nbsp;&nbsp;&nbsp;Please contact <a href='%s?subject=Request Support for %s'>Cloud Test Service</a> support for further troubleshooting.</p>",
                                            feature.getName(), "mailto:"
                                                    + UserUtil.getAdmin()
                                                            .getEmail(), info
                                                    .getId())).append("\n");
        } else if (info.getStatus().equals(FeatureStatus.Cancelled)) {
            body
                    .append(
                            String
                                    .format(
                                            "<p>&nbsp;&nbsp;&nbsp;&nbsp;Your %s task has been canceled.</p>",
                                            feature.getName())).append("\n");
        }
        ByteArrayOutputStream ous = new ByteArrayOutputStream();
        FileUtils.copyStream(LocalFeatureRunner.class
                .getResourceAsStream("reportfragment.tpl"), ous);
        String content = ous.toString();
        String report = content.replace("$CONTENT$", body.toString());
        logWriter.append(report);
        /*
         * CedarMail mail = new
         * CedarMail(UserUtil.getUserById(info.getUserId()), subject, important,
         * urgent, body.toString()); mail.setEmails(info.getReceivers());
         * mail.sendMail();
         */
    }

    /*
     * protected void appendHistory(){ HistoryInfo history = new HistoryInfo();
     * history.setDesc(info.getDesc()); history.setEndTime(info.getEndTime());
     * history.setFeatureId(info.getFeatureId()); history.setId(info.getId());
     * history.setStatus(info.getStatus());
     * history.setSubmitTime(info.getSubmitTime());
     * history.setUserId(info.getUserId()); EntityWrapper<HistoryInfo> db = new
     * EntityWrapper<HistoryInfo>(); db.add(history); db.commit(); }
     */

    protected class SubRunner implements Runnable {
        private Throwable[] tlist;
        private int index;
        private ConcurrentLinkedQueue queue;
        private CountDownLatch barrier;

        public SubRunner(Exception[] tlist, int index,
                ConcurrentLinkedQueue queue, CountDownLatch barrier) {
            this.tlist = tlist;
            this.index = index;
            this.queue = queue;
            this.barrier = barrier;
        }

        public void run() {
            try {
                while (!killed && !earlyExit) {
                    Object task = queue.poll();
                    if (task == null)
                        break;
                    if (task instanceof SequenceTasklets) {
                        executeFlow((TaskletsFlow) task);
                    } else if (task instanceof ParallelTasklets) {
                        executeFlow((ParallelTasklets) task);
                    } else if (task instanceof TaskletFlow) {
                        executeFlow((TaskletFlow) task);
                    }
                }
            } catch (Exception e) {
                tlist[index] = e;
            } finally {
                barrier.countDown();
            }
        }
    }

    protected void executeFlow(ParallelTasklets flow) throws Exception {
        String level = flow.getLevel();
        int num = 0;
        try {
            num = Integer.parseInt(level);
        } catch (Exception e) {
            num = 0;
        }
        final ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();
        for (Object o : flow.getChilds()) {
            queue.offer(o);
        }
        if (num <= 0 || num > queue.size()) {
            num = queue.size();
        }
        Exception[] exceptionList = new Exception[num];
        final CountDownLatch barrier = new CountDownLatch(num);
        for (int i = 0; i < num; i++) {
            SubRunner runner = new SubRunner(exceptionList, i, queue, barrier);
            exe.submit(runner);
        }
        while (true) {
            try {
                barrier.await();
                break;
            } catch (InterruptedException e) {
            }
        }
        Machine m = flow.getMachine();
        if (m != null) {
            synchronized (m) {
                // pool.releaseResource(flow.getResource());
                flow.setResource(null);
            }
        }
        for (Exception t : exceptionList) {
            if (t != null) {
                throw t;
            }
        }
    }

    protected void executeFlow(TaskletFlow tasklet) throws Exception {
        TaskletRuntimeInfo rt = getTaskletRuntime(tasklet);
        if (rt.getTaskRunner() == null) {
            throw new RuntimeException("TaskRunner not instantiated correctly");
        }

        if (rt.getWorkingQueue().size() == 0) {
            logWriter.append("Skipping tasklet: " + tasklet.getID()
                    + " with empty task item queue");
            return;
        }

        logWriter
                .append("Allocating resources for tasklet: " + tasklet.getID());

        rt.setStatus("Allocating resources ...");
        Machine m = tasklet.getMachine();
        Resource resource = tasklet.getResource();
        synchronized (m) {
            if (killed)
                throw new Exception("Job is killed");
            resource = tasklet.getResource();
            if (resource == null) {
                try {
                    resource = ResourcePool
                            .allocateResourceTest(getResourceRequest(tasklet));
                    tasklet.setResource(resource);
                } catch (InterruptedException e) {
                    // killed
                    throw new Exception("Job is killed");
                }
            }
        }

        rt.setStatus("Executing ... ");
        logWriter.append("Allocated " + resource.getResourceCount()
                + " resources");
        for (ResourceItem item : resource.getResources()) {
            AbstractHostInfo host = item.getNode().getHost();
            VolumeInfo volume = item.getVolume();
            logWriter.append("    " + host.getHost() + "," + volume.getPath());
        }

        List<String> features = new ArrayList<String>();
        features.add(info.getFeatureId());
        if (!rt.getFeatureId().equals(info.getFeatureId()))
            features.add(rt.getFeatureId());

        CountDownLatch barrier = new CountDownLatch(resource.getResourceCount());
        for (ResourceItem item : resource.getResources()) {
            ComputeNode node = item.getNode();
            node.attachTasklet(tasklet.getID());
            VolumeInfo volume = item.getVolume();
            AbstractHostInfo host = node.getHost();
            IExtensiveAgent agent = new LocalExtensiveAgent();
            agent.setVariableManager(vars);
            agent.setPropertiesManager(props);
            // agent.installFeatures(rt.getTaskRunner(), features.toArray(new
            // String[]{}));

            AgentRunner runner = new AgentRunner(agent, rt.cloneTaskRunner(), logWriter, rt.getTasklet().getFailurePolicy(evaluator), rt, volume,
                    host, barrier);
            runner.setTimeout(tasklet.getTimeout());
            runner.setWorkingDir(volume.getPath());
            runner.setStorage(info.getStorage());
            rt.addRunner(runner);
            exe.submit(runner);
        }

        while (true) {
            try {
                if (barrier.await(1, java.util.concurrent.TimeUnit.SECONDS))
                    break;

                for (AgentRunner runner : rt.getRunners()) {
                    if (runner.isEarlyExit()) {
                        earlyExit = true;
                    }
                }
                if (earlyExit) {
                    for (AgentRunner runner : rt.getRunners()) {
                        runner.earlyExit();
                    }
                    barrier.await();
                    break;
                }
                if (killed) {
                    for (AgentRunner runner : rt.getRunners()) {
                        runner.kill();
                    }
                    barrier.await();
                    break;
                }
            } catch (InterruptedException e) {
            }
        }
        rt.setStatus("Releasing agents ... ");
        logWriter.append("Releasing agents for tasklet: " + tasklet.getID());
        for (AgentRunner runner : rt.getRunners()) {
            AgentManager.getInstance().releaseAgent(runner.getAgent());
        }

        rt.setStatus("Releasing Resources ... ");
        for (ComputeNode node : resource.getComputeNodes()) {
            node.detachTasklet(tasklet.getID());
        }
        synchronized (m) {
            Resource r = tasklet.getSelfResource();
            if (r != null) {
                // pool.releaseResource(r);
                tasklet.setResource(null);
            }
        }

        rt.setStatus("Finished");
        logWriter.append("Finished tasklet: " + tasklet.getID());
    }

    protected void executeFlow(TaskletsFlow flow) throws Exception {
        try {
            for (Object o : flow.getChilds()) {
                if (o instanceof SequenceTasklets) {
                    executeFlow((TaskletsFlow) o);
                } else if (o instanceof ParallelTasklets) {
                    executeFlow((ParallelTasklets) o);
                } else if (o instanceof TaskletFlow) {
                    executeFlow((TaskletFlow) o);
                }
                if (earlyExit || killed)
                    break;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            Machine m = flow.getMachine();
            if (m != null) {
                synchronized (m) {
                    // pool.releaseResource(flow.getResource());
                    flow.setResource(null);
                }
            }
        }
    }

    protected int getMachineLimit(MachineParameter param) {
        int value = -1;
        if (param.getValue() != null) {
            value = evaluator.evalAsInteger(param.getValue());
        }
        if (value < param.getMin())
            value = param.getMin();
        if (value > param.getMax())
            value = param.getMax();
        return value;
    }

    protected ResourceRequest getResourceRequest(TaskletFlow tasklet) {
        TaskletRuntimeInfo rt = getTaskletRuntime(tasklet);
        Machine request = tasklet.getMachine();
        Properties props = request.getProperties();
        int cpu = getMachineLimit(request.getCPU());
        int mem = getMachineLimit(request.getMemory());
        int disk = getMachineLimit(request.getDisk());
        int count = getMachineLimit(request.getCount());
        // only allocate enough resources
        if (rt.getItemCount() < count) {
            count = rt.getItemCount();
        }
        MachineInfo.OS os = MachineInfo.OS.fromString(evaluator.eval(request
                .getOS()));
        MachineInfo.ARCH arch = MachineInfo.ARCH.fromString(evaluator
                .eval(request.getARCH()));
        String host = evaluator.eval(request.getHost());

        ResourceRequest rr = new ResourceRequest(tasklet.getID(), info
                .getUserId(), info.getFeatureId(), info.isReproducable(),
        /* TaskletInfo.load(tasklet.getID(), info.getFeatureId()).getSharable() */
        Sharable.none);
        rr.setMachineRequest(host, os, arch, cpu, mem, disk, count, props, request.getRecycle(), request.getVisible());
        return rr;
    }

    protected void collectTaskItems(TaskletsFlow flow) {
        for (Object o : flow.getChilds()) {
            if (o instanceof TaskletsFlow) {
                collectTaskItems((TaskletsFlow) o);
            } else if (o instanceof TaskletFlow) {
                TaskletFlow tasklet = (TaskletFlow) o;
                TaskletRuntimeInfo rf = new TaskletRuntimeInfo(tasklet);
                findTaskRunner(tasklet, rf);
                for (ITaskItem item : getTaskItems(tasklet, rf)) {
                    if (item != null)
                        rf.addItem(item);
                }
                logWriter.append("Collected " + rf.getItemCount()
                        + " task items");
                for (int i = 0; i < rf.getItemCount(); i++) {
                    ITaskItem item = rf.getItems().get(i);
                    String value = item.getValue();
                    if (value != null && value.length() > 0)
                        logWriter
                                .append(String.format("%5d: %s", i + 1, value));
                    else
                        logWriter.append(String.format("%5d: %s", i + 1, item
                                .getClass().getCanonicalName()));
                }
                tasklets.add(rf);
            }
        }
    }

    protected List<GenericTaskItem> convertToTaskItems(Variable variable) {
        List<GenericTaskItem> items = Lists.newArrayList();
        for (VarValue var : variable.getVarValues()) {
            GenericTaskItem item = new GenericTaskItem();
            item.setValue(var.getValue());
            for (String key : var.getParamNames()) {
                item.setProperty(key, var.getParamValue(key));
            }
            items.add(item);
        }
        return items;
    }

    protected List<ITaskItem> getTaskItems(TaskletFlow tasklet,
            TaskletRuntimeInfo rf) {
        logWriter.append("Generating TaskItems for " + tasklet.getID());
        String itemProvider = tasklet.getItems().getProvider();
        if (itemProvider != null) {
            if (itemProvider.equals("embedded")) {
                if (rf.getTaskRunner() instanceof ITaskItemProvider) {
                    return ((ITaskItemProvider) rf.getTaskRunner())
                            .getTaskItems(env);
                } else {
                    throw new RuntimeException(
                            "TaskRunner does not implement ITaskItemProvider");
                }
            } else {
                List<ITaskItem> items = Lists.newArrayList();
                String vars[] = itemProvider.split(" +");
                for (String v : vars) {
                    v = v.trim();
                    if (v.charAt(0) == '$' && v.length() > 1) {
                        Variable var = this.vars.getVariable(v.substring(1, v
                                .length() - 1));
                        items.addAll(convertToTaskItems(var));
                    }
                }
                return items;
            }
        } else {
            throw new RuntimeException("NOT SUPPORTED YET");
        }
    }
}