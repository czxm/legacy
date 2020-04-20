package com.intel.cedar.cloud.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.intel.cedar.agent.IAgent;
import com.intel.cedar.agent.impl.AgentManager;
import com.intel.cedar.cloud.CloudEucaException;
import com.intel.cedar.cloud.CloudEucaReqTimeoutException;
import com.intel.cedar.cloud.CloudException;
import com.intel.cedar.core.entities.CloudInfo;
import com.intel.cedar.core.entities.CloudNodeInfo;
import com.intel.cedar.core.entities.GatewayInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.MachineTypeInfo;
import com.intel.cedar.core.entities.MachineInfo.ARCH;
import com.intel.cedar.core.entities.MachineInfo.OS;
import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.impl.CommandTaskItem;
import com.intel.cedar.tasklet.impl.CommandTaskRunner;
import com.intel.cedar.tasklet.impl.GenericTaskItem;
import com.intel.cedar.tasklet.impl.ShellProxyRunner;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.cedar.util.EC2Util;
import com.xerox.amazonws.ec2.AvailabilityZone;
import com.xerox.amazonws.ec2.EC2Exception;

public class EucaConnector extends EC2Connector {
    private static Logger LOG = LoggerFactory.getLogger(EucaConnector.class);

    public EucaConnector(CloudInfo cloud) {
        super(cloud);
    }

    protected void handleException(Exception e) throws CloudException {
        LOG.info(e.getMessage());
        Pattern pat = Pattern.compile("408\\sRequest\\sTimeout");
        Matcher matcher = pat.matcher(e.getMessage());
        if (matcher.find()) {
            throw new CloudEucaReqTimeoutException(e.getMessage(), e);
        } else {
            throw new CloudEucaException(e.getMessage(), e);
        }
    }

    @Override
    public List<MachineTypeInfo> getMachineTypes() throws CloudException {
        List<MachineTypeInfo> result = Lists.newArrayList();
        if (ec2 != null) {
            try {
                List<AvailabilityZone> descs = ec2
                        .describeAvailabilityZones(Lists
                                .newArrayList("verbose"));
                for (AvailabilityZone desc : descs) {
                    if (EC2Util.containTypeInfo(desc)) {
                        MachineTypeInfo i = new MachineTypeInfo();
                        i.setCloudId(cloud.getId());
                        EC2Util.setTypeInfo(desc, i);
                        result.add(i);
                    }
                }
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return result;
    }

    @Override
    public List<CloudNodeInfo> getCloudNodes() throws CloudException {
        List<CloudNodeInfo> result = new ArrayList<CloudNodeInfo>();
        IAgent agent = AgentManager.getInstance().createAgent(this.cloud, true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CommandTaskItem item = new CommandTaskItem();
        item.setCommandLine("/opt/eucalyptus/usr/sbin/euca_conf --list-nodes");
        CommandTaskRunner runner = new CommandTaskRunner();
        agent.setOutputStream(runner, bos);
        IResult r = agent.run(runner, item, "300", "/");
        AgentManager.getInstance().releaseAgent(agent);
        if (!r.getID().isSucceeded()) {
            LOG.info("getCloudNodes " + r.getFailureMessage());
            return result;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(bos.toByteArray())));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] items = line.trim().split(" ");
                if (items != null && items.length > 2) {
                    CloudNodeInfo n = new CloudNodeInfo();
                    n.setHost(items[0]);
                    n.setContent(line);
                    n.setArch(ARCH.x86_64);
                    n.setOs(OS.ubuntu);
                    result.add(n);
                }
            }
        } catch (Exception e) {
            LOG.info("getCloudNodes", e);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
        return result;
    }

    @Override
    public URI getInstanceDisplay(List<CloudNodeInfo> nodes,
            InstanceInfo instance) {
        if (nodes == null || instance == null)
            return null;
        CloudNodeInfo hostingNode = null;
        for (CloudNodeInfo node : nodes) {
            if (node.getContent() != null
                    && node.getContent().contains(instance.getInstanceId())) {
                hostingNode = node;
                break;
            }
        }
        if (hostingNode == null)
            return null;
        IResult r = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CloudInfo ci = CloudInfo.load(hostingNode.getCloudId());
        GatewayInfo gw = ci.findGateway();
        IAgent agent = null;
        if (gw != null) {
            agent = AgentManager.getInstance().createAgent(gw, true);
        } else {
            agent = AgentManager.getInstance().createAgent(ci, true);
        }
        GenericTaskItem item = new GenericTaskItem();
        item.setProperty("host", hostingNode.getHost());
        item.setProperty("user", this.cloud.getNodeUser());
        item.setProperty("passwd", this.cloud.getNodePassword());
        item.setProperty("cmd", "virsh vncdisplay " + instance.getInstanceId());
        ShellProxyRunner runner = new ShellProxyRunner();
        agent.setOutputStream(runner, bos);
        r = agent.run(runner, item, "300", "/");
        AgentManager.getInstance().releaseAgent(agent);
        if (r != null && r.getID().isUnreachable()) {
            return URI.create("unknown://unreachable");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(bos.toByteArray())));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains(instance.getInstanceId())) {
                    // the instance is not yet started on the node
                    return null;
                } else if (line.trim().startsWith(":")) {
                    String portStr = line.trim().substring(
                            line.indexOf(":") + 1);
                    int port = 5900 + Integer.parseInt(portStr);
                    return URI.create("vnc://" + hostingNode.getHost() + ":"
                            + port);
                }
            }
        } catch (Exception e) {
            LOG.info("getInstanceDisplay", e);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
        return URI.create("unknown://unreachable");
    }
}
