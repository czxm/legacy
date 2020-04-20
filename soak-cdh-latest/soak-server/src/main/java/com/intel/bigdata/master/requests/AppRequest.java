package com.intel.bigdata.master.requests;

import com.google.common.collect.ImmutableList;
import com.intel.bigdata.common.protocol.Payload;
import scala.concurrent.duration.FiniteDuration;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/1/13
 * Time: 8:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class AppRequest implements Serializable{
    private final String id;
    private final boolean waitForAllNodes;
    private final ImmutableList<String> destinationNodeIds;
    private final FiniteDuration timeout;
    private final Payload payload;
    private final boolean concOnNode;

    public AppRequest(
            String id,
            boolean waitForAllNodes,
            ImmutableList<String> destinationNodeIds,
            FiniteDuration timeout,
            Payload payload,
            boolean concOnNode
    ) {
        this.id = (id == null) ? UUID.randomUUID().toString() : id;
        this.waitForAllNodes = waitForAllNodes;
        this.destinationNodeIds = destinationNodeIds;
        this.timeout = timeout;
        this.payload = payload;
        this.concOnNode = concOnNode;
    }

    public String getId() {
        return id;
    }

    public ImmutableList<String> getDestinationNodeIds() {
        return destinationNodeIds;
    }

    public Payload getPayload() {
        return payload;
    }

    public boolean isWaitForAllNodes() {
        return waitForAllNodes;
    }

    public FiniteDuration getTimeout() {
        return timeout;
    }

    public boolean isConcOnNode() {
        return concOnNode;
    }

    @Override
    public String toString() {
        return "AppRequest{" +
                "id='" + id + '\'' +
                ", destinationNodeIds='" + destinationNodeIds + '\'' +
                ", payload=" + payload +
                ", waitForAllNodes=" + waitForAllNodes +
                ", timeout=" + timeout +
                ",ConcurrenceOnNode" + concOnNode +
                '}';
    }
}
