package com.intel.cedar.service.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intel.cedar.core.entities.CloudInfo;
import com.intel.cedar.core.entities.CloudNodeInfo;
import com.intel.cedar.core.entities.GatewayInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.PhysicalNodeInfo;
import com.intel.cedar.engine.EngineFactory;
import com.intel.cedar.engine.FeatureJobInfo;
import com.intel.cedar.engine.IEngine;
import com.intel.cedar.engine.model.feature.LaunchSet;
import com.intel.cedar.engine.model.loader.FeatureDescLoader;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.feature.impl.FeatureLoader;
import com.intel.cedar.feature.util.FeatureUtil;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.cedar.util.CloudUtil;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;

public class RestfulServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void writeJobInfo(Writer writer, FeatureJobInfo info)
            throws IOException {
        writer.write("JobId: " + info.getId() + "\n");
        writer.write("Status: " + info.getStatus().name() + "\n");
        writer.write("JobStorage: " + CedarConfiguration.getStorageServiceURL()
                + "?cedarURL=" + info.getStorage().getURI().toString() + "\n");
    }

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        Writer writer = response.getWriter();
        String url = request.getRequestURI();
        int index = url.lastIndexOf("/");
        String action = url.substring(index + 1);
        String subString = url.substring(0, index);
        String subject = subString.substring(subString.lastIndexOf("/") + 1);
        String[] tokens = subject.split("_|\\.");
        if (action.equalsIgnoreCase("list")) {
            for (FeatureJobInfo info : EngineFactory.getInstance().getEngine()
                    .listFeatureJob(null)) {
                writeJobInfo(writer, info);
            }
        } else if (action.equalsIgnoreCase("kill")) {
            EngineFactory.getInstance().getEngine().kill(subject);
        } else if (action.equalsIgnoreCase("status")) {
            FeatureJobInfo info = EngineFactory.getInstance().getEngine()
                    .queryFeatureJob(subject);
            writeJobInfo(writer, info);
        } else if (action.equalsIgnoreCase("console")) {
            if (subject.indexOf("_") > 0) {
                CloudInfo cloud = CloudUtil.getCloudByName(tokens[0]);
                for (InstanceInfo instance : cloud.getInstances()) {
                    if (instance.getRemoteDisplay() != null
                            && instance.getInstanceId().equals(tokens[1])) {
                        URI uri = URI.create(instance.getRemoteDisplay());
                        String service = null;
                        if (uri.getScheme().equals("vnc")) {
                            service = "VNC";
                        }
                        if (service != null) {
                            CloudNodeInfo theNode = null;
                            CloudNodeInfo node = new CloudNodeInfo();
                            node.setHost(uri.getHost());
                            EntityWrapper<CloudNodeInfo> db = EntityUtil
                                    .getCloudNodeEntityWrapper();
                            List<CloudNodeInfo> result = db.query(node);
                            db.rollback();
                            if (result.size() == 1) {
                                theNode = result.get(0);
                            }
                            if (theNode != null) {
                                String gateway = GatewayInfo.load(theNode.getGatewayId()).getHost();
                                String forwarder = cloud.getSeperated() ? CedarConfiguration
                                        .getInstance().getForwader()
                                        : "N/A";
                                String destHost = (cloud.getSeperated() && !forwarder.equals(gateway)) ? gateway
                                        : theNode.getHost();
                                int destPort = (cloud.getSeperated() && !forwarder.equals(gateway)) ? theNode
                                        .getInstanceDisplayPort(instance) : uri
                                        .getPort();
                                if (destPort > 0) {
                                    response
                                            .sendRedirect(String
                                                    .format(
                                                            "/jws/viewer?SERVICE=%s&DESTHOST=%s&DESTPORT=%d&FORWARDER=%s",
                                                            service, destHost,
                                                            destPort, forwarder));
                                }
                            }
                        }
                    }
                }
            }
        } else if (action.equalsIgnoreCase("view")) {
            if (subject.indexOf("_") > 0) {
                CloudInfo cloud = CloudUtil.getCloudByName(tokens[0]);
                for (InstanceInfo instance : cloud.getInstances()) {
                    if (instance.getInstanceId().equals(tokens[1])) {
                        String gateway = GatewayInfo.load(instance.getGatewayId()).getHost();
                        String service = instance.getOs().isWindows() ? "RDP"
                                : "VNC";
                        int servicePort = instance.getOs().isWindows() ? 3389 : 5901;
                        String forwarder = cloud.getSeperated() ? CedarConfiguration
                                .getInstance().getForwader()
                                : "N/A";
                        String destHost = (cloud.getSeperated() && !forwarder.equals(gateway)) ? GatewayInfo
                                .load(instance.getGatewayId()).getHost()
                                : instance.getHost();
                        int destPort = (cloud.getSeperated() && !forwarder.equals(gateway)) ?
                                instance.getRemoteDesktopPort() : servicePort;
                        if (destPort > 0) {
                            response
                                    .sendRedirect(String
                                            .format(
                                                    "/jws/viewer?SERVICE=%s&DESTHOST=%s&DESTPORT=%d&FORWARDER=%s",
                                                    service, destHost,
                                                    destPort, forwarder));
                        }
                    }
                }
            } else {
                PhysicalNodeInfo node = CloudUtil
                        .getPhysicalNodeByHost(subject);
                if (node != null) {
                    String service = node.getOs().isWindows() ? "RDP" : "VNC";
                    String destHost = node.getGatewayId() != null
                            && node.getGatewayId() > 0 ? GatewayInfo.load(
                            node.getGatewayId()).getHost() : node.getHost();
                    int destPort = node.getRemoteDesktopPort();
                    String forwarder = node.getGatewayId() != null
                            && node.getGatewayId() > 0 ? CedarConfiguration
                            .getInstance().getForwader() : "N/A";
                    if (destPort > 0) {
                        response
                                .sendRedirect(String
                                        .format(
                                                "/jws/viewer?SERVICE=%s&DESTHOST=%s&DESTPORT=%d&FORWARDER=%s",
                                                service, destHost, destPort,
                                                forwarder));
                    }
                }
            }
        }
        writer.flush();
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        Writer writer = response.getWriter();
        IEngine engine = EngineFactory.getInstance().getEngine();
        String url = request.getRequestURI();
        String action = url.substring(url.lastIndexOf("/") + 1);
        if (action.equalsIgnoreCase("submit")) {
            String feature = request.getParameter("feature"); // id or name
            String launchset = request.getParameter("launchset"); // XML stream
                                                                  // or name
            if (feature != null && launchset != null) {
                String version = request.getParameter("version");
                FeatureInfo featureInfo = FeatureUtil
                        .getFeatureInfoById(feature);
                if (featureInfo == null) {
                    featureInfo = FeatureUtil.getFeatureInfo(feature,
                            version == null ? null : version.trim());
                }
                if (featureInfo != null) {
                    launchset = launchset.trim();
                    if (launchset.startsWith("<launchset")) {
                        try {
                            StringBuilder sb = new StringBuilder();
                            sb
                                    .append("<pushdoc xmlns=\"http://www.intel.com/soae/cedar\">");
                            sb.append(launchset);
                            sb.append("</pushdoc>");
                            FeatureLoader fload = new FeatureLoader();
                            ClassLoader cl = fload.loadFeature(featureInfo)
                                    .getFeatureClassLoader();
                            ByteArrayInputStream is = new ByteArrayInputStream(
                                    sb.toString().getBytes());
                            FeatureDescLoader descLoader = new FeatureDescLoader();
                            descLoader.setClassLoader(cl);
                            LaunchSet ls = descLoader.loadLaunchSet(is);
                            for (String jobId : engine.submit(featureInfo
                                    .getId(), ls)) {
                                writer.write(jobId + "\n");
                            }
                        } catch (Exception e) {
                            writer.write(e.getMessage());
                        }
                    } else {
                        for (String jobId : engine.submit(featureInfo.getId(),
                                launchset)) {
                            writer.write(jobId + "\n");
                        }
                    }
                }
            }
        }
        writer.flush();
    }

    @Override
    public void init() throws ServletException {
    }
}
