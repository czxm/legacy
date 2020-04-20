package com.intel.bigdata.agent;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.agent.Agent;
import com.google.common.collect.ImmutableMap;
import com.intel.bigdata.agent.events.MetricStatusList;
import com.intel.bigdata.agent.events.Tick;
import com.intel.bigdata.common.protocol.AgentConfig;
import com.intel.bigdata.common.protocol.HeartbeatMessage;
import com.intel.bigdata.common.protocol.State;
import com.intel.bigdata.common.util.ProcessState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.Option;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

@Component("HeartbeatSender")
@Scope("prototype")
public class HeartbeatSender extends UntypedActor {

    protected static final Logger LOG = LoggerFactory.getLogger(HeartbeatSender.class);

    private Agent<AgentConfig> agentConfig;

    private MetricStatusList metricList = MetricStatusList.EMPTY;

    @Inject
    @Required
    public void setAgentConfig(@Named("agentConfig") Agent<AgentConfig> agentConfig) {
        this.agentConfig = agentConfig;
    }

    @Override
	public void preStart() throws Exception {
		super.preStart();
        scheduleHeartbeats();
	}

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        super.preRestart(reason, message);
        scheduleHeartbeats();
    }

    @Override
	public void onReceive(Object msg) throws Exception {
        if (msg instanceof Tick) {
            LOG.debug("Send heartbeat");
            ActorRef pa = getContext().parent();
            HeartbeatMessage hbms = new HeartbeatMessage(
                    agentConfig.get().getHostIdentifier(), getContext().parent(), metricList.getStatuses());
            pa.tell( hbms, getSelf());
            // send and forget
            metricList = MetricStatusList.EMPTY;
        } else if (msg instanceof MetricStatusList) {
            metricList = (MetricStatusList) msg;
        } else {
            unhandled(msg);
        }
	}

    private void scheduleHeartbeats() {
        getContext().system().scheduler().schedule(
                Duration.Zero(),
                Duration.create(3, TimeUnit.SECONDS),
                getSelf(), new Tick(),
                getContext().system().dispatcher(), getSelf());
    }
}
