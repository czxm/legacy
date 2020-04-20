package com.intel.ca360.loadmeter.transaction;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.xml.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;
import com.intel.splat.identityservice.saml2.Saml2Utils;


public class LdapSSOPostSignInTransaction extends AbstractHttpTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(LdapSSOPostSignInTransaction.class);
			
	private String appLoginUrl = "unreachable";
	private String iceServerSSOUrl = "unreachable";
	private String logoutURL = "unreachable";
	private String updateCredUrl = "unreachable";
	private String webAppSSOUrl = "unreachable";
	private boolean updateCredOnly = false;
	protected boolean doLogout = true;
	private String randomPass = Integer.toString(new Random().nextInt(10000));
	private HashMap<String, String> loginParams = new HashMap<String, String>();
	private HashMap<String, String> credParams = new HashMap<String, String>();
	private HashMap<String, String> samlLogoutParams = new HashMap<String, String>();
	private HashMap<String, String> negSamlLogoutParams = new HashMap<String, String>();
	private HashMap<String,String> negLdapLoginParams = new HashMap<String, String>();
	private int tenances = 0;
	private static Pattern pat = Pattern.compile(".*Login.*|.*authnService.*", Pattern.DOTALL);
	private int negCount = 0;
	
	public LdapSSOPostSignInTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		for(ParamType p : params){
			if(p.getName().equals("appLoginUrl")){
				appLoginUrl = p.getValue();
			}
			else if(p.getName().equals("iceServerSSOUrl")){
				iceServerSSOUrl = p.getValue();
			}
			else if(p.getName().equals("updateCredURL")){
				updateCredUrl = p.getValue();
			}
			else if(p.getName().equals("webAppSSOURL")){
				webAppSSOUrl = p.getValue();
			}
			else if(p.getName().equals("logoutURL")){
				logoutURL = p.getValue();
			}
			else if(p.getName().equals("updateCredOnly")){
				updateCredOnly = Boolean.parseBoolean(p.getValue());
			}
			else if(p.getName().equals("tenances")){
				tenances = Integer.parseInt(p.getValue());
			}
		}
	}

	@Override
	public boolean startup(){
		if(!updateCredOnly){
			boolean result = false;					
			try{
				result = getRequest(appLoginUrl, pat) &&
				postRequest(iceServerSSOUrl, loginParams, "SAMLResponse") &&
				postRequest(updateCredUrl, credParams, "SUCCESS") &&
				(!doLogout ||  postRequest(logoutURL, samlLogoutParams, "SAMLResponse"));				
			}
			catch(Exception e){
				LOG.error(e.getMessage(), e);
			}
			finally{
				if(!result || !doLogout)
					clearCookies();
			}
			return result;
		}
		return true;
	}
	
	@Override
	public boolean execute(boolean negative) {
		boolean result = false;
		try{			
			if(negative){
				if(negCount % 2== 0){
					result = getRequest(appLoginUrl, pat) &&
						postRequest(iceServerSSOUrl, negLdapLoginParams, "Incorrect Login");
				}
				else{
					result = getRequest(appLoginUrl, pat) &&
						postRequest(iceServerSSOUrl, loginParams, "SAMLResponse") &&
						(postRequest(logoutURL, negSamlLogoutParams, "Transaction Error"));
				}
				negCount++;
			}
			else{
				if(updateCredOnly){
					credParams.put("password", Integer.toString(new Random().nextInt(10000)));
				}
				result = getRequest(appLoginUrl, pat) &&
					postRequest(iceServerSSOUrl, loginParams, "SAMLResponse") &&
					(updateCredOnly ? postRequest(updateCredUrl, credParams, "SUCCESS") : true) &&
					getRequest(webAppSSOUrl, credParams.get("password")) &&
					(doLogout && postRequest(logoutURL, samlLogoutParams, "SAMLResponse"));
			}
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		finally{
			if(!result || negative){
				clearCookies();
			}
		}
		return result;
	}
	
	@Override
	public void setup(AuthzData authz){
    	if(logoutURL.equals("unreachable"))
    		doLogout = false;
    	
		loginParams.put("Input0", authz.getUserName());
		loginParams.put("Input1", authz.getPasswords()[0]);
		loginParams.put("moduleIndex", "0");
		
		credParams.put("username", authz.getUserName());
		credParams.put("password", randomPass);
		
		negLdapLoginParams.put("Input0", authz.getUserName());
		negLdapLoginParams.put("Input1", "This is a wrong password");
		negLdapLoginParams.put("moduleIndex", "0");	
		try{
			if(tenances > 0){
				int userIndex = Integer.parseInt(authz.getUserName().replace("user", ""));
				int tenanceIndex = userIndex % tenances + 1;
				String tenanceName = "domain" + tenanceIndex;
				appLoginUrl = appLoginUrl.replace("splat", tenanceName);
				appLoginUrl = appLoginUrl.replace("8443", Integer.toString(8443 + (userIndex % tenances)));
				iceServerSSOUrl = iceServerSSOUrl.replace("splat", tenanceName);
				iceServerSSOUrl = iceServerSSOUrl.replace("8443", Integer.toString(8443 + (userIndex % tenances)));
				logoutURL = logoutURL.replace("splat", tenanceName);
				logoutURL = logoutURL.replace("8443", Integer.toString(8443 + (userIndex % tenances)));
			}
			
			LogoutRequest logoutRequest = Saml2Utils.createLogoutRequest(
					Saml2Utils.generateRequestID(), 
					"consumer",
					new DateTime(),
					logoutURL,
					"dummy", 
					null);			
			Saml2Utils.signSamlObject(logoutRequest);
			Document doc = Saml2Utils.asDOMDocument(logoutRequest);
	        ByteArrayOutputStream sSamlResponse = Saml2Utils.dumpDocument(doc);
			samlLogoutParams.put("SAMLRequest", Base64.encodeBytes(sSamlResponse.toByteArray()));
			
			logoutRequest = Saml2Utils.createLogoutRequest(
					Saml2Utils.generateRequestID(), 
					"This is a wrong issuer",
					new DateTime(),
					logoutURL,
					"dummy", 
					null);			
			Saml2Utils.signSamlObject(logoutRequest);
			doc = Saml2Utils.asDOMDocument(logoutRequest);
	        sSamlResponse = Saml2Utils.dumpDocument(doc);
			negSamlLogoutParams.put("SAMLRequest", Base64.encodeBytes(sSamlResponse.toByteArray()));		
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
