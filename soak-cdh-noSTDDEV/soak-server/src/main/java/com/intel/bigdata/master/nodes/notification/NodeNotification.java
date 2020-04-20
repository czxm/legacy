package com.intel.bigdata.master.nodes.notification;

import com.intel.bigdata.master.nodes.NodeState;

import java.io.Serializable;

public class NodeNotification  implements Serializable {
    private final String nodeId;
    private final NodeState state;

    public NodeNotification(String nodeId, NodeState state) {
        this.nodeId = nodeId;
        this.state = state;
    }

    public String getNodeId() {
        return nodeId;
    }

    public NodeState getState() {
        return state;
    }
}
