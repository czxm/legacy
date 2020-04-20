package com.intel.cedar.agent;

import java.util.List;

import com.intel.cedar.core.entities.AbstractHostInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.NATInfo;

public interface IGatewayAgent extends IAgent {
    public String getHostName(String host);

    public int createPortMapping(NATInfo n);

    public void releasePortMapping(NATInfo n);

    public List<NATInfo> getMappedPorts(AbstractHostInfo i);

    public void clearPortMappings();
}
