package com.intel.splat.identityservice.saml2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.BaseID;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.signature.SignatureConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.intel.e360.identityservice.conn.ServiceCredential;
import com.intel.e360.identityservice.util.DocBuilderPool;
import com.intel.splat.client.model.identityservice.config.common.C14NGenMethodType;
import com.intel.splat.client.model.identityservice.config.common.SignatureGenMethodType;
import com.intel.splat.identityservice.certificate.CertificateManager;
import com.intel.splat.identityservice.utils.CurrDateTime;
import com.intel.splat.identityservice.utils.GenSecurityID;
import com.intel.splat.identityservice.utils.SecureRandomUtils;
/**
 * Provide the methods to create SAML2 components,
 * except for signing the object. 
 *
 */
public class Saml2Utils {
    /**
     * Create NameID according to the following parameters
     * @param value
     * @param nameQualifier
     * @param spNameQualifier
     * @param format
     * @param spProviderID
     * @return
     */
    public static NameID createNameID(String value, 
            String nameQualifier, 
            String spNameQualifier,
            String format,
            String spProviderID) {
        NameID nameID = create (NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue (value);
        if (nameQualifier != null) {
            nameID.setNameQualifier(nameQualifier);
        }
        if (spNameQualifier != null) {
            nameID.setSPNameQualifier(spNameQualifier);
        }
        if (format != null) {
            nameID.setFormat (format);
        }
        return nameID;
    }
    /**
     * 
     * @param value
     * @param nameQualifier
     * @param spNameQualifier
     * @param format
     * @param spProviderID
     * @return
     */
    public static Issuer createIssuer(String value, 
            String nameQualifier, 
            String spNameQualifier,
            String format,
            String spProviderID) {
        Issuer issuer = create (Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue (value);
        if (nameQualifier != null) {
            issuer.setNameQualifier(nameQualifier);
        }
        if (spNameQualifier != null) {
            issuer.setSPNameQualifier(spNameQualifier);
        }
        if (format != null) {
            issuer.setFormat (format);
        }
        return issuer;
    }
    /**
     * create BaseID
     * @param nameQualifier
     * @param spNameQualifier
     * @return
     */
    public static BaseID createBaseID(String nameQualifier, String spNameQualifier) {
        BaseID baseID = create (BaseID.class, BaseID.DEFAULT_ELEMENT_NAME);
        if (nameQualifier != null) {
            baseID.setNameQualifier(nameQualifier);
        }
        if (spNameQualifier != null) {
            baseID.setSPNameQualifier(spNameQualifier);
        }
        return baseID;
    }
    
    //TODO. public static EncryptedID getEncryptedID() {}
    
    /**
     * create SubjectConfirmationData
     * @param notBefore - if value is not equal to null, set it
     * @param notOnOrAfter - if value is not equal to null, set it
     * @param recipient - optional
     * @param inResponseTo - optional
     * @param address - optional
     * @return
     */
    public static SubjectConfirmationData createSubjectConfirmationData (
            DateTime notBefore,
            DateTime notOnOrAfter,
            String recipient,
            String inResponseTo,
            String address) {
        SubjectConfirmationData confirmationData = create 
                (SubjectConfirmationData.class,
            	 SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
        
        if (notBefore != null) {
            confirmationData.setNotBefore(notBefore);
        }
        if (notOnOrAfter != null) {
            confirmationData.setNotOnOrAfter(notOnOrAfter);
        }
        if (recipient != null) {
            confirmationData.setRecipient(recipient);
        }
        if (inResponseTo != null) {
            confirmationData.setInResponseTo(inResponseTo);
        }
        if (address != null) {
            confirmationData.setAddress(address);
        }
        return confirmationData;
    }
    
    /**
     * 
     * @param confirmationMethod
     * @param subConfirmData
     * @return
     */
    public static SubjectConfirmation createSubjectConfirmation (
            String confirmationMethod,
            SubjectConfirmationData confirmationData) {
        SubjectConfirmation confirmation = 
                create (SubjectConfirmation.class, 
                    SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        confirmation.setMethod (confirmationMethod);
        confirmation.setSubjectConfirmationData(confirmationData);
        return confirmation;
    } 
    
    /**
     * create Subject according to NameID and SubjectConfirmation
     * @param nameID
     * @param confirmation
     * @return
     */
    public static Subject createSubject(NameID nameID,
            SubjectConfirmation confirmation) {
        Subject subject = create (Subject.class, Subject.DEFAULT_ELEMENT_NAME);
        subject.setNameID (nameID);
        subject.getSubjectConfirmations().add(confirmation);
        return subject;
    }
    
    /**
     * 
     * @param audienceName
     * @return
     */
    public static Audience createAudience(String audienceName) {
        Audience audience = create (Audience.class, Audience.DEFAULT_ELEMENT_NAME);
        	audience.setAudienceURI(audienceName);
        return audience;
    }
    
    /**
     * create Conditions
     * @param notBefore
     * @param notOnOrAfter
     * @param audienceName
     * @return
     */
    public static Conditions createConditions(
            DateTime notBefore,
            DateTime notAfter,
            String[] audiences) {
        Conditions conditions = create 
            (Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
        conditions.setNotBefore (notBefore);
        conditions.setNotOnOrAfter (notAfter);
        
        if(audiences != null && audiences.length > 0) {
	        AudienceRestriction audienceRestriction = create
	                (AudienceRestriction.class, AudienceRestriction.DEFAULT_ELEMENT_NAME);
	        for(String audienceName : audiences) {
		        Audience audience = createAudience(audienceName);
		        audienceRestriction.getAudiences().add(audience);
	        }
	        conditions.getAudienceRestrictions().add(audienceRestriction);
        }
        
        return conditions;
    }
    
    /**
     * create a simple enough response
     * @param nameIDValue
     * @param issuerValue
     * @param attrList
     * @return
     */
    public static Response createSimpleResponse(
    		String nameIDValue,
    		String issuerValue,
    		List<ServiceCredential.Attribute> attrList){
    	DateTime curDateTime = CurrDateTime.getCurrDateTime(true);
        Response response = createResponse (
        		StatusCode.SUCCESS_URI, null, null, curDateTime);
        Assertion assertion = 
        	createSimpleSaml2Assertion(nameIDValue, issuerValue, curDateTime);
        Issuer issuer = createIssuer(issuerValue, null, null, null, null);
        response.setIssuer(issuer);
        response.getAssertions().add(assertion);
        
        return response;
    }
    
    /**
     * create a simple enough SAML2 Assertion
     * @param nameIDValue
     * @param issuerValue
     * @param curDateTime
     * @return
     */
    public static Assertion createSimpleSaml2Assertion(
    		String nameIDValue,
    		String issuerValue,
    		DateTime curDateTime){
    	Assertion assertion = 
            create (Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setID (GenSecurityID.getSecurityID());
        assertion.setIssueInstant (curDateTime);
        assertion.setVersion(SAMLVersion.valueOf("2.0"));
        Issuer issuer = Saml2Utils.createIssuer(issuerValue, null, null, null, null);
		NameID nameID = Saml2Utils.createNameID(nameIDValue, null, null, null, null);
		Subject subject = Saml2Utils.createSubject(nameID, null);
		assertion.setIssuer(issuer);
		assertion.setSubject(subject);
		
		return assertion;
    }
    
    /**
     * create the response according to the following parameters
     * @param nameIDValue
     * @param nameIDformat
     * @param nameQualifier
     * @param spNameQualifier
     * @param recipient
     * @param inResponseTo
     * @param confirmationMethod
     * @param audienceName
     * @param attr
     * @param attrValue
     * @return
     */
    public static Response createResponse(
            String nameIDValue,
            String nameIDformat,
            String nameQualifier,
            String spNameQualifier,
            String recipient,
            String inResponseTo,
            String confirmationMethod,
            int clockSkew,
            int lifetime,
            String[] audiences,
            List<ServiceCredential.Attribute> attrList) {
        DateTime currDate = CurrDateTime.getCurrDateTime(true);
        Response response = createResponse (StatusCode.SUCCESS_URI, null, inResponseTo, currDate);
        
        Assertion assertion = createSaml2Assertion(
            nameIDValue,
            nameIDformat,
            nameQualifier,
            spNameQualifier,
            recipient,
            inResponseTo,
            confirmationMethod,
            clockSkew,
            lifetime,
            audiences,
            currDate);
        if (attrList != null) {
            // attribute statement
        	for(ServiceCredential.Attribute attr : attrList) {
        		AttributeStatement attrStatement = 
                    createAttributeStatement(attr.getName(), attr.getType(), attr.getValue());
        		assertion.getStatements ().add (attrStatement);
        	}
        }
        Issuer respIssuer = createIssuer(nameQualifier, null, null, null, null);
        response.setIssuer(respIssuer);
        response.getAssertions().add(assertion);
        response.setDestination(recipient);
        return response;
    }
    
    public static Response createResponse (
            String statusCode,
            String message,
            String inResponseTo,
            DateTime currDate)
    {
        Response response = create 
            (Response.class, Response.DEFAULT_ELEMENT_NAME);
        response.setID (GenSecurityID.getSecurityID());

        if (inResponseTo != null) {
            response.setInResponseTo (inResponseTo);
        }
        
        response.setIssueInstant (currDate);
        
        StatusCode statusCodeElement = create 
            (StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
        statusCodeElement.setValue (statusCode);
        
        Status status = create (Status.class, Status.DEFAULT_ELEMENT_NAME);
        status.setStatusCode (statusCodeElement);
        response.setStatus (status);

        if (message != null)
        {
            StatusMessage statusMessage = create 
                (StatusMessage.class, StatusMessage.DEFAULT_ELEMENT_NAME);
            statusMessage.setMessage (message);
            status.setStatusMessage (statusMessage);
        }
        
        return response;
    }
    /**
     * 
     * @param nameIDValue - username
     * @param nameIDformat - urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified
     * @param nameQualifier - http://soae-fnt-hpt086.sh.intel.com:8080/opensso
     * @param spNameQualifier - https://saml.salesforce.com
     * @param recipient - https://www.google.com/a/XXX.com/acs
     * @param inResponseTo - AuthnRequest issuer ID
     * @param confirmationMethod - urn:oasis:names:tc:SAML:2.0:cm:bearer
     * @param audienceName - "google.com/a/XXX.com" or "https://saml.salesforce.com"
     * @return
     */
    public static Assertion createSaml2Assertion(
            String nameIDValue,
            String nameIDformat,
            String nameQualifier,
            String spNameQualifier,
            String recipient,
            String inResponseTo,
            String confirmationMethod,
            int clockSkew,
            int lifetime,
            String[] audiences,
            DateTime currDate) {
        // Assertion
        Assertion assertion = 
            create (Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setID (GenSecurityID.getSecurityID());
        assertion.setIssueInstant (currDate);
        assertion.setVersion(SAMLVersion.valueOf("2.0"));
        
        // Issuer
        Issuer issuer = createIssuer(nameQualifier, null, null, null, null);
        assertion.setIssuer(issuer);
        
        NameID nameId = createNameID(nameIDValue, 
                                  nameQualifier, 
                                  spNameQualifier,
                                  nameIDformat,
                                  null);
        // NotBefore is forbidden to set
        SubjectConfirmationData confirmData = createSubjectConfirmationData(
                null, currDate.plusSeconds(lifetime + clockSkew), recipient, inResponseTo, null);
        
        SubjectConfirmation confirmation = createSubjectConfirmation(
                confirmationMethod, confirmData);
        // Subject
        Subject subject = createSubject(nameId, confirmation);
        assertion.setSubject(subject);
        // Conditions
        Conditions conditions = createConditions(currDate.minusSeconds(clockSkew), currDate.plusSeconds(clockSkew + lifetime), audiences);
        assertion.setConditions(conditions);
        // authnStatement
        AuthnContextClassRef ref = create (AuthnContextClassRef.class, 
                AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        ref.setAuthnContextClassRef (AuthnContext.PPT_AUTHN_CTX);
        AuthnContext authnContext = create 
            (AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
        authnContext.setAuthnContextClassRef (ref);
        AuthnStatement authnStatement = create 
        	(AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
        authnStatement.setAuthnContext (authnContext);
        authnStatement.setAuthnInstant(currDate);
        authnStatement.setSessionIndex(GenSecurityID.getSecurityID());
        assertion.getStatements ().add (authnStatement);
        
        return assertion;
    }
    
    /**
     * create attributeStatement according to attributes
     * @param attr
     * @param attrValue
     * @return
     */
    public static AttributeStatement createAttributeStatement (
            String attrName,
            String attrNameFormat,
            String attrValue) {
    	return createAttributeStatement(attrName, attrNameFormat, new String[]{attrValue});
    } 
    
    /**
     * create attributeStatement according to attributes
     * @param attr
     * @param attrValues
     * @return
     */
    public static AttributeStatement createAttributeStatement (
            String attrName,
            String attrNameFormat,
            String[] attrValues) {
        AttributeStatement attrStatement = create (AttributeStatement.class,
                        AttributeStatement.DEFAULT_ELEMENT_NAME);
    	addAttribute(attrStatement, attrName, attrNameFormat, attrValues);
        return attrStatement;
    } 
        
    /**
     * 
     * @param statement
     * @param name
     * @param value
     */
    private static void addAttribute (
            AttributeStatement statement, 
            String name, 
            String nameFormat,
            String[] values){
        // Build attribute values as XMLObjects;
        //  there is an AttributeValue interface, but it's apparently dead code
        final XMLObjectBuilder builder = 
            Configuration.getBuilderFactory ().getBuilder (XSAny.TYPE_NAME);
        
        Attribute attribute = create 
        (Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName (name);
        if(nameFormat != null) {
        	attribute.setNameFormat(nameFormat);
        }
        for(String value : values){
	        XSAny valueElement = (XSAny) builder.buildObject 
	            (AttributeValue.DEFAULT_ELEMENT_NAME);
	        valueElement.setTextContent (value);
	        //valueElement.setValue(value);
	        
	        attribute.getAttributeValues ().add (valueElement);
        }
        statement.getAttributes ().add (attribute);
    }
    
    public static SessionIndex createSessionIndex(String index) {
    	SessionIndex sessionIndex = create (SessionIndex.class, SessionIndex.DEFAULT_ELEMENT_NAME);
    	sessionIndex.setSessionIndex(index);
    	return sessionIndex;
    }
    
    public static AuthnRequest createAuthnRequest(String requestId,
    		String requestBinding,
    		String acsUrl,
    		String issuerName,
    		DateTime issueDate,
    		String nameIdFormat) {
    	AuthnRequest authnRequest = create(AuthnRequest.class, AuthnRequest.DEFAULT_ELEMENT_NAME);
    	
		authnRequest.setForceAuthn(false);
		authnRequest.setIsPassive(false);
		authnRequest.setIssueInstant(issueDate);
		authnRequest.setProtocolBinding(requestBinding);
		authnRequest.setAssertionConsumerServiceURL(acsUrl);
		
	    Issuer issuer = Saml2Utils.createIssuer(issuerName, null, null, null, null);
		authnRequest.setIssuer(issuer);
		
	    NameIDPolicy nameIdPolicy = Saml2Utils.createNameIDPolicy();
	    nameIdPolicy.setFormat(nameIdFormat);
	    nameIdPolicy.setAllowCreate(true);
		authnRequest.setNameIDPolicy(nameIdPolicy);
		
		authnRequest.setID(requestId);
		authnRequest.setVersion(SAMLVersion.VERSION_20);
		
		return authnRequest;
    }
    
    public static NameIDPolicy createNameIDPolicy() {
    	return create(NameIDPolicy.class, NameIDPolicy.DEFAULT_ELEMENT_NAME); 
    }
    
    public static String generateRequestID() {
    	return Saml2Constants.AUTHN_REQUEST_ID_PREFIX + 
    		SecureRandomUtils.generateRandomHexString(Saml2Constants.AUTHN_REQUEST_ID_SUFFIX_LENGTH);
    }
    
    public static LogoutRequest createLogoutRequest(String requestId,
    		String issuerName,
    		DateTime issueDate,
    		String desUrl,
    		String nameIdValue,
    		List<String> sessionIndexList) {
    	LogoutRequest logoutRequest = create(LogoutRequest.class, LogoutRequest.DEFAULT_ELEMENT_NAME);
    	
	    Issuer issuer = Saml2Utils.createIssuer(issuerName, null, null, null, null);
	    
        NameID nameId = Saml2Utils.createNameID(nameIdValue, 
                null, 
                null,
                "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecifie",
                null);
	    logoutRequest.setIssueInstant(issueDate);
	    logoutRequest.setIssuer(issuer);
	    logoutRequest.setNameID(nameId);
	    logoutRequest.setID(requestId);
	    logoutRequest.setVersion(SAMLVersion.VERSION_20);
	    if(desUrl != null) {
	    	logoutRequest.setDestination(desUrl);
	    }
	    if(sessionIndexList != null && !sessionIndexList.isEmpty()) {
	    	for(String sessionIndex : sessionIndexList) {
	    	    logoutRequest.getSessionIndexes().add(Saml2Utils.createSessionIndex(sessionIndex));
	    	}
	    }
	    return logoutRequest;
    }
    
    public static LogoutResponse createLogoutResponse (
            String statusCode,
            String message,
            String issuerName,
            String inResponseTo,
            DateTime currDate) {
        LogoutResponse logoutResponse = create 
            (LogoutResponse.class, LogoutResponse.DEFAULT_ELEMENT_NAME);
        logoutResponse.setID (GenSecurityID.getSecurityID());

        if (inResponseTo != null) {
        	logoutResponse.setInResponseTo (inResponseTo);
        }
        
        logoutResponse.setIssueInstant (currDate);
        
        StatusCode statusCodeElement = create 
            (StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
        statusCodeElement.setValue (statusCode);
        
        Status status = create (Status.class, Status.DEFAULT_ELEMENT_NAME);
        status.setStatusCode (statusCodeElement);
        logoutResponse.setStatus (status);

        if (message != null)
        {
            StatusMessage statusMessage = create 
                (StatusMessage.class, StatusMessage.DEFAULT_ELEMENT_NAME);
            statusMessage.setMessage (message);
            status.setStatusMessage (statusMessage);
        }
        Issuer respIssuer = createIssuer(issuerName, null, null, null, null);
        logoutResponse.setIssuer(respIssuer);
        return logoutResponse;
    }

    public static Document asDOMDocument(XMLObject object)
        	throws IOException, 
        	MarshallingException, 
        	TransformerException, 
        	ParserConfigurationException {
/*        DocumentBuilderFactory factory = 
                DocumentBuilderFactory.newInstance ();
        factory.setNamespaceAware (true);
        DocumentBuilder builder = factory.newDocumentBuilder();  */
    	DocumentBuilder builder = DocBuilderPool.getDocumentBuilder(String.valueOf(Thread.currentThread().getId()));
        Document document = builder.newDocument ();
        Marshaller out = Configuration.getMarshallerFactory ().getMarshaller (object);
        out.marshall (object, document);
        return document;
    }
    
    public static ByteArrayOutputStream dumpDocument(Document doc) 
			throws TransformerConfigurationException, 
			TransformerException, 
			TransformerFactoryConfigurationError {
    	ByteArrayOutputStream buffer = new ByteArrayOutputStream ();
    	TransformerFactory.newInstance().newTransformer()
    		.transform (new DOMSource(doc), new StreamResult(buffer));
    	return buffer;
    }
    
    public static String getInResponseToAttrFromResponse(Response response) {
		if(response.getAssertions() != null && !response.getAssertions().isEmpty()) {
			Assertion assertion = response.getAssertions().get(0);
			if(assertion.getSubject() != null && 
					assertion.getSubject().getSubjectConfirmations() != null &&
					!assertion.getSubject().getSubjectConfirmations().isEmpty()) {
				SubjectConfirmation subjectConfirmation = assertion.getSubject().getSubjectConfirmations().get(0);
				SubjectConfirmationData confirmData = subjectConfirmation.getSubjectConfirmationData();
				if(confirmData != null) {
					return confirmData.getInResponseTo();
				}
			}
		}
		return null;
    }
    
    public static boolean verifyResponse(Response response,
    		String issuer,
    		String recipient,
    		String audience) 
    		throws SecurityException {
		Status status = response.getStatus();
		if(status == null || !status.getStatusCode().getValue().equals(StatusCode.SUCCESS_URI)) {
			return false;
		}
		Assertion assertion = null;
		if((response.getAssertions() == null ||
				(assertion = response.getAssertions().get(0)) == null)) {
			return false;
		}
		
		if(assertion.getIssuer().getValue() == null ||
				!assertion.getIssuer().getValue().equals(issuer)) {
			return false;
		}
		
		if(assertion.getSubject() == null || 
				assertion.getSubject().getSubjectConfirmations() == null ||
				assertion.getSubject().getSubjectConfirmations().isEmpty()) {
			return false;
		}
		SubjectConfirmation subjectConfirmation = assertion.getSubject().getSubjectConfirmations().get(0);
		SubjectConfirmationData confirmData = subjectConfirmation.getSubjectConfirmationData();
		if(confirmData == null ||
				confirmData.getRecipient() == null ||
				confirmData.getNotOnOrAfter() == null) {
			return false;
		}
		String recvRecipient = confirmData.getRecipient();
		DateTime recvNotOnOrAfter = confirmData.getNotOnOrAfter();
		if(recvRecipient == null || !recvRecipient.equals(recipient)) {
			return false;
		}
		if(!recvNotOnOrAfter.isAfterNow()) {
			return false;
		}
		
		if(assertion.getConditions() != null && audience != null) {
			Conditions conditions = assertion.getConditions();
			if(conditions.getAudienceRestrictions() != null) {
				for(AudienceRestriction audienceRestriction : conditions.getAudienceRestrictions()) {
					boolean hasMatchedAudience = false;
					for(Audience aud : audienceRestriction.getAudiences()) {
						if(audience.equals(aud.getAudienceURI())) {
							hasMatchedAudience = true;
						}
					}
					if(!hasMatchedAudience) {
						return false;
					}
				}
			}
		}
		
    	return true;
    }
    
    public static boolean verifyLogoutRequest(LogoutRequest request,
    		String issuer) throws SecurityException {
		if(!issuer.equals(request.getIssuer().getValue())) {
			return false;
		}
    	return true;
    }
    
    public static boolean verifyLogoutResponse(LogoutResponse response,
    		String issuer) throws SecurityException {
		Status status = response.getStatus();
		if(status == null || !status.getStatusCode().getValue().equals(StatusCode.SUCCESS_URI)) {
			return false;
		}
		if(!issuer.equals(response.getIssuer().getValue())) {
			return false;
		}
    	return true;
    }
    
    // cast to SAMLObjectBuilder<T> is caller's choice 
    @SuppressWarnings ("unchecked")
    private static <T> T create (Class<T> cls, QName qname) {
        return (T)((XMLObjectBuilder) 
            Configuration.getBuilderFactory().getBuilder(qname))
                .buildObject (qname);
    }
    
    public static <T> T deserialize(Class<T> cls, String objStr) 
    		throws XMLParserException, UnmarshallingException {
		BasicParserPool bpp = new BasicParserPool();
	    bpp.setNamespaceAware(true);

	    ByteArrayInputStream ins = new ByteArrayInputStream(objStr.getBytes());
	    
	    Document ReqDoc = bpp.parse(ins);
		Element requestElem = ReqDoc.getDocumentElement();
	    UnmarshallerFactory uf = Configuration.getUnmarshallerFactory();
	    Unmarshaller unmarshaller = uf.getUnmarshaller(requestElem);
	    XMLObject xmlObject =  unmarshaller.unmarshall(requestElem);
	    if(xmlObject != null && cls.isAssignableFrom(xmlObject.getClass())) {
			return cls.cast(xmlObject);
		} else {
			return null;
		}
    }
    
    private static String convertC14NMethod(String localC14NMethod) {
		String c14nMethod = CanonicalizationMethod.EXCLUSIVE;
		if(C14NGenMethodType.C_14_N_EXCLUSIVE.value().equals(localC14NMethod)) {
			c14nMethod = SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
		}
		return c14nMethod;
    }
    
    private static String convertSignatureMethod(String localSignatureMethod) {
		String signatureMethod = SignatureMethod.RSA_SHA1;
		if(SignatureGenMethodType.RSA_WITH_SHA_1.equals(localSignatureMethod)) {
			signatureMethod = SignatureMethod.RSA_SHA1;
		}
		return signatureMethod;
    }
	
    public static void signSamlObject(SignableSAMLObject samlObject) 
    		throws Exception {
		String alias = "intel cloud expressway";
		PublicKey publicKey = CertificateManager.getInstance().getCertificateWithStatus(alias).getPublicKey();
		PrivateKey privateKey = CertificateManager.getInstance().getPrivateKey(alias);
		String canonicalizationMethod = convertC14NMethod("C14N_exclusive");
		String signatureMethod = convertSignatureMethod("rsaWithSha1");
			
		// Sign SAML assertion
		SamlSignatureUtils.signSamlObject(samlObject, 
				publicKey, 
				privateKey,
	        	canonicalizationMethod, 
	        	signatureMethod);  	
    }
}

