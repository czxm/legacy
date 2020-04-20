package com.intel.soak.plugin.hive.transaction;

import java.util.List;

import org.apache.hadoop.hive.service.HiveClient;

import com.intel.soak.logger.TransactionLogger;
import com.intel.soak.plugin.hive.HiveTemplate;

public class HiveAuthzTemplate extends HiveTemplate {
    private String user;
    private List<String> groups;
    private TransactionLogger logger;

    public void setTransactionLogger(TransactionLogger logger){
        this.logger = logger;
    }
    
    public void setUserGroup(String user, List<String> groups){
        this.user = user;
        this.groups = groups;
    }
    
    @Override
    protected HiveClient createHiveClient() {
        HiveClient client = super.createHiveClient();
        try{
            client.set_ugi(user, groups);
            return client;
        }
        catch(Exception e){
            logger.error(e.getMessage());
        }
        return null;
    }
}
