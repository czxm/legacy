package com.intel.bigdata.master.nodes;

import akka.actor.ActorRef;

import java.io.Serializable;

public class NodeState implements Serializable {
	private final String id; 
	private final boolean running;
	private final ActorRef dispatcher;
    private long timestamp;

	public NodeState(String id, boolean running, long timestamp, ActorRef dispatcher) {
		this.id = id;
		this.running = running;
		this.dispatcher = dispatcher;
        this.timestamp = timestamp;
	}

	public NodeState(String id) {
		this(id, false, 0, null);
	}
	
	public String getId() {
		return id;
	}
	public boolean isRunning() {
		return running;
	}
	public ActorRef getDispatcher() {
		return dispatcher;
	}
    public long getTimestamp(){
        return this.timestamp;
    }
    public void updateTimeStamp(long timestamp){
        this.timestamp = timestamp;
    }
}
