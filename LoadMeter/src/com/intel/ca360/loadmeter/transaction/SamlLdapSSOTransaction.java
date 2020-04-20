package com.intel.ca360.loadmeter.transaction;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

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


public class SamlLdapSSOTransaction extends AbstractHttpTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(SamlLdapSSOTransaction.class);
	
	private HashMap<String,String> samlLoginParams;
	private HashMap<String,String> negSamlLoginParams;
	private HashMap<String,String> ldapLoginParams;
	private HashMap<String,String> negLdapLoginParams;
	private HashMap<String,String> samlLogoutParams;
	private HashMap<String,String> negSamlLogoutParams;
	private String acsLocation = "unreachable";
	private String logoutURL = "unreachable";
	private String appLogoutURL = "unreachable";
	private String idpIssuer = "dummy";
	private boolean doLogout = true;
	private int negCount = 0;
	private int samlExpire = 60;
	private int samlClockSkew = 30;
	private int tenances = 0;
	private static Pattern pat = Pattern.compile(".*Login.*|.*authnService.*", Pattern.DOTALL);

	public SamlLdapSSOTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		samlLoginParams = new HashMap<String, String>();
		negSamlLoginParams = new HashMap<String, String>();
		ldapLoginParams = new HashMap<String, String>();
		negLdapLoginParams = new HashMap<String, String>();
		samlLogoutParams = new HashMap<String, String>();
		negSamlLogoutParams = new HashMap<String, String>();
		for(ParamType p : params){
			if(p.getName().equals("acsLocation")){
				acsLocation = p.getValue();
			}
			else if(p.getName().equals("appLogoutURL")){
				appLogoutURL = p.getValue();
			}
			else if(p.getName().equals("logoutURL")){
				logoutURL = p.getValue();
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
	}
	
	@Override
	public boolean execute(boolean negative) {
		boolean result = false;
		try{			
			if(negative){
				if(negCount % 3 == 0){
					result = postRequest(acsLocation, samlLoginParams, pat) &&
						postRequest(acsLocation, ldapLoginParams, "ICEResponse") &&
						(getRequest(appLogoutURL, "Access rights validated")) &&
						(postRequest(logoutURL, negSamlLogoutParams, "Transaction Error"));
				}
				else if(negCount % 3 == 1){
					result = postRequest(acsLocation, negSamlLoginParams, "Authentication Fail");				
				}
				else if(negCount % 3 == 2){
					result = postRequest(acsLocation, samlLoginParams, pat) &&
						postRequest(acsLocation, negLdapLoginParams, "LDAP authentication failed");					
				}
				negCount++;
			}
			else{
				result = postRequest(acsLocation, samlLoginParams, pat) &&
			       postRequest(acsLocation, ldapLoginParams, "ICEResponse") &&
				   (!doLogout || getRequest(appLogoutURL, "Access rights validated")) &&
				   (!doLogout || postRequest(logoutURL, samlLogoutParams, "You have successfully logged out"));				
			}
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		finally{
			if(!result || negative || !doLogout){
				clearCookies();
			}
		}
		return result;
	}

	@Override
	public void setup(AuthzData authz) {
		try{
	    	if(logoutURL.equals("unreachable"))
	    		doLogout = false;
	    	
			if(tenances > 0){
				int userIndex = Integer.parseInt(authz.getUserName().replace("user", ""));
				int tenanceIndex = userIndex % tenances + 1;
				String tenanceName = "domain" + tenanceIndex;
				acsLocation = acsLocation.replace("splat", tenanceName);
				acsLocation = acsLocation.replace("8443", Integer.toString(8443 + (userIndex % tenances)));
				appLogoutURL = appLogoutURL.replace("splat", tenanceName);
				appLogoutURL = appLogoutURL.replace("8443", Integer.toString(8443 + (userIndex % tenances)));
				logoutURL = logoutURL.replace("splat", tenanceName);
				logoutURL = logoutURL.replace("8443", Integer.toString(8443 + (userIndex % tenances)));
			}
			
			ServiceCredential cred = new ServiceCredential();
			cred.addAttribute("subject", "SPAccountAttribute", new String[]{authz.getUserName()});
			StatusResponseType samlResponse = Saml2Utils.createResponse(authz.getUserName(),
					"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified",
					/* issuer */idpIssuer,
					/* spID */ "samlSaaS",
					/* acsEndPoint */acsLocation,
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
			
			samlResponse = Saml2Utils.createResponse(authz.getUserName(),
					"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified",
					/* issuer */"this is a wrong issuer",
					/* spID */ "samlSaaS",
					/* acsEndPoint */acsLocation,
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
			
			ldapLoginParams.put("Input0", authz.getUserName());
			ldapLoginParams.put("Input1", authz.getPasswords()[0]);
			ldapLoginParams.put("moduleIndex", "1");
			
			negLdapLoginParams.put("Input0", authz.getUserName());
			negLdapLoginParams.put("Input1", "This is a wrong password");
			negLdapLoginParams.put("moduleIndex", "1");			
			
			samlResponse = Saml2Utils.createLogoutResponse(StatusCode.SUCCESS_URI,
		            "Success",
		            idpIssuer,
		            null,
		            new DateTime());
			Saml2Utils.signSamlObject(samlResponse);
			doc = Saml2Utils.asDOMDocument(samlResponse);
			sSamlResponse = Saml2Utils.dumpDocument(doc);
			samlLogoutParams.put("SAMLResponse", Base64.encodeBytes(sSamlResponse.toByteArray()));
			
			samlResponse = Saml2Utils.createLogoutResponse(StatusCode.AUTHN_FAILED_URI,
		            "Success",
		            idpIssuer,
		            null,
		            new DateTime());
			Saml2Utils.signSamlObject(samlResponse);
			doc = Saml2Utils.asDOMDocument(samlResponse);
			sSamlResponse = Saml2Utils.dumpDocument(doc);
			negSamlLogoutParams.put("SAMLResponse", Base64.encodeBytes(sSamlResponse.toByteArray()));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
