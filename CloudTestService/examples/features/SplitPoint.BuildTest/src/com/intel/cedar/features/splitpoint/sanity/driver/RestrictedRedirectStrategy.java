package com.intel.cedar.features.splitpoint.sanity.driver;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

public class RestrictedRedirectStrategy extends DefaultRedirectStrategy {
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
        if(restricted){
        	return doRedirect && locationHeader.getValue().contains(((HttpHost)context.getAttribute("http.target_host")).getHostName());
        }
        else{
        	return doRedirect;
        }
    }
}
