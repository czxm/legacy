package com.intel.cedar.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.intel.cedar.agent.impl.TaskRunnerStatus;
import com.intel.cedar.agent.runtime.AgentRuntime;
import com.intel.cedar.agent.runtime.HeartBeat;
import com.intel.cedar.agent.runtime.ServerRuntimeInfo;
import com.intel.cedar.feature.impl.FeatureDeploy;
import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.util.Digester;
import com.intel.cedar.util.SubDirectory;
import com.intel.cedar.util.protocal.ConversionProtocal;
import com.intel.cedar.util.protocal.ModelStream;

public class AgentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private ConversionProtocal conversionProtocal = null;

    public AgentServlet() {
        super();
        conversionProtocal = new ConversionProtocal();
    }

    private String doSubmit(HttpServletRequest request) {
        // Get className, taskItem content from the request
        String classNameOfTaskRunner = request
                .getParameter("ClassNameOfTaskRunner");
        String classNameOfTaskItem = request
                .getParameter("ClassNameOfTaskItem");
        String contentOfTaskItem = request.getParameter("ContentOfTaskItem");
        String timeoutString = request.getParameter("timeout");
        String cwd = request.getParameter("cwd");
        String storageRoot = request.getParameter("storageRoot");
        String featureString = request.getParameter("features");
        String persistString = request.getParameter("persist");
        String agentIDString = request.getParameter("agentID");
        String debugString = request.getParameter("debug");
        boolean debug = false;
        if (debugString != null && debugString.length() > 0) {
            debug = Boolean.parseBoolean(debugString);
        }
        String[] features = new String[] {};
        if (featureString != null && !featureString.equals(""))
            features = featureString.split(" ");
        boolean queued = Boolean.parseBoolean(request.getParameter("queued"));
        boolean isPersist = false;
        if (persistString != null)
            isPersist = Boolean.parseBoolean(persistString);
        if (cwd == null || cwd.equals(""))
            cwd = System.getProperty("java.io.tmpdir");
        int timeout = 0;
        try {
            timeout = Integer.parseInt(timeoutString);
        } catch (Exception e) {
            timeout = 0;
        }
        return AgentRuntime.getInstance().submit(queued, features,
                classNameOfTaskRunner, classNameOfTaskItem, contentOfTaskItem,
                timeout, cwd, storageRoot, isPersist, agentIDString, debug);
    }

    private String doCedarResponse(HttpServletRequest request) {
        String runningId = request.getParameter("runningId");
        String cedarResponse = request.getParameter("CedarResponse");
        OutputStream stream = AgentRuntime.getInstance()
                .getControlOutputStream(runningId);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                stream));
        if (stream != null) {
            try {
                writer.write("@@CedarResponse");
                writer.newLine();
                writer.write(cedarResponse);
                writer.newLine();
                writer.write("CedarResponse@@");
                writer.newLine();
                writer.flush();
            } catch (Exception e) {
            }
        }
        return "OK";
    }

    private String doPutStorage(HttpServletRequest request) {
        if (ServletFileUpload.isMultipartContent(request)) {
            try {
                DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
                fileItemFactory.setSizeThreshold(1024 * 1024);
                for (DiskFileItem item : (List<DiskFileItem>) new ServletFileUpload(
                        fileItemFactory).parseRequest(request)) {
                    if ("storage".equals(item.getFieldName())
                            && !item.isFormField()) {
                        if (item.getSize() == 0) {
                            throw new IllegalArgumentException(
                                    "Given file is empty or can't be read.");
                        }
                        String runningId = item.getName();
                        String file = AgentRuntime.getInstance()
                                .getStorageFile(runningId);
                        if (file != null) {
                            InputStream ins = item.getInputStream();
                            FileOutputStream output = new FileOutputStream(file);
                            FileUtils.copyStream(ins, output);
                            ins.close();
                            output.close();
                        }
                        item.getStoreLocation().delete();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
            return "OK";
        }
        return "BAD";
    }

    private synchronized String doInstallFeature(HttpServletRequest request) {
        if (ServletFileUpload.isMultipartContent(request)) {
            try {
                DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
                fileItemFactory.setSizeThreshold(1024 * 1024);
                for (DiskFileItem item : (List<DiskFileItem>) new ServletFileUpload(
                        fileItemFactory).parseRequest(request)) {
                    if ("feature".equals(item.getFieldName())
                            && !item.isFormField()) {
                        if (item.getSize() == 0) {
                            throw new IllegalArgumentException(
                                    "Given file is empty or can't be read.");
                        }
                        String origin = item.getName();
                        String orgFileName;
                        int lastSep = origin.lastIndexOf('/') != -1 ? origin
                                .lastIndexOf('/') : origin.lastIndexOf('\\');
                        if (lastSep == -1)
                            orgFileName = origin;
                        else
                            orgFileName = origin.substring(lastSep + 1);
                        File tmpFile = File.createTempFile("feature", "jar");
                        FileOutputStream output = new FileOutputStream(tmpFile);
                        FileUtils.copyStream(item.getInputStream(), output);
                        output.close();
                        new FeatureDeploy().deploy(tmpFile.getAbsolutePath(),
                                false, false);
                        tmpFile.delete();
                        item.getStoreLocation().delete();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
            return "OK";
        }
        return "BAD";
    }

    private synchronized String doRemoveFeature(HttpServletRequest request) {
        String feature = request.getParameter("feature");
        FileUtils.deleteFolderAndContents(new File(SubDirectory.FEATURES
                .toString()
                + feature));
        return "OK";
    }

    private synchronized String doUpdateCedar(HttpServletRequest request) {
        if (ServletFileUpload.isMultipartContent(request)) {
            try {
                DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
                fileItemFactory.setSizeThreshold(1024 * 1024);
                for (DiskFileItem item : (List<DiskFileItem>) new ServletFileUpload(
                        fileItemFactory).parseRequest(request)) {
                    if ("cedar".equals(item.getFieldName())
                            && !item.isFormField()) {
                        if (item.getSize() == 0) {
                            throw new IllegalArgumentException(
                                    "Given file is empty or can't be read.");
                        }
                        String origin = item.getName();
                        String orgFileName;
                        int lastSep = origin.lastIndexOf('/') != -1 ? origin
                                .lastIndexOf('/') : origin.lastIndexOf('\\');
                        if (lastSep == -1)
                            orgFileName = origin;
                        else
                            orgFileName = origin.substring(lastSep + 1);
                        String newFile = SubDirectory.FEATURES.toString()
                                + orgFileName;
                        FileOutputStream output = new FileOutputStream(newFile);
                        FileUtils.copyStream(item.getInputStream(), output);
                        output.close();
                        System.setProperty("cedar.digest", Digester
                                .getMD5Digest(newFile));
                        System.setProperty("cedar.version", orgFileName
                                .replace("cedar-", "").replace(".jar", ""));
                        item.getStoreLocation().delete();
                        ServerRuntimeInfo.getInstance().updateVersion();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
            return "OK";
        }
        return "BAD";
    }

    private String doNewPortMapping(HttpServletRequest request) {
        String host = request.getParameter("host");
        String port = request.getParameter("port");
        int mappedPort = GatewayManager.getInstance().allocatePortMapping(host,
                port);
        return String.format("%d", mappedPort);
    }

    private String doDeletePortMapping(HttpServletRequest request) {
        String mappedPort = request.getParameter("mapped");
        String host = request.getParameter("host");
        String port = request.getParameter("port");
        GatewayManager.getInstance().releasePortMapping(host, port, mappedPort);
        return "OK";
    }

    private String doClearPortMappings(HttpServletRequest request) {
        GatewayManager.getInstance().clearPortMappings();
        return "OK";
    }

    private String doGetHostname(HttpServletRequest request) {
        String host = request.getParameter("host");
        try {
            return InetAddress.getByName(host).getCanonicalHostName();
        } catch (Exception e) {
        }
        return host;
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        ServerRuntimeInfo.getInstance().increaseConnection();
        String result = "";
        String url = request.getRequestURI();
        if (url.endsWith("submit")) {
            result = doSubmit(request);
        } else if (url.endsWith("cedarResponse")) {
            result = doCedarResponse(request);
        } else if (url.endsWith("putStorage")) {
            result = doPutStorage(request);
        } else if (url.endsWith("updateCedar")) {
            result = doUpdateCedar(request);
        } else if (url.endsWith("installFeature")) {
            result = doInstallFeature(request);
        } else if (url.endsWith("removeFeature")) {
            result = doRemoveFeature(request);
        } else if (url.endsWith("newPortMapping")) {
            result = doNewPortMapping(request);
        } else if (url.endsWith("deletePortMapping")) {
            result = doDeletePortMapping(request);
        } else if (url.endsWith("clearPortMappings")) {
            result = doClearPortMappings(request);
        }

        Writer writer = response.getWriter();
        writer.write(result);
        writer.flush();
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        ServerRuntimeInfo.getInstance().increaseConnection();
        // get action, running id from request
        String action = request.getParameter("action");
        if (action == null)
            action = "listTasklets";
        String runningId = request.getParameter("runningId");
        String outcome = TaskRunnerStatus.NotAvailable.name();
        if (action.equalsIgnoreCase("wait")) {
            BufferedWriter writer = new BufferedWriter(response.getWriter());
            HeartBeat hb = new HeartBeat(writer, new AtomicBoolean(false));
            hb.start();
            while (AgentRuntime.getInstance().getStatus(runningId).equals(
                    TaskRunnerStatus.Submitted)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            hb.shutdown();
            InputStream stream = AgentRuntime.getInstance()
                    .getControlInputStream(runningId);
            if (stream != null) {
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(stream));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("@@CedarFinish@@")){
                            writer.newLine();
                            break;
                        }
                        writer.write(line);
                        writer.newLine();
                        writer.flush();
                    }
                    reader.close();
                } catch (Exception e) {
                }
            }
            outcome = conversionProtocal.serializeResult(AgentRuntime
                    .getInstance().getResult(runningId));
            AgentRuntime.getInstance().purgeTask(runningId);
        } else if (action.equalsIgnoreCase("kill")) {
            AgentRuntime.getInstance().kill(runningId);
        } else if (action.equalsIgnoreCase("getLog")) {
            response.setCharacterEncoding(AgentConfiguration.getInstance()
                    .getCharacterEncoding());
            Writer writer = response.getWriter();
            HeartBeat hb = new HeartBeat(new BufferedWriter(writer),
                    new AtomicBoolean(false));
            hb.start();
            while (AgentRuntime.getInstance().getStatus(runningId).equals(
                    TaskRunnerStatus.Submitted)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            hb.shutdown();
            InputStream stream = AgentRuntime.getInstance().getTaskLogStream(
                    runningId);
            if (stream != null) {
                try {
                    InputStreamReader reader = new InputStreamReader(stream);
                    char[] buf = new char[2048];
                    int n;
                    while ((n = reader.read(buf)) != -1) {
                        writer.write(buf, 0, n);
                        writer.flush();
                    }
                    reader.close();
                } catch (Exception e) {
                }
            }
            return;
        } else if (action.equalsIgnoreCase("getStatus")) {
            outcome = AgentRuntime.getInstance().getStatus(runningId)
                    .toString();
        } else if (action.equals("listFeatures")) {
            StringBuilder sb = new StringBuilder();
            for (String file : new File(SubDirectory.FEATURES.toString())
                    .list()) {
                if (new File(SubDirectory.FEATURES.toString() + file)
                        .isDirectory()) {
                    sb.append(file);
                    sb.append(" ");
                }
            }
            outcome = sb.toString();
        } else if (action.equals("getServerInfo")) {
            outcome = new ModelStream<ServerRuntimeInfo>()
                    .serialize(ServerRuntimeInfo.getInstance());
        } else if (action.equals("getHostname")) {
            outcome = doGetHostname(request);
        } else if (action.equals("listTasklets")) {
            BufferedWriter writer = new BufferedWriter(response.getWriter());
            writer.write("tasklets:");
            writer.newLine();
            for (String result : AgentRuntime.getInstance().listTasks()) {
                writer.write(result);
                writer.newLine();
            }
            writer.flush();
            writer.close();
            return;
        } else if (action.equals("listNAT")) {
            String host = request.getParameter("host");
            outcome = new ModelStream<List<NATEntry>>()
                    .serialize(GatewayManager.getInstance()
                            .getNATEntriesByHost(host));
        } else if (action.equals("getStorage")) {
            String file = AgentRuntime.getInstance().getStorageFile(runningId);
            FileInputStream ins = new FileInputStream(file);
            OutputStream output = response.getOutputStream();
            FileUtils.copyStream(ins, output);
            ins.close();
            output.close();
            return;
        }
        Writer writer = response.getWriter();
        writer.write(outcome);
        writer.flush();
    }
}
