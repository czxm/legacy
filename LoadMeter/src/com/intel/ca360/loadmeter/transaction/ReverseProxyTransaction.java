package com.intel.ca360.loadmeter.transaction;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

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
import com.intel.e360.identityservice.conn.ServiceCredential;
import com.intel.splat.identityservice.saml2.Saml2Utils;


public class ReverseProxyTransaction extends AbstractHttpTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(ReverseProxyTransaction.class);
	protected HashMap<String,String> samlLoginParams;
	protected HashMap<String,String> samlLogoutParams;
	protected HashMap<String,String> negSamlLoginParams;
	protected String appLoginURL = "unreachable";
	protected String appLogoutURL = "unreachable";
	protected String logoutURL = "unreachable";
	protected String spnegoAppURL = "unreachable";	
	protected String appURL = "unreachable";	
	protected String appMatch = "";
	protected String idpIssuer = "invalid";
	protected boolean doLogout = true;
	protected int samlExpire = 60;
	protected int samlClockSkew = 30;
	protected String username = "";

	public ReverseProxyTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		samlLoginParams = new HashMap<String, String>();
		samlLogoutParams = new HashMap<String, String>();
		negSamlLoginParams = new HashMap<String, String>();
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
			else if(p.getName().equals("spnegoAppURL")){
				spnegoAppURL = p.getValue();
			}
			else if(p.getName().equals("appURL")){
				appURL = p.getValue();
			}
			else if(p.getName().equals("appMatch")){
				appMatch = p.getValue();
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
		}
	}

	@Override
	public boolean execute(boolean negative) {
		boolean result = false;
		try{
			if(negative){
				result = true;
			}
			else{
				if(!spnegoAppURL.equals("unreachable")){
					result = getRequest(spnegoAppURL, username);
					if(result && !appURL.equals("unreachable")){
						result = getRequest(appURL, appMatch);
					}
				}
				else if(!appURL.equals("unreachable")){
					result = getRequest(appURL, appMatch);
				}
			}
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		finally{
			if(!result){
				clearCookies();
				//login again
				logout();
				if(login(false)){
					this.setAutoPostResponse(true);
					if(!spnegoAppURL.equals("unreachable")){
						getRequest(spnegoAppURL, username);
					}
					else if(!appURL.equals("unreachable")){
						getRequest(appURL, appMatch);
					}
					this.setAutoPostResponse(false);
				}
			}
		}
		return result;
	}
	
	protected boolean login(boolean negative){
		if(negative)
			return postRequest(appLoginURL, negSamlLoginParams, "You have no rights");			
		else
			return postRequest(appLoginURL, samlLoginParams, "Intel SSO Demo");
	}

	protected boolean logout(){
		if(doLogout){
			boolean result = getRequest(appLogoutURL, "SAMLRequest");
			if(result){
				// relogin logic of ECA360
				return postRequest(logoutURL, samlLogoutParams, "SAMLRequest");
			}
			else
				return false;
		}
		else{
			return true;
		}
	}
	
	@Override
	public void setup(AuthzData authz) {
		try{
	    	if(logoutURL.equals("unreachable"))
	    		doLogout = false;
	    	
			username = authz.getUserName();
			ServiceCredential cred = new ServiceCredential();
			cred.addAttribute("subject", "SPAccountAttribute", new String[]{username});
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
			
			cred = new ServiceCredential();
			cred.addAttribute("subject", "SPAccountAttribute", new String[]{"baduser"});
			samlResponse = Saml2Utils.createResponse("baduser",
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
			doc = Saml2Utils.asDOMDocument(samlResponse);
	        sSamlResponse = Saml2Utils.dumpDocument(doc);
			negSamlLoginParams.put("SAMLResponse", Base64.encodeBytes(sSamlResponse.toByteArray()));

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
			boolean result = getRequest(appLoginURL, "SAMLRequest");
			if(result && login(false)){
				this.setAutoPostResponse(true);
				if(!spnegoAppURL.equals("unreachable")){
					result = getRequest(spnegoAppURL, username);
				}
				else if(!appURL.equals("unreachable")){
					result = getRequest(appURL, appMatch);
				}
				this.setAutoPostResponse(false);
			}
			return result;
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		return false;
	}
}
