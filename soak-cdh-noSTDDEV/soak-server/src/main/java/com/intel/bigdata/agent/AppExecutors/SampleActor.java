package com.intel.bigdata.agent.AppExecutors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.agent.Agent;
import com.intel.bigdata.common.protocol.*;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/8/13
 * Time: 9:54 AM
 * To change this template use File | Settings | File Templates.
 */
@Component("SampleActor")
@Scope("prototype")
public class SampleActor extends UntypedActor {
    private Agent<AgentConfig> agentConfig;

    @Inject
    @Required
    public void setAgentConfig(@Named("agentConfig") Agent<AgentConfig> agentConfig) {
        this.agentConfig = agentConfig;
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof AgentRequest) {
            final AgentRequest agentRequest = (AgentRequest) msg;
            Object payload = agentRequest.getPayload();
            if (payload instanceof HelloAkkaMessage) {

                final HelloAkkaMessage pl = (HelloAkkaMessage) payload;
                if (pl.getId().equals("h1")) {

                    final ActorRef sender = getSender();
                    Thread t = new Thread(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                Thread.sleep(10 * 1000);
                                System.out.println(sender.path()+"#############");
                                sender.tell(new AgentResponse(agentRequest.getSessionId(), agentConfig.get().getHostIdentifier(),
                                        new IMAgentMessage("h1", "fetfewgew")), getSelf());
                            } catch (Exception e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                        }
                    });
                    t.start();

                } else {
                    String replyMsg = pl.getMessage() + "Reply: Hi, I AM AGENT Actor!";
                    IMAgentMessage immsg = new IMAgentMessage(pl.getId(), replyMsg);
                    getSender().tell(
                            new AgentResponse(agentRequest.getSessionId(), agentConfig.get().getHostIdentifier(),
                                    immsg), getSelf());
                }


            }
        }
    }
}
