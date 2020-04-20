package com.intel.bigdata.master;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import akka.contrib.pattern.ClusterReceptionistExtension;
import com.intel.bigdata.common.protocol.HeartbeatMessage;
import com.intel.bigdata.common.protocol.MasterRequest;
import com.intel.bigdata.common.util.SpringExtension;
import com.intel.bigdata.master.nodes.NodeState;
import com.intel.bigdata.master.nodes.notification.NodeNotification;
import com.intel.bigdata.master.nodes.notification.NodesDiagnose;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 10/29/13
 * Time: 5:38 PM
 * To change this template use File | Settings | File Templates.
 */
@Component("MasterServerActor")
@Scope("prototype")
public class MasterServerActor extends UntypedActor {

    private HashMap<String, ActorRef> actors = new HashMap<String, ActorRef>();

    final ActorRef requestDispatcher = getContext().actorOf(SpringExtension.SpringExtProvider.get(getContext().system()).props("RequestDispatcher"), "requestDispatcher");

    final ActorRef notificationSystem = getContext().actorOf(SpringExtension.SpringExtProvider.get(getContext().system()).props("NotificationSystem"), "notificationSystem");

    protected static Logger LOG = LoggerFactory.getLogger(MasterServerActor.class);

    @Override
    public void preStart() throws Exception {
        ActorSystem system = getContext().system();
        ClusterReceptionistExtension.get(system).registerService(self());

        for(Map.Entry<String, ConfigValue> entry : system.settings().config().getConfig("akka.appActors").entrySet()){
            String path = entry.getKey();
            ConfigValue c = entry.getValue();
            Object value = c.unwrapped();
            if(value instanceof  String){
                actors.put(path, getContext().actorOf(
                    SpringExtension.SpringExtProvider.get(getContext().system()).props((String)value), path));
            }
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof HeartbeatMessage) {
            HeartbeatMessage msg = (HeartbeatMessage) message;
            LOG.debug("receive heartbeat from "+msg.getNodeId());
            notificationSystem.tell(new NodeNotification(msg.getNodeId(),
                    new NodeState(msg.getNodeId(), true, msg.getTimestamp(), msg.getAgentDispatcher())), ActorRef.noSender());
            updateProcessStatus(msg);

        } else if (message instanceof MasterRequest){
            MasterRequest request = (MasterRequest)message;
            String consumer = request.getPayload().getConsumer();
            if(consumer != null){
                ActorRef target = actors.get(consumer);
                if(target != null){
                    target.tell(request, getSender());
                }
            }
        } else {
            unhandled(message);
        }
    }

    private void updateProcessStatus(HeartbeatMessage msg) {
        //TODO update to notificationSystem
    }
}
