package com.intel.bigdata.common.util;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.dispatch.OnSuccess;
import akka.japi.Function2;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * provide actor related utility methods
 */
public class ActorUtil {

    public static ActorRef selectActor(UntypedActor actor, String actorPath) throws Exception {
        ActorSelection selection = actor.getContext().actorSelection(actorPath);
        Timeout timeout = Timeout.durationToTimeout(new FiniteDuration(300, TimeUnit.SECONDS));
        Future ft = selection.resolveOne(timeout);
        return (ActorRef) Await.result(ft, timeout.duration());
    }

    public static <T> T ask(ActorRef actorRef, Object message, Timeout timeout) throws Exception {
        if(timeout == null)
            timeout = Timeout.durationToTimeout(new FiniteDuration(300, TimeUnit.SECONDS));
        Future<Object> future = Patterns.ask(actorRef, message, timeout);
        return (T) Await.result(future, timeout.duration());
    }

    public static <T> T ask(ActorSelection selection, Object message, Timeout timeout) throws Exception {
        if(timeout == null)
            timeout = Timeout.durationToTimeout(new FiniteDuration(300, TimeUnit.SECONDS));
        Future<Object> future = Patterns.ask(selection, message, timeout);
        return (T) Await.result(future, timeout.duration());
    }

    public static <T> Future<List<T>> joinFutureList(ExecutionContext context, List<Future<T>> futures) {
        final Future<List<T>> transformed = Futures.fold(new ArrayList<T>(), futures, new Function2<List<T>, T, List<T>>() {
            public List<T> apply(List<T> r, T t) {
                r.add(t);
                return r;
            }
        }, context);
        return transformed;
    }
}
