package com.intel.bigdata.master;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intel.bigdata.common.protocol.AgentRequest;
import com.intel.bigdata.common.protocol.AgentResponse;
import com.intel.bigdata.master.nodes.NodeState;
import com.intel.bigdata.master.nodes.notification.NodeListRequest;
import com.intel.bigdata.master.nodes.notification.NodeNotification;
import com.intel.bigdata.master.nodes.notification.SystemStateRequest;
import com.intel.bigdata.master.nodes.notification.SystemStateResponse;
import com.intel.bigdata.master.requests.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 10/31/13
 * Time: 10:29 PM
 * To change this template use File | Settings | File Templates.
 */

@org.springframework.stereotype.Component("RequestDispatcher")
@Scope("prototype")
public class RequestDispatcher extends UntypedActor {

    protected static Logger LOG = LoggerFactory.getLogger(RequestDispatcher.class);

    private HashMap<String, ActorRef> requestId2sender = new HashMap<String, ActorRef>();
    private HashMap<String, AppRequest> id2request = new HashMap<String, AppRequest>();
    private ArrayListMultimap<String, String> node2requestIdPending = ArrayListMultimap.<String, String>create(); // Per node command queue
    private HashBasedTable<String, String, Cancellable> timeoutsTable = HashBasedTable.<String, String, Cancellable>create(); // Command -> Node -> Scheduled Timeout
    private HashBasedTable<String, String, Object> requestNodeResult = HashBasedTable.<String, String, Object>create(); // Command -> Node -> Result
    private HashMap<String, NodeState> nodeStates = new HashMap();

    @Override
    public void preStart() throws Exception {
        String notificationSystemPath = getContext().parent().path() + "/notificationSystem";
        ActorSelection notificationSystem = context().actorSelection(notificationSystemPath);
        notificationSystem.tell(new SystemStateRequest(), self());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof NodeNotification) { // Node state update
            NodeNotification nn = (NodeNotification) message;
            if (nn.getState().isRunning()) {
                nodeStates.put(nn.getNodeId(), nn.getState());
                LOG.info("Register " + nn.getNodeId() + " into Master!");
            } else {
                nodeStates.put(nn.getNodeId(), new NodeState(nn.getNodeId()));
                LOG.info("Unregister " + nn.getNodeId() + " from Master!");
            }
        }
        else if (message instanceof SystemStateResponse) { // Initial snapshot of the node states
            SystemStateResponse ssr = (SystemStateResponse) message;
            LOG.info("Initial snapshot of Cluster Topology");
            nodeStates.putAll(ssr.getNodeStates());
        }
        else if(message instanceof NodeListRequest){
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            for(String n : nodeStates.keySet()){
                if(nodeStates.get(n).isRunning())
                    builder.add(n);
            }
            sender().tell(builder.build(), self());
        }
        else if (message instanceof AppRequest) {
            AppRequest req = (AppRequest) message;
            id2request.put(req.getId(), req); // Remember the request
            requestId2sender.put(req.getId(), sender());
            for (String nodeId : req.getDestinationNodeIds()) {
                if (req.isConcOnNode()) {
                    ActorRef nodeDispatcher = getNodeDispatcher(nodeId);
                    if (nodeDispatcher != null) { // Send the command to the node
                        nodeDispatcher.tell(new AgentRequest(req.getId(), nodeId, req.getPayload()), self());
                        Cancellable timeoutTask = getContext().system().scheduler().scheduleOnce(
                                req.getTimeout(),
                                self(),
                                new AgentResponse(req.getId(), nodeId, AppRequestStatus.RequestNodeStatus.TIMEOUT),
                                getContext().system().dispatcher(),
                                self()
                        );
                        timeoutsTable.put(req.getId(), nodeId, timeoutTask);
                    } else {
                        requestNodeResult.put(req.getId(), nodeId, AppRequestStatus.RequestNodeStatus.DOWN);
                        self().tell(new AgentResponse(req.getId(), nodeId, AppRequestStatus.RequestNodeStatus.DOWN), self());
                    }

                } else {
                    requestNodeResult.put(req.getId(), nodeId, AppRequestStatus.RequestNodeStatus.PENDING);
                    node2requestIdPending.put(nodeId, req.getId()); // Put to the node queue
                    if (!requestNodeResult.column(nodeId).containsValue(AppRequestStatus.RequestNodeStatus.IN_PROGRESS)) { // Check if node is not already running a request
                        self().tell(new NodeReady(nodeId), self());
                    }
                }
            }
        } else if (message instanceof NodeReady) {
            NodeReady nr = (NodeReady) message;
            String nodeId = nr.getNodeId();
            // really ready? or somebody already submitted a command?
            if (requestNodeResult.column(nodeId).containsValue(AppRequestStatus.RequestNodeStatus.IN_PROGRESS)) {
                return;
            }
            AppRequest req = checkPendingRequestsForNode(nodeId);
            if (req != null) {
                ActorRef nodeDispatcher = getNodeDispatcher(nodeId);
                if (nodeDispatcher != null) { // Send the command to the node
                    nodeDispatcher.tell(new AgentRequest(req.getId(), nodeId, req.getPayload()), self());
                    node2requestIdPending.remove(nodeId, req.getId());
                    requestNodeResult.put(req.getId(), nodeId, AppRequestStatus.RequestNodeStatus.IN_PROGRESS);

                    //Schedule command timeout
                    Cancellable timeoutTask = getContext().system().scheduler().scheduleOnce(
                            req.getTimeout(),
                            self(),
                            new AgentResponse(req.getId(), nodeId, AppRequestStatus.RequestNodeStatus.TIMEOUT),
                            getContext().system().dispatcher(),
                            self()
                    );
                    timeoutsTable.put(req.getId(), nodeId, timeoutTask);

                } else { // handle node down state
                    node2requestIdPending.remove(nodeId, req.getId());
                    requestNodeResult.put(req.getId(), nodeId, AppRequestStatus.RequestNodeStatus.DOWN);
                    // Send a dummy response to self
                    self().tell(new AgentResponse(req.getId(), nodeId, AppRequestStatus.RequestNodeStatus.DOWN), self());
                }
            }
        } else if (message instanceof AgentResponse) {
            AgentResponse ar = (AgentResponse) message;

            String nodeId = ar.getNodeId();
            String requestId = ar.getSessionId();

            AppRequest req = id2request.get(requestId);
            if (req == null) {
                return; // do nothing, the request should have timed out
            }

            // Remove the timeout task and cancel it
            Cancellable timeoutTask = timeoutsTable.remove(requestId, nodeId);
            if (timeoutTask != null) {
                timeoutTask.cancel();
            }

            requestNodeResult.put(requestId, nodeId, ar.getPayload());
            // TODO - need to send any state notifications

            Map<String, Object> reqResult = requestNodeResult.row(requestId);
            boolean allNodesDone = !(
                    reqResult.containsValue(AppRequestStatus.RequestNodeStatus.IN_PROGRESS) ||
                            reqResult.containsValue(AppRequestStatus.RequestNodeStatus.PENDING)
            );

            if (req.isWaitForAllNodes()) {
                if (allNodesDone) { // Release all nodes
                    for (String nid : req.getDestinationNodeIds()) {
                        if (getNodeDispatcher(nid) != null) {
                            self().tell(new NodeReady(nid), self()); // Release the waiting nodes
                        }
                    }
                }
            } else { // Release the node that replied
                if (getNodeDispatcher(nodeId) != null) {
                    self().tell(new NodeReady(nodeId), self());
                }
            }

            if (allNodesDone) { // All the nodes are done - reply and discard the request result
                // Send the request result back
                ActorRef reqSender = requestId2sender.get(requestId);
                AppRequestStatus result = new AppRequestStatus(requestId, ImmutableMap.<String, Object>copyOf(reqResult));

                if (LOG.isDebugEnabled()) {
                    LOG.debug("sending message: " + result);
                }
                reqSender.tell(result, self());

                // Cleanup request
                requestNodeResult.rowMap().remove(requestId);
                id2request.remove(requestId);
                requestId2sender.remove(requestId);
            }
        } else if (message instanceof AppRequestStatusQuery) {
            AppRequestStatusQuery arq = (AppRequestStatusQuery) message;
            String reqId = arq.getRequestId();
            Map<String, Object> reqStatus = requestNodeResult.row(reqId);
            if (reqStatus == null) {
                sender().tell(new AppRequestStatusMissing(reqId), self());
            } else {
                AppRequestStatus result = new AppRequestStatus(reqId, ImmutableMap.<String, Object>copyOf(reqStatus));
                sender().tell(result, self());
            }

        } else {
            unhandled(message);
        }
    }

    private static class NodeReady {
        private final String nodeId;

        public NodeReady(String nodeId) {
            super();
            this.nodeId = nodeId;
        }

        public String getNodeId() {
            return nodeId;
        }

        @Override
        public String toString() {
            return "NodeReady{" +
                    "nodeId='" + nodeId + '\'' +
                    '}';
        }
    }

    private AppRequest checkPendingRequestsForNode(String nodeId) {
        List<String> pendingRequests = node2requestIdPending.get(nodeId);
        for (String requestId : pendingRequests) {
            if (checkCanRun(requestId, nodeId)) {
                return id2request.get(requestId);
            }
        }
        return null;
    }

    private boolean checkCanRun(String requestId, String nodeId) {
        // TODO: dependencies
        return true;
    }

    private ActorRef getNodeDispatcher(String nodeId) {
        NodeState nodeState = nodeStates.get(nodeId);
        if (nodeState.isRunning()) {

            return nodeState.getDispatcher();
        }
        return null;
    }
}
