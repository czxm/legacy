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
public class GaugeRequest extends Payload {
    public static enum RequestKey{
        JobName,
        Metrics,
        Logs,
        GangliaRequest
    }

    public static enum RequestType{
        Initialize,
        SendMetrics,
        SendLogs,
        GetGangliaMetrics,
        Finalize
    }

    @Override
    public String getConsumer(){
        return "gauge";
    }

    private HashMap<RequestKey, Object> items;
    private RequestType type;

    public GaugeRequest(){
        this.items = new HashMap<RequestKey, Object>();
    }

    public GaugeRequest setItem(RequestKey key, Object value){
        this.items.put(key, value);
        return this;
    }

    public GaugeRequest setType(RequestType type){
        this.type = type;
        return this;
    }

    public <T>T getItem(RequestKey name){
        return (T)items.get(name);
    }

    public RequestType getType(){
        return this.type;
    }
}

