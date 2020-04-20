package com.intel.soak.master;

import akka.actor.ActorRef;
import com.google.common.collect.ImmutableList;
import com.intel.bigdata.common.protocol.Payload;
import com.intel.bigdata.common.util.ActorUtil;
import com.intel.bigdata.master.nodes.notification.NodeListRequest;
import com.intel.bigdata.master.requests.AppRequest;
import com.intel.bigdata.master.requests.AppRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: xzhan27
 * Date: 12/5/13
 * Time: 2:45 PM
 */
public abstract class AbstractAppMaster {
    private Logger LOG = LoggerFactory.getLogger(AbstractAppMaster.class);

    private ActorRef target;

    public abstract Object onReceive(Payload request);

    public void setTarget(ActorRef target){
        this.target = target;
    }

    protected ActorRef getTarget(){
        return this.target;
    }

    protected <T>T sendRequest(ImmutableList<String> nodes, Payload request){
        try{
            AppRequest appRequest = new AppRequest(null, true, ImmutableList.copyOf(nodes),
                    FiniteDuration.apply(300, TimeUnit.SECONDS),
                    request, false);
            AppRequestStatus result = ActorUtil.ask(target, appRequest, null);
            return (T)result.getNode2result();
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    protected void postRequest(List<String> nodes, Payload request){
        AppRequest appRequest = new AppRequest(null, false, ImmutableList.copyOf(nodes),
                FiniteDuration.apply(300, TimeUnit.SECONDS),
                request, false);
        target.tell(appRequest, target);
    }

    protected List<String> getLiveNodes(){
        try{
            return (List<String>)ActorUtil.ask(target, new NodeListRequest(), null);
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return new ArrayList<String>();
    }
}
