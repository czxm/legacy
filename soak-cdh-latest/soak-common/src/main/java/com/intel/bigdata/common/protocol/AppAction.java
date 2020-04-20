package com.intel.bigdata.common.protocol;

import java.io.Serializable;

public abstract class AppAction extends Payload {
    public String appId;

    public String getAppId(){
        return appId;
    }

    public void setAppId(String appId){
        this.appId = appId;
    }

}
