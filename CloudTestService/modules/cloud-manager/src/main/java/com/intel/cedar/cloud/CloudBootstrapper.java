package com.intel.cedar.cloud;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.agent.IGatewayAgent;
import com.intel.cedar.agent.impl.AgentManager;
import com.intel.cedar.core.Bootstrapper;
import com.intel.cedar.core.entities.AbstractHostInfo;
import com.intel.cedar.core.entities.CloudInfo;
import com.intel.cedar.core.entities.CloudNodeInfo;
import com.intel.cedar.core.entities.GatewayInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.NATInfo;
import com.intel.cedar.core.entities.PhysicalNodeInfo;
import com.intel.cedar.scheduler.CedarTimer;
import com.intel.cedar.scheduler.CedarTimerTask;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;

public class CloudBootstrapper implements Bootstrapper {
    private static Logger LOG = LoggerFactory
            .getLogger(CloudBootstrapper.class);

    public CloudBootstrapper() {
    }

    @Override
    public void start() {
        LOG.info("starting cedar cloud");
        CedarTimer.getInstance().scheduleTask(300, new CedarTimerTask("NAT Synchronizer"){
            protected void cleanupNATInfo(){
                EntityWrapper<NATInfo> db = EntityUtil.getNATEntityWrapper();
                try {
                    NATInfo n = new NATInfo();                                    
                    for(NATInfo ni : db.query(n)){
                        if((ni.getInstanceId() == null || InstanceInfo.load(ni.getInstanceId()) == null) &&
                           (ni.getCloudNodeId() == null || CloudNodeInfo.load(ni.getCloudNodeId()) == null) &&
                           (ni.getNodeId()== null || PhysicalNodeInfo.load(ni.getNodeId()) == null)){
                            db.delete(ni);
                        }
                    }
                    db.commit();
                }
                catch(Exception e){
                }
            }
            
            protected void syncPortMappings(AgentManager am, AbstractHostInfo i){
                GatewayInfo gw = GatewayInfo.load(i.getGatewayId());
                if(gw == null)
                    return;
                IGatewayAgent agent = am.createAgent(gw, true);
                List<NATInfo> gw_nat = agent.getMappedPorts(i);
                EntityWrapper<NATInfo> db = EntityUtil.getNATEntityWrapper();
                try {
                    NATInfo n = new NATInfo();
                    n.setHost(i);                                    
                    for(NATInfo ni : db.query(n)){
                        boolean mapExist = false;
                        for(NATInfo gw_ni : gw_nat){
                            if(gw_ni.getPort().equals(ni.getPort())){
                                mapExist = true;
                                if(!gw_ni.getMappedPort().equals(ni.getMappedPort())){
                                    // the port mapping in db does not match with the Gateway
                                    // we should use the gateway's mapped port
                                    ni.setMappedPort(gw_ni.getMappedPort());
                                    db.merge(ni);
                                }
                                break;
                            }
                        }
                        if(!mapExist){
                            // the port mapping does not exist on the Gateway
                            // we will create new mapping and use the gateway's mapped port
                            ni.setMappedPort(agent.createPortMapping(ni));
                            db.merge(ni);
                        }
                    }
                    db.commit();
                }
                catch(Exception e){
                }
                am.releaseAgent(agent);
            }
            
            @Override
            public void run() {
                try {
                    cleanupNATInfo();
                    
                    AgentManager am = AgentManager.getInstance();
                    for(CloudInfo c : EntityUtil.listClouds()){
                        if(c.getSeperated()){
                            for(InstanceInfo i : c.getInstances()){
                                syncPortMappings(am, i);
                            }                            
                        }
                    }
                    
                    for(CloudNodeInfo n : EntityUtil.listCloudNodes()){
                        syncPortMappings(am, n);
                    }
                    
                    for(PhysicalNodeInfo n : EntityUtil.listPhysicalNodes()){
                        syncPortMappings(am, n);
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    @Override
    public void stop() {
    }
}
