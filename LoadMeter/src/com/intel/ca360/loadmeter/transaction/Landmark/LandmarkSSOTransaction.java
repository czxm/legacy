package com.intel.ca360.loadmeter.transaction.Landmark;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;
import com.intel.ca360.loadmeter.transaction.HibernateHelper;
import com.intel.ca360.loadmeter.transaction.SamlOtpSSOTransaction;
import com.intel.ca360.loadmeter.transaction.SimpleHttpTransaction;
import com.intel.ca360.loadmeter.transaction.HibernateHelper.TransactionalCallback;

public class LandmarkSSOTransaction extends SimpleHttpTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(LandmarkSSOTransaction.class);
	
	private String username = "n/a";
	private String samlAppUrls = "unreachable";
	private String postAppUrls = "unreachable";	
	private List<String> randomPassList = new ArrayList<String>();
	private List<String> samlAppUrlList = new ArrayList<String>();
	private List<String> postAppUrlList = new ArrayList<String>();
	private List<String> updateCredUrlList = new ArrayList<String>();	
	private String OTP = "n/a";
	private String UpdatePledgeKeyCounterSQL = "n/a";
	private static HibernateHelper helper;
	
	public LandmarkSSOTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		for(ParamType p : params){
			if(p.getName().equals("samlAppURLs")){
				samlAppUrls = p.getValue();
				if(samlAppUrls != null && samlAppUrls.length() > 0)
					samlAppUrls = samlAppUrls.trim();
			}
			else if(p.getName().equals("postAppURLs")){
				postAppUrls = p.getValue();
				if(postAppUrls != null && postAppUrls.length() > 0)
					postAppUrls = postAppUrls.trim();
			}
			if(p.getName().equals("OTP")){
				OTP = p.getValue();
			}				
			else if(p.getName().equals("UpdatePledgeKeyCounterSQL")){
				UpdatePledgeKeyCounterSQL = p.getValue();
			}
		}
		
		if(helper == null){
			synchronized(SamlOtpSSOTransaction.class){
				if(helper == null)
					helper = new HibernateHelper(params); 
			}
		}
	}
	
	protected boolean loginOTP(String url, Object checker) throws Exception{
		HashMap<String, String> otpPasswd = new HashMap<String, String>();
		otpPasswd.put("Input0", OTP);
		helper.doInTransaction(new TransactionalCallback(){
				@Override
				public Object execute(Connection connection)
						throws Exception {
					Statement st = connection.createStatement();
					st.execute(String.format(UpdatePledgeKeyCounterSQL, username));
					st.close();
					return true; 
				}
		});
		return this.postRequest(url, otpPasswd, checker);		
	}
	
	protected boolean doPostSSO(String url, String match) throws Exception{
		ResponseChecker checker = new ResponseChecker();
		if(this.getRequest(url, checker)){
			if(checker.getContent().contains("One-time Password")){
				// OTP required
				if(!loginOTP(url, checker))
					return false;
			}
			if(checker.getContent().contains(match))			
				return true;
			int statusCode = checker.getHttpResponse().getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_MOVED_TEMPORARILY){
				Header lh = checker.getHttpResponse().getFirstHeader("Location");
				if(lh != null){
					LOG.error(statusCode + " " + lh.getValue());
				}
			}
			else{
				LOG.error(checker.getContent());
			}
		}
		return false;
	}
	
	
	protected boolean doSamlSSO(String url) throws Exception{
		ResponseChecker checker = new ResponseChecker();
		if(this.getRequest(url, checker)){
			if(checker.getContent().contains("One-time Password")){
				// OTP required
				if(!loginOTP(url, checker))
					return false;
			}
			if(checker.getContent().contains("SAMLResponse"))			
				return true;
			
			int statusCode = checker.getHttpResponse().getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_MOVED_TEMPORARILY){
				Header lh = checker.getHttpResponse().getFirstHeader("Location");
				if(lh != null){
					LOG.error(statusCode + " " + lh.getValue());
				}
			}
			else{
				LOG.error(checker.getContent());
			}
		}
		return false;
	}
	
	@Override
	public boolean execute(boolean negative) {
		boolean result = true;
		try{
			resetCredentialStore(); // credential store is actually reset for only once
			for(int i = 0; i < postAppUrlList.size(); i++){
				if(!doPostSSO(postAppUrlList.get(i), randomPassList.get(i))){
					result = false;
				}
			}
			for(String u : samlAppUrlList){
				if(!doSamlSSO(u)){
					result = false;
				}
			}
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
			result = false;
		}
		if(!result){
			this.clearCookies(); //force relogin
		}
		return result;
	}
	
	public boolean resetCredentialStore() throws Exception{
		boolean result = true;
		if(updateCredUrlList.size() > 0){
			for(int i = 0; i < updateCredUrlList.size(); i++){
				HashMap<String, String> credParams = new HashMap<String, String>();
				credParams.put("username", username);
				credParams.put("password", randomPassList.get(i));
				ResponseChecker checker = new ResponseChecker();
				postRequest(updateCredUrlList.get(i), credParams, checker);
				if(checker.getContent().contains("One-time Password")){
					loginOTP(updateCredUrlList.get(i), checker);
					postRequest(updateCredUrlList.get(i), credParams, checker);
				}
				if(!checker.getContent().contains("SUCCESS"))
					result = false;
				if(!result){
					LOG.error(checker.getContent());
				}
			}
			updateCredUrlList.clear();
		}
		return result;
	}
	
	@Override
	public void setup(AuthzData authz) {
		try{
			super.setup(authz);
			username = authz.getUserName();
			for(String u : samlAppUrls.split(" +")){
				String url = u.trim();
				if(url.length() > 0)
					samlAppUrlList.add(url);
			}
			for(String u : postAppUrls.split(" +")){
				String url = u.trim();
				if(url.length() > 0){
					postAppUrlList.add(url);
					updateCredUrlList.add(url.replace("SSO", "http/UPDATE"));
					randomPassList.add(Integer.toString(new Random().nextInt(10000)));
				}
			}
			for(int i = 0; i < samlAppUrlList.size(); i++){
				samlAppUrlList.set(i, concatURL(samlAppUrlList.get(i)));
			}
			for(int i = 0; i < postAppUrlList.size(); i++){
				postAppUrlList.set(i, concatURL(postAppUrlList.get(i)));
			}
			for(int i = 0; i < updateCredUrlList.size(); i++){
				updateCredUrlList.set(i, concatURL(updateCredUrlList.get(i)));
			}
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
	}
}
