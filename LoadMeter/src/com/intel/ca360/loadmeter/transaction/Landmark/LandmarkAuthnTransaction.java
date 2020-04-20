package com.intel.ca360.loadmeter.transaction.Landmark;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;


public class LandmarkAuthnTransaction extends LandmarkRestTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(LandmarkAuthnTransaction.class);
	
	private String cookiePath = "/identityservice/package/idpsf";
	private String acsLocation = "unreachable";
	private String splashLoginCookie = null;
	private APIChecker oneTimeTokenChecker = new APIChecker();
	
	public LandmarkAuthnTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		for(ParamType p : params){
			if(p.getName().equals("acsLocation")){
				acsLocation = p.getValue();
			}		
			else if(p.getName().equals("cookiePath")){
				cookiePath = p.getValue();
			}				
		}
	}
	
	protected boolean loginLandmark(){
		boolean result = true;
		try{
			for(Action a : actions){
				if(!doAction(a))
					result = false;
			}
			if(result){
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("ICEResponse", this.oneTimeTokenChecker.getResponse());
				result = this.postRequest(acsLocation, params, "My Apps");
			}
		}
		catch(Exception e){
			result = false;
			LOG.error(e.getMessage());
		}
		return result;
	}
	
	@Override
	public boolean execute(boolean negative) {
		for(Cookie c : this.getCookies()){
			if(c.getName().equals("SplashLoginCookie")){
				if(c.getPath().equals(cookiePath) && c.getValue().equals(splashLoginCookie) && !c.isExpired(new Date(System.currentTimeMillis())))
					return true;
			}
		}
		splashLoginCookie = null;
		boolean result = loginLandmark();
		if(result){
			for(Cookie c : this.getCookies()){
				if(c.getPath().equals(cookiePath) && c.getName().equals("SplashLoginCookie")){
					splashLoginCookie = c.getValue();
					break;
				}
			}
		}
		return result;
	}

	@Override
	public void setup(AuthzData authz) {
		try{
			super.setup(authz);
			acsLocation = concatURL(acsLocation);
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	protected List<Action> createActions(Action start) {
		// compose the REST call actions to get one time authntoken
		List<Action> result = new ArrayList<Action>();
		final APIChecker authnChecker = (APIChecker)start.getCheck();
		final APIChecker userTokenChecker = new APIChecker();		
		result.add(new DynamicAction(HTTP_METHOD.GET, "getUserAuthnToken") {
			@Override
			public Object[] getArguments(){
				return new String[]{idp, authnChecker.getResponse()};
			}
			@Override
			public Object getCheck(){
				return userTokenChecker;
			}
		});	
		result.add(new DynamicAction(HTTP_METHOD.GET, "getOneTimeToken") {
			@Override
			public Object[] getArguments(){
				return new String[]{idp, userTokenChecker.getResponse()};
			}
			@Override
			public Object getCheck(){
				return oneTimeTokenChecker;
			}
		});
		return result;
	}
}
