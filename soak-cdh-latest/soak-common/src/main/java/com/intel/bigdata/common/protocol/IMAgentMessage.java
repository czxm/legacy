package com.intel.bigdata.common.protocol;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/4/13
 * Time: 5:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class IMAgentMessage extends Payload {

    public String id;
    public String message;
    public IMAgentMessage(String id, String message){
        this.id = id;
        this.message = message;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }
}
