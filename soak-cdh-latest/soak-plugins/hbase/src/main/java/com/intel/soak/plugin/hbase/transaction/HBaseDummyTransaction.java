package com.intel.soak.plugin.hbase.transaction;

import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.hbase.util.StringUtils;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTablePool;

import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: xhao1
 * Date: 11/22/13
 * Time: 2:19 AM
 * To change this template use File | Settings | File Templates.
 */
@Plugin(desc = "HBaseDummyTransaction", type = PLUGIN_TYPE.TRANSACTION)
public class HBaseDummyTransaction extends HBaseBaseTransaction {
    protected HTable htable;

    protected static final String DEFAULT_TABLE_NAME = "unknown_table";
    protected static final String DEFAULT_IS_AUTO_FLUSH = "false";
    protected static final String DEFAULT_WRITE_BUFFER_SIZE = "6291456"; //6 * 1024 * 1024; 6M
    protected static final String DEFAULT_IS_USE_POOL = "true";
    protected static final String DEFAULT_MAX_POOL_SIZE = "100";

    protected static HTablePool POOL;


    @Override
    public boolean startup() {
        try {
            //logger.info(String.format("Enter startup()"));
            super.startup();

            String tableNameStr = getParamValue("TableName");
            tableNameStr = StringUtils.setAsDefaultValueIfEmpty(tableNameStr, DEFAULT_TABLE_NAME);
            String isAutoFlushStr = getParamValue("IsAutoFlush");
            isAutoFlushStr = StringUtils.setAsDefaultValueIfEmpty(isAutoFlushStr, DEFAULT_IS_AUTO_FLUSH);
            String writeBufferSizeStr = getParamValue("WriteBufferSize");
            writeBufferSizeStr = StringUtils.setAsDefaultValueIfEmpty(writeBufferSizeStr, DEFAULT_WRITE_BUFFER_SIZE);
            String isUsePoolStr = getParamValue("IsUsePool");
            isUsePoolStr = StringUtils.setAsDefaultValueIfEmpty(isUsePoolStr,DEFAULT_IS_USE_POOL);
            String poolSizeStr = getParamValue("PoolSize");
            poolSizeStr = StringUtils.setAsDefaultValueIfEmpty(poolSizeStr, DEFAULT_MAX_POOL_SIZE);

            boolean isUsePool = (isUsePoolStr.equalsIgnoreCase("true")? true: false);
            if(isUsePool) {
                poolLock.lock();
                try {
                    if (POOL == null) {
                        POOL = new HTablePool(conf, Integer.parseInt(poolSizeStr));
                    }
                }
                finally {
                    poolLock.unlock();
                }
            }

            htable = getTable(isUsePool,tableNameStr);
            boolean isAutoFlush = isAutoFlushStr.equalsIgnoreCase("true")? true:false;
            htable.setAutoFlush(isAutoFlush);
            htable.setWriteBufferSize(Integer.parseInt(writeBufferSizeStr));

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IOException occurs during startup!");
            return false;
        } catch (Exception e){
            e.printStackTrace();
            logger.error("Exception occurs during startup!");
        }
        return true;

    }

    @Override
    public boolean beforeExecute() {
        //logger.info(String.format("Enter beforeExecute(), "));
        return true;
    }

    @Override
    public boolean execute() {
        //logger.info(String.format("Enter execute()" ));
        return true;
    }


    @Override
    public boolean afterExecute() {
        //logger.info(String.format("Enter afterExecute(), "));
        return true;
    }

    @Override
    public void kill() {
        //logger.info("Enter kill()");
    }

    @Override
    public void shutdown() {
        //logger.info("Enter shutdown()");
        if (htable != null) {
            try {
                htable.close();
            } catch (IOException e) {
                logger.error("Exception occurs during HTable close!");
            }
        }
    }

    protected HTable getTable(boolean isUsePool, String tableNameStr) throws IOException {
        if(!StringUtils.isNullOrEmpty(tableNameStr)){
            return isUsePool ? (HTable) POOL.getTable(tableNameStr) : new HTable(
                    conf, tableNameStr);
        }else{
            return null;
        }
    }

}
