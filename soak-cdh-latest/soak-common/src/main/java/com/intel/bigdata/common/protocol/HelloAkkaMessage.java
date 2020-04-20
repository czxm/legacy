package com.intel.bigdata.common.protocol;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/4/13
 * Time: 5:48 PM
 * To change this template use File | Settings | File Templates.
 */
@AppAnnotation(type = ExecType.Concurrent, appActor = "SampleActor", appClass = "com.intel.bigdata.agent.AppExecutors.SampleApp")
public class HelloAkkaMessage extends Payload implements Serializable {
    public String id;
    public String message;

    public HelloAkkaMessage(String id, String message){
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
