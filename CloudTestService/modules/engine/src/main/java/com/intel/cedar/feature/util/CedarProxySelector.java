package com.intel.cedar.feature.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class CedarProxySelector extends ProxySelector{
    private final ProxySelector def;
    private String proxy;
    private int port;
    public CedarProxySelector(String proxy, int port){
        this.def = ProxySelector.getDefault();
        this.proxy = proxy;
        this.port = port;
    }
    @Override
    public void connectFailed(URI arg0, SocketAddress arg1, IOException arg2) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public List<Proxy> select(URI uri) {
        List<Proxy> proxies = new ArrayList<Proxy>();
        if(uri.getScheme().startsWith("http")){
            Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy, port));
            proxies.add(p);
        }
        else{
            proxies = def.select(uri);
        }
        return proxies;
    }
    
}