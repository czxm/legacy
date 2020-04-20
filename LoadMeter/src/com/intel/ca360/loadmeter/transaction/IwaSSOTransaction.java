package com.intel.ca360.loadmeter.transaction;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;

import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.xml.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;
import com.intel.ca360.loadmeter.driver.KerbCredential;
import com.intel.splat.identityservice.saml2.Saml2Utils;


public class IwaSSOTransaction extends AbstractHttpTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(IwaSSOTransaction.class);
	private AuthzData authz = null;
	private Credentials cred = null;
	private LoginContext context = null;
	private String iceServerSSOUrl = "unreachable";
	private String logoutURL = "unreachable";
	private String saas = "unreachable";
	private String protectedURL = "unreachable";
	private int negCount = 0;
	private HashMap<String, String> samlLogoutParams = new HashMap<String, String>();
	private HashMap<String, String> negSamlLogoutParams = new HashMap<String, String>();
	private boolean autoSubmitToken = true;
	private boolean doLogout = true;
	private int tenances = -1;
	private long startTimestamp = 0;
	private boolean multiIWA = false;
	private String realm = "@ACME.COM";
	
	public IwaSSOTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		for(ParamType p : params){
			if(p.getName().equals("iceServerSSOUrl")){
				iceServerSSOUrl = p.getValue();
			}
			else if(p.getName().equals("logoutURL")){
				logoutURL = p.getValue();
			}
			else if(p.getName().equals("SaaS")){
				saas = p.getValue();
			}		
			else if(p.getName().equals("tenances")){
				tenances = Integer.parseInt(p.getValue());
			}
			else if(p.getName().equals("multiIWA")){
				multiIWA = Boolean.parseBoolean(p.getValue());
			}
		}
	}


	@Override
	public boolean execute(boolean negative) {
		boolean result = false;
		try{
			if(negative){
				if(negCount % 2 == 0){
					result = getRequest(iceServerSSOUrl, autoSubmitToken ? "Consumer WebApp" : "SAMLResponse", cred) &&
					(!autoSubmitToken || getRequest(protectedURL, "Protected")) && 
					(postRequest(logoutURL, negSamlLogoutParams, "Transaction Error"));
				}
				else{
					result = getRequest(iceServerSSOUrl, "Continue...");					
				}
				negCount++;
			}
			else{
				result = getRequest(iceServerSSOUrl, autoSubmitToken ? "Consumer WebApp" : "SAMLResponse", cred) &&
			   		(!autoSubmitToken || getRequest(protectedURL, "Protected")) && 
			   		(!doLogout || postRequest(logoutURL, samlLogoutParams, autoSubmitToken ? "Consumer WebApp" : "SAMLResponse"));
			}
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		finally{
			if(!result || negative || !doLogout){
				clearCookies();
			}
			if(System.currentTimeMillis() - startTimestamp > 8 * 3600 * 1000){
				logout();
				login();
			}
		}
		return result;
	}

	@Override
	public void shutdown() {
		logout();
	}

	@Override
	public void setup(final AuthzData authz) {
    	this.protectedURL = saas + "protected.jsp";
    	if(saas.equals("unreachable"))
    		autoSubmitToken = false;
    	if(logoutURL.equals("unreachable"))
    		doLogout = false;
    	this.setAutoPostResponse(autoSubmitToken);
    	this.authz = authz;
    	
    	try{
			int userIndex = Integer.parseInt(authz.getUserName().replace("user", ""));
			if(tenances > 0){
				int tenanceIndex = userIndex % tenances + 1;
				String tenanceName = "domain" + tenanceIndex;
				iceServerSSOUrl = iceServerSSOUrl.replace("splat", tenanceName);
				iceServerSSOUrl = iceServerSSOUrl.replace("8443", Integer.toString(8443 + (userIndex % tenances)));
				logoutURL = logoutURL.replace("splat", tenanceName);
				logoutURL = logoutURL.replace("8443", Integer.toString(8443 + (userIndex % tenances)));
			}
			if(multiIWA && userIndex % 2 == 1){
				realm = "@ECA.COM";
				iceServerSSOUrl = iceServerSSOUrl.replace("ACME-AD", "ECA-AD");
				iceServerSSOUrl = iceServerSSOUrl.replace("consumer-acme", "consumer-eca");
				logoutURL = logoutURL.replace("ACME-AD", "ECA-AD");
				logoutURL = logoutURL.replace("consumer-acme", "consumer-eca");
			}
			if(iceServerSSOUrl.endsWith("subject=")){
				iceServerSSOUrl = iceServerSSOUrl + authz.getUserName() + realm;
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
    		LOG.info(e.getMessage(), e);
    	}
    	
		login();
		startTimestamp = System.currentTimeMillis();		
	}
	
	protected void logout(){
		if(context != null){
			try{
				context.logout();
			}
			catch(Exception e){
		    	e.printStackTrace();
			}
		}
	}
	
	protected void login(){
		try{
		    context = new LoginContext("com.sun.security.jgss.login", getUsernamePasswordHandler(authz));
	    	context.login();
		
			cred = new KerbCredential(){
				@Override
				public Subject getSubject() {
					return context.getSubject();
				}			
			};
	    }
	    catch(Exception e){
	    	context = null;
	    	e.printStackTrace();
	    }
	}

    protected CallbackHandler getUsernamePasswordHandler(final AuthzData authz){
        return new CallbackHandler() {
            public void handle(Callback[] callback)
            {
                for(int i = 0; i < callback.length; i++){
                    if(callback[i] instanceof NameCallback)
                    {
                        NameCallback nameCallback = (NameCallback)callback[i];
                        nameCallback.setName(authz.getUserName() + realm);
                    } else
                    if(callback[i] instanceof PasswordCallback)
                    {
                        PasswordCallback passCallback = (PasswordCallback)callback[i];
                        passCallback.setPassword(authz.getPasswords().length == 1 ? authz.getPassword().toCharArray() : authz.getPasswords()[1].toCharArray());
                    }
                }
            }
        };
    }
}
