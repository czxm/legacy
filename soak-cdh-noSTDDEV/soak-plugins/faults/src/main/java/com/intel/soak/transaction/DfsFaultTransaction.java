package com.intel.soak.transaction;

import com.cloudera.api.model.ApiRole;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.util.CMClient;
import com.intel.soak.util.CMClientFactory;
import com.intel.soak.failures.CMRoleDownFailure;

import java.util.ArrayList;
import java.util.List;

@Plugin(desc="DFS Failover Transaction", type = PLUGIN_TYPE.TRANSACTION)
public class DfsFaultTransaction extends AbstractTransaction {
    private String wait = "60000";

    @Override
    public boolean startup(){
        String v = getParamValue("wait");
        if(v != null){
            wait = v;
        }
        return true;
    }

    @Override
    public boolean execute() {
        CMClient client = CMClientFactory.INSTANCE.createClient();
        String[] nameNodes = new String[2];
        List<ApiRole> nnRoles = client.findRoles("hdfs", "NAMENODE");
        if(nnRoles.size() > 1){
            if(nnRoles.get(0).getHaStatus().equals(ApiRole.HaStatus.ACTIVE)){
                nameNodes[0] = client.findHostById(nnRoles.get(0).getHostRef().getHostId()).getHostname();
                nameNodes[1] = client.findHostById(nnRoles.get(1).getHostRef().getHostId()).getHostname();
            }
            else{
                nameNodes[0] = client.findHostById(nnRoles.get(1).getHostRef().getHostId()).getHostname();
                nameNodes[1] = client.findHostById(nnRoles.get(0).getHostRef().getHostId()).getHostname();
            }
            List<String> nn = new ArrayList<String>();
            nn.add(nameNodes[0]);
            final CMRoleDownFailure failover = new CMRoleDownFailure(nn, "hdfs", "NAMENODE");
            final Thread t1 = new Thread(failover);
            t1.start();
            while(!failover.isWaiting()){
                try{
                    Thread.sleep(5000);
                }
                catch(Exception e){
                }
            }
            try{
                Thread.sleep(Integer.parseInt(wait));
                t1.interrupt();
                t1.join();
            }
            catch(Exception e){
            }
        }
        else{
            logger.error("Skipped as HDFS HA is not enabled");
            return false;
        }
        return true;
    }
}
