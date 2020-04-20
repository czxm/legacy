package com.intel.splat.identityservice.saml2;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;

import org.opensaml.common.SignableSAMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.StaticCredentialResolver;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.signature.impl.KeyInfoBuilder;
import org.opensaml.xml.signature.impl.KeyInfoImpl;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.signature.impl.SignatureImpl;

import com.intel.e360.identityservice.util.DocBuilderPool;
import com.intel.splat.identityservice.utils.X509KeyUtils;
/**
 * Sign/Verify SAML object according to the specified key.
 * 
 */
public class SamlSignatureUtils {
    public static void signSamlObject(SignableSAMLObject samlObject,
    		PublicKey publicKey, 
    		PrivateKey privateKey,
			String c14nAlgorithm, 
			String signAlgorithm) 
    		throws SignatureException,
    		MarshallingException {
        SignatureBuilder signatureBuilder = new SignatureBuilder();
        SignatureImpl signature = signatureBuilder.buildObject();
        BasicX509Credential credential = new BasicX509Credential();
        // Set the private key used to sign the messages
        credential.setPrivateKey(privateKey);
        // add the public key if we have it
        if (publicKey != null) {
        	credential.setPublicKey(publicKey);
            // Now add a KeyInfo section to the signature 
        	// so we can send our public certificate in it
        	KeyInfoBuilder keyInfoBuilder = new KeyInfoBuilder();
        	KeyInfoImpl keyInfo = (KeyInfoImpl) keyInfoBuilder.buildObject();
            KeyInfoHelper.addPublicKey(keyInfo, publicKey);
            signature.setKeyInfo(keyInfo);
        }
        signature.setSigningCredential(credential);
        signature.setCanonicalizationAlgorithm(c14nAlgorithm);
        signature.setSignatureAlgorithm(signAlgorithm);
        
        samlObject.setSignature(signature);
        // Get the marshaller factory
        MarshallerFactory marshallerFactory = org.opensaml.Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(samlObject);

        // By marshalling the assertion, we will create the XML 
        // so that the signing will have something to sign
        marshaller.marshall(samlObject,DocBuilderPool.getDocumentBuilder(String.valueOf(Thread.currentThread().getId())).newDocument());

        // Now sign it
        org.opensaml.xml.signature.Signer.signObject(signature);
    }
    
    public static boolean verifySamlObject(SignableSAMLObject samlObject,
    		String issuer,
    		PublicKey publicKey) throws SecurityException {
        BasicX509Credential credential = new BasicX509Credential();
        credential.setPublicKey(publicKey);
        StaticCredentialResolver credResolver = new StaticCredentialResolver(credential);
        KeyInfoCredentialResolver kiResolver = SecurityTestHelper.buildBasicInlineKeyInfoResolver();
        ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(credResolver, kiResolver);
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIDCriteria(issuer) );
        return trustEngine.validate(samlObject.getSignature(), criteriaSet);
    }
    
    public static void signSamlObjectByKeyFile(SignableSAMLObject samlObject,
			String certFile, 
			String privateKeyFile,
			String c14nAlgorithm, 
			String signAlgorithm) 
    		throws IOException, 
    		CertificateException, 
    		SignatureException, 
    		MarshallingException {
        PrivateKey privateKey = X509KeyUtils.getPrivateKeyFromFile(privateKeyFile);
        PublicKey publicKey = X509KeyUtils.getCertificateFromFile(certFile).getPublicKey();
        signSamlObject(samlObject, publicKey, privateKey, c14nAlgorithm, signAlgorithm);
	}
    
    public static void signSamlObjectByKeyContent(SignableSAMLObject samlObject,
			String certContent, 
			String privateKeyContent,
			String c14nAlgorithm, 
			String signAlgorithm) 
    		throws IOException, 
    		CertificateException, 
    		SignatureException, 
    		MarshallingException {
        PrivateKey privateKey = X509KeyUtils.getPrivateKeyContentFromMemory(privateKeyContent);
        PublicKey publicKey = X509KeyUtils.getCertificateFromContent(certContent).getPublicKey();
        signSamlObject(samlObject, publicKey, privateKey, c14nAlgorithm, signAlgorithm);
    }
}
