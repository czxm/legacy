package com.intel.ca360.loadmeter.transaction;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.http.client.HttpClient;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusResponseType;
import org.opensaml.xml.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;
import com.intel.ca360.loadmeter.transaction.HibernateHelper.TransactionalCallback;
import com.intel.e360.identityservice.conn.ServiceCredential;
import com.intel.splat.identityservice.saml2.Saml2Utils;


public class SamlOtpSSOTransaction extends AbstractHttpTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(SamlOtpSSOTransaction.class);
	
	private HashMap<String,String> samlLoginParams;
	private HashMap<String,String> samlLogoutParams;
	private String appLoginURL = "unreachable";
	private String appLogoutURL = "unreachable";
	private String logoutURL = "unreachable";	
	private String samlAppURL = "unreachable";
	private String postAppURL = "unreachable";
	private String updateCredURL = "unreachable";	
	private String otpAppURL = "unreachable";	
	private String OTP = "n/a";
	private String UpdatePledgeKeyCounterSQL = "n/a";
	private List<String> randomPassList = new ArrayList<String>();
	private List<String> samlAppURLs = new ArrayList<String>();
	private List<String> postAppURLs = new ArrayList<String>();	
	private List<String> updateCredURLs = new ArrayList<String>();
	private List<String> otpAppURLs = new ArrayList<String>();	
	private List<String> appLogoutURLs = new ArrayList<String>();	
	private int samlApps = 1;
	private int postApps = 1;
	private int otpApps = 1;
	private String idpIssuer = "invalid";
	private int samlExpire = 60;
	private int samlClockSkew = 30;
	private String username = "";
	private int tenances = -1;
	private boolean doLogout = false;
	private int logoutInterval = 0;
	private static HibernateHelper helper;
	private int count = 0;
	
	public SamlOtpSSOTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		samlLoginParams = new HashMap<String, String>();
		samlLogoutParams = new HashMap<String, String>();
		for(ParamType p : params){
			if(p.getName().equals("appLoginURL")){
				appLoginURL = p.getValue();
			}
			else if(p.getName().equals("appLogoutURL")){
				appLogoutURL = p.getValue();
			}				
			else if(p.getName().equals("logoutURL")){
				logoutURL = p.getValue();
			}		
			else if(p.getName().equals("logoutInterval")){
				logoutInterval = Integer.parseInt(p.getValue());
			}	
			else if(p.getName().equals("samlAppURL")){
				samlAppURL = p.getValue();
			}
			else if(p.getName().equals("postAppURL")){
				postAppURL = p.getValue();
			}
			else if(p.getName().equals("updateCredURL")){
				updateCredURL = p.getValue();
			}
			else if(p.getName().equals("otpAppURL")){
				otpAppURL = p.getValue();
			}		
			else if(p.getName().equals("OTP")){
				OTP = p.getValue();
			}				
			else if(p.getName().equals("UpdatePledgeKeyCounterSQL")){
				UpdatePledgeKeyCounterSQL = p.getValue();
			}			
			else if(p.getName().equals("samlApps")){
				samlApps = Integer.parseInt(p.getValue());
			}
			else if(p.getName().equals("postApps")){
				postApps = Integer.parseInt(p.getValue());
			}
			else if(p.getName().equals("otpApps")){
				otpApps = Integer.parseInt(p.getValue());
			}			
			else if(p.getName().equals("idpIssuer")){
				idpIssuer = p.getValue();
			}
			else if(p.getName().equals("SamlExpire")){
				samlExpire = Integer.parseInt(p.getValue());
			}
			else if(p.getName().equals("SamlClockSkew")){
				samlClockSkew = Integer.parseInt(p.getValue());
			}
			else if(p.getName().equals("tenances")){
				tenances = Integer.parseInt(p.getValue());
			}
		}
		
		if(helper == null){
			synchronized(SamlOtpSSOTransaction.class){
				if(helper == null)
					helper = new HibernateHelper(params); 
			}
		}
	}

	@Override
	public boolean execute(boolean negative) {
		boolean result = false;
		try{
			for(String url : samlAppURLs){
				result = getRequest(url, "SAMLResponse");
				if(!result)
					break;
			}
			for(int i = 0 ; i < postAppURLs.size(); i++){
				result = getRequest(postAppURLs.get(i), randomPassList.get(i));
				if(!result)
					break;
			}
			// OTP protected app is just SAML2
			for(String url : otpAppURLs){
				result = getRequest(url, "SAMLResponse");
				if(!result)
					break;
			}			
			count++;
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		finally{
			if(!result){
				clearCookies();
				//login again
				startup();
			}
			else{
				if(doLogout){
					if(logoutInterval == 0 || logoutInterval > 0 && count == logoutInterval){
						logout();
						startup();
						count = 0;
					}
				}
			}
		}
		return result;
	}

	protected String getTenancedURL(String url, String tenanceName, String port){
		if(tenanceName.length() > 0)
			return url.replace("8443/splat", port + "/" + tenanceName);
		else
			return url.replace("/splat", "");
	}
	
	@Override
	public void setup(AuthzData authz) {
		try{
	    	if(!logoutURL.equals("unreachable"))
	    		doLogout = true;
			username = authz.getUserName();
			String tenanceName = "";
			String port = "8443";
			int userIndex = Integer.parseInt(username.replace("user", ""));
			if(tenances > 0){
				int tenanceIndex = userIndex % tenances + 1;
				tenanceName = "domain" + tenanceIndex;
				port = Integer.toString(8443 + (userIndex % tenances));
			}

			appLoginURL = getTenancedURL(appLoginURL, tenanceName, port);
			if(doLogout){
				appLogoutURL = getTenancedURL(appLogoutURL, tenanceName, port);
				logoutURL = getTenancedURL(logoutURL, tenanceName, port);
			}
			
			for(int i = 1; i <= samlApps; i++){
				String u = getTenancedURL(String.format(samlAppURL, i), tenanceName, port);
				samlAppURLs.add(u);
				if(doLogout)
					appLogoutURLs.add(u.replace("SSO?SpEntity=", "saml2/SLO/"));
			}
			for(int i = 1; i <= postApps; i++){
				postAppURLs.add(getTenancedURL(String.format(postAppURL, i), tenanceName, port));
				updateCredURLs.add(getTenancedURL(String.format(updateCredURL, i), tenanceName, port));
				randomPassList.add(Integer.toString(new Random().nextInt(10000)));
			}
			for(int i = 1; i <= otpApps; i++){
				String u = getTenancedURL(String.format(otpAppURL, i), tenanceName, port);
				otpAppURLs.add(u);
				if(doLogout)
					appLogoutURLs.add(u.replace("SSO?SpEntity=", "saml2/SLO/"));
			}
			
			ServiceCredential cred = new ServiceCredential();
			cred.addAttribute("subject", "SPAccountAttribute", new String[]{username});
			cred.addAttribute("email", "SPAccountAttribute", new String[]{username + "@acme.com"});
			cred.addAttribute("username", "SPAccountAttribute", new String[]{username});
			StatusResponseType samlResponse = Saml2Utils.createResponse(username,
					"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified",
					/* issuer */idpIssuer,
					/* spID */ "samlSaaS",
					/* acsEndPoint */appLoginURL,
					/* authnRequestId */null,
					"urn:oasis:names:tc:SAML:2.0:cm:bearer",
					/* clock skew */samlClockSkew,
					/* expire */samlExpire,
					/* audience */null,
					/* attrList */cred.getAttributes());			
			Saml2Utils.signSamlObject(samlResponse);
			Document doc = Saml2Utils.asDOMDocument(samlResponse);
	        ByteArrayOutputStream sSamlResponse = Saml2Utils.dumpDocument(doc);
			samlLoginParams.put("SAMLResponse", Base64.encodeBytes(sSamlResponse.toByteArray()));
			
			samlResponse = Saml2Utils.createLogoutResponse(StatusCode.SUCCESS_URI,
		            "Success",
		            idpIssuer,
		            null,
		            new DateTime());
			Saml2Utils.signSamlObject(samlResponse);
			doc = Saml2Utils.asDOMDocument(samlResponse);
			sSamlResponse = Saml2Utils.dumpDocument(doc);
			samlLogoutParams.put("SAMLResponse", Base64.encodeBytes(sSamlResponse.toByteArray()));
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public boolean startup() {
		try{			
			boolean result = getRequest(appLoginURL, "SAMLRequest") && postRequest(appLoginURL, samlLoginParams, "Intel SSO Demo");
			if(result){
				for(int i = 0; i < postApps; i++){
					HashMap<String, String> credParams = new HashMap<String, String>();
					credParams.put("username", username);
					credParams.put("password", randomPassList.get(i));
					result = postRequest(updateCredURLs.get(i), credParams, "SUCCESS");
					if(!result)
						break;
				}
				
				// login OTP protected Apps
				HashMap<String, String> otpPasswd = new HashMap<String, String>();
				otpPasswd.put("Input0", OTP);
				for(String u : otpAppURLs){
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

					result = getRequest(u, "One-time Password") &&
					         postRequest(u, otpPasswd, "SAMLResponse");
				}
			}
			return result;
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		return false;
	}
	
	protected boolean logout(){
		boolean result = getRequest(appLogoutURL, "SAMLRequest");
		if(result){
			for(String u : appLogoutURLs){
				if(!postRequest(u, samlLogoutParams, "SAMLRequest"))
					result = false;
			}
		}
		// relogin logic of ECA360
		return postRequest(logoutURL, samlLogoutParams, "SAMLRequest");
	}
}
