package com.intel.cedar.agent.impl;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

public class HttpClientUtils {
    private static final Integer MAX_TOTAL_CONNECTIONS = 100;
    private static final Integer MAX_PER_ROUTE = 100;
    private static final Integer CONN_TIMEOUT_MILLI = 60 * 1000;
    private static final Integer SO_TIMEOUT_MILLI = 300 * 1000;
    private static final Integer QUICK_CONN_TIMEOUT_MILLI = 10 * 1000;
    private static final Integer QUICK_SO_TIMEOUT_MILLI = 30 * 1000;
    private static final String HTTP = "http";
    private static final Integer HTTP_SCHEME_PORT = 80;
    private static final Integer HTTPS_SCHEME_PORT = 443;
    private static final String HTTPS = "https";

    public static HttpClient getHttpClient(ClientConnectionManager connManager, boolean quickTimeOut) {
        return new DefaultHttpClient(connManager, getParams(quickTimeOut));
    }

    public static ThreadSafeClientConnManager getClientConnManager() {
        // Create and initialize scheme registry
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme(HTTP, HTTP_SCHEME_PORT,
                PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme(HTTPS, HTTPS_SCHEME_PORT,
                SSLSocketFactory.getSocketFactory()));
        // Create an HttpClient with the ThreadSafeClientConnManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(
                schemeRegistry);
        manager.setDefaultMaxPerRoute(MAX_PER_ROUTE);
        manager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        return manager;
    }

    private static HttpParams getParams(boolean quickTimeOut) {
        // Create and initialize HTTP parameters
        HttpParams params = new BasicHttpParams();
        DefaultHttpClient.setDefaultHttpParams(params);
        params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET,
                "ISO-8859-1");
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);
        if(quickTimeOut){
            params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                    QUICK_CONN_TIMEOUT_MILLI);
            params.setParameter(CoreConnectionPNames.SO_TIMEOUT, QUICK_SO_TIMEOUT_MILLI);
        }
        else{
            params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                    CONN_TIMEOUT_MILLI);
            params.setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT_MILLI);
        }
        return params;
    }
}