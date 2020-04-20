package com.intel.ca360.loadmeter.transaction;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.client.HttpClient;
import org.joda.time.DateTime;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusResponseType;
import org.opensaml.xml.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;
import com.intel.ca360.loadmeter.util.Util;
import com.intel.e360.identityservice.conn.ServiceCredential;
import com.intel.splat.identityservice.saml2.Saml2Constants;
import com.intel.splat.identityservice.saml2.Saml2Utils;

public class FederationBrokerTransaction extends AbstractHttpTransaction {
    private static final Logger LOG = LoggerFactory.getLogger(FederationBrokerTransaction.class);

    private HashMap<String, String> samlLoginRequestParams;
    private List<HashMap<String, String>> samlLoginResponseParamsList;
    private HashMap<String, String> samlLogoutRequestParams;
    private HashMap<String, String> samlLogoutResponseParams;
    private String spLoginURL = "unreachable";
    private String idpLoginURL = "unreachable";
    private String spLogoutURL = "unreachable";
    private String idpLogoutURL = "unreachable";
    private String idpIssuer = "dummy";
    private String spIssuer = "dummy";
    private int numIDPs = 0;
    private int numSPs = 0;
    private boolean doLogout = true;
    private boolean verifyRelayState = false;
    private int samlExpire = 60;
    private int samlClockSkew = 30;

    public FederationBrokerTransaction(HttpClient client, List<ParamType> params) {
        super(client, params);
        samlLoginRequestParams = new HashMap<String, String>();
        samlLoginResponseParamsList = new ArrayList<HashMap<String, String>>();
        samlLogoutRequestParams = new HashMap<String, String>();
        samlLogoutResponseParams = new HashMap<String, String>();
        for (ParamType p : params) {
            if (p.getName().equals("SPLoginURL")) {
                spLoginURL = p.getValue();
            } else if (p.getName().equals("SPLogoutURL")) {
                spLogoutURL = p.getValue();
            } else if (p.getName().equals("IDPLoginURL")) {
                idpLoginURL = p.getValue();
            } else if (p.getName().equals("IDPLogoutURL")) {
                idpLogoutURL = p.getValue();
            } else if (p.getName().equals("numIDPs")) {
                numIDPs = Integer.parseInt(p.getValue());
            } else if (p.getName().equals("numSPs")) {
                numSPs = Integer.parseInt(p.getValue());
            } else if (p.getName().equals("IDPIssuer")) {
                idpIssuer = p.getValue();
            } else if (p.getName().equals("SPIssuer")) {
                spIssuer = p.getValue();
            } else if (p.getName().equals("doLogout")) {
                doLogout = Boolean.parseBoolean(p.getValue());
            } else if (p.getName().equals("SamlExpire")) {
                samlExpire = Integer.parseInt(p.getValue());
            } else if (p.getName().equals("SamlClockSkew")) {
                samlClockSkew = Integer.parseInt(p.getValue());
            } else if (p.getName().equals("verifyRelayState")) {
                verifyRelayState = Boolean.parseBoolean(p.getValue());
            }
        }
    }

    @Override
    public boolean execute(boolean negative) {
        boolean result = false;
        try{
            int idpIndex = new Random().nextInt(numIDPs) + 1;
            int spIndex = new Random().nextInt(numSPs) + 1;
            String theIdpLoginURL = String.format(idpLoginURL, idpIndex);
            String theIdpLogoutURL = String.format(idpLogoutURL, idpIndex);
            String theSpLoginURL = String.format(spLoginURL, spIndex, idpIndex, idpIndex);
            String theSpLogoutURL = String.format(spLogoutURL, spIndex);
            String relayState = Long.toString(new Random().nextLong());
    
            samlLoginRequestParams.put("RelayState", relayState);
            ResponseChecker rc = new ResponseChecker("SAMLRequest");
            result = postRequest(theSpLoginURL, samlLoginRequestParams, rc);
            if (!result){
                throw new Exception(rc.getContent());
            }
    
            HashMap<String, String> samlLoginResponseParams = samlLoginResponseParamsList.get(idpIndex - 1);
            String content = rc.getContent();
            String relayStateValueOfECA = Util.stringRegexMatch(
                            "<INPUT TYPE=\"HIDDEN\" NAME=\"RelayState\" VALUE=\"([^\">]+)\">",
                            content);
            if (relayStateValueOfECA != null && relayStateValueOfECA.length() > 0) {
                relayStateValueOfECA = StringEscapeUtils.unescapeHtml(relayStateValueOfECA);
            }
            samlLoginResponseParams.put("RelayState", relayStateValueOfECA);
            result = postRequest(theIdpLoginURL, samlLoginResponseParams, verifyRelayState ? relayState : "SAMLResponse");
            
            if(doLogout){
                relayState = Long.toString(new Random().nextLong());
                samlLogoutRequestParams.put("RelayState", relayState);
                result = postRequest(theSpLogoutURL, samlLogoutRequestParams, rc);
                if (!result){
                    throw new Exception(rc.getContent());
                }
                
                content = rc.getContent();
                relayStateValueOfECA = Util.stringRegexMatch(
                                "<INPUT TYPE=\"HIDDEN\" NAME=\"RelayState\" VALUE=\"([^\">]+)\">",
                                content);
                if (relayStateValueOfECA != null && relayStateValueOfECA.length() > 0) {
                    relayStateValueOfECA = StringEscapeUtils.unescapeHtml(relayStateValueOfECA);
                }
                samlLogoutResponseParams.put("RelayState", relayStateValueOfECA);
                result = postRequest(theIdpLogoutURL, samlLogoutResponseParams, verifyRelayState ? relayState : "SAMLResponse");
            }
            else{
                this.clearCookies();
            }
        }
        catch(Exception e){
            LOG.error(e.getMessage());
            this.clearCookies();
            result = false;
        }
        return result;
    }

    @Override
    public void setup(AuthzData authz) {
        try {
            StatusResponseType samlResponse = Saml2Utils.createLogoutResponse(
                    StatusCode.SUCCESS_URI, "Success", idpIssuer, null,
                    new DateTime());
            Saml2Utils.signSamlObject(samlResponse);
            Document doc = Saml2Utils.asDOMDocument(samlResponse);
            ByteArrayOutputStream sSamlResponse = Saml2Utils.dumpDocument(doc);
            samlLogoutResponseParams.put("SAMLResponse", Base64
                    .encodeBytes(sSamlResponse.toByteArray()));

            for (int i = 0; i < numIDPs; i++) {
                String acsURL = String.format(idpLoginURL, i + 1);
                ServiceCredential cred = new ServiceCredential();
                cred.addAttribute("subject", "SPAccountAttribute",
                        new String[] { authz.getUserName() });
                cred.addAttribute("email", "SPAccountAttribute",
                        new String[] { authz.getUserName() + "@acme.com" });
                samlResponse = Saml2Utils.createResponse(
                                authz.getUserName(),
                                "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified",
                                /* issuer */idpIssuer,
                                /* spID */"samlSaaS",
                                /* acsEndPoint */acsURL,
                                /* authnRequestId */null,
                                "urn:oasis:names:tc:SAML:2.0:cm:bearer",
                                /* clock skew */samlClockSkew,
                                /* expire */samlExpire,
                                /* audience */null,
                                /* attrList */cred.getAttributes());
                Saml2Utils.signSamlObject(samlResponse);
                doc = Saml2Utils.asDOMDocument(samlResponse);
                sSamlResponse = Saml2Utils.dumpDocument(doc);
                HashMap<String, String> samlLoginResponseParams = new HashMap<String, String>();
                samlLoginResponseParams.put("SAMLResponse", Base64.encodeBytes(sSamlResponse.toByteArray()));
                samlLoginResponseParamsList.add(samlLoginResponseParams);
            }

            AuthnRequest authnRequest = Saml2Utils.createAuthnRequest(
                    Saml2Utils.generateRequestID(),
                    SAMLConstants.SAML2_POST_BINDING_URI, null, spIssuer,
                    new DateTime(), Saml2Constants.NAMEID_FORMAT_ENTITY);
            Saml2Utils.signSamlObject(authnRequest);
            doc = Saml2Utils.asDOMDocument(authnRequest);
            sSamlResponse = Saml2Utils.dumpDocument(doc);
            samlLoginRequestParams.put("SAMLRequest", Base64
                    .encodeBytes(sSamlResponse.toByteArray()));

            LogoutRequest logoutRequest = Saml2Utils.createLogoutRequest(
                    Saml2Utils.generateRequestID(), spIssuer, new DateTime(),
                    "dummy", Saml2Constants.NAMEID_FORMAT_ENTITY, null);
            Saml2Utils.signSamlObject(logoutRequest);
            doc = Saml2Utils.asDOMDocument(logoutRequest);
            sSamlResponse = Saml2Utils.dumpDocument(doc);
            samlLogoutRequestParams.put("SAMLRequest", Base64
                    .encodeBytes(sSamlResponse.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
