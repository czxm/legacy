package com.intel.cedar.engine.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Lists;
import com.intel.cedar.agent.IExtensiveAgent;
import com.intel.cedar.core.entities.AbstractHostInfo;
import com.intel.cedar.core.entities.MachineInfo;
import com.intel.cedar.core.entities.PhysicalNodeInfo;
import com.intel.cedar.core.entities.VolumeInfo;
import com.intel.cedar.engine.EngineFactory;
import com.intel.cedar.engine.FeatureJobInfo;
import com.intel.cedar.engine.FeatureStatus;
import com.intel.cedar.engine.HistoryInfo;
import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.engine.model.feature.FeatureDoc;
import com.intel.cedar.engine.model.feature.Tasklet;
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
import com.intel.cedar.feature.INotifyConfig;
import com.intel.cedar.feature.impl.FeatureLoader;
import com.intel.cedar.feature.util.FeatureUtil;
import com.intel.cedar.mail.CedarMail;
import com.intel.cedar.pool.ComputeNode;
import com.intel.cedar.pool.Resource;
import com.intel.cedar.pool.ResourceItem;
import com.intel.cedar.pool.ResourcePool;
import com.intel.cedar.pool.ResourceRequest;
import com.intel.cedar.pool.ResourceRequestException;
import com.intel.cedar.service.client.feature.model.VarValue;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ITaskItemProvider;
import com.intel.cedar.tasklet.ITaskRunner;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.SimpleTaskItem;
import com.intel.cedar.tasklet.TaskletInfo;
import com.intel.cedar.user.UserInfo;
import com.intel.cedar.user.util.UserUtil;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.cedar.util.EntityWrapper;
import com.intel.xml.rss.util.DateTimeRoutine;

public class FeatureRunner implements Runnable {
    private LogWriter logWriter;
    private FeatureEnvironment env;
    private VariableManager vars;
    private FeaturePropsManager props;
    private Evaluator evaluator;
    private FeatureJobInfo info;
    private Feature feature;
    private ConcurrentLinkedQueue<TaskletRuntimeInfo> tasklets;
    ClassLoader featureClassLoader;
    private Object featureImpl;
    private ResourcePool pool = ResourcePool.getPool();
    private ExecutorService exe = Executors.newCachedThreadPool();
    private AtomicBoolean killed = new AtomicBoolean(false);
    private AtomicBoolean earlyExit = new AtomicBoolean(false);
    private AtomicBoolean noResource = new AtomicBoolean(false);

    private INotifyConfig defaultNotifyConfig = new INotifyConfig(){

        @Override
        public String getFromName() {
            return getFeatureInfo().getContributer().replaceAll("[t|T]eam", "").trim();
        }

        @Override
        public String getFromAddr() {
            return null;
        }
        
    };
    
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
                TaskletInfo t = TaskletInfo.load(taskletId, featureId);
                if (t != null) {
                    featureId = t.getFeatureId();
                    String clz = t.getProvider();
                    Object o = Class.forName(
                            clz,
                            true,
                            EngineFactory.getInstance().getEngine()
                                    .loadFeature(featureId)
                                    .getFeatureClassLoader()).newInstance();
                    if (o instanceof ITaskRunner) {
                        taskRunner = (ITaskRunner) o;
                    }
                }
            }
            rt.setTaskRunner(taskRunner);
            rt.setFeatureId(featureId);
        } catch (Exception e) {
            logWriter.append(e);
        }
    }

    public void kill() {
        this.killed.set(true);
        exe.shutdownNow();
    }
    
    protected FeatureInfo getFeatureInfo(){
        return FeatureUtil.getFeatureInfoById(info.getFeatureId());
    }
    

    public FeatureRunner(FeatureJobInfo info, List<Variable> vars) {
        this.info = info;
        this.vars = new VariableManager(vars);
        this.evaluator = new Evaluator(this.vars);
        this.tasklets = new ConcurrentLinkedQueue<TaskletRuntimeInfo>();
        IFile logger = info.getStorage().getFile("job.log");
        if (logger.create()) {
            this.logWriter = new LogWriter(logger);
            this.logWriter.start();
        }
        try {
            logWriter.append("Owner:");
            UserInfo u = UserUtil.getUserById(info.getUserId());
            logWriter.append(u != null ? u.getUser() : "N/A");

            logWriter.append("Received variables:");
            for (Variable var : vars) {
                logWriter.append("    " + var.toString());
            }

            FeatureInfo fi = getFeatureInfo();
            this.props = new FeaturePropsManager(fi.getName(), fi.getVersion());

            feature = EngineFactory.getInstance().getEngine().loadFeature(
                    info.getFeatureId());
            featureClassLoader = feature.getFeatureClassLoader();
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
            FeatureFlow featureFlow = new FeatureLoader().loadFeatureFlow(info
                    .getFeatureId());
            TaskletsFlow flow = featureFlow.getTasklets();
            statusUpdater = new StatusUpdater(tasklets, info);
            exe.submit(statusUpdater);
            logWriter.append("Executing task flow");
            executeFlow(flow);
            if (killed.get())
                info.setStatus(FeatureStatus.Cancelled);
            else if (noResource.get())
                info.setStatus(FeatureStatus.Failed);
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
                appendHistory();
                logWriter.append("Job was appended to history successfully");
            } catch (Throwable e) {
                logWriter.append("Job finalize failed");
                logWriter.append(e);
            } finally{
                info.setStatus(FeatureStatus.Evicted);
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

    protected Document newDocument() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    protected Document generateMetaData() throws Exception {
        Document doc = newDocument();
        Element root = doc.createElement("tasklets");
        doc.appendChild(root);
        for (TaskletRuntimeInfo t : tasklets) {
            Element ele = doc.createElement("tasklet");
            root.appendChild(ele);
            ele.setAttribute("id", t.getTasklet().getID());
            Element items = doc.createElement("items");
            ele.appendChild(items);
            int i = 1;
            for (ITaskItem item : t.getItems()) {
                if (item.getResult().getID().equals(ResultID.NotAvailable))
                    continue;
                Element itemEle = doc.createElement("item");
                Element itemValue = doc.createElement("value");
                if (item.getValue().length() > 0) {
                    itemValue.setTextContent(item.getValue());
                } else {
                    itemValue.setTextContent(Integer.toString(i));
                }
                Element itemResult = doc.createElement("result");
                itemResult.setTextContent(item.getResult().getID().name());
                Element itemFailure = doc.createElement("failure");
                itemFailure
                        .setTextContent(item.getResult().getFailureMessage());
                Element itemURL = doc.createElement("url");
                itemURL.setTextContent(CedarConfiguration
                        .getStorageServiceURL()
                        + "?cedarURL=" + t.getItemStorage(item));
                Element itemVolume = doc.createElement("path");
                itemVolume.setTextContent(t.getItemVolume(item).getPath());
                if (t.getItemVolume(item).isCloudVolume()) {
                    itemVolume.setAttribute("volume", t.getItemVolume(item)
                            .getImageId());
                }
                Element itemHost = doc.createElement("host");
                Object m = t.getItemMachine(item);
                if (m instanceof MachineInfo) {
                    itemHost.setTextContent(((MachineInfo) m).getImageId());
                } else {
                    itemHost.setTextContent(((PhysicalNodeInfo) m).getHost());
                }
                itemEle.appendChild(itemValue);
                itemEle.appendChild(itemResult);
                itemEle.appendChild(itemFailure);
                itemEle.appendChild(itemURL);
                itemEle.appendChild(itemVolume);
                itemEle.appendChild(itemHost);
                items.appendChild(itemEle);
                i++;
            }
            ele.appendChild(items);
        }
        return doc;
    }

    protected boolean allItemsSucceeded() {
        for (TaskletRuntimeInfo t : tasklets) {
            for (ITaskItem item : t.getItems()) {
                if (!item.getResult().getID().isSucceeded())
                    return false;
            }
        }
        return true;
    }

    protected String doSerialize(Document doc, String xsl) throws Exception {
        Source xmlInput = new DOMSource(doc);
        StreamResult xmlOutput = new StreamResult(new StringWriter());
        TransformerFactory tfFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        if (xsl == null) {
            transformer = tfFactory.newTransformer(); // An identity transformer
        } else {
            Source xslSource = new StreamSource(getClass().getClassLoader()
                    .getResourceAsStream(xsl));
            transformer = tfFactory.newTransformer(xslSource);
        }
        if (transformer != null) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
        }
        return xmlOutput.getWriter().toString();
    }

    protected String genJobData(Document result) throws Exception {
        IFile file = info.getStorage().getFile("job.xml");
        IFile htmFile = info.getStorage().getFile("job.html");
        String str = doSerialize(result, null);
        String htmStr = doSerialize(result, "items.xsl");
        try {
            file.setContents(new ByteArrayInputStream(str.getBytes()));
            htmFile.setContents(new ByteArrayInputStream(htmStr.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return htmStr;
    }

    protected String getReport(String htmStr) throws Exception {
        StringBuilder sb = new StringBuilder();
        try {
            String report = ((IFeature) featureImpl).getReportBody(env);
            if (report != null && report.length() > 0)
                sb.append(report);
        } catch (Throwable e) {
            logWriter.append("Executing getReportBody() failed");
            sb.append("Executing getReportBody() failed: <br>");
            logWriter.append(e);
            sb.append("<font color='red'>");
            sb.append(e.getMessage());
            sb.append("</font>");
            info.setStatus(FeatureStatus.Failed);
        }
        if (sb.toString().length() == 0) {
            sb.append(htmStr);
        }
        return sb.toString();
    }
    
    protected String loadFromStream(InputStream ins){
        StringBuffer sb = new StringBuffer();
        InputStreamReader fr = null;
        try{
            fr = new InputStreamReader(ins);
            char[] buff = new char[4096];
            int size = fr.read(buff, 0, 4096);
            while (size > 0) {
                sb.append(buff, 0, size);
                size = fr.read(buff, 0, 4096);
            }
        }
        catch(Exception e){
            logWriter.append(e);
        }
        finally {
            if(fr != null)
                try {
                    fr.close();
                } catch (IOException e) {
                    logWriter.append(e);
                }
        }
        return sb.toString();
    }

    protected void sendReport() throws Exception {        
        String subject = null;
        try {
            subject = ((IFeature) featureImpl).getReportTitle(env);
            if (subject == null || subject.length() == 0)
                subject = String.format("%s: %s", info.getStatus().name(), feature.getName());
        } catch (Throwable e) {
            logWriter.append(e);
        }
        if (info.getDesc() != null && !info.getDesc().equals("")) {
            subject = subject + " (" + info.getDesc() + ")";
        }
                
        String footnote = null;
        try {
            footnote = ((IFeature) featureImpl).getReportFootnote(env);
            if (footnote == null || footnote.length() == 0)
                footnote = "Powered by " + getFeatureInfo().getContributer();
        } catch (Throwable e) {
            logWriter.append(e);
        }
        
        String defaultcsscontent = "";
        try{
            InputStream defaultcss = FeatureRunner.class.getClassLoader().getResourceAsStream("default.css");
            defaultcsscontent = loadFromStream(defaultcss);
        } catch (Throwable e) {
            logWriter.append(e);
        }
        
        String csscontent = "";
        try{
            InputStream css = ((IFeature) featureImpl).getReportCSS(env);
            if(css == null){
                css = FeatureRunner.class.getClassLoader().getResourceAsStream("summary.css");
            }
            csscontent = loadFromStream(css);
        } catch (Throwable e) {
            logWriter.append(e);
        }
        
        INotifyConfig nc = null;
        try{
            nc = ((IFeature) featureImpl).getNotifyConfig(env);
            if(nc == null){
                nc = defaultNotifyConfig;
            }
        } catch (Throwable e) {
            logWriter.append(e);
        }
        
        Document result = generateMetaData();
        String defReport = genJobData(result);
        StringBuilder body = new StringBuilder();
        if (info.getStatus().equals(FeatureStatus.Finished)) {
            body.append(getReport(defReport));
            if (!allItemsSucceeded()) {
                body
                        .append(
                                String
                                        .format(
                                                "<p>*Some task items didn't finish successfully, and this report may be incompleted. Please contact %s for further troubleshooting.</p>",
                                                this.getFeatureInfo().getContributer()))
                        .append("\n");
            }
        } else if (info.getStatus().equals(FeatureStatus.Failed)) {
            body
                    .append(
                            String
                                    .format(
                                            "<p>We're sorry that your <b>%s</b> has <font color='red'>failed</font>%s. Please contact <a href='%s?subject=Request Support for %s'>Cloud Test Service</a> support for further troubleshooting.</p>",
                                            feature.getName(), noResource.get() ? " due to insufficient resource" : "", "mailto:"
                                                    + UserUtil.getAdmin()
                                                            .getEmail(), info
                                                    .getId())).append("\n");
        } else if (info.getStatus().equals(FeatureStatus.Cancelled)) {
            body
                    .append(
                            String
                                    .format(
                                            "<p>Your <b>%s</b> task has been canceled. </p>",
                                            feature.getName())).append("\n");
        }
        body
                .append(
                        String
                                .format(
                                        "<p>It took <i><b>%s</b></i> for this task. ",
                                        DateTimeRoutine.millisToShortDuration(info
                                                .getEndTime()
                                                - info.getSubmitTime())));

        body.append(String.format("Please click <a href=\"%s\">here</a> for detailed logs.</p>",
                    env.getHyperlink(info.getStorage()))).append("\n");
        body.append(String.format("<br><em>%s</em>", footnote));

        StringBuffer resultBuffer = new StringBuffer();
        resultBuffer.append("<html><head><title>");
        resultBuffer.append(subject);
        resultBuffer.append("</title>");
        resultBuffer.append("<style>");
        resultBuffer.append(defaultcsscontent);
        resultBuffer.append(csscontent);
        resultBuffer.append("</style>");
        resultBuffer.append("</head>");
        resultBuffer.append(body);
        resultBuffer.append("</html>");
        String report = resultBuffer.toString();
        logWriter.append(report);
        
        boolean important = false;
        boolean urgent = false;
        if (info.getStatus().equals(FeatureStatus.Cancelled)
                || info.getStatus().equals(FeatureStatus.Failed)) {
            important = true;
            urgent = true;
        } 
        CedarMail mail = new CedarMail(nc.getFromName(), nc.getFromAddr(), UserUtil.getUserById(info.getUserId()),
                subject, important, urgent, report);
        if(!info.getStatus().equals(FeatureStatus.Finished) && info.getFailureReceivers() != null && info.getFailureReceivers().size() > 0)
            mail.setEmails(info.getFailureReceivers());
        else
            mail.setEmails(info.getReceivers());
        mail.sendMail();
    }

    protected void appendHistory() {
        HistoryInfo history = new HistoryInfo();
        history.setDesc(info.getDesc());
        history.setEndTime(info.getEndTime());
        history.setFeatureId(info.getFeatureId());
        history.setId(info.getId());
        history.setStatus(info.getStatus());
        history.setSubmitTime(info.getSubmitTime());
        history.setUserId(info.getUserId());
        history.setLocation(env.getHyperlink(info.getStorage()));
        EntityWrapper<HistoryInfo> db = new EntityWrapper<HistoryInfo>();
        db.add(history);
        db.commit();
    }

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
                while (!killed.get() && !earlyExit.get()) {
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
                pool.releaseResource(flow.getResource());
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
        collectTaskItems(tasklet);
        TaskletRuntimeInfo rt = getTaskletRuntime(tasklet);
        if (rt.getTaskRunner() == null) {
            throw new RuntimeException("TaskRunner not instantiated correctly");
        }
        
        if (rt.getWorkingQueue().size() == 0) {
            logWriter.append("Skipping tasklet: " + tasklet.getID()
                    + " with empty task item queue");
            return;
        }

        int timeout = evaluator.evalAsInteger(tasklet.getTimeout());

        logWriter
                .append("Allocating resources for tasklet: " + tasklet.getID());

        rt.setStatus("Allocating resources ...");
        Machine m = tasklet.getMachine();
        Resource resource = tasklet.getResource();
        synchronized (m) {
            if (killed.get())
                throw new Exception("Job is killed");
            if (earlyExit.get())
                return;
            resource = tasklet.getResource();
            if (resource == null) {
                try {
                    resource = pool.allocateResource(
                            getResourceRequest(tasklet), timeout);
                    tasklet.setResource(resource);
                } catch (ResourceRequestException e) {
                    if (e.getCause() instanceof InterruptedException) {
                        killed.set(true);
                        throw new Exception("Job is killed");
                    } else {
                        rt.setStatus("Timeout when requesting resources");
                        logWriter
                                .append("Timeout when requesting resources for tasklet: "
                                        + tasklet.getID());
                        earlyExit.set(true);
                        noResource.set(true);
                        return;
                    }
                }
            }
        }

        rt.setStatus("Executing ... ");
        logWriter.append("Allocated " + resource.getResourceCount()
                + " resources");

        boolean resourceAvailable = true;
        for (ResourceItem item : resource.getResources()) {
            AbstractHostInfo host = item.getNode().getHost();
            VolumeInfo volume = item.getVolume();
            IExtensiveAgent agent = item.getAgent();
            if(agent == null || volume.getPath() == null)
                resourceAvailable = false;
            else{
                String resourceDesc = agent.getAgentID() + ": " +
                                      host.getServerInfo().getServer() +
                                      "(" + host.getHost() + ")," +
                                      volume.getPath();
                logWriter.append(resourceDesc);
            }
        }

        if (resourceAvailable && rt.getItemCount() > 0) {
            List<String> features = new ArrayList<String>();
            features.add(info.getFeatureId());
            if (!rt.getFeatureId().equals(info.getFeatureId()))
                features.add(rt.getFeatureId());

            CountDownLatch barrier = new CountDownLatch(resource
                    .getResourceCount());
            for (ResourceItem item : resource.getResources()) {
                ComputeNode node = item.getNode();
                node.attachTasklet(tasklet.getID());
                VolumeInfo volume = item.getVolume();
                AbstractHostInfo host = node.getHost();
                IExtensiveAgent agent = item.getAgent();
                agent.setVariableManager(vars);
                agent.setPropertiesManager(props);
                ITaskRunner r = rt.cloneTaskRunner();
                agent.installFeatures(r, features
                        .toArray(new String[] {}));                
                AgentRunner runner = new AgentRunner(agent, r, logWriter, rt.getTasklet().getFailurePolicy(evaluator), rt,
                        volume, host, barrier);
                runner.setTimeout(Integer.toString(timeout));
                runner.setWorkingDir(volume.getPath());
                runner.setStorage(info.getStorage());
                rt.addRunner(runner);
            }

            for (AgentRunner runner : rt.getRunners()) {
                exe.submit(runner);
            }

            while (true) {
                try {
                    if (barrier.await(1, java.util.concurrent.TimeUnit.SECONDS))
                        break;

                    for (AgentRunner runner : rt.getRunners()) {
                        if (runner.isEarlyExit()) {
                            earlyExit.set(true);
                        }
                    }
                    if (earlyExit.get()) {
                        for (AgentRunner runner : rt.getRunners()) {
                            runner.earlyExit();
                        }
                        barrier.await();
                        break;
                    }
                    if (killed.get()) {
                        for (AgentRunner runner : rt.getRunners()) {
                            runner.kill();
                        }
                        barrier.await();
                        break;
                    }
                } catch (InterruptedException e) {
                }
            }
            // check again to make sure other Tasklets can be notified
            for (AgentRunner runner : rt.getRunners()) {
                if (runner.isEarlyExit()) {
                    earlyExit.set(true);
                }
            }
        }

        rt.setStatus("Releasing Resources ... ");
        for (ResourceItem item : resource.getResources()) {            
            item.releaseAgent();
            item.getNode().detachTasklet(tasklet.getID());
        }
        
        synchronized (m) {
            Resource r = tasklet.getSelfResource();
            if (r != null) {
                pool.releaseResource(r);
                tasklet.setResource(null);
            }
        }

        if (resourceAvailable) {
            rt.setStatus("Finished");
            logWriter.append("Finished tasklet: " + tasklet.getID());
        } else {
            throw new RuntimeException("Resource is unexpectedly unavailable!");
        }
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
                if (earlyExit.get() || killed.get())
                    break;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            Machine m = flow.getMachine();
            if (m != null) {
                synchronized (m) {
                    pool.releaseResource(flow.getResource());
                    flow.setResource(null);
                }
            }
        }
    }

    protected int getMachineLimit(MachineParameter param) {
        int value = -1;
        if (param.getValue() != null && param.getValue().length() > 0) {
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
        if (props != null) {
            for (String key : props.stringPropertyNames()) {
                String value = (String) props.getProperty(key);
                props.setProperty(key, evaluator.eval(value));
            }
        }
        int cpu = getMachineLimit(request.getCPU());
        int mem = getMachineLimit(request.getMemory());
        int disk = getMachineLimit(request.getDisk());
        int count = getMachineLimit(request.getCount());

        if (request.getCount().getValue() == null || request.getCount().getValue().length() == 0) {
            if(rt.getItemCount() > request.getCount().getMax()){
                count = request.getCount().getMax();
            }
            else{
                count = rt.getItemCount();
            }
        }
        
        MachineInfo.OS os = MachineInfo.OS.fromString(evaluator.eval(request
                .getOS()));
        MachineInfo.ARCH arch = MachineInfo.ARCH.fromString(evaluator
                .eval(request.getARCH()));
        String host = evaluator.eval(request.getHost());

        ResourceRequest rr = new ResourceRequest(tasklet.getID(), info
                .getUserId(), info.getFeatureId(), info.isReproducable(),
                TaskletInfo.load(tasklet.getID(), info.getFeatureId())
                        .getSharable());
        rr.setMachineRequest(host, os, arch, cpu, mem, disk, count, props, request.getRecycle(), request.getVisible());
        return rr;
    }

    protected void collectTaskItems(TaskletFlow tasklet) {
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
            String strV = item.toString();
            if(strV == null)
                strV = "";
            if (value != null && value.length() > 0)
                logWriter.append(String.format("%5d: %s %s", i + 1, value, strV));
            else
                logWriter.append(String.format("%5d: %s %s", i + 1, item
                        .getClass().getCanonicalName(), strV));
        }
        tasklets.add(rf);
    }

    protected List<SimpleTaskItem> convertToTaskItems(Variable variable) {
        List<SimpleTaskItem> items = Lists.newArrayList();
        for (VarValue var : variable.getVarValues()) {
            SimpleTaskItem item = new SimpleTaskItem();
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