package com.intel.cedar.pool;

import com.intel.cedar.agent.IAgent;
import com.intel.cedar.agent.IExtensiveAgent;
import com.intel.cedar.agent.impl.AgentManager;
import com.intel.cedar.core.entities.VolumeInfo;

public class ResourceItem {
    private ComputeNode node;
    private VolumeInfo volume;
    private IExtensiveAgent agent;
    
    public ResourceItem(ComputeNode n) {
        node = n;
    }

    public ResourceItem(ComputeNode n, VolumeInfo v) {
        node = n;
        this.volume = v;
    }

    public void setVolume(VolumeInfo v) {
        this.volume = v;
    }

    public ComputeNode getNode() {
        return this.node;
    }

    public VolumeInfo getVolume() {
        return this.volume;
    }
    
    public void releaseAgent(){
        if(agent != null)
            AgentManager.getInstance().releaseAgent(agent);
        agent = null;
    }
    
    public IExtensiveAgent getAgent(){
        if(agent == null){
            IAgent a = AgentManager.getInstance().createAgent(node.getHost(), false);
            if(a instanceof IExtensiveAgent)
                agent = (IExtensiveAgent)a;
        }
        return agent;
    }
}