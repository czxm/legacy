package com.intel.bigdata.agent.events;

import akka.actor.ActorRef;

/**
 * Created with IntelliJ IDEA.
 * User: holm
 * Date: 10/25/13
 * Time: 1:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegisterListener {

    private final ActorRef listener;

    public RegisterListener(ActorRef listener) {
        this.listener = listener;
    }

    public ActorRef getListener() {
        return listener;
    }
}
