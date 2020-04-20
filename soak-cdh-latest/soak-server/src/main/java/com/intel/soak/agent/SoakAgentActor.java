package com.intel.soak.agent;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import com.intel.bigdata.common.protocol.AgentRequest;
import com.intel.bigdata.common.protocol.AgentResponse;
import com.intel.bigdata.common.protocol.MasterRequest;
import com.intel.bigdata.common.protocol.Payload;
import com.intel.bigdata.master.requests.AppRequest;
import com.intel.soak.config.SoakConfig;
import com.intel.soak.model.LoadConfig;
import com.intel.soak.protocol.SoakRequest;
import com.intel.soak.utils.SpringBeanFactoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: xzhan27
 * Date: 12/9/13
 * Time: 1:28 AM
 */
@Component("soakAgentActor")
@Scope("prototype")
public class SoakAgentActor extends UntypedActor {

    private HashMap<String, AbstractAppSlave> apps = new HashMap<String, AbstractAppSlave>();

    @Autowired
    private SoakConfig soakConfig;

    private final ActorSelection agentDispatcher = context().actorSelection(context().parent().path());

    @Override
    public void preStart() throws Exception {
        super.preStart();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if(msg instanceof AgentRequest){
            AgentRequest request = (AgentRequest)msg;
            Payload payload = request.getPayload();
            if(payload instanceof SoakRequest){
                SoakRequest appRequest = (SoakRequest)payload;
                AbstractAppSlave app = null;
                if(apps.containsKey(appRequest.getJobId())){
                    app = apps.get(appRequest.getJobId());
                }
                else{
                    ApplicationContext appCxt = SpringBeanFactoryManager.getSystemAppCxt();
                    app = appCxt.getBean(soakConfig.getConfig(SoakConfig.ConfigKey.SoakSlave), AbstractAppSlave.class);
                    if(app != null){
                        app.setTarget(this.getSelf());
                        apps.put(appRequest.getJobId(), app);
                    }
                }
                AgentResponse response = new AgentResponse(request.getSessionId(), request.getNodeId(), app.onReceive(appRequest));
                getSender().tell(response, getSelf());

                if(appRequest.getType().equals(SoakRequest.RequestType.Finalize)){
                    apps.remove(appRequest.getJobId());
                }
            }
        }
        else if(msg instanceof AppRequest){
            AppRequest appRequest = (AppRequest)msg;
            MasterRequest request = new MasterRequest(appRequest.getId(), appRequest.getPayload());
            agentDispatcher.tell(request, getSender());
        }
        else{
            unhandled(msg);
        }
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
