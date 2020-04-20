package com.intel.cedar.agent.impl;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.intel.cedar.agent.IAgent;
import com.intel.cedar.agent.IExtensiveAgent;
import com.intel.cedar.agent.IGatewayAgent;
import com.intel.cedar.core.entities.AbstractHostInfo;
import com.intel.cedar.core.entities.CloudInfo;
import com.intel.cedar.core.entities.CloudNodeInfo;
import com.intel.cedar.core.entities.GatewayInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.PhysicalNodeInfo;
import com.intel.cedar.tasklet.impl.CedarAdminTaskItem;
import com.intel.cedar.tasklet.impl.CedarAdminTaskRunner;
import com.intel.cedar.tasklet.impl.CedarAdminTaskItem.AdminTaskType;

public class AgentManager {
    private static Logger LOG = LoggerFactory.getLogger(AgentManager.class);
    private static AgentManager singleton;
    private HashMap<String, IAgent> agents;

    public static synchronized AgentManager getInstance() {
        if (singleton == null) {
            singleton = new AgentManager();
        }
        return singleton;
    }

    private AgentManager() {
        agents = new HashMap<String, IAgent>();
    }
    
    public IAgent createAgent(AbstractHostInfo host, boolean quickTimeOut) {
        if (host instanceof InstanceInfo)
            return createAgent((InstanceInfo) host, quickTimeOut);
        else if (host instanceof PhysicalNodeInfo)
            return createAgent((PhysicalNodeInfo) host, quickTimeOut);
        else if (host instanceof GatewayInfo)
            return createAgent((GatewayInfo) host, quickTimeOut);
        else if (host instanceof CloudNodeInfo)
            return createAgent((CloudNodeInfo) host, quickTimeOut);
        else
            return null;
    }

    public IExtensiveAgent createAgent(InstanceInfo instance, boolean quickTimeOut) {
        String port = "10614";
        String host = instance.getHost();
        CloudInfo c = instance.getCloudInfo();
        if (c.getSeperated()) {
            if (instance.getGatewayId() == null || instance.getGatewayId() < 0) {
                instance.setGatewayId(c.findGateway().getId());
                instance.saveChanges();
            }
            host = GatewayInfo.load(instance.getGatewayId()).getHost();
            port = Integer.toString(instance.getCedarPort());
        }
        ExtensiveAgent agent = new ExtensiveAgent(host, port, quickTimeOut);
        String proxy = c.getProxyHost();
        if (proxy != null && !proxy.equals("")) {
            agent.setProxy(proxy, c.getProxyPort(), c.getProxyAuth(), c
                    .getProxyPasswd());
        }
        agents.put(agent.getAgentID(), agent);
        return agent;
    }

    public IExtensiveAgent createAgent(PhysicalNodeInfo node, boolean quickTimeOut) {
        String port = "10614";
        String host = node.getHost();
        if (node.getGatewayId() != null && node.getGatewayId() > 0) {
            GatewayInfo gw = GatewayInfo.load(node.getGatewayId());
            if (gw != null) {
                host = gw.getHost();
                port = Integer.toString(node.getCedarPort());
            }
        }
        ExtensiveAgent agent = new ExtensiveAgent(host, port, quickTimeOut);
        String proxy = node.getProxyHost();
        if (proxy != null && !proxy.equals("")) {
            agent.setProxy(proxy, node.getProxyPort(), node.getProxyAuth(),
                    node.getProxyPasswd());
        }
        agents.put(agent.getAgentID(), agent);
        return agent;
    }

    public IGatewayAgent createAgent(GatewayInfo node, boolean quickTimeOut) {
        GatewayAgent agent = new GatewayAgent(node.getHost(), "10614", quickTimeOut);
        String proxy = node.getProxyHost();
        if (proxy != null && !proxy.equals("")) {
            agent.setProxy(proxy, node.getProxyPort(), node.getProxyAuth(),
                    node.getProxyPasswd());
        }
        agents.put(agent.getAgentID(), agent);
        return agent;
    }

    public IExtensiveAgent createAgent(CloudInfo node, boolean quickTimeOut) {
        ExtensiveAgent agent = new ExtensiveAgent(node.getHost(), "10614", quickTimeOut);
        String proxy = node.getProxyHost();
        if (proxy != null && !proxy.equals("")) {
            agent.setProxy(proxy, node.getProxyPort(), node.getProxyAuth(),
                    node.getProxyPasswd());
        }
        agents.put(agent.getAgentID(), agent);
        return agent;
    }

    public IExtensiveAgent createAgent(CloudNodeInfo node, boolean quickTimeOut) {
        String port = "10614";
        String host = node.getHost();
        if (node.getGatewayId() != null && node.getGatewayId() > 0) {
            GatewayInfo gw = GatewayInfo.load(node.getGatewayId());
            if (gw != null) {
                host = gw.getHost();
                port = Integer.toString(node.getCedarPort());
            }
        }
        ExtensiveAgent agent = new ExtensiveAgent(host, port, quickTimeOut);
        String proxy = node.getProxyHost();
        if (proxy != null && !proxy.equals("")) {
            agent.setProxy(proxy, node.getProxyPort(), node.getProxyAuth(),
                    node.getProxyPasswd());
        }
        agents.put(agent.getAgentID(), agent);
        return agent;
    }

    public IAgent createAgent(String host) {
        IAgent agent = new XmlBasedAgent(host, "10614", false);
        agents.put(agent.getAgentID(), agent);
        return agent;
    }

    public IAgent createSimpleAgent(String host) {
        IAgent agent = new SimpleAgent(host);
        agents.put(agent.getAgentID(), agent);
        return agent;
    }

    public void releaseAgent(IAgent agent) {
        if (agent instanceof XmlBasedAgent) {
            ((XmlBasedAgent) agent).finalize();
        }
        agents.remove(agent.getAgentID());
    }

    public List<IAgent> getAgents() {
        return Lists.newArrayList(agents.values());
    }

    public IAgent findAgentByID(String id) {
        return agents.get(id);
    }
}
