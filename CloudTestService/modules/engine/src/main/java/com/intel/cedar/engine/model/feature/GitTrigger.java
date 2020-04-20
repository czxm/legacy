package com.intel.cedar.engine.model.feature;

import com.intel.cedar.engine.model.IDataModelDocument;

public class GitTrigger extends ScmTrigger {
    private String proxyHost;
    private String proxyHost_bind;
    private int proxyPort;
    private String proxyPort_bind;
    private String privatekey;
    private String privatekey_bind;
    
    public GitTrigger(IDataModelDocument document) {
        super(document);
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getPrivatekey() {
        return privatekey;
    }

    public void setPrivatekey(String privatekey) {
        this.privatekey = privatekey;
    }

    public String getProxyHost_bind() {
        return proxyHost_bind;
    }

    public void setProxyHost_bind(String proxyHost_bind) {
        this.proxyHost_bind = proxyHost_bind;
    }

    public String getProxyPort_bind() {
        return proxyPort_bind;
    }

    public void setProxyPort_bind(String proxyPort_bind) {
        this.proxyPort_bind = proxyPort_bind;
    }

    public String getPrivatekey_bind() {
        return privatekey_bind;
    }

    public void setPrivatekey_bind(String privatekey_bind) {
        this.privatekey_bind = privatekey_bind;
    }
}
