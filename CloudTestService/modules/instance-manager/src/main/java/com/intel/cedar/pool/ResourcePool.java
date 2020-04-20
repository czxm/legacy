package com.intel.cedar.pool;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.cloud.CloudEucaReqTimeoutException;
import com.intel.cedar.cloud.CloudException;
import com.intel.cedar.core.entities.AbstractHostInfo;
import com.intel.cedar.core.entities.CloudInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.MachineInfo;
import com.intel.cedar.core.entities.MachineTypeInfo;
import com.intel.cedar.core.entities.PhysicalNodeInfo;
import com.intel.cedar.core.entities.VolumeInfo;
import com.intel.cedar.engine.model.feature.Tasklet.Sharable;
import com.intel.cedar.feature.util.FeatureUtil;
import com.intel.cedar.scheduler.CedarTimer;
import com.intel.cedar.scheduler.CedarTimerTask;
import com.intel.cedar.user.UserInfo;
import com.intel.cedar.user.util.UserUtil;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.cedar.util.CloudUtil;
import com.intel.cedar.util.EntityListener;
import com.intel.cedar.util.EntityNotifier;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.Hashes;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

public class ResourcePool {
    private static Logger LOG = LoggerFactory.getLogger(ResourcePool.class);
    private static ResourcePool singleton;

    public static synchronized ResourcePool getPool() {
        if (singleton == null)
            singleton = new ResourcePool();
        return singleton;
    }

    class Request {
        Object token;
        Long cloudId;
        int request;

        Request(Object t, int r, Long cloudId) {
            this.token = t;
            this.request = r;
            this.cloudId = cloudId;
        }
    }

    class VMRequest extends Request {
        MachineTypeInfo mi;

        VMRequest(Object t, int r, Long cloudId, MachineTypeInfo mi) {
            super(t, r, cloudId);
            this.mi = mi;
        }
    }

    class VolumeRequest extends Request {
        VolumeRequest(Object t, int r, Long cloudId) {
            super(t, r, cloudId);
        }
    }

    private ConcurrentLinkedQueue<VMRequest> standbyRequests;// requested VM to for later usage
    
    private ConcurrentLinkedQueue<VMRequest> vmRequests;// requested VM to avoid
                                                        // dead lock
    private ConcurrentLinkedQueue<VolumeRequest> volumeRequests;// requested
                                                                // volumes to
                                                                // avoid dead
                                                                // lock
    private ConcurrentLinkedQueue<ComputeNode> standbyNodes; // standby computing nodes
    private ConcurrentLinkedQueue<ComputeNode> nodes; // pooled computing nodes
    private ConcurrentLinkedQueue<VolumeInfo> volumes; // pooled volumes
    private ConcurrentHashMap<String, HashMap<String, Integer>> nodeNumbers;
    private ConcurrentHashMap<String, Integer> totalVolumes;
    
    private boolean shutdown;
    private Thread standbyMonitorThread;
    
    private ResourcePool() {
        standbyRequests = new ConcurrentLinkedQueue<VMRequest>();
        vmRequests = new ConcurrentLinkedQueue<VMRequest>();
        volumeRequests = new ConcurrentLinkedQueue<VolumeRequest>();
        volumes = new ConcurrentLinkedQueue<VolumeInfo>();
        nodes = new ConcurrentLinkedQueue<ComputeNode>();
        standbyNodes = new ConcurrentLinkedQueue<ComputeNode>();
        nodeNumbers = new ConcurrentHashMap<String, HashMap<String, Integer>>();
        totalVolumes = new ConcurrentHashMap<String, Integer>();

        EntityNotifier.getInstance().addListener(InstanceInfo.class,
                new EntityListener<InstanceInfo>() {
                    @Override
                    public void entityAdded(InstanceInfo e) {
                    }

                    @Override
                    public void entityDeleted(InstanceInfo e) {
                        if (e.getPooled()) {
                            ComputeNode node = findNode(e);
                            removeComputeNode(node, e.getStandby());
                        }
                    }

                    @Override
                    public void entityUpdated(InstanceInfo e) {
                    }
                });

        EntityNotifier.getInstance().addListener(PhysicalNodeInfo.class,
                new EntityListener<PhysicalNodeInfo>() {
                    @Override
                    public void entityAdded(PhysicalNodeInfo e) {
                        if (e.getPooled()) {
                            ComputeNode n = findNode(e);
                            if (n == null) {
                                addComputeNode(new ComputeNode(e, e.getCpu(), e
                                        .getMemory(), e.testConnection(false)));
                            }
                        }
                    }

                    @Override
                    public void entityDeleted(PhysicalNodeInfo e) {
                        if (e.getPooled()) {
                            ComputeNode n = findNode(e);
                            removeComputeNode(n);
                        }
                    }

                    @Override
                    public void entityUpdated(PhysicalNodeInfo e) {
                        if (e.getPooled()) {
                            ComputeNode n = findNode(e);
                            if (n == null) {
                                addComputeNode(new ComputeNode(e, e.getCpu(), e
                                        .getMemory(), e.testConnection(false)));
                            }
                        } else {
                            ComputeNode n = findNode(e);
                            removeComputeNode(n);
                        }
                    }
                });

        EntityNotifier.getInstance().addListener(VolumeInfo.class,
                new EntityListener<VolumeInfo>() {
                    @Override
                    public void entityAdded(VolumeInfo e) {
                    }

                    @Override
                    public void entityDeleted(VolumeInfo e) {
                        if (e.getPooled() && e.isCloudVolume()) {
                            VolumeInfo v = findVolume(e);
                            removeVolume(v);
                        }
                    }

                    @Override
                    public void entityUpdated(VolumeInfo e) {
                    }
                });
        
        standbyMonitorThread = new Thread("Standby Nodes Monitor"){
            @Override
            public void run() {
                while(!shutdown){
                    int maxStandy = CedarConfiguration.getInstance()
                    .getMaximumStandbyInstances();
                    int newStandby = 0;
                    while(!standbyRequests.isEmpty() && standbyNodes.size() < maxStandy){
                        VMRequest req = standbyRequests.poll();
                        if (req != null) {
                            try {
                                for (InstanceInfo i : allocateStandbyInstances(
                                        (MachineInfo)req.token, req.mi, 1, 60)) {
                                    newStandby++;
                                    i.setStandy(true);
                                    i.saveChanges();
                                    ComputeNode node = new ComputeNode(i, req.mi.getCpu(), req.mi.getMemory() / 1024, true);
                                    addComputeNode(node, true);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    standbyRequests.clear();
                    if(newStandby > 0)
                        LOG.info("Created {} instances as standby nodes", new Object[] {newStandby});
                    try{
                        Thread.sleep(300 * 1000);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
           }
        };
    }

    public void start() {
        // load physical nodes to computing pool
        int count = 0;
        for (PhysicalNodeInfo h : EntityUtil.listPhysicalNodes()) {
            if (h.getPooled()) {
                addComputeNode(new ComputeNode(h, h.getCpu(), h.getMemory(), h
                        .testConnection(false)));
                count++;
            }
        }
        LOG.info("Collected {} physical nodes for resource pool", count);
        count = 0;
        // load virtual nodes to computing pool
        for (InstanceInfo h : EntityUtil.listInstances(null)) {
            if (h.getPooled()) {
                MachineTypeInfo type = h.getMachineTypeInfo();
                addComputeNode(new ComputeNode(h, type.getCpu(), type
                        .getMemory() / 1024, h.testConnection(false)), h.getStandby());
                count++;
            }
        }
        LOG.info("Collected {} virtual machines for resource pool", count);
        count = 0;
        // load volumes to storage pool
        for (VolumeInfo v : EntityUtil.listVolumes(null)) {
            if (v.getPooled() && v.isCloudVolume() && v.getAttached() == null) {
                addVolume(v);
                count += v.getSize();
            }
        }
        LOG.info("Collected {}G volumes for resource pool", count);
        
        standbyMonitorThread.start();

        CedarTimer.getInstance().scheduleTask(30,
                new CedarTimerTask("Pool Monitor") {
                    private long tsc = System.currentTimeMillis();

                    @Override
                    public void run() {
                        long curTSC = System.currentTimeMillis();
                        int minAvail = CedarConfiguration.getInstance()
                                .getMinimumAvailable();
                        int maxVolumes = CedarConfiguration.getInstance()
                                .getMaximumVolumePool();
                        
                        for (ComputeNode n : nodes) {
                           final AbstractHostInfo h = n.getHost();
                           if(h instanceof PhysicalNodeInfo)
                               ((PhysicalNodeInfo)h).refresh();
                           else if(h instanceof InstanceInfo)
                               ((InstanceInfo)h).refresh();
                           n.setAlive(h.testConnection(false));
                           CloudUtil.asyncExec(new Runnable() {
                               @Override
                               public void run() {
                                   h.syncDateTime();
                               }
                           });
                        }
                        
                        for (ComputeNode n : standbyNodes) {
                            final AbstractHostInfo h = n.getHost();
                            if(h instanceof PhysicalNodeInfo)
                                ((PhysicalNodeInfo)h).refresh();
                            else if(h instanceof InstanceInfo)
                                ((InstanceInfo)h).refresh();
                            n.setAlive(h.testConnection(false));
                            CloudUtil.asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    h.syncDateTime();
                                }
                            });
                        }

                        synchronized (ResourcePool.this.nodes) {
                            for (CloudInfo cloud : EntityUtil.listClouds()) {
                                float ratio = cloud.getPoolRatio();
                                String cloudName = cloud.getName();
                                HashMap<String, Integer> numbers = nodeNumbers
                                        .get(cloudName);
                                if (numbers == null || numbers.isEmpty())
                                    continue;
                                for (MachineTypeInfo t : cloud
                                        .getMachineTypes(false)) {
                                    if (t.getMax() <= 0)
                                        continue;
                                    Integer num = numbers.get(t.getType());
                                    if (num != null) {
                                        boolean exceedRatio = false;
                                        int toKill = 0;
                                        if (t.getFree() >= 0
                                                && t.getFree() < minAvail) {
                                            toKill = minAvail - t.getFree();
                                        }
                                        if (num > Math.round((ratio * t
                                                .getMax()))) {
                                            int toKill2 = num
                                                    - Math.round(ratio
                                                            * t.getMax());
                                            if (toKill2 > toKill)
                                                toKill = toKill2;
                                            exceedRatio = true;
                                        }

                                        if (toKill > 0) {
                                            int count = 0;
                                            List<InstanceInfo> candidates = getLRUInstances();
                                            for (InstanceInfo i : candidates) {
                                                if (i.getTypeId().equals(
                                                        t.getId())
                                                        && i
                                                                .getCloudId()
                                                                .equals(
                                                                        cloud
                                                                                .getId())) {
                                                    CloudUtil
                                                            .terminateInstance(i);
                                                    toKill--;
                                                    count++;
                                                    if (toKill == 0)
                                                        break;
                                                }
                                            }
                                            if (count > 0) {
                                                if (exceedRatio) {
                                                    LOG
                                                            .info(
                                                                    "Released {} instances of {} as {} exceeds ratio({})*{}",
                                                                    new Object[] {
                                                                            count,
                                                                            t
                                                                                    .getType(),
                                                                            num,
                                                                            ratio,
                                                                            t
                                                                                    .getMax() });
                                                } else {
                                                    LOG
                                                            .info(
                                                                    "Released {} instances of {} for at lease {} available instances",
                                                                    new Object[] {
                                                                            count,
                                                                            t
                                                                                    .getType(),
                                                                            minAvail });
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if ((curTSC - tsc) > 300 * 1000) {
                                tsc = curTSC;
                                VMRequest req = vmRequests.poll();
                                if (req != null) {
                                    int toKill = req.request;
                                    if (toKill > 0) {
                                        boolean killed = false;
                                        List<InstanceInfo> candidates = getLRUInstances();
                                        for (InstanceInfo i : candidates) {
                                            if (i != null && i.getCloudId().equals(
                                                    req.cloudId)
                                                    && i.getMachineTypeInfo()
                                                            .compareTo(req.mi) >= 0) {
                                                CloudUtil.terminateInstance(i);
                                                killed = true;
                                                toKill--;
                                                if (toKill == 0)
                                                    break;
                                            }
                                        }
                                        if(!killed && candidates.size() > 0){
                                            CloudUtil.terminateInstance(candidates.get(0));
                                            toKill--;
                                        }
                                    }
                                    if (req.request - toKill > 0)
                                        LOG
                                                .info(
                                                        "Released {} instances to avoid dead lock",
                                                        new Object[] { req.request
                                                                - toKill });
                                }
                            }                                                        
                        }

                        synchronized (ResourcePool.this.volumes) {
                            for (VolumeInfo v : getSortedVolumes()) {
                                String cloudName = v.getCloudInfo().getName();
                                Integer totalSize = totalVolumes.get(cloudName);
                                if (totalSize != null && totalSize > maxVolumes) {
                                    try {
                                        CloudUtil.deleteVolume(v);
                                    } catch (Exception e) {
                                    }
                                }
                            }

                            if ((curTSC - tsc) > 300 * 1000) {
                                VolumeRequest vreq = volumeRequests.poll();
                                if (vreq != null) {
                                    int count = 0;
                                    int toKill = vreq.request;
                                    if (toKill > 0) {
                                        for (VolumeInfo i : getSortedVolumes()) {
                                            if (i.getCloudId().equals(
                                                    vreq.cloudId)) {
                                                try {
                                                    CloudUtil.deleteVolume(i);
                                                } catch (Exception e) {
                                                }
                                                count++;
                                                toKill = toKill - i.getSize();
                                                if (toKill <= 0)
                                                    break;
                                            }
                                        }
                                    }
                                    if (count > 0)
                                        LOG
                                                .info(
                                                        "Released {} volumes to avoid dead lock",
                                                        new Object[] { count });
                                }
                            }
                        }
                    }
                });
    }

    public void stop(){
        this.shutdown = true;
        standbyMonitorThread.interrupt();
        try{
            standbyMonitorThread.join(5000);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private List<VolumeInfo> getSortedVolumes() {
        ArrayList<VolumeInfo> result = new ArrayList<VolumeInfo>();
        for (VolumeInfo v : volumes) {
            result.add(v);
        }
        if (result.size() > 0) {
            Collections.sort(result, new Comparator<VolumeInfo>() {
                @Override
                public int compare(VolumeInfo o1, VolumeInfo o2) {
                    return o1.getSize() - o2.getSize();
                }
            });
        }
        return result;
    }

    private List<InstanceInfo> getLRUInstances(){
        ArrayList<InstanceInfo> result = new ArrayList<InstanceInfo>();
        result.addAll(getLRUInstances(nodes));
        result.addAll(getLRUInstances(standbyNodes));
        return result;
    }
    
    private List<InstanceInfo> getLRUInstances(ConcurrentLinkedQueue<ComputeNode> nodes) {
        ArrayList<InstanceInfo> result = new ArrayList<InstanceInfo>();
        ArrayList<ComputeNode> mNodes = new ArrayList<ComputeNode>();
        ArrayList<ComputeNode> cNodes = new ArrayList<ComputeNode>();
        for (ComputeNode n : nodes) {
            if (n.getHost() instanceof InstanceInfo && !n.isOccupied()) {
                cNodes.add(n);
            }
        }
        if (cNodes.size() > 0) {
            Collections.sort(cNodes, new Comparator<ComputeNode>() {
                public int compare(ComputeNode o1, ComputeNode o2) {
                    return o1.getUsed() - o2.getUsed();
                }
            });
            for (ComputeNode n : cNodes) {
                if (mNodes.size() == 0) {
                    mNodes.add(n);
                    continue;
                }
                int i = 0;
                while (i < mNodes.size()) {
                    ComputeNode m = mNodes.get(i);
                    if (m.getUsed() == n.getUsed()
                            && ((InstanceInfo) m.getHost()).getCreationTime() <= ((InstanceInfo) n
                                    .getHost()).getCreationTime()) {
                        break;
                    }
                    i++;
                }
                if (i == mNodes.size())
                    mNodes.add(n);
                else
                    mNodes.add(i + 1, n);
            }
            for (ComputeNode m : mNodes) {
                result.add((InstanceInfo) m.getHost());
            }
        }
        return result;
    }
    
    private List<ComputeNode> getSortedNodes() {
        ArrayList<ComputeNode> mNodes = new ArrayList<ComputeNode>();
        ArrayList<ComputeNode> cNodes = new ArrayList<ComputeNode>();
        for (ComputeNode n : nodes) {
            cNodes.add(n);
        }
        if (cNodes.size() > 0) {
            Collections.sort(cNodes, new Comparator<ComputeNode>() {
                public int compare(ComputeNode o1, ComputeNode o2) {
                    return o1.getUsed() - o2.getUsed();
                }
            });
            for (ComputeNode n : cNodes) {
                if (mNodes.size() == 0) {
                    mNodes.add(n);
                    continue;
                }
                int i = 0;
                while (i < mNodes.size()) {
                    ComputeNode m = mNodes.get(i);
                    if (m.getUsed() == n.getUsed()){
                        break;
                    }
                    i++;
                }
                if (i == mNodes.size())
                    mNodes.add(n);
                else
                    mNodes.add(i + 1, n);
            }
        }
        return mNodes;
    }

    private ComputeNode findNode(PhysicalNodeInfo i) {
        for (ComputeNode n : nodes) {
            if (n.getHost() instanceof PhysicalNodeInfo) {
                PhysicalNodeInfo node = (PhysicalNodeInfo) n.getHost();
                if (node.getId().equals(i.getId()))
                    return n;
            }
        }
        return null;
    }

    private ComputeNode findNode(InstanceInfo i) {
        for (ComputeNode n : nodes) {
            if (n.getHost() instanceof InstanceInfo) {
                InstanceInfo node = (InstanceInfo) n.getHost();
                if (node.getId().equals(i.getId()))
                    return n;
            }
        }
        for (ComputeNode n : standbyNodes) {
            if (n.getHost() instanceof InstanceInfo) {
                InstanceInfo node = (InstanceInfo) n.getHost();
                if (node.getId().equals(i.getId()))
                    return n;
            }
        }
        return null;
    }

    private VolumeInfo findVolume(VolumeInfo v) {
        for (VolumeInfo n : volumes) {
            if (n.getCloudId().equals(v.getCloudId())
                    && n.getId().equals(v.getId())) {
                return n;
            }
        }
        return null;
    }

    protected void addComputeNode(ComputeNode node){
        addComputeNode(node, false);
    }
    
    protected void addComputeNode(ComputeNode node, boolean standby) {
        if(standby){
            if (node != null && !this.standbyNodes.contains(node)) {
                this.standbyNodes.add(node);
                LOG.info("Successfully added standby node: " + node.getHost().getHost());
            }
        }
        else{
            if (node != null && !this.nodes.contains(node)) {
                this.nodes.add(node);
                LOG.info("Successfully added node: " + node.getHost().getHost());
                if (node.getHost() instanceof InstanceInfo) {
                    InstanceInfo instance = (InstanceInfo) node.getHost();
                    String cloudName = instance.getCloudInfo().getName();
                    String typeName = MachineTypeInfo.load(instance.getTypeId())
                            .getType();
                    HashMap<String, Integer> numbers = nodeNumbers.get(cloudName);
                    if (numbers == null) {
                        numbers = new HashMap<String, Integer>();
                        numbers.put(typeName, 1);
                        nodeNumbers.put(cloudName, numbers);
                    } else {
                        Integer num = numbers.get(typeName);
                        if (num == null) {
                            num = 1;
                        } else {
                            num = num + 1;
                        }
                        numbers.put(typeName, num);
                    }
                }
            }
        }

    }

    protected void removeComputeNode(ComputeNode node) {
        removeComputeNode(node, false);
    }
    
    protected void removeComputeNode(ComputeNode node, boolean standby) {
        if(standby){
            if (node != null && this.standbyNodes.contains(node)) {
                this.standbyNodes.remove(node);
                LOG.info("Successfully removed standby node: " + node.getHost().getHost());
            }
        }
        else{
            if (node != null && this.nodes.contains(node)) {
                this.nodes.remove(node);
                LOG.info("Successfully removed node: " + node.getHost().getHost());
                if (node.getHost() instanceof InstanceInfo) {
                    InstanceInfo instance = (InstanceInfo) node.getHost();
                    String cloudName = instance.getCloudInfo().getName();
                    String typeName = MachineTypeInfo.load(instance.getTypeId())
                            .getType();
                    HashMap<String, Integer> numbers = nodeNumbers.get(cloudName);
                    if (numbers != null) {
                        Integer num = numbers.get(typeName);
                        if (num != null) {
                            num = num - 1;
                            numbers.put(typeName, num);
                            return;
                        }
                    }
                    LOG.error("Instance is not registered in the pool");
                }
            }
        }
    }

    protected void addVolume(VolumeInfo volume) {
        this.volumes.add(volume);
        String cloudName = volume.getCloudInfo().getName();
        Integer totalSize = totalVolumes.get(cloudName);
        if (totalSize == null) {
            totalSize = 0;
        }
        totalVolumes.put(cloudName, totalSize + volume.getSize());
        LOG.info("Successfully added volume: " + volume.getImageId());
    }

    protected void removeVolume(VolumeInfo volume) {
        if (volume != null) {
            for (VolumeInfo v : volumes) {
                if (v.getCloudId().equals(volume.getCloudId())
                        && v.getImageId().equals(volume.getImageId())) {
                    volumes.remove(v);
                    String cloudName = volume.getCloudInfo().getName();
                    Integer totalSize = totalVolumes.get(cloudName);
                    if (totalSize == null) {
                        LOG.info("volume is not registered in the pool");
                    }
                    totalVolumes.put(cloudName, totalSize - v.getSize());
                    LOG.info("Successfully removed volume: " + v.getImageId());
                    break;
                }
            }
        }
    }

    public static Resource allocateResourceTest(ResourceRequest request)
            throws InterruptedException {
        int count = request.getCount();
        Resource r = new Resource(request);
        for (int i = 0; i < count; i++) {
            PhysicalNodeInfo instance = new PhysicalNodeInfo();
            instance.setHost("localhost");
            ComputeNode n = new ComputeNode(instance, 1, 1, true);
            VolumeInfo v = new VolumeInfo();
            v.setImageId("testimage");
            v.setPath(Hashes.generateId("V", "D"));
            ResourceItem item = new ResourceItem(n);
            item.setVolume(v);
            r.addResourceItem(item);
        }
        return r;
    }

    public Resource allocateResource(ResourceRequest request, int timeout)
            throws ResourceRequestException {
        Resource r = new Resource(request);
        try {
            internalAllocateResource(request, r, timeout);
            return r;
        } catch (Exception e) {
            // user cancelled
            releaseResource(r);
            throw new ResourceRequestException(e.getMessage(), e);
        }
    }

    private void internalAllocateResource(ResourceRequest request, Resource r,
            int timeout) throws ResourceRequestException, InterruptedException {
        long beforeAllocateTSP;
        int elasped = 0;
        int count = request.getCount();
        UserInfo user = UserUtil.getUserById(request.getUserId());
        LOG
                .info(
                        "{} requested {} {}({}) machine(s) for feature {} with {} CPU(s),{}G MEM and {}G DISK",
                        new Object[] {
                                user != null ? user.getUser() : "N/A",
                                count,
                                request.getOs().getOSName(),
                                request.getArch().toString(),
                                FeatureUtil.getFeatureInfoById(
                                        request.getFeatureId()).getName(),
                                request.getCpu(), request.getMem(),
                                request.getDisk() });
        if (request.getProperties().keySet().size() > 0) {
            LOG.info("--- with below properties");
            for (Object key : request.getProperties().keySet()) {
                LOG.info("--- {} = {}", key, request.getProperties().get(key));
            }
        }
        while (r.getResourceCount() < count) {
            int pooledCount = 0;
            if(request.getRecycle()){
                synchronized (this.nodes) {
                    while (r.getResourceCount() < count) {
                        boolean changed = false;
                        for (ComputeNode node : getSortedNodes()) {
                            // don't allocate same nodes to the request, this is to
                            // maximum parallelism
                            boolean nodeAllocated = false;
                            for (ResourceItem item : r.getResources()) {
                                if (item.getNode() == node && node.getHost() instanceof InstanceInfo) {
                                    nodeAllocated = true;
                                    break;
                                }
                            }
                            if (nodeAllocated)
                                continue;
    
                            if (node.matchRequest(request)) {
                                node.meetRequest(request);
                                ResourceItem item = new ResourceItem(node);
                                r.addResourceItem(item);
                                pooledCount++;
                                changed = true;
                                if (count == r.getResourceCount())
                                    break;
                            }
                        }
                        if (!changed)
                            break;
                    }
                }            
                if(pooledCount > 0)
                    LOG.info("Allocated {} ComputeNodes from pool", pooledCount);
            }
            
            int needToAllocate = count - r.getResourceCount();
            if(needToAllocate > 0){
                synchronized (this.standbyNodes) {
                    while (r.getResourceCount() < count) {
                        boolean changed = false;
                        for (ComputeNode node : standbyNodes) {
                            // don't allocate same nodes to the request, this is to
                            // maximum parallelism
                            boolean nodeAllocated = false;
                            for (ResourceItem item : r.getResources()) {
                                if (item.getNode() == node) {
                                    nodeAllocated = true;
                                    break;
                                }
                            }
                            if (nodeAllocated)
                                continue;

                            if (node.matchRequest(request)) {
                                node.meetRequest(request);
                                ResourceItem item = new ResourceItem(node);
                                r.addResourceItem(item);
                                pooledCount++;
                                changed = true;
                                if (count == r.getResourceCount())
                                    break;
                            }
                        }
                        if (!changed)
                            break;
                    }
                }
                if(pooledCount > 0)
                    LOG.info("Allocated {} standby ComputeNodes from pool", pooledCount);
            }
            
            needToAllocate = count - r.getResourceCount();

            beforeAllocateTSP = System.currentTimeMillis();
            if (needToAllocate > 0) {
                for (MachineInfo machine : EntityUtil.listMachines(null)) {
                    if (!machine.getManaged())
                        continue;
                    if (checkMachineInfo(machine, request.getProperties(),
                            request.getFeatureId())) {
                        if (checkMachineInfo(machine, request.getOs(), request
                                .getArch())) {
                            for (MachineTypeInfo type : machine.getCloudInfo()
                                    .getMachineTypes(true)) {
                                if (checkMachineType(type, request.getCpu(),
                                        request.getMem())) {
                                    for (InstanceInfo i : allocateInstances(
                                            machine, type, needToAllocate, 500)) {
                                        if(!request.getRecycle()){
                                            i.setStandy(true);
                                            i.saveChanges();
                                        }
                                        ComputeNode node = new ComputeNode(i,
                                                type.getCpu(),
                                                type.getMemory() / 1024, true);
                                        node.meetRequest(request);
                                        ResourceItem item = new ResourceItem(
                                                node);
                                        r.addResourceItem(item);
                                    }
                                    // no need to try more powerful types
                                    needToAllocate = count - r.getResourceCount();
                                    break;
                                }
                            }
                        }
                    }
                    if (needToAllocate == 0)
                        break;
                }
            }
            if (needToAllocate == 0)
                break;
            Thread.sleep(1000);
            elasped += (System.currentTimeMillis() - beforeAllocateTSP) / 1000;
            if (elasped > timeout && timeout > 0) {
                throw new ResourceRequestException("timeout");
            }
        }

        for (ResourceItem item : r.getResources()) {
            AbstractHostInfo h = item.getNode().getHost();
            VolumeInfo v = null;
            while (v == null) {
                beforeAllocateTSP = System.currentTimeMillis();
                if (request.getDisk() > 0
                        && h instanceof InstanceInfo
                        && (request.isReproducable() || h.getLocalVolumes()
                                .size() >= 1)) {
                    v = allocateVolume((InstanceInfo) h, request.getDisk());
                } else {
                    v = h.createVolume(request.getDisk());
                }
                if (v != null) {
                    v
                            .setHeld(request.getDisk() > 0
                                    && request.isReproducable() ? true : false);
                    v.setPooled(request.isReproducable() ? false : true);
                    v.saveChanges();
                    item.setVolume(v);
                    break;
                } else {
                    Thread.sleep(1000);
                    elasped += (System.currentTimeMillis() - beforeAllocateTSP) / 1000;
                    if (elasped > timeout && timeout > 0) {
                        throw new ResourceRequestException("timeout");
                    }
                }
            }
        }
    }

    public void releaseResource(Resource resource) {
        if (resource == null)
            return;
        int count = 0;
        synchronized (ResourcePool.class) {
            for (ResourceItem item : resource.getResources()) {
                ComputeNode node = item.getNode();
                if (node.getHost().isValid()) {
                    VolumeInfo v = item.getVolume();
                    // refresh from DB, because monitor thread maybe changed its
                    // status due to external actions
                    if (v != null && v.isValid()) {
                        if (v.isCloudVolume()) {
                            try {
                                if (v.getHeld()) {
                                    v.setUserId(resource.getRequest()
                                            .getUserId());
                                }
                                CloudUtil.detachVolume(v);
                                if (v.getPooled())
                                    addVolume(v);
                            } catch (Exception e) {
                                LOG.info("", e);
                            }
                        } else {
                            if (!v.getHeld() && resource.getRequest().getRecycle())
                                node.getHost().deleteVolume(v);
                        }
                    }
                    if(node.getHost() instanceof InstanceInfo){
                        InstanceInfo instance = (InstanceInfo)node.getHost();
                        if(instance.getStandby()){
                            MachineInfo mi = instance.getMachineInfo();
                            MachineTypeInfo mti = instance.getMachineTypeInfo();
                            if(resource.getRequest().getRecycle()){
                                instance.setStandy(false);
                                instance.saveChanges();
                                removeComputeNode(node, true);
                                addComputeNode(node);
                                count++;
                            }
                            else{
                                CloudUtil.terminateInstance(instance);
                            }
                            newStandbyRequest(1, mi, mti); 
                        }
                        else{
                            addComputeNode(node);
                            count++;
                        }
                    }
                    else{
                        addComputeNode(node);
                        count++;
                    }
                }
                node.releaseRequest(resource.getRequest());
            }
        }
        LOG.info("Released {} ComputeNodes to pool", count);
    }

    private VolumeRequest findVolumeRequest(Object t) {
        for (VolumeRequest r : volumeRequests) {
            if (r.token == t)
                return r;
        }
        return null;
    }

    private void newVolumeRequest(Object t, int num, Long cloudId) {
        VolumeRequest r = findVolumeRequest(t);
        if (r == null)
            volumeRequests.add(new VolumeRequest(t, num, cloudId));
    }

    private void deleteVolumeRequest(Object t) {
        while (true) {
            VolumeRequest r = findVolumeRequest(t);
            if (r != null)
                volumeRequests.remove(r);
            else
                break;
        }
    }

    private VolumeInfo allocateVolume(InstanceInfo i, int size)
            throws InterruptedException {
        VolumeInfo vi = null;
        LOG.info("Requesting volume sized {}G", size);
        synchronized (this.volumes) {
            for (VolumeInfo v : getSortedVolumes()) {
                if (v.getSize() >= size
                        && v.getCloudId().equals(i.getCloudId()) && v.isValid()) {
                    vi = v;
                    break;
                }
            }
            volumes.remove(vi);
        }
        int wait = 0;
        while (true) {
            try {
                if (vi == null) {
                    vi = CloudUtil.createVolume(i.getCloudInfo(), Hashes
                            .generateId("vol", "V"), size, UserUtil.getAdmin()
                            .getId());
                }
                if (vi != null && CloudUtil.formatVolume(i, vi)) {
                    vi = VolumeInfo.load(vi.getId());
                    break;
                }
                if (vi != null) {
                    CloudUtil.deleteVolume(vi);
                }
                vi = null;
            } catch (Exception e) {
                LOG.info("", e);
                if (e.getCause() instanceof InterruptedException)
                    throw (InterruptedException) e.getCause();
            }
            LOG.info("Requested volume sized {}G allocate failed", size);
            if (wait % 15 == 0) {
                newVolumeRequest(Thread.currentThread(), size, i.getCloudId());
            }
            wait++;
            TimeUnit.MINUTES.sleep(1);
        }
        if (vi != null)
            deleteVolumeRequest(Thread.currentThread());
        return vi;
    }

    private VMRequest findRequest(Object t) {
        for (VMRequest r : vmRequests) {
            if (r.token == t)
                return r;
        }
        return null;
    }

    private void newVMRequest(Object t, int num, Long cloudId,
            MachineTypeInfo mi) {
        VMRequest r = findRequest(t);
        if (r == null)
            vmRequests.add(new VMRequest(t, num, cloudId, mi));
    }

    private void deleteVMRequest(Object t) {
        while (true) {
            VMRequest r = findRequest(t);
            if (r != null)
                vmRequests.remove(r);
            else
                break;
        }
    }

    private void newStandbyRequest(int num, MachineInfo m, MachineTypeInfo mi) {
        standbyRequests.add(new VMRequest(m, num, m.getCloudId(), mi));
    }
    
    private List<InstanceInfo> allocateInstances(MachineInfo machine,
            MachineTypeInfo type, int count, int timeout)
            throws ResourceRequestException, InterruptedException {
        List<InstanceInfo> result = new ArrayList<InstanceInfo>();
        int wait = 0;
        while ((wait * 60) < timeout) {
            try {
                List<InstanceInfo> instances = CloudUtil.startInstances(machine
                        .getCloudInfo(), machine, type, count, UserUtil
                        .getAdmin().getId(), true);
                if(instances.size() > 0){
                    result.addAll(instances);
                    break;
                }
            } catch (CloudException e) {
                if (e.getCause() instanceof InterruptedException
                        || e instanceof CloudEucaReqTimeoutException) {
                    // this is unfriendly exception,
                    // just cancel this task to avoid issuing more of this
                    throw new InterruptedException();
                }
                // resource exhausted
                LOG.info(e.getMessage());
            }
            if (wait % 5 == 0) {
                newVMRequest(Thread.currentThread(), count, machine
                        .getCloudId(), type);
            }
            wait++;
            TimeUnit.MINUTES.sleep(1);
        }
        deleteVMRequest(Thread.currentThread());
        return result;
    }
    
    private List<InstanceInfo> allocateStandbyInstances(MachineInfo machine,
            MachineTypeInfo type, int count, int timeout)
            throws ResourceRequestException, InterruptedException {
        List<InstanceInfo> result = new ArrayList<InstanceInfo>();
        int wait = 0;
        while ((wait * 60) < timeout) {
            try {
                List<InstanceInfo> instances = CloudUtil.startInstances(machine
                        .getCloudInfo(), machine, type, count, UserUtil
                        .getAdmin().getId(), true, true);
                if(instances.size() > 0){
                    result.addAll(instances);
                    break;
                }
            } catch (CloudException e) {
                if (e.getCause() instanceof InterruptedException
                        || e instanceof CloudEucaReqTimeoutException) {
                    // this is unfriendly exception,
                    // just cancel this task to avoid issuing more of this
                    throw new InterruptedException();
                }
                // resource exhausted
                LOG.info(e.getMessage());
            }
            wait++;
            TimeUnit.MINUTES.sleep(1);
        }
        return result;
    }

    private boolean checkMachineType(MachineTypeInfo type, int cpu, int mem) {
        return type.getCpu() >= cpu && type.getMemory() >= mem * 1024;
    }

    private boolean checkMachineInfo(MachineInfo machine, MachineInfo.OS os,
            MachineInfo.ARCH arch) {
        if (!os.isAnyOS() && !machine.getOs().equals(os))
            return false;
        if (!arch.isAnyArch() && !machine.getArch().equals(arch))
            return false;
        return true;
    }

    private boolean checkMachineInfo(MachineInfo machine, Properties props,
            String featureId) {
        // check if this machine has the capability
        List<String> caps = machine.getCapabilities();
        if (caps.contains(featureId))
            return true;

        // machine request has no special requirement
        if (props.isEmpty())
            return true;

        Properties machineProps = machine.getProperties();
        for (Object k : props.keySet()) {
            String value = (String) props.get(k);
            String key = (String) k;
            if (machineProps.getProperty(key) == null) {
                return false;
            }
            else if (!value.equals(machineProps.getProperty(key))) {
                return false;
            }
        }
        return true;
    }
}
