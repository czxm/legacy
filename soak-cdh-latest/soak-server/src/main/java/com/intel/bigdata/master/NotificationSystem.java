package com.intel.bigdata.master;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.contrib.pattern.ClusterReceptionistExtension;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.intel.bigdata.common.protocol.State;
import com.intel.bigdata.master.nodes.NodeList;
import com.intel.bigdata.master.nodes.NodeState;
import com.intel.bigdata.master.nodes.notification.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Component("NotificationSystem")
@Scope("prototype")
public class NotificationSystem extends UntypedActor {

    protected static Logger LOG = LoggerFactory.getLogger(NodeNotification.class);

    private static HashMap<String, NodeState> nodeStore = new HashMap();
    private ImmutableTable<String, String, State> processStates;
    static boolean isInitialed = false;

    private final ActorSelection requestDispatcher = context().actorSelection(context().parent().path() + "/requestDispatcher");

    @Override
    public void preStart() throws Exception {
        NodeList nodeList = new NodeList();
        nodeStore = nodeList.getNodesStatus();
        if (!isInitialed) {
            scheduleDiagnose();
        }
        isInitialed = true;
    }

    @Override
    public void onReceive(Object message) throws Exception {
         if (message instanceof SystemStateRequest) {
            sender().tell(getCurrentSystemState(), self());
         } else if (message instanceof NodesDiagnose) {
             LOG.debug("start NodesDiagnose");
             ArrayList<NodeNotification> downNodes = nodeStatusDiagnose();
             for (NodeNotification node : downNodes) {
                 requestDispatcher.tell(node, ActorRef.noSender());
             }
         } else if(message instanceof NodeNotification) {
             NodeNotification nn = (NodeNotification) message;
             updateNodeState(nn);
         } else{
            unhandled(message);
         }
    }

    public SystemStateResponse getCurrentSystemState() {
        ImmutableMap<String, NodeState> currentNodeStates = ImmutableMap.copyOf(nodeStore);
        SystemStateResponse currentSystemState = new SystemStateResponse(currentNodeStates, processStates);
        return currentSystemState;
    }

    private boolean timeSynced(long timestamp){
        return Math.abs(timestamp - System.currentTimeMillis()) < 5 * 60 * 1000;
    }

    private ArrayList<NodeNotification> nodeStatusDiagnose() {
        ArrayList<String> toRemove = new ArrayList<String>();
        ArrayList<NodeNotification> downNodes = new ArrayList<NodeNotification>();
        for (String nodeId : nodeStore.keySet()) {
            NodeState state = nodeStore.get(nodeId);
            if(!timeSynced(state.getTimestamp())){
                LOG.info(nodeId + " time skew detected, will be marked DOWN");
                NodeState hbNodeState = new NodeState(nodeId);
                downNodes.add(new NodeNotification(nodeId, hbNodeState));
                toRemove.add(nodeId);
            }
        }
        for(String nodeId : toRemove){
            nodeStore.remove(nodeId);
        }
        return downNodes;
    }

    private void updateNodeState(NodeNotification nn) {
        String nodeId = nn.getNodeId();
        NodeState nodeState = nodeStore.get(nodeId);
        long timestamp = nn.getState().getTimestamp();
        if (nodeState == null && timeSynced(timestamp)){
            NodeState state = new NodeState(nodeId, true, nn.getState().getTimestamp(), nn.getState().getDispatcher());
            nodeStore.put(nodeId, state);
            requestDispatcher.tell(new NodeNotification(nodeId, state), self());
            LOG.debug("Send updated NodeState");
        }
        else if(nodeState != null){
            nodeState.updateTimeStamp(nn.getState().getTimestamp());
        }
    }

    private void scheduleDiagnose() {
        LOG.debug("start scheduleDiagnose");
        getContext().system().scheduler().schedule(
                Duration.create(90, TimeUnit.SECONDS),
                Duration.create(60, TimeUnit.SECONDS),
                getSelf(), new NodesDiagnose(),
                getContext().system().dispatcher(), getSelf());
    }
}
