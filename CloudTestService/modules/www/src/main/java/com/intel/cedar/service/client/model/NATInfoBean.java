package com.intel.cedar.service.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

public class NATInfoBean extends BaseModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = -4885288024125582027L;

    private Long id = -1L;
    private Long instanceId;
    private Long gatewayId;
    private String name;
    private Integer port;
    private Integer mappedPort;

    public NATInfoBean() {

    }

    public NATInfoBean(String name, Integer port) {
        this.name = name;
        this.port = port;
    }

    public void refresh() {
        set("Id", id);
        set("InstanceId", instanceId);
        set("GatewayId", gatewayId);
        set("Name", name);
        set("Port", port);
        set("MappedPort", mappedPort);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setGatewayId(Long gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Long getGatewayId() {
        return gatewayId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return port;
    }

    public void setMappedPort(Integer mappedPort) {
        this.mappedPort = mappedPort;
    }

    public Integer getMappedPort() {
        return mappedPort;
    }

}
