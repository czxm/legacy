package com.intel.soak.util;

import com.intel.soak.model.ParamType;

import java.util.List;

public enum CMClientFactory {

    INSTANCE;

    protected static String getParamValue(String name, List<ParamType> params){
        if(params != null){
            for(ParamType param : params){
                if(param.getName().equals(name))
                    return param.getValue();
            }
        }
        return null;
    }

    private static String cm_username;
    private static String cm_password;
    private static String cm_host;
    private static String cm_port;
    private static String cm_cluster;

    public void setupFromParams(List<ParamType> params){
        cm_username = getParamValue("cm_username", params);
        cm_password = getParamValue("cm_password", params);
        cm_host = getParamValue("cm_host", params);
        cm_port = getParamValue("cm_port", params);
        cm_cluster = getParamValue("cm_cluster", params);
    }

    public CMClient createClient(){
        return new CMClient(cm_username, cm_password, cm_host, Integer.parseInt(cm_port), cm_cluster);
    }
}
