package com.intel.cedar.service.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

public class GatewayInfoBean extends BaseModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 7343242803829348790L;

    private Long id = -1L;
    private String host;
    private int mappedPorts;

    // private Long userId;
    // private Long timeStamp;

    public GatewayInfoBean() {

    }

    public void refresh() {
        set("Id", id);
        set("Host", host);
        set("mappedPorts", mappedPorts);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setMappedPorts(int mappedPorts) {
        this.mappedPorts = mappedPorts;
    }

    public int getMappedPorts() {
        return this.mappedPorts;
    }
}
