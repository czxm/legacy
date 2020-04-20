package com.intel.bigdata.master.nodes.notification;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.intel.bigdata.common.protocol.State;
import com.intel.bigdata.master.nodes.NodeState;

import java.io.Serializable;

public class SystemStateResponse implements Serializable {
    private final ImmutableMap<String, NodeState> nodeStates; // nodeID -> state
    private final ImmutableTable<String, String, State> processStates; // node -> process - > state


    public SystemStateResponse(ImmutableMap<String, NodeState> nodeStates, ImmutableTable<String, String, State> processStates) {
        this.nodeStates = nodeStates;
        this.processStates = processStates;
    }

    public ImmutableMap<String, NodeState> getNodeStates() {
        return nodeStates;
    }

    public ImmutableTable<String, String, State> getProcessStates() {
        return processStates;
    }
}
