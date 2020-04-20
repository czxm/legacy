package com.intel.ca360.loadmeter.transaction;

import java.util.List;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.ca360.config.ParamType;


public class SamlKCDSSOTransaction extends ReverseProxyTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(SamlKCDSSOTransaction.class);

	public SamlKCDSSOTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
	}

	@Override
	public boolean execute(boolean negative) {
		boolean result = false;
		try{
			if(negative){
				clearCookies();
				result = login(true);
			}
			else{
				result = login(false);
				this.setAutoPostResponse(true);
				if(result && !spnegoAppURL.equals("unreachable")){
					result = getRequest(spnegoAppURL, username);
				}
				if(result && !appURL.equals("unreachable")){
					result = getRequest(appURL, appMatch);
				}
				this.setAutoPostResponse(false);
				result = logout();
			}
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		finally{
			if(negative || !result || !doLogout){
				clearCookies();
			}
		}
		return result;
	}

	@Override
	public boolean startup() {
		try{			
			return getRequest(appLoginURL, "SAMLRequest");
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		return false;
	}
}
