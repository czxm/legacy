package com.intel.bigdata.agent;

import akka.actor.*;
import akka.contrib.pattern.ClusterClient;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.SmallestMailboxRouter;
import com.intel.bigdata.agent.events.RegisterListener;
import com.intel.bigdata.common.protocol.AgentRequest;
import com.intel.bigdata.common.protocol.HeartbeatMessage;
import com.intel.bigdata.common.protocol.MasterRequest;
import com.intel.bigdata.common.util.SpringExtension;
import com.typesafe.config.ConfigValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @note The scope here is prototype since we want to create a new actor
 *       instance for use of this bean.
 */
@Component("AgentDispatcher")
@Scope("prototype")
class AgentDispatcher extends UntypedActor {

    private HashMap<String, ActorRef> actors = new HashMap<String, ActorRef>();

    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private static final int COMMAND_ROUTER_SIZE = 3;

	private final ActorRef heartbeatSender = getContext().actorOf(
			SpringExtension.SpringExtProvider.get(getContext().system()).props("HeartbeatSender"), "heartbeatSender");

    private final ActorRef commandRouter = getContext().actorOf(
            SpringExtension.SpringExtProvider.get(getContext().system()).props("RequestExecutor").
                    withRouter(new SmallestMailboxRouter(COMMAND_ROUTER_SIZE)));

    private final ActorRef configManager = getContext().actorOf(
            SpringExtension.SpringExtProvider.get(getContext().system()).props("ConfigManager"), "configManager");

    private final ActorRef metricDispatcher = getContext().actorOf(
            SpringExtension.SpringExtProvider.get(getContext().system()).props("MetricDispatcher"), "metricDispatcher");

    private ActorRef clusterClient;

    @Override
    public void preStart() throws Exception {
        super.preStart();

        LOG.debug("initialization...");

        // initialize
        ActorSystem system = getContext().system();
        List<String> initialContacts = system.settings().config().getStringList("akka.cluster.seed-nodes");
        Set<ActorSelection> selectors = new HashSet<ActorSelection>();
        if (initialContacts != null) {
            for (String initialContact : initialContacts) {
                selectors.add(system.actorSelection(initialContact + "/user/receptionist"));
            }
        }
        clusterClient = system.actorOf(ClusterClient.defaultProps(selectors));

        // subscribe heartbeat sender on metrics values
        metricDispatcher.tell(new RegisterListener(heartbeatSender), getSelf());

        initAppExec(system);
    }

    private void initAppExec(ActorSystem system) {
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
	public void onReceive(Object msg) throws Exception {
        if (msg instanceof HeartbeatMessage) {
            clusterClient.tell(new ClusterClient.Send("/user/masterServer/master", msg, false), self());
        } else if (msg instanceof AgentRequest) {
            AgentRequest request = (AgentRequest)msg;
            String consumer = request.getPayload().getConsumer();
            if(consumer != null){
                ActorRef target = actors.get(consumer);
                if(target != null){
                    target.tell(msg, getSender());
                }
            }
            else{
                commandRouter.tell(msg, getSender());
                configManager.tell(msg, getSender());
            }
        } else if (msg instanceof MasterRequest){
            clusterClient.tell(new ClusterClient.Send("/user/masterServer/master", msg, false), getSender());
        } else {
            unhandled(msg);
        }
	}
}