package com.intel.cedar.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.intel.cedar.agent.IAgent;
import com.intel.cedar.agent.IGatewayAgent;
import com.intel.cedar.agent.impl.AgentManager;
import com.intel.cedar.cloud.CloudException;
import com.intel.cedar.cloud.Connector;
import com.intel.cedar.cloud.ConnectorFactory;
import com.intel.cedar.cloud.ResourceExhaustedException;
import com.intel.cedar.cloud.UnsupportedCloudException;
import com.intel.cedar.core.CedarException;
import com.intel.cedar.core.entities.CloudInfo;
import com.intel.cedar.core.entities.CloudNodeInfo;
import com.intel.cedar.core.entities.GatewayInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.KeyPairDescription;
import com.intel.cedar.core.entities.MachineInfo;
import com.intel.cedar.core.entities.MachineMappingInfo;
import com.intel.cedar.core.entities.MachineTypeInfo;
import com.intel.cedar.core.entities.NATInfo;
import com.intel.cedar.core.entities.PhysicalNodeInfo;
import com.intel.cedar.core.entities.VolumeInfo;
import com.intel.cedar.core.entities.MachineInfo.OS;
import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.impl.CedarAdminTaskItem;
import com.intel.cedar.tasklet.impl.CedarAdminTaskRunner;
import com.intel.cedar.tasklet.impl.CedarAdminTaskItem.AdminTaskType;
import com.intel.cedar.user.util.UserUtil;

public class CloudUtil {
    private static Logger LOG = LoggerFactory.getLogger(CloudUtil.class);
    private static ExecutorService exe = Executors.newCachedThreadPool();

    public static void asyncExec(Runnable runable) {
        if (runable != null) {
            exe.submit(runable);
        }
    }

    public static Connector getCloudConnector(CloudInfo cloud)
            throws CloudException {
        try {
            return ConnectorFactory.getInstance().createConnector(cloud);
        } catch (UnsupportedCloudException e) {
            LOG.info("getCloudConnector", e);
            throw e;
        }
    }

    public static List<InstanceInfo> getCurrentInstances(CloudInfo cloud) {
        List<InstanceInfo> result = Lists.newArrayList();
        try {
            Connector conn = getCloudConnector(cloud);
            if (conn != null)
                result = conn.getInstances();
        } catch (CloudException e) {
            LOG.info("getCurrentInstances", e);
        }
        return result;
    }

    public static List<VolumeInfo> getCurrentVolumes(CloudInfo cloud) {
        List<VolumeInfo> result = Lists.newArrayList();
        try {
            Connector conn = getCloudConnector(cloud);
            if (conn != null)
                result = conn.getVolumes();
        } catch (CloudException e) {
            LOG.info("getCurrentVolumes", e);
        }
        return result;
    }

    public static List<MachineInfo> getCurrentMachines(CloudInfo cloud) {
        List<MachineInfo> result = Lists.newArrayList();
        try {
            Connector conn = getCloudConnector(cloud);
            if (conn != null) {
                result = conn.getMachines(null);
                for (MachineInfo m : result) {
                    matchImageType(m);
                    fillMachineProperties(m);
                }
            }
        } catch (CloudException e) {
            LOG.info("getCurrentMachines", e);
        }
        return result;
    }

    public static List<MachineTypeInfo> getCurrentMachineTypes(CloudInfo cloud) {
        List<MachineTypeInfo> result = Lists.newArrayList();
        try {
            Connector conn = getCloudConnector(cloud);
            if (conn != null)
                result = conn.getMachineTypes();
        } catch (CloudException e) {
            LOG.info("getCurrentMachineTypes", e);
        }
        return result;
    }

    public static List<CloudNodeInfo> getCurrentCloudNodeInfos(CloudInfo cloud) {
        List<CloudNodeInfo> result = Lists.newArrayList();
        try {
            Connector conn = getCloudConnector(cloud);
            if (conn != null)
                result = conn.getCloudNodes();
        } catch (Exception e) {
        }
        return result;
    }

    public static List<KeyPairDescription> getCurrentKeyPairs(CloudInfo cloud) {
        List<KeyPairDescription> result = Lists.newArrayList();
        try {
            Connector conn = getCloudConnector(cloud);
            if (conn != null)
                result = conn.getKeyPairs();
        } catch (CloudException e) {
            LOG.info("getCurrentKeyPairs", e);
        }
        return result;
    }

    public static boolean testConnection(CloudInfo cloud) {
        Connector conn;
        try {
            conn = getCloudConnector(cloud);
            if (conn != null) {
                return conn.testConnection();
            }
        } catch (CloudException e) {
        }
        return false;
    }

    public static boolean isRegistered(CloudInfo cloud) {
        // do not need precise match, we just extract some 'keyword' to query
        // from database
        CloudInfo info = new CloudInfo();
        info.setName(cloud.getName());
        info.setHost(cloud.getHost());
        info.setProtocol(cloud.getProtocol());
        EntityWrapper<CloudInfo> db = EntityUtil.getCloudEntityWrapper();
        try {
            if (db.query(info).size() > 0)
                return true;
            else
                return false;
        } finally {
            db.rollback();
        }
    }

    public static List<CloudInfo> getClouds() {
        return EntityUtil.listClouds();
    }

    public static List<VolumeInfo> getVolumes() {
        return EntityUtil.listVolumes(null);
    }

    public static CloudInfo getCloudByName(String name) {
        EntityWrapper<CloudInfo> db = EntityUtil.getCloudEntityWrapper();
        try {
            CloudInfo c = new CloudInfo();
            c.setName(name);
            return db.getUnique(c);
        } catch (CedarException e) {
            return null;
        } finally {
            db.rollback();
        }
    }

    public static CloudInfo getCloudById(Long id) {
        return CloudInfo.load(id);
    }

    public static InstanceInfo getInstanceInfoById(Long id) {
        return InstanceInfo.load(id);
    }

    public static PhysicalNodeInfo getPhysicalNodeById(Long id) {
        EntityWrapper<PhysicalNodeInfo> db = EntityUtil
                .getPhysicalNodeEntityWrapper();
        try {
            return db.load(PhysicalNodeInfo.class, id);
        } catch (Exception e) {
            return null;
        } finally {
            db.rollback();
        }
    }

    public static PhysicalNodeInfo getPhysicalNodeByHost(String host) {
        EntityWrapper<PhysicalNodeInfo> db = EntityUtil
                .getPhysicalNodeEntityWrapper();
        try {
            PhysicalNodeInfo n = new PhysicalNodeInfo();
            n.setHost(host);
            return db.getUnique(n);
        } catch (Exception e) {
            return null;
        } finally {
            db.rollback();
        }
    }

    public static GatewayInfo getGatewayById(Long id) throws CedarException {
        EntityWrapper<GatewayInfo> db = EntityUtil.getGatewayEntityWrapper();
        try {
            return db.load(GatewayInfo.class, id);
        } finally {
            db.rollback();
        }
    }

    public static VolumeInfo getVolumeById(Long id) throws CedarException {
        EntityWrapper<VolumeInfo> db = EntityUtil.getVolumeEntityWrapper();
        try {
            return db.load(VolumeInfo.class, id);
        } finally {
            db.rollback();
        }
    }

    public static GatewayInfo getGatewayByHost(String host)
            throws CedarException {
        EntityWrapper<GatewayInfo> db = EntityUtil.getGatewayEntityWrapper();
        try {
            GatewayInfo n = new GatewayInfo();
            n.setHost(host);
            return db.getUnique(n);
        } finally {
            db.rollback();
        }
    }

    public static void matchImageType(MachineInfo m) {
        EntityWrapper<MachineMappingInfo> db = new EntityWrapper<MachineMappingInfo>();
        for (MachineMappingInfo i : db.query(new MachineMappingInfo())) {
            String name = m.getImageName();
            if (name.matches(i.getPattern())) {
                m.setArch(i.getArch());
                m.setOs(i.getOs());
                m.setManaged(true);
                break;
            }
        }
        db.rollback();
    }

    // TODO: it's just for test
    private static void fillMachineProperties(MachineInfo m) {
        Properties props = new Properties();
        m.setComment("");
        String name = m.getImageName();
        if (name.contains("cpp")) {
            props.setProperty("CPP", "true");
            m.setComment("Develop Install");
        } else if (name.contains("bare")) {
            props.setProperty("JDK", "1.6u22");
            m.setComment("Bare Install");
        } else if (name.contains("office")) {
            props.setProperty("Office", "2007");
        } else if (name.contains("autoit")) {
            props.setProperty("autoit", "true");
        }

        if (name.contains("win")) {
            String n = m.getComment();
            m.setComment("Standard Edition " + n);
            if (name.contains("ee")) {
                m.setComment("Enterprise Edition " + n);
            } else if (name.contains("de")) {
                m.setComment("Data Center Edition " + n);
            } else if (name.contains("office")) {
                m.setComment("Office 2007");
            } else if (name.contains("autoit")) {
                m.setComment("AutoIt");
            }
        }
        m.setEnabled(true);
        m.setProperties(props);
    }

    public static void registerCloud(CloudInfo cloud, List<GatewayInfo> gateways)
            throws CedarException {
        if (isRegistered(cloud)) {
            LOG.error("trying to register an existent cloud: "
                    + cloud.toString());
            throw new CedarException("trying to register an existent cloud: "
                    + cloud.toString());
        }
        if (ConnectorFactory.getInstance().supportedCloud(cloud)) {
            cloud.registerGateways(gateways);

            if(cloud.getNodeUser() == null)
                cloud.setNodeUser("root");
            if(cloud.getVolumeDevice() == null)
                cloud.setVolumeDevice("vda");
            if(cloud.getPoolRatio() == null)
                cloud.setPoolRatio(0.1f);
            
            EntityWrapper<CloudInfo> db = EntityUtil.getCloudEntityWrapper();
            db.add(cloud);
            db.commit();

            EntityWrapper<CloudNodeInfo> cndb = EntityUtil
                    .getCloudNodeEntityWrapper();
            for (CloudNodeInfo m : getCurrentCloudNodeInfos(cloud)) {
                GatewayInfo gw = cloud.findGateway();
                if (gw != null)
                    m.setGatewayId(gw.getId());
                m.setState("running");
                m.setManaged(true);
                m.setCloudId(cloud.getId());
                m.setProxyHost(cloud.getProxyHost());
                m.setProxyPort(cloud.getProxyPort());
                m.setProxyAuth(cloud.getProxyAuth());
                m.setProxyPasswd(cloud.getProxyPasswd());
                cndb.add(m);
            }
            cndb.commit();

            EntityWrapper<MachineInfo> mdb = EntityUtil
                    .getMachineEntityWrapper();
            for (MachineInfo m : getCurrentMachines(cloud)) {
                if (m.getOs() != null && m.getArch() != null)
                    mdb.add(m);
            }
            mdb.commit();

            EntityWrapper<MachineTypeInfo> mtdb = EntityUtil
                    .getMachineTypeEntityWrapper();
            for (MachineTypeInfo mti : getCurrentMachineTypes(cloud)) {
                mtdb.add(mti);
            }
            mtdb.commit();

            EntityWrapper<KeyPairDescription> kpdb = EntityUtil
                    .getKeyPairEntityWrapper();
            for (KeyPairDescription m : getCurrentKeyPairs(cloud)) {
                kpdb.add(m);
            }
            kpdb.commit();
        }
    }

    public static void importFreeVolumes(CloudInfo cloud) {
        EntityWrapper<VolumeInfo> vdb = EntityUtil.getVolumeEntityWrapper();
        List<VolumeInfo> volumes = getCurrentVolumes(cloud);
        for (VolumeInfo v : volumes) {
            v.setName(UUID.randomUUID().toString());
            vdb.add(v);
        }
        vdb.commit();
    }

    public static void importInstances(CloudInfo cloud) {
        EntityWrapper<InstanceInfo> idb = EntityUtil.getInstanceEntityWrapper();
        List<InstanceInfo> runningInstances = getCurrentInstances(cloud);
        for (InstanceInfo i : runningInstances) {
            if (i.getMachineInfo() != null && i.getState().equals("running")) { // image
                                                                                // is
                                                                                // registered
                idb.add(i);
            }
        }
        idb.commit();
        for (InstanceInfo i : cloud.getInstances()) {
            for (GatewayInfo p : cloud.getGateways()) {
                IGatewayAgent gw = AgentManager.getInstance().createAgent(p, true);
                i.setPortMappings(gw.getMappedPorts(i));
                AgentManager.getInstance().releaseAgent(gw);
            }
        }
    }

    public static ResultCode deregisterCloud(CloudInfo cloud) {
        if (!isRegistered(cloud)) {
            LOG.error("trying to deregister a non-existent cloud:"
                    + cloud.toString());
            return ResultCode.NOT_FOUND;
        }

        // if the cloud is in-use, return directly
        List<InstanceInfo> aliveList = cloud.getInstances();
        if (aliveList != null && aliveList.size() > 0) {
            return ResultCode.IN_USE;
        }

        EntityWrapper<CloudInfo> db = EntityUtil.getCloudEntityWrapper();
        try {
            EntityUtil.executeSQL(EntityUtil.getMachineEntityWrapper(),
                    "DELETE FROM MACHINES WHERE CLOUDID = "
                            + cloud.getId().toString());
            EntityUtil.executeSQL(EntityUtil.getMachineTypeEntityWrapper(),
                    "DELETE FROM MACHINETYPES WHERE CLOUDID = "
                            + cloud.getId().toString());
            EntityUtil.executeSQL(EntityUtil.getKeyPairEntityWrapper(),
                    "DELETE FROM KEYPAIRS WHERE CLOUDID = "
                            + cloud.getId().toString());
            EntityUtil.executeSQL(EntityUtil.getInstanceEntityWrapper(),
                    "DELETE FROM INSTANCES WHERE CLOUDID = "
                            + cloud.getId().toString());
            EntityWrapper<VolumeInfo> vdb = EntityUtil.getVolumeEntityWrapper();
            for (VolumeInfo v : EntityUtil.listVolumes(vdb, cloud))
                vdb.delete(v);
            vdb.commit();
            db.delete(db.getUnique(cloud));

            EntityWrapper<CloudNodeInfo> cndb = EntityUtil
                    .getCloudNodeEntityWrapper();
            for (CloudNodeInfo node : EntityUtil.listCloudNodes(cndb, cloud)) {
                node.disablePortMappings(node.getPortMappings());
                cndb.delete(node);
            }
            cndb.commit();

            // also add Engine callback function here to distribute this event
            // and delegate the cloud management work to a daemon thread.
            return ResultCode.SUCCESS;
        } catch (CedarException e) {
            LOG.info("deregisterCloud", e);
        } finally {
            db.commit();
        }

        return ResultCode.INTERNEL_ERROR;
    }

    // specially for WI
    public static boolean startInstance(CloudInfo c, MachineInfo m,
            MachineTypeInfo t, int num, List<NATInfo> n, Long userId)
            throws CloudException {
        // the cloud may be concurrently edited or de-registered
        CloudInfo rc = m.getCloudInfo();
        if (rc == null) {
            // de-registered by others
            throw new CloudException("Cloud " + c.getName()
                    + " is de-registered.");
        } else {
            EntityWrapper<InstanceInfo> idb = null;
            try {
                MachineInfo dm = MachineInfo.load(m.getId());
                MachineTypeInfo dt = MachineTypeInfo.load(t.getId());

                InstanceInfo object = new InstanceInfo();
                object.setCloudId(c.getId());
                object.setTypeId(dt.getId());
                object.setPooled(false);
                if (dt.getMax() > 0) {
                    float poolRatio = rc.getPoolRatio();
                    int permitNum = Math.round(dt.getMax() * (1 - poolRatio));
                    idb = EntityUtil.getInstanceEntityWrapper();
                    if (idb.query(object).size() >= permitNum) {
                        throw new ResourceExhaustedException(
                                "Resource exhausted, please wait for a while and try again");
                    }
                    idb.rollback();
                    idb = null;
                }
                LOG
                        .info(
                                "{} requests {} {}({}) in {} with {} CPUs and {}M Mem by WI",
                                new Object[] {
                                        UserUtil.loadUser(userId).getUser(),
                                        num, dm.getOSName(),
                                        dm.getArchitecture(), c.getName(),
                                        dt.getCpu(), dt.getMemory() });
                Connector conn = CloudUtil.getCloudConnector(rc);
                // if reserved successfully, return immediately
                // later the management code will polling the instance status;
                idb = EntityUtil.getInstanceEntityWrapper();
                List<InstanceInfo> result = new ArrayList<InstanceInfo>();
                result = conn.runInstances(m, t, num);
                for (InstanceInfo i : result) {
                    if (n.size() > 0) {
                        GatewayInfo g = rc.findGateway();
                        if (g != null)
                            i.setGatewayId(g.getId());
                    }
                    i.setUserId(userId);
                    idb.add(i);
                }
                idb.commit();
                idb = null;
                if (n.size() > 0) {
                    for (InstanceInfo i : result)
                        i.assignPortMappings(n);
                }
                return true;
            } catch (CloudException e) {
                throw e;
            } catch (Exception e) {
                throw new CloudException(e.getMessage(), e);
            } finally {
                if (idb != null) {
                    idb.commit();
                }
            }
        }
    }

    public static boolean instanceReady(InstanceInfo instance)
            throws CloudException {
        boolean ready = false;
        CloudInfo c = instance.getCloudInfo();
        Connector conn = CloudUtil.getCloudConnector(c);
        try {
            if (instance.isValidHost()) {
                if (instance.testConnection(true)) {
                    instance.setState("running");
                    instance.saveChanges();
                    return true;
                }
            } else {
                ready = conn.instanceReady(instance);
                if (ready) {
                    instance.setHost(conn.getInstancePublicAddress(instance));
                    instance.setPrivateIp(conn.getInstanceAddress(instance));
                    instance.setPrivateDns(conn.getInstanceHostname(instance));
                    instance.setKeyName(conn.getInstanceKeyName(instance));
                    if (instance.isValidHost()) {
                        instance.setRetryCount(0L);
                        instance.saveChanges();
                        if (instance.testConnection(true)) {
                            instance.setState("running");
                            instance.saveChanges();
                            return true;
                        }
                    } else {
                        long n = instance.getRetryCount();
                        if (n < 120) {
                            n = n + 1;
                            instance.setRetryCount(n);
                            if(n > 3 && !instance.getAssociated()) { // public address is not allocated successfully
                                String addr = conn.allocateAddress();
                                conn.associateAddress(instance, addr);
                                instance.setAssociated(true);
                            }
                        }
                        else {
                            CloudUtil.rebootInstance(instance);
                            instance.setRetryCount(0L);
                        }
                        instance.saveChanges();
                    }
                }
            }
        } catch (CloudException e) {
        }
        return false;
    }

    public static String getConsoleOuput(InstanceInfo info)
            throws CloudException {
        CloudInfo c = info.getCloudInfo();
        if (c == null) {
            LOG.error("instance {} is not attached to a cloud", info
                    .getInstanceId());
            throw new CloudException();
        }

        try {
            return CloudUtil.getCloudConnector(c).getConsoleOutput(info);
        } catch (CloudException e) {
            throw e;
        }
    }

    public static List<InstanceInfo> startInstances(CloudInfo c, MachineInfo m,
            MachineTypeInfo t, int count, Long userId, boolean pooled) throws CloudException{
        return startInstances(c, m, t, count, userId, pooled, false);
    }
    
    public static List<InstanceInfo> startInstances(CloudInfo c, MachineInfo m,
            MachineTypeInfo t, int count, Long userId, boolean pooled, boolean standby)
            throws CloudException {
        List<InstanceInfo> result = new ArrayList<InstanceInfo>();
        CloudInfo rc = m.getCloudInfo();
        if (rc == null) {
            // de-registered by others
            throw new CloudException("Cloud " + c.getName()
                    + " is de-registered.");
        } else {
            EntityWrapper<InstanceInfo> idb = null;
            try {
                Long gwId = null;
                if (c.getSeperated()) {
                    gwId = c.findGateway().getId();
                }
                InstanceInfo object = new InstanceInfo();
                object.setCloudId(c.getId());
                object.setTypeId(t.getId());
                object.setPooled(pooled);
                if (t.getMax() > 0) {
                    float poolRatio = rc.getPoolRatio();
                    int permitNum = Math.round(t.getMax() * poolRatio);
                    idb = EntityUtil.getInstanceEntityWrapper();
                    if (idb.query(object).size() >= permitNum) {
                        throw new ResourceExhaustedException();
                    }
                    idb.rollback();
                    idb = null;
                }
                LOG.info("starting {} {}({}) in {} with {} CPUs and {}M Mem",
                        new Object[] { count, m.getOSName(),
                                m.getArchitecture(), c.getName(), t.getCpu(),
                                t.getMemory() });
                Connector conn = CloudUtil.getCloudConnector(rc);
                result = conn.runInstances(m, t, count);
                idb = EntityUtil.getInstanceEntityWrapper();
                for (InstanceInfo i : result) {
                    i.setUserId(userId);
                    i.setPooled(pooled);
                    i.setStandy(standby);
                    if (gwId != null) {
                        i.setGatewayId(gwId);
                    }
                    idb.add(i);
                }
                idb.commit();
                idb = null;

                List<InstanceInfo> instances = new ArrayList<InstanceInfo>();
                for (InstanceInfo i : result) {
                    int waitCount = 0;
                    while (true) {
                        try {
                            InstanceInfo r = InstanceInfo.load(i.getId());
                            if (r != null) {
                                if (r.getState().equals("running")
                                        && r.testConnection(false)) {
                                    instances.add(r);
                                    break;
                                } else {
                                    if (waitCount == 120) {
                                        // half an hour is somewhat not endurable
                                        CloudUtil.terminateInstance(r);
                                        break;
                                    } else {
                                        TimeUnit.SECONDS.sleep(15);
                                        waitCount = waitCount + 1;
                                    }
                                }
                            } else {
                                break;
                            }
                        } catch (InterruptedException e) {
                            // should be canceled
                            CloudUtil.terminateInstances(result);
                            throw e;
                        }
                    }
                }
                if(gwId != null){
                    for (InstanceInfo i : instances) {
                        if (pooled) {
                            NATInfo nat = new NATInfo();
                            nat.setName("CedarDebug");
                            nat.setPort(8002);
                            i.assignPortMapping(nat);
                            i.enablePortMapping(nat);
                        }
                    }
                }
                return instances;
            } catch (Exception e) {
                throw new CloudException(e.getMessage(), e);
            } finally {
                if (idb != null)
                    idb.commit();
            }
        }
    }

    public static void terminateInstance(InstanceInfo instance) {
        CloudInfo c = instance.getCloudInfo();
        try {
            if("suspend".equals(instance.getState()))
                return;
            LOG.info("terminating instance: " + instance.getHost());
            if (c != null && instance.isValid()) {
                EntityWrapper<VolumeInfo> db = EntityUtil
                        .getVolumeEntityWrapper();
                for (VolumeInfo v : instance.getAttachedVolumes()) {
                    if (v.isCloudVolume())
                        CloudUtil.detachVolume(v);
                    else {
                        db.delete(db.load(VolumeInfo.class, v.getId()));
                    }
                }
                db.commit();

                if (instance.isManagedHost()
                        && instance.getState().equals("running")) {
                    // force terminate cedar agent runtime, so that all running
                    // agents will return
                    IAgent agent = AgentManager.getInstance().createAgent(
                            instance, true);
                    try {
                        CedarAdminTaskItem item = new CedarAdminTaskItem(
                                AdminTaskType.StopCedar);
                        item.setTaskParameters(new Object[] { instance
                                .getMachineInfo().getOs().isWindows() });
                        agent.run(new CedarAdminTaskRunner(), item, "120", "");
                    } finally {
                        AgentManager.getInstance().releaseAgent(agent);
                    }
                }

                if (instance.getCloudInfo().getUpdateDNS() != null
                        && instance.getCloudInfo().getUpdateDNS()) {
                    // force delete DNS record (this assumes that gateway is
                    // allowed to update domain DNS)
                    if (instance.getGatewayId() != null
                            && instance.getGatewayId() > 0) {
                        IGatewayAgent agent = AgentManager.getInstance()
                                .createAgent(
                                        GatewayInfo.load(instance
                                                .getGatewayId()), true);
                        try {
                            CedarAdminTaskItem item = new CedarAdminTaskItem(
                                    AdminTaskType.UpdateDNS);
                            item.setTaskParameters(new Object[] { instance
                                    .getHost() });
                            agent
                                    .run(new CedarAdminTaskRunner(), item, "30",
                                            "");
                        } finally {
                            AgentManager.getInstance().releaseAgent(agent);
                        }
                    }
                }
            }
        } catch (CloudException e) {
            // just ignore this harmless exception
        } finally {
            try{
                Connector conn = CloudUtil.getCloudConnector(c);
                if(instance.getAssociated()){
                    conn.disassociateAddress(instance.getHost());
                    conn.releaseAddress(instance.getHost());
                }
                conn.terminateInstances(
                        Lists.newArrayList(instance));
            }
            catch(Exception e){
                LOG.error(e.getMessage(), e);
            }
            if (instance.isValid()) {
                instance.disablePortMappings(instance.getPortMappings());
                EntityWrapper<InstanceInfo> db = EntityUtil
                        .getInstanceEntityWrapper();
                db.delete(db.load(InstanceInfo.class, instance.getId()));
                db.commit();
            }
        }
    }

    public static void terminateInstances(List<InstanceInfo> instances) {
        for (InstanceInfo instance : instances) {
            terminateInstance(instance);
        }
    }

    public static void rebootInstance(InstanceInfo instance) {
        try {
            CloudInfo c = instance.getCloudInfo();
            if (c != null) {
                CloudUtil.getCloudConnector(c).rebootInstances(
                        Lists.newArrayList(instance));
                instance.setRemoteDisplay(null);
                instance.saveChanges();
            }
        } catch (CloudException e) {
        }
    }

    public static void rebootInstances(List<InstanceInfo> instances) {
        for (InstanceInfo instance : instances) {
            rebootInstance(instance);
        }
    }

    public static VolumeInfo createVolume(CloudInfo c, String name, int size,
            Long userId) throws CloudException {
        return createVolume(c, name, "", size, userId);
    }

    public static VolumeInfo createVolume(CloudInfo c, String name,
            String comment, int size, Long userId) throws CloudException {
        if (c != null) {
            VolumeInfo v = CloudUtil.getCloudConnector(c).createVolume(size);
            v.setName(name);
            v.setComment(comment);
            v.setSize(size);
            v.setUserId(userId);
            EntityUtil.getVolumeEntityWrapper().mergeAndCommit(v);
            EntityWrapper<VolumeInfo> db = null;
            try {
                db = EntityUtil.getVolumeEntityWrapper();
                return db.getUnique(v);
            } catch (Exception e) {
                throw new CloudException(e.getMessage(), e.getCause());
            } finally {
                if (db != null)
                    db.rollback();
            }
        }
        return null;
    }

    public static boolean deleteVolume(VolumeInfo v) throws CloudException {
        CloudInfo c = getCloudById(v.getCloudId());
        if (c != null) {
            CloudUtil.detachVolume(v);
            EntityWrapper<VolumeInfo> db = EntityUtil.getVolumeEntityWrapper();
            db.delete(db.load(VolumeInfo.class, v.getId()));
            db.commit();
            return getCloudConnector(c).deleteVolume(v);
        }
        return false;
    }

    // try to find a disk index for the volume
    protected static int findDeviceIndex(InstanceInfo instance) {
        EntityWrapper<VolumeInfo> db = EntityUtil.getVolumeEntityWrapper();
        VolumeInfo v = new VolumeInfo();
        v.setAttached(instance.getId());
        List<VolumeInfo> list = db.query(v);
        db.rollback();
        int i = 0;
        if (instance.getMachineTypeInfo().getSecondDisk() > 0)
            i = 1;
        for (; i < 25; i++) {
            boolean found = false;
            for (VolumeInfo vi : list) {
                if (vi.isCloudVolume() && i == vi.getDeviceIndex().intValue()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return i;
            }
        }
        return -1;
    }

    public static String findMountPath(InstanceInfo instance) {
        OS os = instance.getMachineInfo().getOs();
        if (os == null)
            return "";
        List<VolumeInfo> result = instance.getAttachedVolumes();
        for (int c = 0; c < 20; c++) {
            String d = String.format("/media%d", c);
            if (os.isWindows()) {
                d = String.format("%c:", (char) ('E' + c));
            }
            boolean found = false;
            for (VolumeInfo vi : result) {
                if (d.equals(vi.getPath())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return d;
            }
        }
        return "";
    }

    public static String findMountPath(InstanceInfo instance, VolumeInfo v) {
        OS os = instance.getMachineInfo().getOs();
        if (os == null)
            return "";

        List<VolumeInfo> result = instance.getAttachedVolumes();
        boolean found = false;
        // respect the Path of original volume
        if (v.getPath() != null) {
            for (VolumeInfo vi : result) {
                if (v.getPath().equals(vi.getPath())) {
                    found = true;
                    break;
                }
            }
            if (!found)
                return v.getPath();
        }
        for (int c = 0; c < 20; c++) {
            String d = String.format("/media%d", c);
            if (os.isWindows()) {
                d = String.format("%c:", (char) ('E' + c));
            }
            found = false;
            for (VolumeInfo vi : result) {
                if (d.equals(vi.getPath())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return d;
            }
        }
        return "";
    }

    // this function is called for below situations:
    // 1. the attaching volume was attached to another OS before, WI should
    // prompt confirmation for users
    // 2. the engine always format volumes for feature jobs
    public static boolean formatVolume(InstanceInfo instance, VolumeInfo v)
            throws CloudException {
        CloudInfo c = instance.getCloudInfo();
        VolumeInfo volume = VolumeInfo.load(v.getId());
        if (volume != null && c != null && c.getId().equals(volume.getCloudId())
                && volume.getAttached() == null) {
            volume.setAttached(instance.getId());
            volume.setDeviceIndex(findDeviceIndex(instance));
            if (CloudUtil.getCloudConnector(c).attachVolume(instance, volume)) {
                // auto format & mount feature is only available for managed
                // instance
                if (instance.isManagedHost()) {
                    // we'll format it, and select an appropriate mount path
                    volume.setPath(findMountPath(instance));
                    IAgent agent = AgentManager.getInstance().createAgent(
                            instance, true);
                    CedarAdminTaskItem item = new CedarAdminTaskItem(
                            AdminTaskType.CreateVolume);
                    item.setTaskParameters(new Object[] {
                            instance.getMachineInfo().getOs().isWindows(),
                            volume });
                    IResult result = agent.run(new CedarAdminTaskRunner(),
                            item, "600", "");
                    LOG.info("Create volume ({}) {}", new Object[] {
                            item.getValue(),
                            result.getID().isSucceeded() ? "succeeded"
                                    : "failed" });
                    AgentManager.getInstance().releaseAgent(agent);
                    if (!result.getID().isSucceeded()) {
                        LOG.info(result.getFailureMessage());
                        // force detach it
                        detachVolume(volume);
                        return false;
                    }
                }
                volume.incAttachedCount();
                volume.setAttachTime(System.currentTimeMillis());
                volume.saveChanges();
                return true;
            }
        }
        return false;
    }

    // the attaching volume was attached to same OS, and user wants to re-attach
    // it
    public static boolean attachVolume(InstanceInfo instance, VolumeInfo v)
            throws CloudException {
        CloudInfo c = instance.getCloudInfo();
        VolumeInfo volume = VolumeInfo.load(v.getId());
        if (volume != null && c != null
                && c.getId().equals(volume.getCloudId())
                && volume.getAttached() == null) {
            volume.setAttached(instance.getId());
            volume.setDeviceIndex(findDeviceIndex(instance));
            if (CloudUtil.getCloudConnector(c).attachVolume(instance, volume)) {
                if (instance.isManagedHost()) {
                    // always find an appropriate mount path
                    volume.setPath(findMountPath(instance, v));
                    IAgent agent = AgentManager.getInstance().createAgent(
                            instance, true);
                    CedarAdminTaskItem item = new CedarAdminTaskItem(
                            AdminTaskType.AttachVolume);
                    item.setTaskParameters(new Object[] {
                            instance.getMachineInfo().getOs().isWindows(),
                            volume });
                    IResult result = agent.run(new CedarAdminTaskRunner(),
                            item, "120", "");
                    LOG.info("Attach volume ({}) {}", new Object[] {
                            item.getValue(),
                            result.getID().isSucceeded() ? "succeeded"
                                    : "failed" });
                    AgentManager.getInstance().releaseAgent(agent);
                    if (!result.getID().isSucceeded()) {
                        LOG.info(result.getFailureMessage());
                        // force detach it
                        detachVolume(volume);
                        return false;
                    }
                }
                volume.incAttachedCount();
                volume.setAttachTime(System.currentTimeMillis());
                EntityUtil.getVolumeEntityWrapper().mergeAndCommit(volume);
                return true;
            }
        }
        return false;
    }

    public static boolean detachVolume(VolumeInfo volume) throws CloudException {
        if (!volume.isValid())
            return false;
        CloudInfo c = getCloudById(volume.getCloudId());
        InstanceInfo instance = null;
        if (volume != null && volume.getAttached() != null) {
            instance = InstanceInfo.load(volume.getAttached());
            // auto removal of detached mount point feature is only available
            // for managed instance
            if (c != null && instance != null && instance.isManagedHost()
                    && volume.getPath() != null && !volume.getPath().equals("")) {
                IAgent agent = AgentManager.getInstance().createAgent(instance, true);
                CedarAdminTaskItem item = new CedarAdminTaskItem(
                        AdminTaskType.DetachVolume);
                item
                        .setTaskParameters(new Object[] {
                                instance.getMachineInfo().getOs().isWindows(),
                                volume });
                IResult result = agent.run(new CedarAdminTaskRunner(), item,
                        "120", "");
                LOG
                        .info("Detach volume ({}) {}", new Object[] {
                                item.getValue(),
                                result.getID().isSucceeded() ? "succeeded"
                                        : "failed" });
                AgentManager.getInstance().releaseAgent(agent);
                if (!result.getID().isSucceeded()) {
                    LOG.info(result.getFailureMessage());
                }
            }
        }
        if (instance == null && volume.getAttached() != null) {
            instance = InstanceInfo.load(volume.getAttached());
        }
        volume.setAttached(null);
        EntityUtil.getVolumeEntityWrapper().mergeAndCommit(volume);
        if (instance != null) {
            CloudUtil.getCloudConnector(c).detachVolume(instance, volume);
        }
        return true;
    }

    public static boolean enablePortMappings(InstanceInfo info)
            throws CloudException {
        EntityWrapper<NATInfo> db = EntityUtil.getNATEntityWrapper();
        NATInfo natInfo = new NATInfo();
        natInfo.setInstanceId(info.getId());
        try {
            info.enablePortMappings(db.query(natInfo));
            return true;
        } finally {
            db.rollback();
        }
    }

    public static boolean checkIsServing(CloudInfo info) {
        EntityWrapper<InstanceInfo> db = EntityUtil.getInstanceEntityWrapper();
        InstanceInfo ins = new InstanceInfo();
        ins.setCloudId(info.getId());
        try {
            List<InstanceInfo> res = db.query(ins);
            if (res != null && res.size() > 0)
                return true;
            return false;
        } finally {
            db.rollback();
        }
    }

    public static ResultCode editCloud(CloudInfo info, List<GatewayInfo> gwList)
            throws CedarException {
        if (checkIsServing(info))
            return ResultCode.IN_USE;
        else {
            EntityWrapper<CloudInfo> db = EntityUtil.getCloudEntityWrapper();
            CloudInfo c = new CloudInfo();
            c.setId(info.getId());
            CloudInfo change;
            try {
                change = db.getUnique(c);
                change.setEnabled(info.getEnabled());
                change.registerGateways(gwList);
                db.merge(change);
            } catch (CedarException e) {
                LOG.error("Exception Occurred", e);
                throw e;
            } finally {
                db.commit();
            }
            return ResultCode.SUCCESS;
        }
    }

    public static List<GatewayInfo> getGateways(CloudInfo info) {
        CloudInfo c = CloudInfo.load(info.getId());
        return c.getGateways();
    }

    private static CloudNodeInfo findCloudNode(String host,
            List<CloudNodeInfo> list) {
        for (CloudNodeInfo i : list) {
            if (i.getHost().equals(host))
                return i;
        }
        return null;
    }

    private static InstanceInfo findInstance(String instanceId,
            List<InstanceInfo> list) {
        for (InstanceInfo i : list) {
            if (i.getInstanceId().equals(instanceId))
                return i;
        }
        return null;
    }

    private static MachineTypeInfo findMachineType(String typeId,
            List<MachineTypeInfo> list) {
        for (MachineTypeInfo v : list) {
            if (v.getType().equals(typeId))
                return v;
        }
        return null;
    }

    public static void refreshCloud(CloudInfo cloud) throws Exception {
        EntityWrapper<InstanceInfo> idb = EntityUtil.getInstanceEntityWrapper();
        List<InstanceInfo> dbInstances = EntityUtil.listInstances(idb, cloud);
        List<InstanceInfo> currentInstances = CloudUtil
                .getCurrentInstances(cloud);
        
        for(InstanceInfo instance : currentInstances){
            if("running".equals(instance.getState()) && findInstance(instance.getInstanceId(), dbInstances) == null){
                LOG.info(cloud.getName() + ": " + instance.getInstanceId() + " is not managed by Cloud Test Service");
                if(CedarConfiguration.getInstance().getRemoveUnknownInstances()){
                    try{
                        CloudUtil.getCloudConnector(cloud).terminateInstances(
                                Lists.newArrayList(instance));
                    }
                    catch(Exception e){
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }
        
        for (InstanceInfo instance : dbInstances) {
            InstanceInfo currentInstance = findInstance(instance
                    .getInstanceId(), currentInstances);
            if (currentInstance != null) {
                if (currentInstance.getState().equals("terminated")
                        || currentInstance.getState().equals("shutting-down")
                        || currentInstance.getState().equals("shutoff")) {
                    CloudUtil.terminateInstance(instance);
                } else if (currentInstance.getState().equals("error")) {
                    // we won't keep those failed instances
                    if (System.currentTimeMillis() - instance.getCreationTime() > 300 * 1000) {
                        CloudUtil.terminateInstance(instance);
                    }
                } else if (currentInstance.getState().equals("suspend") && 
                           !instance.getState().equals("suspend")) {
                    instance.setState("suspend");
                    instance.setRemoteDisplay(null);                    
                } else if ("running".equals(currentInstance.getState()) &&
                           instance.getState().equals("suspend")) {
                    instance.setState("running");
                } else if (instance.getState().equals("pending")                            
                            && CloudUtil.instanceReady(instance)) {
                    instance.setKeyName(currentInstance.getKeyName());
                    enablePortMappings(instance);
                    // force sync timezone/date/time with the
                    // CloudTestService
                    final InstanceInfo theInstance = instance;
                    asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            theInstance.syncDateTime();
                        }
                    });
                }
            } else {
                // don't delete this instance, otherwise, we ping its status
                // CloudUtil.terminateInstance(instance);
                if (instance.getState() != null
                        && !instance.getState().equals("inactive")) {
                    instance.setRetryCount(1L);
                    instance.setState("inactive");
                }
            }
        }
        idb.commit();
        
        idb = EntityUtil.getInstanceEntityWrapper();
        dbInstances = EntityUtil.listInstances(idb, cloud);
        idb.rollback();
        boolean needRefreshCloudNodes = false;
        for (InstanceInfo instance : dbInstances) {
            if ("running".equals(instance.getState()) &&
                (instance.getRemoteDisplay() == null
                || instance.getRemoteDisplay().length() == 0)) {
                needRefreshCloudNodes = true;
                break;
            }
        }

        if (needRefreshCloudNodes
                || CloudUtil.getCloudConnector(cloud)
                        .isLiveMigrationSupported()) {
            EntityWrapper<CloudNodeInfo> cndb = EntityUtil
                    .getCloudNodeEntityWrapper();
            List<CloudNodeInfo> dbNodes = EntityUtil
                    .listCloudNodes(cndb, cloud);
            List<CloudNodeInfo> currentNodes = getCurrentCloudNodeInfos(cloud);
            for (CloudNodeInfo node : dbNodes) {
                CloudNodeInfo currentNode = findCloudNode(node.getHost(),
                        currentNodes);
                if (currentNode == null) { // node is removed
                    node.disablePortMappings(node.getPortMappings());
                    cndb.delete(node);
                }
            }
            for (CloudNodeInfo node : currentNodes) {
                CloudNodeInfo currentNode = findCloudNode(node.getHost(),
                        dbNodes);
                if (currentNode == null) { // node is to be added
                    GatewayInfo gw = cloud.findGateway();
                    if (gw != null)
                        node.setGatewayId(gw.getId());
                    node.setState("running");
                    node.setManaged(true);
                    node.setCloudId(cloud.getId());
                    node.setProxyHost(cloud.getProxyHost());
                    node.setProxyPort(cloud.getProxyPort());
                    node.setProxyAuth(cloud.getProxyAuth());
                    node.setProxyPasswd(cloud.getProxyPasswd());
                    cndb.add(node);
                } else { // node is to be updated
                    currentNode.setContent(node.getContent());
                    cndb.merge(currentNode);
                }
            }
            cndb.commit();

            cndb = EntityUtil.getCloudNodeEntityWrapper();
            dbNodes = EntityUtil.listCloudNodes(cndb, cloud);
            cndb.rollback();

            Connector conn = CloudUtil.getCloudConnector(cloud);
            for (InstanceInfo instance : dbInstances) {
                if (instance.getRemoteDisplay() == null) {
                    try {
                        URI uri = conn.getInstanceDisplay(dbNodes, instance);
                        if (uri != null) {
                            instance.setRemoteDisplay(uri.toString());
                            instance.saveChanges();
                        }
                    } catch (Exception e) {
                        LOG.warn("Refreshing Cloud " + cloud.getName(), e);
                    }
                }
            }
        }
        
        EntityWrapper<MachineTypeInfo> mtdb = EntityUtil
                .getMachineTypeEntityWrapper();
        List<MachineTypeInfo> currentMachineTypes = CloudUtil
                .getCurrentMachineTypes(cloud);
        for (MachineTypeInfo m : EntityUtil.listMachineTypes(mtdb, cloud)) {
            MachineTypeInfo currentType = findMachineType(m.getType(),
                    currentMachineTypes);
            if (currentType != null) {
                m.setCpu(currentType.getCpu());
                m.setMemory(currentType.getMemory());
                m.setDisk(currentType.getDisk());
                m.setSecondDisk(currentType.getSecondDisk());
                m.setFree(currentType.getFree());
                m.setMax(currentType.getMax());
                mtdb.merge(m);
            }
        }
        mtdb.commit();

        // conservation: don't delete any missing volumes to tolerate cloud errors
        /*
        EntityWrapper<VolumeInfo> vdb = EntityUtil.getVolumeEntityWrapper();
        List<VolumeInfo> currentVolumes = CloudUtil.getCurrentVolumes(cloud);
        for (VolumeInfo volume : EntityUtil.listVolumes(vdb, cloud)) {
            VolumeInfo currentVolume = findVolume(volume.getImageId(),
                    currentVolumes);
            if (currentVolume == null) {
                vdb.delete(volume);
            }
        }
        vdb.commit();
         */
        
        // conservation: don't delete any missing Images to tolerate cloud errors
        /*
        EntityWrapper<MachineInfo> mdb = EntityUtil.getMachineEntityWrapper();
        List<MachineInfo> currentMachines = CloudUtil.getCurrentMachines(cloud);
        for (MachineInfo m : EntityUtil.listMachines(mdb, cloud)) {
            MachineInfo currentMachine = findMachine(m.getImageId(),
                    currentMachines);
            if (currentMachine == null) {
                // the image is deregistered by external administrators
                // should check if there's instance using this Machine
                boolean inUse = false;
                for (InstanceInfo i : cloud.getInstances()) {
                    if (i.getMachineId().equals(m.getId())) {
                        inUse = true;
                        break;
                    }
                }
                if (!inUse)
                    mdb.delete(m);
            }
        }
        mdb.commit();
        */
    }

    public static boolean isRegistered(PhysicalNodeInfo node) {
        PhysicalNodeInfo info = new PhysicalNodeInfo();
        info.setNodeName(node.getNodeName());
        EntityWrapper<PhysicalNodeInfo> db = EntityUtil
                .getPhysicalNodeEntityWrapper();
        try {
            if (db.query(info).size() > 0)
                return true;
            else
                return false;
        } finally {
            db.rollback();
        }
    }

    public static boolean isRegistered(CloudNodeInfo node) {
        CloudNodeInfo info = new CloudNodeInfo();
        info.setHost(node.getHost());
        EntityWrapper<CloudNodeInfo> db = EntityUtil
                .getCloudNodeEntityWrapper();
        try {
            if (db.query(info).size() > 0)
                return true;
            else
                return false;
        } finally {
            db.rollback();
        }
    }

    public static Boolean registerPhysicalNode(PhysicalNodeInfo node,
            List<NATInfo> nat) throws CedarException {
        if (isRegistered(node)) {
            LOG.error("trying to register an existent physical node: "
                    + node.getNodeName());
            throw new CedarException(
                    "trying to register an existent physical node: "
                            + node.getNodeName());
        }
        EntityWrapper<PhysicalNodeInfo> db = EntityUtil
                .getPhysicalNodeEntityWrapper();
        db.add(node);
        db.commit();
        db = EntityUtil.getPhysicalNodeEntityWrapper();
        PhysicalNodeInfo n = db.getUnique(node);
        db.rollback();
        n.assignPortMappings(nat);
        n.enablePortMappings(nat);
        return Boolean.TRUE;
    }

    public static void deregisterPhysicalNode(PhysicalNodeInfo node)
            throws CedarException {
        if (node.isValid()) {
            for (VolumeInfo v : node.getAttachedVolumes()) {
                node.deleteVolume(v);
            }
            node.disablePortMappings(node.getPortMappings());
            EntityWrapper<PhysicalNodeInfo> db = EntityUtil
                    .getPhysicalNodeEntityWrapper();
            PhysicalNodeInfo n = db.load(PhysicalNodeInfo.class, node.getId());
            if (n != null)
                db.delete(n);
            db.commit();
        }
    }

    public static boolean isRegistered(GatewayInfo node) {
        GatewayInfo info = new GatewayInfo();
        info.setNodeName(node.getNodeName());
        EntityWrapper<GatewayInfo> db = EntityUtil.getGatewayEntityWrapper();
        try {
            if (db.query(info).size() > 0)
                return true;
            else
                return false;
        } finally {
            db.rollback();
        }
    }

    public void registerGateway(GatewayInfo node) throws CedarException {
        if (isRegistered(node)) {
            LOG.error("trying to register an existent gateway: "
                    + node.getNodeName());
            throw new CedarException("trying to register an existent gateway: "
                    + node.getNodeName());
        }
        EntityWrapper<GatewayInfo> db = EntityUtil.getGatewayEntityWrapper();
        db.add(node);
        db.commit();
    }

    public void deregisterGateway(GatewayInfo node) throws CedarException {
        if (node.isValid()) {
            EntityWrapper<GatewayInfo> db = EntityUtil
                    .getGatewayEntityWrapper();
            db.delete(GatewayInfo.load(node.getId()));
            db.commit();
        }
    }

}
