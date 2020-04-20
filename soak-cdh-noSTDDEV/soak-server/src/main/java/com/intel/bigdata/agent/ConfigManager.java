package com.intel.bigdata.agent;

import akka.actor.UntypedActor;
import akka.agent.Agent;
import com.intel.bigdata.common.protocol.AgentConfig;
import com.intel.bigdata.common.protocol.AgentRequest;
import com.intel.bigdata.common.protocol.AgentResponse;
import com.intel.bigdata.common.protocol.Payload;
import com.intel.soak.config.SoakConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXB;
import java.io.File;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: holm
 * Date: 10/23/13
 * Time: 12:06 PM
 * To change this template use File | Settings | File Templates.
 */
@Component("ConfigManager")
@Scope("prototype")
public class ConfigManager extends UntypedActor {

    protected static final Logger LOG = LoggerFactory.getLogger(ConfigManager.class);

    private static final String AGENT_CONFIG_PATH = SoakConfig.Dir.Conf.toString() + File.separator + "agent.xml";

    private Agent<AgentConfig> agentConfig;

    public static AgentConfig readAgentConfig() {
        try {
            return JAXB.unmarshal(new File(AGENT_CONFIG_PATH), AgentConfig.class);
        } catch (Exception e) {
            LOG.error("Couldn't read configuration from file: {}", e);
            return new AgentConfig(
                    "default","localhost");
        }
    }

    public static void writeAgentConfig(AgentConfig agentConfig) {
        try {
            JAXB.marshal(agentConfig, new File(AGENT_CONFIG_PATH));
        } catch (Exception e) {
            LOG.error("Couldn't write configuration to file: {}", e);
            // TODO: throw exception
        }
    }

    @Inject
    @Required
    public void setAgentConfig(@Named("agentConfig") Agent<AgentConfig> agentConfig) {
        this.agentConfig = agentConfig;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof AgentRequest) {
            AgentRequest request = (AgentRequest) message;
            Payload payload = request.getPayload();
            /*
            if (payload instanceof AgentConfig) {
                agentConfig.send((AgentConfig) payload);
                writeAgentConfig((AgentConfig) payload);
                getSender().tell(
                        new AgentResponse(request.getSessionId(),
                                ((AgentConfig) payload).getHostIdentifier(), payload), getSelf());
            } else {
                unhandled(message);
            } */
        } else {
            unhandled(message);
        }
    }

}
