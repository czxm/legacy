package com.intel.soak.protocol;

import com.intel.bigdata.common.protocol.Payload;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: xzhan27
 * Date: 12/5/13
 * Time: 4:31 PM
 */
public class SoakRequest extends Payload {
    public static enum RequestKey{
        Config,
        VUserData,
        Plugins,
        Timestamp,
        Node,
        VUsers
    }

    public static enum RequestType{
        Initialize,
        CreateVUser,
        CheckPoint,
        VUserFinished,
        KillVUsers,
        Finalize
    }

    @Override
    public String getConsumer(){
        return "soak";
    }

    private String jobId;
    private HashMap<RequestKey, Object> items;
    private RequestType type;

    public SoakRequest(String jobId){
        this.jobId = jobId;
        this.items = new HashMap<RequestKey, Object>();
    }

    public SoakRequest setItem(RequestKey key, Object value){
        this.items.put(key, value);
        return this;
    }

    public SoakRequest setType(RequestType type){
        this.type = type;
        return this;
    }

    public String getJobId(){
        return jobId;
    }

    public <T>T getItem(RequestKey name){
        return (T)items.get(name);
    }

    public RequestType getType(){
        return this.type;
    }
}
