package com.intel.soak.plugin.hbase.transaction;

import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.hbase.util.StringUtils;
import com.intel.soak.transaction.AbstractTransaction;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: xhao1
 * Date: 11/22/13
 * Time: 2:19 AM
 * To change this template use File | Settings | File Templates.
 */
@Plugin(desc = "HBaseBaseTransaction", type = PLUGIN_TYPE.TRANSACTION)
public class HBaseBaseTransaction extends AbstractTransaction {
    protected static final String DEFAULT_ADDITIONAL_CONFIGURATIONS = "";
    protected Configuration conf = new Configuration();
    protected HBaseAdmin hba;
    protected static Lock poolLock = new ReentrantLock();

    @Override
    public boolean startup() {
        //logger.info(String.format("Enter startup()" ));
        String additionalConfigurations = getParamValue("AdditionalConfigs");
        additionalConfigurations = StringUtils.setAsDefaultValueIfEmpty(additionalConfigurations, DEFAULT_ADDITIONAL_CONFIGURATIONS);
        setAdditionalConfigurations(additionalConfigurations);
        return true;
    }

    @Override
    public boolean execute() {
        //logger.info(String.format("Enter execute()" ));
        return true;
    }

    @Override
    public void kill() {
        //logger.info("Enter kill()");
    }

    public Configuration getConf() {
        return conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    protected void setAdditionalConfigurations(String additionalConfigurations){
        if(!StringUtils.isNullOrEmpty(additionalConfigurations)){
            String[] configItems = additionalConfigurations.split(" ");
            for(String configItem : configItems){
                if(!configItem.startsWith("-D"))
                    continue;
                String[] keyValue = configItem.split("=");
                if(keyValue.length != 2)
                    continue;
                String key = keyValue[0];
                String value = keyValue[1];
                logger.info(String.format("## setAdditionalConfigurations: key=%s; value=%s", key, value));
                conf.set(key, value);
            }
        }
    }

    public interface WaitCondition {
        boolean checkCondition() throws Throwable;
    }

    public void wait(int retry, WaitCondition condition)
            throws Throwable {
        int currentRetry = 0;
        while (currentRetry < retry) {
            if (condition.checkCondition()) {
                return;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
            ++currentRetry;
        }
        throw new TimeoutException();
    }

    protected boolean createTableOrExit(HTableDescriptor htd) {
        final byte[] tName = htd.getName();
        try {
            if (hba.tableExists(tName)) {
                    if (hba.isTableEnabled(tName))
                        hba.disableTable(tName);
                    hba.deleteTable(tName);
            }

            hba.createTable(htd);
            wait(10, new WaitCondition() {
                @Override
                public boolean checkCondition() throws IOException {
                    return hba.tableExists(tName);
                }
            });
            logger.info(String.format("Create table %s successfully!",
                    Bytes.toString(tName)));
        } catch (Throwable e) {
            logger.error(String.format("Create table %s failed! Stop test: %s",
                    Bytes.toString(tName), e.toString()));
            return false;
        }
        return true;

    }

    public class Puts {
        public List<Put> puts = new ArrayList<Put>();
        public List<String> data = new ArrayList<String>();
    }

    public Puts generatePuts(long recordCount, byte[] cf, byte[] qualifier, byte[] value) {
        Puts puts = new Puts();
        for (long i = 0; i < recordCount; i++) {
            String rowId = UUID.randomUUID().toString();
            puts.puts.add(new Put(Bytes.toBytes(rowId)).add(cf, qualifier, value));
            puts.data.add(rowId);
        }
        return puts;
    }




}

