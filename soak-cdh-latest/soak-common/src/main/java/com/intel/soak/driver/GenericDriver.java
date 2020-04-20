package com.intel.soak.driver;

import com.intel.soak.logger.DriverLogger;
import com.intel.soak.model.ParamType;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.transaction.Transaction;

import java.util.List;

@Plugin(desc="Generic Driver", type = PLUGIN_TYPE.DRIVER)
public class GenericDriver implements IDriver {
    protected List<ParamType> params;
    protected DriverLogger logger;
    
    public void setLogger(DriverLogger logger){
        this.logger = logger;
    }
    
    protected String getParamValue(String name){
        if(params != null){
            for(ParamType param : params){
                if(param.getName().equals(name))
                    return param.getValue();
            }
        }
        return null;
    }
    
    @Override
    public boolean startup(){
        return true;
    }

    @Override
    public void shutdown(){
    }

    @Override
    public void setParams(List<ParamType> params) {
        this.params = params;
    }

    @Override
    public void prepareTransaction(Transaction transaction) {
    }
}
