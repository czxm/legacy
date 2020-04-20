package com.intel.cedar.features.splitpoint.sanity.transaction;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.features.splitpoint.sanity.ParamType;


public class LdapSSOTransaction extends AbstractHttpTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(LdapSSOTransaction.class);
			
	private String appLoginUrl = "unreachable";
	private String iceServerSSOUrl = "unreachable";
	private String logoutURL = "unreachable";
	private HashMap<String, String> loginParams = new HashMap<String, String>();
	private int tenances = 0;
	private static Pattern pat = Pattern.compile(".*Login.*|.*authnService.*", Pattern.DOTALL);
	private String username = "intel.cloudexpressway";
	private String password = "123456";
	private String matchString = "";
	private Pattern matchPattern = null;
	
	public LdapSSOTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		for(ParamType p : params){
			if(p.getName().equals("appLoginUrl")){
				appLoginUrl = p.getValue();
			}
			else if(p.getName().equals("iceServerSSOUrl")){
				iceServerSSOUrl = p.getValue();
			}
			else if(p.getName().equals("logoutURL")){
				logoutURL = p.getValue();
			}
			else if(p.getName().equals("tenances")){
				tenances = Integer.parseInt(p.getValue());
			}
			else if(p.getName().equals("username")){
				username = p.getValue();
			}
			else if(p.getName().equals("password")){
				password = p.getValue();
			}
			else if(p.getName().equals("matchString")){
				matchString = p.getValue();
			}
			else if(p.getName().equals("matchPattern")){
				matchPattern = Pattern.compile(p.getValue(), Pattern.DOTALL);
			}			
		}
	}

	@Override
	public boolean execute(boolean negative) {
		boolean result = false;
		try{			
			result = getRequest(appLoginUrl, pat) &&
				postRequest(iceServerSSOUrl, loginParams, matchPattern != null ? matchPattern : matchString);
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		finally{
			if(!result){
				clearCookies();
			}
		}
		return result;
	}
	
	@Override
	public void setup(){
		loginParams.put("Input0", username);
		loginParams.put("Input1", password);
		loginParams.put("moduleIndex", "0");
		this.setAutoPostResponse(true);
		try{
			if(tenances > 0){
				int userIndex = Integer.parseInt(username.replace("user", ""));
				int tenanceIndex = userIndex % tenances + 1;
				String tenanceName = "domain" + tenanceIndex;
				appLoginUrl = appLoginUrl.replace("splat", tenanceName);
				appLoginUrl = appLoginUrl.replace("8443", Integer.toString(8443 + (userIndex % tenances)));
				iceServerSSOUrl = iceServerSSOUrl.replace("splat", tenanceName);
				iceServerSSOUrl = iceServerSSOUrl.replace("8443", Integer.toString(8443 + (userIndex % tenances)));
				logoutURL = logoutURL.replace("splat", tenanceName);
				logoutURL = logoutURL.replace("8443", Integer.toString(8443 + (userIndex % tenances)));
			}else{
				String tenanceName = "";
				appLoginUrl = appLoginUrl.replace("splat/", tenanceName);
				iceServerSSOUrl = iceServerSSOUrl.replace("splat/", tenanceName);
				logoutURL = logoutURL.replace("splat/", tenanceName);	
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
