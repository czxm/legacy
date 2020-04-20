package com.intel.soak.plugin.hive.transaction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.intel.soak.logger.TransactionLogger;
import com.intel.soak.model.ParamType;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.transaction.AbstractTransaction;
import com.intel.soak.plugin.hive.HiveScript;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Plugin( desc = "Hive Thrift", type = PLUGIN_TYPE.TRANSACTION )
public class HiveThriftTransaction extends AbstractTransaction {

    protected HiveAuthzTemplate template;
    protected HiveScript script;
    protected String authnScheme="SIMPLE";

    public void setHiveAuthzTemplate(HiveAuthzTemplate template){
        this.template = template;
    }       
    
    @Override
    public void setLogger(TransactionLogger logger) {
        super.setLogger(logger);
        template.setTransactionLogger(logger);
    }

    @Override
    public boolean startup(){
        String scheme = getParamValue("authnScheme");
        if(scheme != null)
            authnScheme = scheme;
        String scriptSrc = getParamValue("script");
        if(scriptSrc == null || scriptSrc.length() == 0)
            return false;
        HashMap<String, String> args = new HashMap<String, String>();
        args.put("user", getUserData().getUsername());
        for(ParamType p : this.params){
            if(!p.getName().equals("script")){
                args.put(p.getName(), p.getValue());
            }
        }
        ClassLoader cl = this.getClass().getClassLoader();
        if(new File(scriptSrc).exists()){
            script = new HiveScript(new FileSystemResource(scriptSrc), args);
        }
        else if(cl.getResourceAsStream(scriptSrc) != null){
            script = new HiveScript(new ClassPathResource(scriptSrc, cl), args);
        }
        else{
            return false;
        }
        return true;
    }
    
    @Override
    public boolean execute() {
        try{
            template.setUserGroup(getUserData().getUsername(), new ArrayList<String>());
            for(String l : template.executeScript(script)){
                logger.info(l);
            }
            return true;
        }
        catch(Exception e){
            logger.error(e.getMessage());
        }
        return false;        
    }
}
