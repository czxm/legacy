package com.intel.soak.plugin.hive.driver;

import com.intel.soak.driver.GenericDriver;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.hive.HiveClientFactoryBean;
import com.intel.soak.plugin.hive.transaction.HiveCLITransaction;
import com.intel.soak.transaction.Transaction;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.sql.Driver;

@Plugin(desc = "Hive Driver", type = PLUGIN_TYPE.DRIVER)
public class HiveDriver extends GenericDriver {

    private HiveClientFactoryBean factoryBean;
    private SimpleDriverDataSource hiveDataSource;
    private String hiveCmd = "/usr/bin/hive";
    private String hiveUser = "hive";
    private String hivePass = "";

    public void setHiveClientFactoryBean(HiveClientFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    public void setHiveDataSource(SimpleDriverDataSource dataSource){
        this.hiveDataSource = dataSource;
    }

    @Override
    public boolean startup() {
        try{
            String server = getParamValue("hive-server");
            int port = Integer.parseInt(getParamValue("hive-port"));
            String v = getParamValue("hive-user");
            if(v != null){
                hiveUser = v;
            }
            v = getParamValue("hive-password");
            if(v != null){
                hivePass = v;
            }
            this.factoryBean.setHost(server);
            this.factoryBean.setPort(port);

            String clz = getParamValue("jdbcDriver");
            if(clz == null)
                clz = "org.apache.hive.jdbc.HiveDriver";
            this.hiveDataSource.setDriverClass(Class.forName(clz).asSubclass(Driver.class));
            this.hiveDataSource.setUrl("jdbc:hive2://" + server + ":" + port + "/default");
            this.hiveDataSource.setUsername(hiveUser);
            this.hiveDataSource.setPassword(hivePass);

            String cmd = getParamValue("hive-cmd");
            if(cmd != null && cmd.length() > 0){
                hiveCmd = cmd;
            }
            return true;
        }
        catch(Exception e){
            logger.error(e.getMessage());
        }
        return false;
    }

    @Override
    public void prepareTransaction(Transaction transaction){
        if(transaction instanceof HiveCLITransaction){
            ((HiveCLITransaction)transaction).setHiveCmd(hiveCmd);
        }
    }
}
