package com.intel.soak.agent;

import akka.actor.ActorRef;
import com.google.common.collect.ImmutableList;
import com.intel.bigdata.common.protocol.Payload;
import com.intel.bigdata.common.util.ActorUtil;
import com.intel.bigdata.master.requests.AppRequest;
import com.intel.bigdata.master.requests.AppRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: xzhan27
 * Date: 12/5/13
 * Time: 2:45 PM
 */
public abstract class AbstractAppSlave {
    private static Logger LOG = LoggerFactory.getLogger(AbstractAppSlave.class);

    private ActorRef target;

    public abstract Object onReceive(Payload request);

    public void setTarget(ActorRef target){
        this.target = target;
    }

    public ActorRef getTarget(){
        return this.target;
    }

    public <T>T sendRequest(Payload request){
        try{
            AppRequest appRequest = new AppRequest(null, true, ImmutableList.<String>of(""),
                    FiniteDuration.apply(300, TimeUnit.SECONDS),
                    request, false);
            return (T)ActorUtil.ask(target, appRequest, null);
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public void postRequest(Payload request){
        AppRequest appRequest = new AppRequest(null, true, ImmutableList.<String>of(""),
                FiniteDuration.apply(300, TimeUnit.SECONDS),
                request, false);
        target.tell(appRequest, target);
    }
}
