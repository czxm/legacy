package com.intel.cedar.core.entities;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.agent.IAgent;
import com.intel.cedar.agent.IGatewayAgent;
import com.intel.cedar.agent.impl.AgentManager;
import com.intel.cedar.agent.impl.XmlBasedAgent;
import com.intel.cedar.agent.runtime.ServerRuntimeInfo;
import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.impl.CedarAdminTaskItem;
import com.intel.cedar.tasklet.impl.CedarAdminTaskItem.AdminTaskType;
import com.intel.cedar.tasklet.impl.CedarAdminTaskRunner;
import com.intel.cedar.user.UserInfo;
import com.intel.cedar.user.util.UserUtil;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;
import com.intel.cedar.util.Hashes;

@MappedSuperclass
public abstract class AbstractHostInfo {
    private static Logger LOG = LoggerFactory.getLogger(AbstractHostInfo.class);
    @Column(name = "host")
    protected String host;

    // indicate that this node needs proxy to access
    @Column(name = "proxyHost")
    protected String proxyHost;
    @Column(name = "proxyPort")
    protected String proxyPort;
    @Column(name = "proxyAuth")
    protected String proxyAuth;
    @Column(name = "proxyPasswd")
    protected String proxyPasswd;

    // indicate that this node needs gateway to access
    @Column(name = "gatewayId")
    protected Long gatewayId;

    // indicate that it's managed by resource pool
    @Column(name = "pooled")
    protected Boolean pooled;

    @Column(name = "userid")
    protected Long userId; // owner

    @Column(name = "state")
    private String state; // pending, running, rebooting, terminated,
                          // shutting-down, inactive

    @Column(name = "retryCount")
    protected Long retryCount; // trial count for testing connection

    @Column(name = "notifyCount")
    protected Long notifyCount; // (email) notification count for this host

    public abstract Long getId();

    public abstract MachineInfo.OS getOs();

    public abstract MachineInfo.ARCH getArch();

    public abstract boolean isValid();

    public abstract Properties getProperties();

    public abstract List<String> getCapabilities();

    public abstract String getRootPath();

    public abstract List<NATInfo> getPortMappings();

    public abstract void assignPortMapping(NATInfo i);

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Long getGatewayId() {
        if (null == gatewayId)
            return -1L;
        return gatewayId;
    }

    public void setGatewayId(Long gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyAuth() {
        return proxyAuth;
    }

    public void setProxyAuth(String proxyAuth) {
        this.proxyAuth = proxyAuth;
    }

    public String getProxyPasswd() {
        return proxyPasswd;
    }

    public void setProxyPasswd(String proxyPasswd) {
        this.proxyPasswd = proxyPasswd;
    }

    public boolean isManagedHost() {
        if (this instanceof PhysicalNodeInfo)
            return ((PhysicalNodeInfo) this).getManaged();
        else if (this instanceof InstanceInfo)
            return ((InstanceInfo) this).getMachineInfo().getManaged();
        else if (this instanceof CloudNodeInfo)
            return ((CloudNodeInfo) this).getManaged();
        else
            // gateway
            return true;
    }

    public boolean testConnection(boolean updateCedar) {
        boolean ok = false;
        if (!isValidHost())
            return false;
        if (!isManagedHost())
            return true;
        IAgent agent = null;
        try {
            agent = AgentManager.getInstance().createAgent(this, true);
            ok = agent.testConnection();
            if(!ok && updateCedar && agent instanceof XmlBasedAgent){
                XmlBasedAgent xa = (XmlBasedAgent)agent;
                if (xa.updateCedar()) {
                    LOG.info("updated to version {} on {}", System
                            .getProperty("cedar.version"), getHost());
                    CedarAdminTaskItem item = new CedarAdminTaskItem(
                            AdminTaskType.RestartCedar);
                    item.setTaskParameters(new Object[] { this.getOs().isWindows() });
                    agent.run(new CedarAdminTaskRunner(), item, "0", "");
                    // tricky here, return false to force waiting for agent starting
                    // this occurs only once
                }
            }
        } catch (Exception e) {
            ok = false;
        } finally {
            if (agent != null)
                AgentManager.getInstance().releaseAgent(agent);
        }
        if (!ok)
            LOG.info("managed instance " + host + " is not ready");
        return ok;

    }

    public ServerRuntimeInfo getServerInfo() {
        if (!isValidHost())
            return null;
        if (!isManagedHost())
            return null;
        IAgent agent = null;
        try {
            agent = AgentManager.getInstance().createAgent(this, true);
            return agent.getServerInfo();
        } catch (Exception e) {
            return null;
        } finally {
            if (agent != null)
                AgentManager.getInstance().releaseAgent(agent);
        }
    }

    public boolean isValidHost() {
        if(host != null && !host.equals("") && !host.equals("0.0.0.0")){
            if(host.toUpperCase().equals(host)){ // IP addr
                return true;
            }
            else{
                try {
                    InetAddress.getByName(host);
                    return true;
                } catch (UnknownHostException e) {
                    return false;
                }
            }
        }
        return false;
    }

    public String getHostName() {
        if (this.getGatewayId() != null) {
            GatewayInfo gw = GatewayInfo.load(this.getGatewayId());
            if (gw != null) {
                IGatewayAgent agent = AgentManager.getInstance()
                        .createAgent(gw, true);
                try {
                    return agent.getHostName(this.host);
                } catch (Exception e) {
                } finally {
                    AgentManager.getInstance().releaseAgent(agent);
                }
            }
        } else {
            try {
                InetAddress inet = InetAddress.getByName(host);
                return inet.getHostName();
            } catch (UnknownHostException e) {
            }
        }
        return host;
    }

    public Boolean getPooled() {
        if (pooled == null)
            return false;
        return pooled;
    }

    public void setPooled(Boolean pooled) {
        this.pooled = pooled;
    }

    public UserInfo getOwner() {
        return UserUtil.getUserById(userId);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void assignPortMappings(List<NATInfo> n) {
        for (NATInfo i : n) {
            NATInfo ci = new NATInfo();
            ci.setGatewayId(i.getGatewayId());
            ci.setName(i.getName());
            ci.setPort(i.getPort());
            assignPortMapping(ci);
        }
    }

    public void enablePortMapping(NATInfo nat) {
        if (nat.getGatewayId() == null || (nat.getMappedPort() != null && nat.getMappedPort() > 0))
            return;
        GatewayInfo gw = GatewayInfo.load(nat.getGatewayId());
        IGatewayAgent agent = AgentManager.getInstance().createAgent(gw, true);
        /*
         * LOG.info("enable {} port mapping for {} on gateway {} ", new
         * Object[]{nat.getName(), host, gw.getHost()});
         */
        int mappedPort = agent.createPortMapping(nat);
        AgentManager.getInstance().releaseAgent(agent);
        if (mappedPort > 0) {
            nat.setMappedPort(mappedPort);
            nat.saveChanges();
        } else {
            LOG.info("failed to enable {} port mapping for {} on gateway {} ",
                    new Object[] { nat.getName(), host, gw.getHost() });
        }
    }

    public void enablePortMappings(List<NATInfo> ns) {
        for (NATInfo n : ns) {
            enablePortMapping(n);
        }
    }

    public void disablePortMapping(NATInfo nat) {
        if (NATInfo.load(nat.getId()) == null || nat.getGatewayId() == null || nat.getMappedPort() == null)
            return;
        /*
         * LOG.info("disable {} port mapping for {} on gateway {} ", new
         * Object[]{nat.getName(), host, gw.getHost()});
         */
        if(nat.getMappedPort() > 0){
            GatewayInfo gw = GatewayInfo.load(nat.getGatewayId());
            IGatewayAgent agent = AgentManager.getInstance().createAgent(gw, true);
            agent.releasePortMapping(nat);
            AgentManager.getInstance().releaseAgent(agent);
        }
        EntityWrapper<NATInfo> db = EntityUtil.getNATEntityWrapper();
        EntityUtil.executeSQL(db, "DELETE FROM NAT WHERE ID = "
                + nat.getId().toString());
        db.commit();
    }

    public void disablePortMappings(List<NATInfo> ns) {
        for (NATInfo n : ns) {
            disablePortMapping(n);
        }
    }

    public int getCedarPort() {
        if (getGatewayId() != null) {
            GatewayInfo gw = GatewayInfo.load(getGatewayId());
            if (gw != null) {
                for (NATInfo n : this.getPortMappings()) {
                    if (n.getPort() == 10614)
                        return n.getMappedPort();
                }
                NATInfo nat = new NATInfo();
                nat.setName("CedarAgent");
                nat.setPort(10614);
                assignPortMapping(nat);
                enablePortMapping(nat);
                return nat.getMappedPort();
            } else {
                return -1;
            }
        } else {
            return 10614;
        }
    }

    public int getRemoteDesktopPort() {
        if (getGatewayId() != null && getGatewayId() > 0) {
            GatewayInfo gw = GatewayInfo.load(getGatewayId());
            if (gw != null) {
                for (NATInfo n : this.getPortMappings()) {
                    if (getOs().isWindows() && n.getPort() == 3389
                            || n.getPort() == 5901) {
                        return n.getMappedPort();
                    }
                }
                NATInfo nat = new NATInfo();
                if (getOs().isWindows()) {
                    nat.setName("RDP");
                    nat.setPort(3389);
                } else {
                    nat.setName("VNC");
                    nat.setPort(5901);
                }
                assignPortMapping(nat);
                enablePortMapping(nat);
                return nat.getMappedPort();
            }
        } else {
            if (getOs().isWindows()) {
                return 3389;
            } else {
                return 5901;
            }
        }
        return -1;
    }

    public List<VolumeInfo> getAttachedVolumes() {
        EntityWrapper<VolumeInfo> db = EntityUtil.getVolumeEntityWrapper();
        VolumeInfo v = new VolumeInfo();
        v.setAttached(getId());
        List<VolumeInfo> result = db.query(v);
        db.rollback();
        return result;
    }

    public List<VolumeInfo> getLocalVolumes() {
        EntityWrapper<VolumeInfo> db = EntityUtil.getVolumeEntityWrapper();
        VolumeInfo v = new VolumeInfo();
        v.setAttached(getId());
        v.setCloudId(-1L);
        v.setImageId("N/A");
        List<VolumeInfo> result = db.query(v);
        db.rollback();
        return result;
    }

    // allocate a "volume" (actually a folder)
    public VolumeInfo createVolume(int size) {
        VolumeInfo v = new VolumeInfo();
        v.setCloudId(-1L);
        v.setAttached(getId());
        v.setName(Hashes
                .generateId(UserUtil.getUserById(userId).getUser(), "D"));
        v.setComment("local folder");
        v.setImageId("N/A");
        v.setSize(size);
        v.setUserId(userId);
        v.setCreationTime(System.currentTimeMillis());
        String d = String.format("%s/%s", getRootPath(), v.getName());
        if (this.getOs().isWindows()) {
            d = String.format("%s\\%s", getRootPath(), v.getName());
        }
        v.setPath(d);
        v.setAttachedCount(1);
        v.setAttachTime(System.currentTimeMillis());
        IAgent agent = AgentManager.getInstance().createAgent(this, true);
        CedarAdminTaskItem item = new CedarAdminTaskItem(
                AdminTaskType.CreateDir);
        item
                .setTaskParameters(new Object[] { getOs().isWindows(),
                        v.getPath() });
        IResult result = agent.run(new CedarAdminTaskRunner(), item, "120", "");
        LOG.info("create folder ({}) {}", new Object[] { item.getValue(),
                result.getID().isSucceeded() ? "succeeded" : "failed" });
        AgentManager.getInstance().releaseAgent(agent);
        if (!result.getID().isSucceeded()) {
            LOG.info(result.getFailureMessage());
            return null;
        }
        EntityWrapper<VolumeInfo> db = EntityUtil.getVolumeEntityWrapper();
        db.add(v);
        db.commit();
        db = EntityUtil.getVolumeEntityWrapper();
        v = db.query(v).get(0);
        db.rollback();
        return v;
    }

    public void deleteVolume(VolumeInfo v) {
        IAgent agent = AgentManager.getInstance().createAgent(this, true);
        CedarAdminTaskItem item = new CedarAdminTaskItem(
                AdminTaskType.DeleteDir);
        item
                .setTaskParameters(new Object[] { getOs().isWindows(),
                        v.getPath() });
        IResult result = agent.run(new CedarAdminTaskRunner(), item, "1800", "");
        LOG.info("delete folder ({}) {}", new Object[] { item.getValue(),
                result.getID().isSucceeded() ? "succeeded" : "failed" });
        AgentManager.getInstance().releaseAgent(agent);
        if (result.getID().isSucceeded()) {
            EntityWrapper<VolumeInfo> db = EntityUtil.getVolumeEntityWrapper();
            VolumeInfo t = db.load(VolumeInfo.class, v.getId());
            db.delete(t);
            db.commit();
        }
        else{
            LOG.info(result.getFailureMessage());
        }
    }

    public boolean setAdminPasswd(String passwd) {
        if (!isManagedHost())
            return false;
        IAgent agent = null;
        try {
            agent = AgentManager.getInstance().createAgent(this, true);
            CedarAdminTaskItem item = new CedarAdminTaskItem(
                    AdminTaskType.SetAdminPassword);
            item
                    .setTaskParameters(new Object[] { getOs().isWindows(),
                            passwd });
            IResult result = agent.run(new CedarAdminTaskRunner(), item, "0",
                    "");
            LOG.info("set admin passwd {} for {}",
                    result.getID().isSucceeded() ? "succeeded" : "failed",
                    getHost());
            AgentManager.getInstance().releaseAgent(agent);
            if (!result.getID().isSucceeded()) {
                LOG.info(result.getFailureMessage());
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (agent != null)
                AgentManager.getInstance().releaseAgent(agent);
        }
    }

    public boolean syncDateTime() {
        return syncDateTime(false);
    }
    
    public boolean syncDateTime(boolean force) {
        if (!isManagedHost())
            return false;
        IAgent agent = null;
        try {
            int clockSkew = CedarConfiguration.getInstance().getAllowedClockSkew() * 1000;
            agent = AgentManager.getInstance().createAgent(this, true);
            ServerRuntimeInfo si = agent.getServerInfo();
            if (force || (si != null && Math.abs(si.getServerTime() - System.currentTimeMillis()) >= clockSkew)) {
                CedarAdminTaskItem item = new CedarAdminTaskItem(
                        AdminTaskType.SyncDateTime);
                item.setTaskParameters(new Object[] { getOs().isWindows() });
                IResult result = agent.run(new CedarAdminTaskRunner(), item, "0",
                        "");
                if (result.getID().isSucceeded() && this.getOs().isWindows()) {
                    // if the Windows machine is configured with different TimeZone
                    // we must call twice to reset it successfully.
                    result = agent.run(new CedarAdminTaskRunner(), item, "0", "");
                }
                LOG.info("sync date & time {} for {}",
                        result.getID().isSucceeded() ? "succeeded" : "failed",
                        getHost());
                AgentManager.getInstance().releaseAgent(agent);
                if (!result.getID().isSucceeded()) {
                    LOG.info(result.getFailureMessage());
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (agent != null)
                AgentManager.getInstance().releaseAgent(agent);
        }
    }

    public Long getRetryCount() {
        if (retryCount == null) {
            retryCount = 0L;
        }
        return retryCount;
    }

    public void setRetryCount(Long retryCount) {
        this.retryCount = retryCount;
    }

    public Long getNotifyCount() {
        if (notifyCount == null) {
            notifyCount = 0L;
        }
        return notifyCount;
    }

    public void setNotifyCount(Long notifyCount) {
        this.notifyCount = notifyCount;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
