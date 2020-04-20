package com.intel.bigdata.agent;

import akka.actor.UntypedActor;
import akka.agent.*;
import akka.agent.Agent;
import com.intel.bigdata.agent.AppExecutors.AppSequExecutor;
import com.intel.bigdata.agent.AppExecutors.ExecutorFactory;
import com.intel.bigdata.common.protocol.*;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/8/13
 * Time: 10:43 AM
 * To change this template use File | Settings | File Templates.
 */
@Component("SequAppExecutor")
@Scope("prototype")
public class SequAppExecutor extends UntypedActor {
    private akka.agent.Agent<AgentConfig> agentConfig;

    @Inject
    @Required
    public void setAgentConfig(@Named("agentConfig") Agent<AgentConfig> agentConfig) {
        this.agentConfig = agentConfig;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof AgentRequest) {
            final AgentRequest agentRequest = (AgentRequest) message;
            Object payload = agentRequest.getPayload();
            if (payload instanceof AppAction) {
                AppAction appPayload = (AppAction) payload;
                ExecutorFactory ef = new ExecutorFactory(appPayload);
                Class<?> targetClass = ef.getAppClass();
                Object appResult = null;
                try {
                    AppSequExecutor realExec = (AppSequExecutor) targetClass.newInstance();
                    appResult = realExec.executor(appPayload);
                } catch (InstantiationException e) {
                    appResult = null;
                } catch (IllegalAccessException e) {
                    appResult = null;
                }
                if (appResult == null) {
                    //TODO: appResult null handling
                } else {
                    getSender().tell(
                            new AgentResponse(agentRequest.getSessionId(), agentConfig.get().getHostIdentifier(),
                                    null/*appResult*/), getSelf());
                }

            }
        }
    }
}
