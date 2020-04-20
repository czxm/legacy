package com.intel.cedar.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.intel.cedar.core.entities.AbstractHostInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.PhysicalNodeInfo;
import com.intel.cedar.engine.model.feature.Tasklet;
import com.intel.cedar.engine.model.feature.Tasklet.Sharable;

public class ComputeNode {
    private AbstractHostInfo host;
    private NodeCapacity capacity;
    private List<String> tasklets; // the running tasklet IDs which are using
                                   // this node
    private int used; // total allocation counts, to support LRU scheduling
    private boolean alive; // is this node currently available?
    private boolean occupied; // indicate that this node is assigned tasks

    
    public ComputeNode(AbstractHostInfo host, int cpu, int mem, boolean live) {
        this.host = host;
        capacity = new NodeCapacity();
        capacity.setCpu(cpu);
        capacity.setMem(mem);
        used = 0;
        tasklets = new ArrayList<String>();
        this.alive = live;
        this.occupied = false;
    }

    public AbstractHostInfo getHost() {
        return host;
    }

    public void setNode(AbstractHostInfo node) {
        this.host = node;
    }

    public NodeCapacity getCapacity() {
        return capacity;
    }

    public void setCapacity(NodeCapacity capacity) {
        this.capacity = capacity;
    }

    public boolean isOccupied() {
        return this.occupied;
    }

    public int getUsed() {
        return used;
    }

    protected boolean checkProperties(Properties props) {
        if (props.isEmpty())
            return true;
        Properties machineProps = host.getProperties();
        for (Object k : props.keySet()) {
            String value = (String) props.get(k);
            String key = (String) k;
            if (machineProps.getProperty(key) != null) {
                if (!value.equals("")
                        && !value.equals(machineProps.getProperty(key))) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    protected boolean capable(ResourceRequest request) {
        if (capacity.isCapable(request.getCpu(), request.getMem())) {
            if (!request.getOs().isAnyOS()
                    && !host.getOs().equals(request.getOs()))
                return false;
            if (!request.getArch().isAnyArch()
                    && !host.getArch().equals(request.getArch()))
                return false;
            return true;
        } else
            return false;
    }

    public boolean matchRequest(ResourceRequest request) {
        if (!isAlive())
            return false;
        /*
         * String requestedHost = request.getHost(); if(requestedHost != null &&
         * !requestedHost.equals("")){
         * if(!host.getAddress().equalsIgnoreCase(requestedHost) &&
         * !host.getHostName().equalsIgnoreCase(requestedHost)) return false; }
         */

        if (host instanceof PhysicalNodeInfo) {
            PhysicalNodeInfo p = (PhysicalNodeInfo) host;
            if (!p.getShared() && !p.getUserId().equals(request.getUserId()))
                return false;
            if (p.getDisk() < request.getDisk())
                return false;
            // physical nodes can only be recycled
            if (!request.getRecycle())
                return false;
        }
        
        // request needs a non-sharable or un-recyclable node, but this node is already allocated
        if ((request.getSharable().equals(Tasklet.Sharable.none) || !request.getRecycle())
                && this.occupied)
            return false;

        if (!capable(request))
            return false;
        
        if (host.getCapabilities().contains(request.getFeatureId())) {
            return true;
        }

        if (checkProperties(request.getProperties())) {
            return true;
        }

        return false;
    }

    public synchronized void attachTasklet(String taskletId) {
        this.tasklets.add(taskletId);
    }

    public synchronized void detachTasklet(String taskletId) {
        this.tasklets.remove(taskletId);
    }

    public synchronized void meetRequest(ResourceRequest request) {
        this.used++;
        this.occupied = true;
        this.capacity.decreaseCapacity(request.getCpu(), request.getMem());
        if (host instanceof PhysicalNodeInfo) {
            ((PhysicalNodeInfo) host).decreaseDiskSize(request.getDisk());
        }
        if(host instanceof InstanceInfo && request.getVisible() && !request.getRecycle() && request.getSharable().equals(Sharable.none)){
            ((InstanceInfo)host).setUserId(request.getUserId());
            ((InstanceInfo)host).saveChanges();
        }
    }

    public synchronized void releaseRequest(ResourceRequest request) {
        this.capacity.increaseCapacity(request.getCpu(), request.getMem());
        if (this.tasklets.size() == 0)
            this.occupied = false;
        if (host instanceof PhysicalNodeInfo) {
            if (!request.isReproducable())
                ((PhysicalNodeInfo) host).increaseDiskSize(request.getDisk());
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
