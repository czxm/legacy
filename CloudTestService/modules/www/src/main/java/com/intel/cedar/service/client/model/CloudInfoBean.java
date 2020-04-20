package com.intel.cedar.service.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

public class CloudInfoBean extends BaseModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 4296991444564175644L;

    private Long id;
    private String cloudName;
    private String protocol;
    private String host;
    private Integer port;
    private boolean enabled;
    private boolean separated;
    private boolean secured;
    private String resourcePrefix;
    private String proxyHost;
    private String proxyPort;
    private String proxyAuth;
    private String proxyPass;
    private String accessKey;
    private String secretKey;

    public CloudInfoBean() {

    }

    public void refresh() {
        set("Id", id);
        set("CloudName", cloudName);
        set("Protocol", protocol);
        set("Enabled", enabled);
        set("Separated", separated);
        set("Host", host);
        set("Port", port);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getCloudName() {
        return cloudName;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setSeparated(boolean separated) {
        this.separated = separated;
    }

    public boolean isSeparated() {
        return separated;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    @Override
    public boolean equals(Object obj) {
        CloudInfoBean cloud;
        if (!(obj instanceof CloudInfoBean))
            return false;
        cloud = (CloudInfoBean) obj;
        if (id.equals(cloud.id) && cloudName.equals(cloud.cloudName)
                && (enabled == cloud.enabled) && (separated == cloud.separated)
                && protocol.equals(cloud.protocol) && host.equals(cloud.host)
                && port.equals(cloud.port))
            return true;
        return false;
    }

    public void setResourcePrefix(String resourcePrefix) {
        this.resourcePrefix = resourcePrefix;
    }

    public String getResourcePrefix() {
        return resourcePrefix;
    }

    public void setProxyHost(String proxy) {
        this.proxyHost = proxy;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyPort(String port) {
        this.proxyPort = port;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPass(String pass) {
        this.proxyPass = pass;
    }

    public String getProxyPass() {
        return proxyPass;
    }

    public void setProxyAuth(String proxyAuth) {
        this.proxyAuth = proxyAuth;
    }

    public String getProxyAuth() {
        return proxyAuth;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    public boolean isSecured() {
        return secured;
    }

}
