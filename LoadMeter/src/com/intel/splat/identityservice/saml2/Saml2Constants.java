package com.intel.splat.identityservice.saml2;

public class Saml2Constants {
	public static final String SAML_REQUEST = "SAMLRequest";
	
    public static final String SAML_RESPONSE = "SAMLResponse";
    
    public static final String RELAY_STATE = "RelayState";
    
    public static final String HTTP_REDIRCT_BINDING = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect";
    
    public static final String HTTP_POST_BINDING = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
    
    public static final String NAMEID_FORMAT_ENTITY = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";
    
    public static final String AUTHN_REQUEST_ID_PREFIX = "s2";
    
    public static final int AUTHN_REQUEST_ID_SUFFIX_LENGTH = 40;
}
