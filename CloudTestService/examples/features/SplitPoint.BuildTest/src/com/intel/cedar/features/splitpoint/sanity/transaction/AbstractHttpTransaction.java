package com.intel.cedar.features.splitpoint.sanity.transaction;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.features.splitpoint.sanity.ParamType;
import com.intel.cedar.features.splitpoint.sanity.Transaction;

public abstract class AbstractHttpTransaction implements Transaction {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractHttpTransaction.class);
	
	protected static interface TransactionChecker{
		public boolean check(HttpResponse response, String content);
	}
	
	protected HttpClient client;
	protected List<ParamType> params;
	protected String proxy = "unreachable";
	protected String port = "N/A";
	protected String proxyUser = null;
	protected String proxyPasswd = null;
	protected String proxyExclusion = null;
	protected String contentType = null;
	private boolean autoPostResponse;
	
	public AbstractHttpTransaction(HttpClient client, List<ParamType> params){
		this.client = client;
		this.params = params;
		this.autoPostResponse = false;
		for(ParamType p : params){
			if(p.getName().equals("proxy")){
				proxy = p.getValue();
			}
			else if(p.getName().equals("port")){
				port = p.getValue();
			} 
			else if(p.getName().equals("proxyUser")){
				proxyUser = p.getValue();
			}
			else if(p.getName().equals("proxyPasswd")){
				proxyPasswd = p.getValue();
			}
			else if(p.getName().equals("proxyExclusion")){
				proxyExclusion = p.getValue();
			}
		}
	}
	
	public boolean isReservedName(String name){
		if(name != null && name.length() > 0){
			if(name.equals("proxy") || name.equals("port") || name.equals("proxyUser") || name.equals("proxyPasswd"))
				return true;
		}
		return false;
	}
	
	protected void setAutoPostResponse(boolean flag){
		this.autoPostResponse = flag;
	}
	
	protected boolean isAutoPostResponse(){
		return this.autoPostResponse;
	}
	
	protected String getContentType(){
		return this.contentType;
	}
	
	protected void setContentType(String type){
		this.contentType = type;
	}
	
	protected String getProxyServer(){
		return this.proxy;
	}
	
	protected String getProxyPort(){
		return this.port;
	}
	
	protected String getProxyUser(){
		return this.proxyUser;
	}
	
	protected String getProxyPasswd(){
		return this.proxyPasswd;
	}
	
	public void setup(){
	}

	public boolean startup() {
		return true;
	}
	
	public void beforeExecute(){
		this.enableProxy();
	}
	
	public void afterExecute(){
		this.disableProxy();
	}
	
	public void shutdown(){		
	}
	
	protected boolean checkResponse(HttpResponse response, String content, Object check){
		if(check instanceof String){
			return content.contains((String)check);
		}
		else if(check instanceof Pattern){
			Matcher matcher =((Pattern)check).matcher(content);
			return (matcher != null) && matcher.matches();
		}
		else if(check instanceof TransactionChecker)
			return ((TransactionChecker) check).check(response, content);
		else if(check != null)
			return false;
		else
			return true;
	}
	
	protected String getAutoSubmitTarget(String content){
    	return Util.stringRegexMatch("<FORM METHOD=\"POST\" ACTION=\"([^\">]+)\">", content);
	}
	
    protected HashMap<String, String> getAutoSubmitParams(String line){
    	HashMap<String, String> newParams = new HashMap<String, String>();
		String samlResponseValue = Util.stringRegexMatch("<INPUT TYPE=\"HIDDEN\" NAME=\"SAMLResponse\" VALUE=\"([^\">]+)\">", line);
		String samlRequestValue = Util.stringRegexMatch("<INPUT TYPE=\"HIDDEN\" NAME=\"SAMLRequest\" VALUE=\"([^\">]+)\">", line);
		String iceResponseValue = Util.stringRegexMatch("<INPUT TYPE=\"HIDDEN\" NAME=\"ICEResponse\" VALUE=\"([^\">]+)\">", line);
		String relayStateValue = Util.stringRegexMatch("<INPUT TYPE=\"HIDDEN\" NAME=\"RelayState\" VALUE=\"([^\">]+)\">", line);
		if(relayStateValue != null && relayStateValue.length() > 0){
			relayStateValue = StringEscapeUtils.unescapeHtml(relayStateValue);
		}
    	if(samlResponseValue != null && samlResponseValue.length() > 0){
    		newParams.put("SAMLResponse", samlResponseValue);
    		if(relayStateValue != null && relayStateValue.length() > 0){
    			newParams.put("RelayState", relayStateValue);
    		}
    	}
    	else if(samlRequestValue != null && samlRequestValue.length() > 0){
    		newParams.put("SAMLRequest", samlRequestValue);
    		if(relayStateValue != null && relayStateValue.length() > 0){
    			newParams.put("RelayState", relayStateValue);
    		}		        		
    	}
    	else if(iceResponseValue != null && iceResponseValue.length() > 0){
    		newParams.put("ICEResponse", iceResponseValue);
    		if(relayStateValue != null && relayStateValue.length() > 0){
    			newParams.put("RelayState", relayStateValue);
    		}		        		
    	}
    	return newParams;
    }
    
    public boolean postRequest(String url, HashMap<String, String> params, Object check){
    	return postRequest(url, params, check, null);
    }
    
    public boolean postRequest(String url, String body, Object check){
    	return postRequest(url, body, check, null);
    }   
    
    public boolean getRequest(String url, Object check){  	
    	return getRequest(url, check, null);
    }	
    
    public boolean deleteRequest(String url, Object check){
        return deleteRequest(url, check, null);
    }
    
    public boolean putRequest(String url, String body, Object check){
    	return putRequest(url, body, check, null);
    }   
    
    public boolean postRequest(String url, HashMap<String, String> params, Object check, Credentials cred){
    	HttpPost post = null;
      	try{
    		if(cred != null){
    			((DefaultHttpClient)this.client).getCredentialsProvider().setCredentials(
    				new AuthScope(null, -1, null),
    				cred);
    		}
    		else{
    			((DefaultHttpClient)this.client).getCredentialsProvider().clear();
    		}
    		post = new HttpPost(StringEscapeUtils.unescapeHtml(url));
			if(params.size() > 0){
				ArrayList<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>();
				for(Entry<String, String> e : params.entrySet()){
					postParams.add(new BasicNameValuePair(e.getKey(), e.getValue()));	
				}
				post.setEntity(new UrlEncodedFormEntity(postParams, "UTF-8"));
			}
			HttpResponse response = null;
			if(cred != null)
				response = this.client.execute(post, createHttpContext((DefaultHttpClient)this.client));
			else
				response = this.client.execute(post);
			HttpEntity entity = response.getEntity();
			StringWriter writer = new StringWriter();
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			String line = "";
			while((line = reader.readLine()) != null){
				writer.append(line); 
			}
			line = writer.toString();
			EntityUtils.consume(entity);
			
			if(this.autoPostResponse){
		    	String postTarget = getAutoSubmitTarget(line);
		    	if(postTarget != null && postTarget.length() > 0){
					HashMap<String, String> newParams = getAutoSubmitParams(line);
			        if(newParams.size() > 0)
			        	return postRequest(postTarget, newParams, check, cred);
		        }
		    }
			
			if(checkResponse(response, line, check)){
				return true;
			}
			else{
				LOG.error(line);
			}
    	}
    	catch(Exception e){
    		LOG.error(e.getMessage(), e);
    		if(post != null){
    			post.abort();
    		}
    	}
    	return false;
    }
    
    public boolean postRequest(String url, String body, Object check, Credentials cred){
    	HttpPost post = null;
      	try{
    		if(cred != null){
    			((DefaultHttpClient)this.client).getCredentialsProvider().setCredentials(
    				new AuthScope(null, -1, null),
    				cred);
    		}
    		else{
    			((DefaultHttpClient)this.client).getCredentialsProvider().clear();
    		}
    		post = new HttpPost(StringEscapeUtils.unescapeHtml(url));
			if(body != null){
				byte[] content = body.getBytes();
				InputStreamEntity ise = new InputStreamEntity(new ByteArrayInputStream(content), content.length);
				if(this.getContentType() != null){
					post.setHeader("Content-Type", contentType);
				}
				post.setEntity(ise);
			}
			HttpResponse response = null;
			if(cred != null)
				response = this.client.execute(post, createHttpContext((DefaultHttpClient)this.client));
			else
				response = this.client.execute(post);
			HttpEntity entity = response.getEntity();
			StringWriter writer = new StringWriter();
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			String line = "";
			while((line = reader.readLine()) != null){
				writer.append(line); 
			}
			line = writer.toString();
			EntityUtils.consume(entity);
			
			if(this.autoPostResponse){
		    	String postTarget = getAutoSubmitTarget(line);
		    	if(postTarget != null && postTarget.length() > 0){
					HashMap<String, String> newParams = getAutoSubmitParams(line);
			        if(newParams.size() > 0)
			        	return postRequest(postTarget, newParams, check, cred);
		        }
		    }
			
			if(checkResponse(response, line, check)){
				return true;
			}
			else{
				LOG.error(line);
			}
    	}
    	catch(Exception e){
    		LOG.error(e.getMessage(), e);
    		if(post != null){
    			post.abort();
    		}
    	}
    	return false;
    }
    
    public boolean putRequest(String url, String body, Object check, Credentials cred){
    	HttpPut put = null;
      	try{
    		if(cred != null){
    			((DefaultHttpClient)this.client).getCredentialsProvider().setCredentials(
    				new AuthScope(null, -1, null),
    				cred);
    		}
    		else{
    			((DefaultHttpClient)this.client).getCredentialsProvider().clear();
    		}
    		put = new HttpPut(StringEscapeUtils.unescapeHtml(url));
			if(body != null){
				byte[] content = body.getBytes();
				InputStreamEntity ise = new InputStreamEntity(new ByteArrayInputStream(content), content.length);
				if(this.getContentType() != null){
					put.setHeader("Content-Type", contentType);
				}
				put.setEntity(ise);
			}
			HttpResponse response = null;
			if(cred != null)
				response = this.client.execute(put, createHttpContext((DefaultHttpClient)this.client));
			else
				response = this.client.execute(put);
			HttpEntity entity = response.getEntity();
			StringWriter writer = new StringWriter();
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			String line = "";
			while((line = reader.readLine()) != null){
				writer.append(line); 
			}
			line = writer.toString();
			EntityUtils.consume(entity);
			
			if(this.autoPostResponse){
		    	String postTarget = getAutoSubmitTarget(line);
		    	if(postTarget != null && postTarget.length() > 0){
					HashMap<String, String> newParams = getAutoSubmitParams(line);
			        if(newParams.size() > 0)
			        	return postRequest(postTarget, newParams, check, cred);
		        }
		    }
			
			if(checkResponse(response, line, check)){
				return true;
			}
			else{
				LOG.error(line);
			}
    	}
    	catch(Exception e){
    		LOG.error(e.getMessage(), e);
    		if(put != null){
    			put.abort();
    		}
    	}
    	return false;
    }
    
    public boolean getRequest(String url, Object check, Credentials cred){
    	HttpGet get = null;
    	try{
    		if(cred != null){
    			((DefaultHttpClient)this.client).getCredentialsProvider().setCredentials(
    				new AuthScope(null, -1, null),
    				cred);
    		}
    		else{
    			((DefaultHttpClient)this.client).getCredentialsProvider().clear();
    		}
			get = new HttpGet(StringEscapeUtils.unescapeHtml(url));
			HttpResponse response = null;
			if(cred != null)
				response = this.client.execute(get, createHttpContext((DefaultHttpClient)this.client));
			else
				response = this.client.execute(get);
			HttpEntity entity = response.getEntity();
			StringWriter writer = new StringWriter();
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			String line = "";
			while((line = reader.readLine()) != null){
				writer.append(line); 
			}
			line = writer.toString();			
			EntityUtils.consume(entity);
			
			if(this.autoPostResponse){
				String postTarget = getAutoSubmitTarget(line);
		    	if(postTarget != null && postTarget.length() > 0){
		    		HashMap<String, String> newParams = getAutoSubmitParams(line);
			        if(newParams.size() > 0)
			        	return postRequest(postTarget, newParams, check, cred);
		        }
			}
			
			if(checkResponse(response, line, check)){
				return true;
			}
			else{
				LOG.error(line);
			}
    	}
    	catch(Exception e){
    		LOG.error(e.getMessage(), e);
    		get.abort();
    	}
    	return false;    	    		
    }	
    
    public boolean deleteRequest(String url, Object check, Credentials cred){
    	HttpDelete delete = null;
    	try{
    		if(cred != null){
    			((DefaultHttpClient)this.client).getCredentialsProvider().setCredentials(
    				new AuthScope(null, -1, null),
    				cred);
    		}
    		else{
    			((DefaultHttpClient)this.client).getCredentialsProvider().clear();
    		}
    		delete = new HttpDelete(StringEscapeUtils.unescapeHtml(url));
			HttpResponse response = null;
			if(cred != null)
				response = this.client.execute(delete, createHttpContext((DefaultHttpClient)this.client));
			else
				response = this.client.execute(delete);
			HttpEntity entity = response.getEntity();
			StringWriter writer = new StringWriter();
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			String line = "";
			while((line = reader.readLine()) != null){
				writer.append(line); 
			}
			line = writer.toString();			
			EntityUtils.consume(entity);
			
			if(this.autoPostResponse){
				String postTarget = getAutoSubmitTarget(line);
		    	if(postTarget != null && postTarget.length() > 0){
		    		HashMap<String, String> newParams = getAutoSubmitParams(line);
			        if(newParams.size() > 0)
			        	return postRequest(postTarget, newParams, check, cred);
		        }
			}
			
			if(checkResponse(response, line, check)){
				return true;
			}
			else{
				LOG.error(line);
			}
    	}
    	catch(Exception e){
    		LOG.error(e.getMessage(), e);
    		delete.abort();
    	}
    	return false;    	    		
    }    
    
    public void clearCookies(){
		((DefaultHttpClient)this.client).getCookieStore().clear();
    }
    
    public void enableProxy(){
    	if(proxyExclusion == null){
    		proxyExclusion = "localhost|127.0.0.1|" + proxy;
    	}
    	else{
    		proxyExclusion = proxyExclusion + "|localhost|127.0.0.1|" + proxy;
    	}
		if(!proxy.equals("unreachable") && !port.equals("N/A"))
			this.enableProxy(proxy, Integer.parseInt(port), proxyUser == null ? false : true, proxyUser, proxyPasswd, proxyExclusion);
    }
    
    public void disableProxy() {
    	((DefaultHttpClient)client).getCredentialsProvider().clear();
    	((DefaultHttpClient)client).setRoutePlanner(null);
    }
 
    public void enableProxy(final String proxyHost, final int proxyPort,
            boolean needAuth, String username, String password,
            final String nonProxyHostRegularExpression) {
    	DefaultHttpClient httpClient = (DefaultHttpClient)client;
        if (needAuth) {
        	httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(proxyHost, proxyPort),
                    new UsernamePasswordCredentials(username, password));
        }
        // Simple proxy setting, can't handle non-proxy-host
        // httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,new
        // HttpHost(proxyHost, proxyPort));
        httpClient.setRoutePlanner(new HttpRoutePlanner() {
			@Override
			public HttpRoute determineRoute(HttpHost target,
                    HttpRequest request, HttpContext contenxt) throws HttpException {
                HttpRoute proxyRoute = new HttpRoute(target, null,
                        new HttpHost(proxyHost, proxyPort), "https"
                                .equalsIgnoreCase(target.getSchemeName()));
                if (nonProxyHostRegularExpression == null) {
                    return proxyRoute;
                }
                if(nonProxyHostRegularExpression.equals(target.getHostName()))
                    return new HttpRoute(target, null, "https".equalsIgnoreCase(target.getSchemeName()));                	
                Pattern pattern = Pattern
                        .compile(nonProxyHostRegularExpression,
                                Pattern.CASE_INSENSITIVE);
                Matcher m = pattern.matcher(target.getHostName());
                if (m.find()) {
                    return new HttpRoute(target, null, "https".equalsIgnoreCase(target.getSchemeName()));
                } else {
                    return proxyRoute;
                }
			}
        });
    }    
    
	/**
	 * createHttpContext - This is a copy of DefaultHttpClient method
	 * createHttpContext with "negotiate" added to AUTH_SCHEME_PREF to allow for 
	 * Kerberos authentication. Could also extend DefaultHttpClient overriding the
	 * default createHttpContext.
	 * 
	 * @param httpclient - our Httpclient
	 * @return HttpContext
	 */
	static HttpContext createHttpContext(DefaultHttpClient httpclient){
		HttpContext context = new BasicHttpContext();
		context.setAttribute(
				ClientContext.AUTHSCHEME_REGISTRY, 
				httpclient.getAuthSchemes());
		context.setAttribute(
				ClientContext.AUTH_SCHEME_PREF, 
				Collections.unmodifiableList( Arrays.asList(new String[] {
						"negotiate",
						"ntlm",
						"digest",
						"basic" 
				}))
		);
		context.setAttribute(
				ClientContext.COOKIESPEC_REGISTRY, 
				httpclient.getCookieSpecs());
		context.setAttribute(
				ClientContext.COOKIE_STORE, 
				httpclient.getCookieStore());
		context.setAttribute(
				ClientContext.CREDS_PROVIDER, 
				httpclient.getCredentialsProvider());
		return context;
	}
}
