package com.intel.soak.transaction;

import com.intel.soak.failures.CMRoleDownFailure;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Plugin(desc="HMS Down Transaction", type = PLUGIN_TYPE.TRANSACTION)
public class HMSDownFaultTransaction extends AbstractTransaction {
    protected String hive_conf_dir = "/etc/hive/conf";
    protected String wait = "60000";
    protected String[] msHosts;
    protected int nextIndex = 0;

    @Override
    public boolean startup(){
        String v = getParamValue("wait");
        if(v != null){
            wait = v;
        }
        v = getParamValue("HIVE_CONF_DIR");
        if(v != null){
            hive_conf_dir = v;
        }
        Configuration config = new Configuration();
        config.addResource(new Path(hive_conf_dir + "/hive-site.xml"));
        String value = config.get("hive.metastore.uris");
        logger.info("HMS URIs: " + value);
        String[] uris = value.split(",");
        if(uris.length > 1){
            int i = 0;
            msHosts = new String[uris.length];
            for(String u : uris){
                msHosts[i] = URI.create(u).getHost();
                i++;
            }
        }
        return true;
    }

    @Override
    public boolean execute() {
        if(msHosts != null){
            List<String> hosts = new ArrayList<String>();
            hosts.add(msHosts[nextIndex]);
            logger.info(hosts.get(0) + " is selected down");
            nextIndex = (nextIndex + 1) % msHosts.length;
            CMRoleDownFailure HMSFailure = new CMRoleDownFailure(hosts, "hive", "METASTORE");
            Thread t = new Thread(HMSFailure);
            t.start();
            try{
                Thread.sleep(Integer.parseInt(wait));
                t.interrupt();
                t.join();
            }
            catch(Exception e){
            };
        }
        else{
            logger.error("Skipped testHMSDown as there's only 1 HMS");
            return false;
        }
        return true;
    }
}
