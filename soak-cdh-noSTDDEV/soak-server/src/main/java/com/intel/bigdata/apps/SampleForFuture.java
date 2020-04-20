package com.intel.bigdata.apps;


import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intel.bigdata.common.protocol.HelloAkkaMessage;
import com.intel.bigdata.common.protocol.IMAgentMessage;
import com.intel.bigdata.master.requests.AppRequest;
import com.intel.bigdata.master.requests.AppRequestStatus;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/7/13
 * Time: 2:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class SampleForFuture extends UntypedActor {

    private final ActorSelection requestDispatcher = context().system().actorSelection(context().system().toString()+"/user/requestDispatcher");

    private AppRequest request1;
    private AppRequest request2;

    private final static String HOSTID = "default";

    @Override
    public void preStart() throws Exception {
        //TODO: get the request from pre-define
        request1 = new AppRequest("request2", true, ImmutableList.<String>of(HOSTID), FiniteDuration.apply(300, TimeUnit.SECONDS),
                new HelloAkkaMessage("h1", "Hello, agent! I used Future!"), true);
        request2 = new AppRequest("request3", true, ImmutableList.<String>of(HOSTID), FiniteDuration.apply(300, TimeUnit.SECONDS),
                new HelloAkkaMessage("h2", "Hello, agent! I am future2!"), true);

        Future<Object> ft1 = Patterns.ask(requestDispatcher, request1, new Timeout(Duration.create(60, TimeUnit.SECONDS)));
        Future<Object> ft2 = Patterns.ask(requestDispatcher, request2, new Timeout(Duration.create(60, TimeUnit.SECONDS)));
        Patterns.pipe(ft1, getContext().system().dispatcher());
        Patterns.pipe(ft2, getContext().system().dispatcher());

        ft1.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object message) throws Throwable {
                AppRequestStatus msg = (AppRequestStatus)message;
                ImmutableMap<String, Object> result = msg.getNode2result();
                Object nodeResult = result.get(HOSTID);
                IMAgentMessage imagent = (IMAgentMessage) nodeResult;
                System.out.println("ft1" + imagent.getMessage()+imagent.getId());
                System.out.println("!!!!"+message);
            }
        }, getContext().system().dispatcher());
        ft2.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object message) throws Throwable {
                AppRequestStatus msg = (AppRequestStatus)message;
                ImmutableMap<String, Object> result = msg.getNode2result();
                Object nodeResult = result.get(HOSTID);
                IMAgentMessage imagent = (IMAgentMessage) nodeResult;
                System.out.println("ft2" + imagent.getMessage()+imagent.getId());
                System.out.println("@@@@"+message);
            }
        }, getContext().system().dispatcher());

    }

    @Override
    public void onReceive(Object message) throws Exception {

    }
}
