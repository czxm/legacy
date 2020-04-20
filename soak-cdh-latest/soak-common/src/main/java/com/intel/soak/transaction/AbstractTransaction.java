package com.intel.soak.transaction;

import com.intel.soak.logger.TransactionLogger;
import com.intel.soak.model.ParamType;
import com.intel.soak.vuser.VUserData;

import java.util.List;

public abstract class AbstractTransaction implements Transaction {

    protected List<ParamType> params;
    protected VUserData userData;
    protected TransactionLogger logger;

    protected String getParamValue(String name){
        if(params != null){
            for(ParamType param : params){
                if(param.getName().equals(name))
                    return param.getValue();
            }
        }
        return null;
    }

    protected VUserData getUserData(){
        return this.userData;
    }

    @Override
    public void setParams(List<ParamType> params){
        this.params = params;
    }

    @Override
    public void setUserData(VUserData userData) {
        this.userData = userData;
    }

    @Override
    public void setLogger(TransactionLogger logger) {
        this.logger = logger;
    }

    @Override
    public boolean startup() {
        return true;
    }

    @Override
    public boolean beforeExecute() {
        return true;
    }

    @Override
    public boolean afterExecute() {
        return true;
    }

    @Override
    public void shutdown() {
    }
    
    @Override
    public void kill() {
    }
}
