package com.intel.bigdata.common.protocol;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: xzhan27
 * Date: 12/6/13
 * Time: 11:18 AM
 */
public abstract class Payload implements Serializable{
    public String getConsumer(){
        return null;
    }
}
