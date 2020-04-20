package com.intel.cedar.cloud.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.intel.cedar.agent.IAgent;
import com.intel.cedar.agent.impl.AgentManager;
import com.intel.cedar.cloud.CloudException;
import com.intel.cedar.core.entities.CloudInfo;
import com.intel.cedar.core.entities.CloudNodeInfo;
import com.intel.cedar.core.entities.GatewayInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.MachineInfo.ARCH;
import com.intel.cedar.core.entities.MachineInfo.OS;
import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.impl.GenericTaskItem;
import com.intel.cedar.tasklet.impl.ShellProxyRunner;
import com.intel.cedar.util.EC2Util;
import com.xerox.amazonws.ec2.AvailabilityZone;

public class OpenStackConnector extends EC2Connector {
    private static Logger LOG = LoggerFactory
            .getLogger(OpenStackConnector.class);

    public OpenStackConnector(CloudInfo cloud) {
        super(cloud);
    }

    private boolean nodeExists(String node, List<CloudNodeInfo> nodes){
        for(CloudNodeInfo n : nodes){
            if(node.equals(n.getHost()))
                return true;
        }
        return false;
    }
    
    @Override
    public List<CloudNodeInfo> getCloudNodes() throws CloudException {
        List<CloudNodeInfo> result = new ArrayList<CloudNodeInfo>();
        if (ec2 != null) {
            try {
                List<AvailabilityZone> descs = ec2
                        .describeAvailabilityZones(Lists
                                .newArrayList("verbose"));
                for (AvailabilityZone desc : descs) {
                    String node = EC2Util.findNovaNode(desc);
                    if (node != null && node.length() > 0 && !nodeExists(node, result)) {
                        CloudNodeInfo n = new CloudNodeInfo();
                        n.setHost(node);
                        n.setContent("");
                        n.setArch(ARCH.x86_64);
                        n.setOs(OS.unix);
                        result.add(n);
                    }
                }
            } catch (Exception e) {
                LOG.info("getCloudNodes", e);
            }
        }
        return result;
    }

    @Override
    public URI getInstanceDisplay(List<CloudNodeInfo> nodes,
            InstanceInfo instance) {
        if (nodes == null || instance == null || instance.getKeyName() == null)
            return null;
        // OpenStack store the CloudNode info in the keyname attri of instance
        String keyname = instance.getKeyName();
        if (keyname.length() == 0)
            return null;
        int si = keyname.lastIndexOf(",");
        int ei = keyname.lastIndexOf(")");
        String cn = keyname.substring(si + 2, ei);
        CloudNodeInfo hostingNode = null;
        for (CloudNodeInfo node : nodes) {
            if (node.getHost().equalsIgnoreCase(cn)) {
                hostingNode = node;
                break;
            }
        }
        if (hostingNode == null)
            return null;

        String vid = instance.getInstanceId().replace("i-", "instance-");
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
        item.setProperty("passwd", this.cloud.getNodePassword() == null ? "nopasswd" : this.cloud.getNodePassword());
        item.setProperty("cmd", "virsh vncdisplay " + vid);
        ShellProxyRunner runner = new ShellProxyRunner();
        agent.setOutputStream(runner, bos);
        r = agent.run(runner, item, "300", "/");
        AgentManager.getInstance().releaseAgent(agent);
        if (r != null && !r.getID().isSucceeded()) {
            LOG.info(r.getFailureMessage());
            if (r.getID().isUnreachable())
                return URI.create("unknown://unreachable");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(bos.toByteArray())));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains(vid)) {
                    // the instance is not yet started on the node
                    return null;
                } else if (line.trim().contains(":")) {
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
