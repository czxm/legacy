package com.intel.ca360.loadmeter.transaction.Landmark;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
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
import com.intel.ca360.loadmeter.transaction.SimpleHttpTransaction;
import com.intel.splat.identityservice.saml2.Saml2Utils;


public class LandmarkSLOTransaction extends SimpleHttpTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(LandmarkSLOTransaction.class);
	
	private String samlAppLogoutUrls;
	private List<String> samlAppLogoutUrlList;
	private String appLogoutURL = "unreachable";
	private String logoutURL = "unreachable";
	private HashMap<String,String> samlLogoutParams;
	private String spIssuer = "invalid";
	
	public LandmarkSLOTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		samlAppLogoutUrlList = new ArrayList<String>();
		samlLogoutParams = new HashMap<String, String>();
		for(ParamType p : params){
			if(p.getName().equals("samlAppLogoutURLs")){
				samlAppLogoutUrls = p.getValue();
				if(samlAppLogoutUrls != null && samlAppLogoutUrls.length() > 0)
					samlAppLogoutUrls = samlAppLogoutUrls.trim();
			}			
			else if(p.getName().equals("appLogoutURL")){
				appLogoutURL = p.getValue();
			}
			else if(p.getName().equals("logoutURL")){
				logoutURL = p.getValue();
			}			
			else if(p.getName().equals("spIssuer")){
				spIssuer = p.getValue();
			}				
		}
	}
	
	@Override
	public boolean execute(boolean negative) {
		boolean result = true;
		try{
			ResponseChecker checker = new ResponseChecker();
			if(getRequest(appLogoutURL, checker) && checker.getContent().contains("SAMLRequest")){
				for(String u : samlAppLogoutUrlList){
					ResponseChecker logoutChecker = new ResponseChecker();
					postRequest(u, samlLogoutParams, logoutChecker);
					String response = logoutChecker.getContent();
					if(response.length() > 0 && !response.contains("SAMLRequest")){
						result = false;
						LOG.error(checker.getContent());
					}
				}
			}
			result = getRequest(logoutURL, checker);
		}
		catch(Exception e){
			result = false;
			LOG.error(e.getMessage(), e);
		}
		if(!result){
			this.clearCookies(); //force relogin
		}
		return result;
	}
	
	@Override
	public void setup(AuthzData authz){
		try{
			for(String u : samlAppLogoutUrls.split(" +")){
				samlAppLogoutUrlList.add(u.trim());
			}
			appLogoutURL = concatURL(appLogoutURL);
			logoutURL = concatURL(logoutURL);
			for(int i = 0; i <  samlAppLogoutUrlList.size(); i++){
				samlAppLogoutUrlList.set(i, concatURL(samlAppLogoutUrlList.get(i)));
			}
			StatusResponseType samlResponse = Saml2Utils.createLogoutResponse(StatusCode.SUCCESS_URI,
		            "Success",
		            spIssuer,
		            null,
		            new DateTime());
			Saml2Utils.signSamlObject(samlResponse);
			Document doc = Saml2Utils.asDOMDocument(samlResponse);
			ByteArrayOutputStream sSamlResponse = Saml2Utils.dumpDocument(doc);
			samlLogoutParams.put("SAMLResponse", Base64.encodeBytes(sSamlResponse.toByteArray()));
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
	}
}
