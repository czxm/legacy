package com.intel.bigdata.apps;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intel.bigdata.common.protocol.AppAction;
import com.intel.bigdata.common.protocol.HelloAkkaMessage;
import com.intel.bigdata.common.protocol.IMAgentMessage;
import com.intel.bigdata.master.nodes.notification.NodesDiagnose;
import com.intel.bigdata.master.requests.AppRequest;
import com.intel.bigdata.master.requests.AppRequestStatus;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/3/13
 * Time: 10:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class SampleCase extends UntypedActor{

//    private final ActorSelection requestDispatcher = context().actorSelection(context().system().toString()+"/user/requestDispatcher");
    private final ActorRef requestDispatcher = context().system().actorFor(context().system().toString()+"/user/requestDispatcher");

    private AppRequest request;
    private final static String HOSTID = "default";

    @Override
    public void preStart() throws Exception {
        //TODO: get the request from pre-define
       request = new AppRequest("request1", true, ImmutableList.<String>of(HOSTID), FiniteDuration.apply(300, TimeUnit.SECONDS),
               new HelloAkkaMessage("h3", "Hello, agent!"), false);
        getContext().system().scheduler().scheduleOnce(
                Duration.create(10, TimeUnit.SECONDS),
                requestDispatcher, request,
                getContext().system().dispatcher(), getSelf());

    }


    @Override
    public void onReceive(Object message) throws Exception {
        AppRequestStatus msg = (AppRequestStatus) message;
        ImmutableMap<String, Object> result = msg.getNode2result();
        Object nodeResult = result.get(HOSTID);
        if (nodeResult instanceof IMAgentMessage) {
            IMAgentMessage imagent = (IMAgentMessage) nodeResult;
            System.out.println(imagent.getMessage());
            System.out.println("!!!!" + message);
            final ActorRef futureCase =
                    getContext().system().actorOf(Props.create(SampleForFuture.class), "sampleForFuture");
        }

    }
}
