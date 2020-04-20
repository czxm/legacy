package com.intel.bigdata.agent.events;

import akka.actor.ActorRef;

/**
 * Created with IntelliJ IDEA.
 * User: holm
 * Date: 10/25/13
 * Time: 1:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class UnregisterListener {

    private final ActorRef listener;

    public UnregisterListener(ActorRef listener) {
        this.listener = listener;
    }

    public ActorRef getListener() {
        return listener;
    }
}
