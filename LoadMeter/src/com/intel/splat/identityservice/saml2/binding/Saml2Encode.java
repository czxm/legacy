package com.intel.splat.identityservice.saml2.binding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.Base64;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.intel.splat.identityservice.saml2.Saml2Utils;

public class Saml2Encode {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Saml2Encode.class);
    
    public static void setSamlRequestForPost(Writer response,
    		RequestAbstractType samlRequest,
    		String requestUrl,
    		String relayState) 
    		throws IOException, 
    		TransformerConfigurationException, 
    		TransformerException, 
    		TransformerFactoryConfigurationError, 
    		MarshallingException, 
    		ParserConfigurationException {
		Document doc = Saml2Utils.asDOMDocument(samlRequest);
        ByteArrayOutputStream sSamlRequest = Saml2Utils.dumpDocument(doc);

        PrintWriter out = new PrintWriter(response);
        out.println("<HTML>");
        out.println("<HEAD>\n");
        out.println("<TITLE>Access rights validated</TITLE>\n");
        out.println("</HEAD>\n");
        out.println("<BODY onLoad=\"document.forms[0].submit()\">");
        out.println("<FORM METHOD=\"POST\" ACTION=\"" + requestUrl + "\">");
        out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"SAMLRequest\" " + 
        			"VALUE=\"" + Base64.encodeBytes(sSamlRequest.toByteArray()) + "\">");
        if (relayState != null && !relayState.equals("")) {
        	out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"RelayState\" "+
                    "VALUE=\"" + relayState + "\">");
        }	        
        out.println("<NOSCRIPT><CENTER>");
        out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"Submit SAMLRequest data\"/>");
        out.println("</CENTER></NOSCRIPT>");
        out.println("</FORM></BODY></HTML>");
        out.close();
    }

    public static void setSamlResponseForPost(Writer response,
    		SAMLObject samlResponse,
    		String requestUrl,
    		String relayState) 
    		throws IOException, 
    		TransformerConfigurationException, 
    		TransformerException, 
    		TransformerFactoryConfigurationError, 
    		MarshallingException, 
    		ParserConfigurationException {
		Document doc = Saml2Utils.asDOMDocument(samlResponse);
        ByteArrayOutputStream sSamlResponse = Saml2Utils.dumpDocument(doc);
        
		PrintWriter out = new PrintWriter(response);	        
	    out.println("<HTML>");
	    out.println("<HEAD>\n");
	    out.println("<TITLE>Access rights validated</TITLE>\n");
	    out.println("</HEAD>\n");
	    out.println("<BODY onLoad=\"document.forms[0].submit()\">");
	    out.println("<FORM METHOD=\"POST\" ACTION=\"" + requestUrl + "\">");
	    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"SAMLResponse\" " + 
	        			"VALUE=\"" + Base64.encodeBytes(sSamlResponse.toByteArray()) + "\">");
	    if (relayState != null && !relayState.equals("")) {
	    	out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"RelayState\" "+
	                   "VALUE=\"" + relayState + "\">");
	    }	        
	    out.println("<NOSCRIPT><CENTER>");
	    out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"Submit SAMLResponse data\"/>");
	    out.println("</CENTER></NOSCRIPT>");
	    out.println("</FORM></BODY></HTML>");
	    out.close();
    }
}
