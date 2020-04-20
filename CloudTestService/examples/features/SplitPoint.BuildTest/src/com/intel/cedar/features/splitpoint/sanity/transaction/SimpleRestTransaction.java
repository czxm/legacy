package com.intel.cedar.features.splitpoint.sanity.transaction;

import java.util.HashMap;
import java.util.List;

import org.apache.http.client.HttpClient;

import com.intel.cedar.features.splitpoint.sanity.ParamType;

public class SimpleRestTransaction extends SimpleHttpTransaction {

	protected static enum ReservedParam{
		proxy,
		port,
		proxyUser,
		proxyPasswd,
		ordered,
		sequential,
		wait,
		base,
		body,
		args,
		ContentType,
		GET,
		POST,
		DELETE,
		PUT
	}
	
	protected static boolean isReserved(String name){
		for(ReservedParam n : ReservedParam.values()){
			if(n.name().equals(name)){
				return true;
			}
		}
		return false;
	}
	
	private HashMap<String, String> restMethods;
	protected HashMap<String, String> getRestMethods(){
		if(restMethods == null){
			restMethods = new HashMap<String, String>();
		}
		return restMethods;
	}
	
	public SimpleRestTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		for(ParamType p : params){
			if(!p.getName().startsWith("/") && !isReserved(p.getName())){
				String v = p.getValue();
				if(v != null && v.length() > 0){
					getRestMethods().put(p.getName(), v);
				}
			}
		}
	}
	
	protected String getFullURL(String url, Object[] args){
		if(url != null && url.length() > 0){
			url = getRestMethods().get(url);
		}
		return super.getFullURL(url, args);
	}

}
