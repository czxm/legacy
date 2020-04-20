package com.intel.cedar.features.splitpoint.sanity.driver;

import java.lang.reflect.Constructor;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import com.intel.cedar.features.splitpoint.sanity.GenericDriver;
import com.intel.cedar.features.splitpoint.sanity.ParamType;
import com.intel.cedar.features.splitpoint.sanity.Transaction;

public class SimpleHttpDriver extends GenericDriver{
	private static final Integer MAX_TOTAL_CONNECTIONS = 100000;
	private static final Integer MAX_PER_ROUTE = 100000;
	private static final Integer CONN_TIMEOUT_MILLI = 5 * 1000;
	private static final Integer SO_TIMEOUT_MILLI = 30 * 60 * 1000;
	private static final String HTTP = "http";
	private static final Integer HTTP_SCHEME_PORT = 80;
	private static final Integer HTTPS_SCHEME_PORT = 443;
	private static final String HTTPS = "https";
	
	private ThreadSafeClientConnManager cm;
	private IdleConnectionMonitorThread monitorThread;
	HttpParams httpParams = new BasicHttpParams();
	
	List<ParamType> params = null;
	
    // SSL handler (ignore untrusted hosts)
    private static TrustManager truseAllManager = new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}
    };
	
	private static ThreadSafeClientConnManager getClientConnManager() {  
        SchemeRegistry schemeRegistry = new SchemeRegistry();  
        schemeRegistry.register(new Scheme(HTTP, HTTP_SCHEME_PORT, PlainSocketFactory.getSocketFactory()));
        try{
	        SSLContext sslcontext = SSLContext.getInstance("TLS");
	        sslcontext.init(null, new TrustManager[] { truseAllManager }, new SecureRandom());
	        SSLSocketFactory sf = new SSLSocketFactory(sslcontext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        schemeRegistry.register(new Scheme(HTTPS, HTTPS_SCHEME_PORT, sf));
        }
        catch(Exception e){
        	e.printStackTrace();
        }
        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(schemeRegistry);
        manager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        manager.setDefaultMaxPerRoute(MAX_PER_ROUTE);
        return manager;
    }
	
	private static CookieSpecFactory csf = new CookieSpecFactory() {
        public CookieSpec newInstance(HttpParams params) {
            return new BrowserCompatSpecEx();
        }
    };
    
    public SimpleHttpDriver(){
    	Runtime.getRuntime().addShutdownHook(new Thread(){
    		@Override
    		public void run(){
    			shutDown();
    		}
    	});
    }
	
	protected void prepare(List<ParamType> params){
		DefaultHttpClient.setDefaultHttpParams(httpParams);
		httpParams.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "ISO-8859-1");
		httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONN_TIMEOUT_MILLI);
		httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT_MILLI);
		cm = getClientConnManager();
		this.params = params;
		for(ParamType param : params){
			if(param.getName().equals("CookiePolicy")){
				httpParams.setParameter(ClientPNames.COOKIE_POLICY, param.getValue());				
			}
			else if(param.getName().equals("SO_Timeout")){
				httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.parseInt(param.getValue()) * 1000);
			}
			else if(param.getName().equals("Conn_Timeout")){
				httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Integer.parseInt(param.getValue()) * 1000);
			}
			else if(param.getName().equals("Restricted_Redirect")){
				httpParams.setParameter("Restricted_Redirect", Boolean.parseBoolean(param.getValue()));
			}
			else if(param.getName().startsWith("http.protocol")){
				httpParams.setParameter(param.getName(), Boolean.parseBoolean(param.getValue()));
			}
			else if(param.getName().equals("java.security.auth.login.config")){
				System.setProperty("java.security.auth.login.config", param.getValue());	
			}
			else if(param.getName().equals("java.security.krb5.conf")){
				System.setProperty("java.security.krb5.conf", param.getValue());
			}
			else if(param.getName().equals("sun.security.krb5.debug")){
				System.setProperty("sun.security.krb5.debug", param.getValue());
			}
			else if(param.getName().equals("javax.security.auth.useSubjectCredsOnly")){
				System.setProperty("javax.security.auth.useSubjectCredsOnly", param.getValue());
			}
		}		
		monitorThread = new IdleConnectionMonitorThread(cm);
		monitorThread.start();
		
	}
	
	protected HttpClient createHttpClient(){
		DefaultHttpClient httpclient = new DefaultHttpClient(cm, httpParams);
		AuthSchemeRegistry authSchemeRegistry = httpclient.getAuthSchemes();
		/*
		authSchemeRegistry.unregister(AuthPolicy.BASIC);
		authSchemeRegistry.unregister(AuthPolicy.DIGEST);
		authSchemeRegistry.unregister(AuthPolicy.NTLM);
		*/
		authSchemeRegistry.register(AuthPolicy.SPNEGO, new MyNegotiateSchemeFactory(null, true));
		httpclient.setAuthSchemes(authSchemeRegistry);
		if("relaxed".equals(httpParams.getParameter(ClientPNames.COOKIE_POLICY))){
			httpclient.getCookieSpecs().register("relaxed", csf);
		}
		Object v = httpParams.getParameter("Restricted_Redirect");
		if(!(v instanceof Boolean))
			v = new Boolean(false);
		httpclient.setRedirectStrategy(new RestrictedRedirectStrategy((Boolean)v));
		return httpclient;
	}
	
	@Override
	public Transaction createTransaction(String name) {
		Transaction tran = null;
		try{
			Class<?> cls = Class.forName(GenericDriver.class.getPackage().getName() + ".transaction." + name + "Transaction");
			Constructor con = cls.getConstructor(HttpClient.class, List.class);
			tran = (Transaction)con.newInstance(createHttpClient(), this.params);
		}
		catch(Throwable t){
			t.printStackTrace();
		}
		return tran;
	}	

	@Override
	public void shutDown() {
		monitorThread.shutdown();
		if(cm != null)
			cm.shutdown();
	}
	
	
	class IdleConnectionMonitorThread extends Thread {    
	    private final ClientConnectionManager connMgr;
	    private volatile boolean shutdown;
	    
	    public IdleConnectionMonitorThread(ClientConnectionManager connMgr) {
	        super();
	        this.connMgr = connMgr;
	    }

	    @Override
	    public void run() {
	        try {
	            while (!shutdown) {
	                synchronized (this) {
	                    wait(5000);
	                    // Close expired connections
	                    connMgr.closeExpiredConnections();
	                    // Optionally, close connections
	                    // that have been idle longer than 30 sec
	                    connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
	                }
	            }
	        } catch (InterruptedException ex) {
	            // terminate
	        }
	    }
	    
	    public void shutdown() {
	        shutdown = true;
	        synchronized (this) {
	            notifyAll();
	        }
	    }
	}
}
