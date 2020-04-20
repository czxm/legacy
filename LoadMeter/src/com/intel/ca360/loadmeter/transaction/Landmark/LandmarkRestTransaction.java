package com.intel.ca360.loadmeter.transaction.Landmark;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;
import com.intel.ca360.loadmeter.transaction.SimpleRestTransaction;

public abstract class LandmarkRestTransaction extends SimpleRestTransaction {
	
	protected LandmarkRestTransaction INSTANCE;
	protected String idp;
	protected String subject;
	protected String idpIssuer = "dummy";
	protected String createAppJSON;
	protected String updateAppJSON;
	protected String usersJSON;
	protected String userAppsJSON;
	protected int appCount;
	protected String userPattern;
	protected int userCount;
	protected String userMatcher;
	protected String appIdMatcher;
	protected String provAppId;
	protected String adminbase;
	
	protected static Header[] requestJSONHeaders = new Header[]{new BasicHeader("Content-Type", "application/json")};
	protected static Header[] responseJSONHeaders = new Header[]{new BasicHeader("Accept", "application/json")};
	protected static Header[] allJSONHeaders = new Header[]{new BasicHeader("Content-Type", "application/json"),new BasicHeader("Accept", "application/json")};
	protected static Header[] requestFORMHeaders = new Header[]{new BasicHeader("Content-Type", "application/x-www-form-urlencoded")};
	protected static Header[] responseTextHeaders = new Header[]{new BasicHeader("Accept", "text/plain")};
	
	protected abstract List<Action> createActions(Action start);

	protected static class APIChecker implements TransactionChecker{
		int statusCode;
		String check;
		String result;
		
		public APIChecker(){			
		}
		
		public APIChecker(String check){
			this.check = check;
		}
		
		@Override
		public boolean check(HttpResponse response, String content) {
			this.result = content;
			if(this.statusCode > 0 && response.getStatusLine().getStatusCode() != this.statusCode)
				return false;
			if(this.check != null && !content.contains(this.check))
				return false;
			return true;
		}
		
		public String getResponse(){
			return result;
		}
		
		public void setCheck(String check){
			this.check = check;
		}
		
		public void setStatusCode(int c){
			this.statusCode = c;
		}
	}
	
	protected static class APIStatusChecker extends APIChecker{
		public APIStatusChecker(){
			super();
			this.setStatusCode(200);
		}
		public boolean check(HttpResponse response, String content) {
			Header[] headers = response.getHeaders("SSOResult");
			if(headers != null){
				for(Header h : headers){
					if(h.getValue().equals("Successful"))
						return super.check(response, content);
				}
			}
			return false;
		}
	}
	
	public LandmarkRestTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		for(ParamType p : params){
			if(p.getName().equals("adminbase")){
				adminbase = p.getValue();
			}
			else if(p.getName().equals("idp")){
				idp = p.getValue();
			}
			else if(p.getName().equals("subject")){
				subject = p.getValue();
			}
			else if(p.getName().equals("idpIssuer")){
				idpIssuer = p.getValue();
			}
			else if(p.getName().equals("createAppJSON")){
				createAppJSON = p.getValue();
			}
			else if(p.getName().equals("updateAppJSON")){
				updateAppJSON = p.getValue();
			}
			else if(p.getName().equals("appCount")){
				appCount = Integer.parseInt(p.getValue());
			}
			else if(p.getName().equals("appIdMatcher")){
				appIdMatcher = p.getValue();
			}
			else if(p.getName().equals("provAppId")){
				provAppId = p.getValue();
			}	
			else if(p.getName().equals("usersJSON")){
				usersJSON = p.getValue();
			}
			else if(p.getName().equals("userAppsJSON")){
				userAppsJSON = p.getValue();
			}
			else if(p.getName().equals("userPattern")){
				userPattern = p.getValue();
			}
			else if(p.getName().equals("userCount")){
				userCount = Integer.parseInt(p.getValue());
			}
			else if(p.getName().equals("userMatcher")){
				userMatcher = p.getValue();
			}
		}
		this.INSTANCE = this;
	}
	
	@Override
	public void setup(AuthzData authz){
		if(subject == null)
			subject = authz.getUserName();
		final APIChecker authnChecker = new APIChecker();
		authnChecker.setStatusCode(200);
		Action a = new StaticAction(HTTP_METHOD.GET, "getAuthnToken", new String[]{subject, subject, subject,idpIssuer}, null, null, authnChecker);
		this.actions.add(a);
		List<Action> others = createActions(a);
		if(others != null){
			for(Action act : others){
				this.actions.add(act);
			}
		}
	}
	
	protected String getFullURL(String url, Object[] args){
		if(url != null && url.length() > 0){
			url = getRestMethods().get(url);
		}
		if(url == null){
			return adminbase;
		}
		String relative = url;
		if(args != null)
			relative = String.format(url, args);
		relative = encodeURL(relative);
		return concatURL(adminbase, relative);
	}
}
