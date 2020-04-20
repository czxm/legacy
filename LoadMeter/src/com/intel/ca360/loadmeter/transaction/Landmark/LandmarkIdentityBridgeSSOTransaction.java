package com.intel.ca360.loadmeter.transaction.Landmark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.http.client.HttpClient;

import org.joda.time.DateTime;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;
import com.intel.ca360.loadmeter.transaction.AbstractHttpTransaction;
import com.intel.splat.identityservice.saml2.Saml2Utils;

public class LandmarkIdentityBridgeSSOTransaction extends AbstractHttpTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(LandmarkIdentityBridgeSSOTransaction.class);

	private String appLoginUrl = "unreachable";
	private String iceServerSSOUrl = "unreachable";
	private String logoutURL = "unreachable";
	private HashMap<String, String> loginParams = new HashMap<String, String>();
	private HashMap<String, String> samlLogoutParams = new HashMap<String, String>();
	private static Pattern pat = Pattern.compile(".*Login.*|.*authnService.*", Pattern.DOTALL);
	private boolean doLogout = true;

	public LandmarkIdentityBridgeSSOTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		for (ParamType p : params) {
			if (p.getName().equals("appLoginUrl")) {
				appLoginUrl = p.getValue();
			} else if (p.getName().equals("iceServerSSOUrl")) {
				iceServerSSOUrl = p.getValue();
			} else if (p.getName().equals("logoutURL")) {
				logoutURL = p.getValue();
			}
		}
	}

	@Override
	public boolean execute(boolean negative) {
		boolean result = false;
		try {
			result = getRequest(iceServerSSOUrl, "Enterprise login") && postRequest(iceServerSSOUrl, loginParams, "My Apps") && getRequest(logoutURL, "Enterprise login");
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (!result || !doLogout) {
				clearCookies();
			}
		}
		return result;
	}

	@Override
	public void setup(AuthzData authz) {
		loginParams.put("Input0", authz.getUserName());
		loginParams.put("Input1", authz.getPasswords()[0]);
		loginParams.put("moduleIndex", "0");

		if (logoutURL.equals("unreachable"))
			doLogout = false;

		LogoutRequest logoutRequest = Saml2Utils.createLogoutRequest(Saml2Utils.generateRequestID(), "consumer", new DateTime(), logoutURL, "dummy", null);
		try {
			Saml2Utils.signSamlObject(logoutRequest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document doc = null;
		try {
			doc = Saml2Utils.asDOMDocument(logoutRequest);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MarshallingException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		ByteArrayOutputStream sSamlResponse = null;
		try {
			sSamlResponse = Saml2Utils.dumpDocument(doc);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		samlLogoutParams.put("SAMLRequest", Base64.encodeBytes(sSamlResponse.toByteArray()));

	}
}
