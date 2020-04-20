package com.intel.ca360.loadmeter.transaction;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusResponseType;
import org.opensaml.xml.util.Base64;
import org.w3c.dom.Document;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;
import com.intel.splat.identityservice.saml2.Saml2Utils;


public class VisaDemoTransaction extends AbstractHttpTransaction {
	private HashMap<String,String> samlLoginParams;
	private HashMap<String,String> samlLogoutParams;
	private String acsLocation = "unreachable";
	private String appLoginURL = "unreachable";
	private String idpIssuer = "dummy";
	private String dummySaaS = Long.toHexString(System.currentTimeMillis()); // make a random 'URL'
	private int samlExpire = 60;
	private int samlClockSkew = 30;

	public VisaDemoTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		samlLoginParams = new HashMap<String, String>();
		samlLogoutParams = new HashMap<String, String>();
		for(ParamType p : params){
			if(p.getName().equals("acsLocation")){
				acsLocation = p.getValue();
			}
			else if(p.getName().equals("appLoginURL")){
				appLoginURL = p.getValue();
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
			result = getRequest(appLoginURL + "?RelayState=" + dummySaaS, dummySaaS);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(!result){
				clearCookies();
			}
		}
		return result;
	}

	@Override
	public void setup(AuthzData authz) {
		try{
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
					/* attrList */null);			
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
			e.printStackTrace();
		}
	}

	@Override
	public boolean startup() {
		try{			
			return postRequest(acsLocation, samlLoginParams, "ICEResponse");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
}
