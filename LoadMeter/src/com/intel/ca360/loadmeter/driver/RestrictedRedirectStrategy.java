package com.intel.ca360.loadmeter.driver;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestrictedRedirectStrategy extends DefaultRedirectStrategy {
	private static final Logger LOG = LoggerFactory.getLogger(RestrictedRedirectStrategy.class);
	
	private Boolean restricted = false;
	
	public RestrictedRedirectStrategy(Boolean v){
		this.restricted = v;
	}
	
    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        if(response == null)
            throw new IllegalArgumentException("HTTP response may not be null");
        int statusCode = response.getStatusLine().getStatusCode();
        String method = request.getRequestLine().getMethod();
        Header locationHeader = response.getFirstHeader("location");
        boolean doRedirect = false;
        switch(statusCode){
        case 302: 
        	doRedirect = (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("HEAD")) && locationHeader != null;
        	break;
        case 301: 
        case 307: 
        	doRedirect = method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("HEAD");
        	break;
        case 303: 
        	doRedirect = true;
        	break;
        case 304: 
        case 305: 
        case 306: 
        default:
        	doRedirect = false;
        }
        
        if(locationHeader == null)
        	return false;
        
        boolean result = true;
        if(restricted){
        	String url = locationHeader.getValue();
        	int paramIndex = url.indexOf("?");
        	if(paramIndex > 0){
        		url = url.substring(0, paramIndex);
        	}
        	result = doRedirect && url.contains(((HttpHost)context.getAttribute("http.target_host")).getHostName());
        }
        else{
        	result = doRedirect;
        }
        LOG.debug("Redirect to " + locationHeader.getValue() + " is " + (result ? "allowed" : "prohibited"));
        return result;
    }
}
