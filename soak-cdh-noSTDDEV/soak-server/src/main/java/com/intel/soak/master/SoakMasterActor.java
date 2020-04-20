package com.intel.soak.master;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import com.intel.bigdata.common.protocol.MasterRequest;
import com.intel.bigdata.common.protocol.MasterResponse;
import com.intel.bigdata.common.protocol.Payload;
import com.intel.bigdata.common.util.ActorUtil;
import com.intel.bigdata.master.nodes.notification.NodeListRequest;
import com.intel.bigdata.master.requests.AppRequest;
import com.intel.soak.InMemSoakContainer;
import com.intel.soak.config.SoakConfig;
import com.intel.soak.protocol.SoakRequest;
import com.intel.soak.utils.SpringBeanFactoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: xzhan27
 * Date: 12/5/13
 * Time: 11:26 AM
 */
@Component("soakMasterActor")
@Scope("prototype")
public class SoakMasterActor extends UntypedActor {
    private Logger LOG = LoggerFactory.getLogger(SoakMasterActor.class);

    private ExecutorService executor = Executors.newCachedThreadPool();
    private InMemSoakContainer container;
    private final ActorSelection requestDispatcher = context().actorSelection(context().parent().path() + "/requestDispatcher");

    @Override
    public void onReceive(Object msg) throws Exception {
        if(msg instanceof AppRequest){
            requestDispatcher.tell(msg, getSender());
        }
        else if(msg instanceof NodeListRequest){
            List<String> nodes = ActorUtil.ask(requestDispatcher, new NodeListRequest(), null);
            getSender().tell(nodes, getSelf());
        }
        else if(msg instanceof MasterRequest){
            MasterRequest request = (MasterRequest)msg;
            Payload payload = request.getPayload();
            if(payload instanceof SoakRequest){
                SoakRequest appRequest = (SoakRequest)payload;
                AbstractAppMaster app = container.retrieve(appRequest.getJobId());
                if(app != null){
                    MasterResponse response = new MasterResponse(request.getSessionId(), app.onReceive(appRequest));
                    getSender().tell(response, getSelf());
                }
            }
        }
    }

    @Override
    public void preStart() throws Exception {
        container = SpringBeanFactoryManager.getSystemAppCxt().getBean(SoakConfig.Container, InMemSoakContainer.class);
        container.setAppMaster(this.self());
    }


    @Override
    public void postStop() throws Exception {
        super.postStop();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
