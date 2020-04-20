package com.intel.bigdata.common.protocol;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: holm
 * Date: 10/22/13
 * Time: 5:34 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
public class AgentConfig implements Serializable {

    private String hostIdentifier;
    private String hostName;

    public AgentConfig(){
    }

    public AgentConfig(String hostIdentifier, String hostName) {
        this.hostIdentifier = hostIdentifier;
        this.hostName = hostName;
    }

    public String getHostIdentifier() {
        return hostIdentifier;
    }

    public void setHostIdentifier(String hostIdentifier) {
        this.hostIdentifier = hostIdentifier;
    }

    public String getHostName(){
        return hostName;
    }

    public void setHostName(String hostName){
        this.hostName = hostName;
    }

    @Override
    public String toString() {
        return "AgentConfig{" +
                "hostIdentifier=" + hostIdentifier +";hostname="+hostName+"}";
    }
}
