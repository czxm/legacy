package com.intel.splat.identityservice.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.opensaml.xml.util.Base64;
import org.slf4j.LoggerFactory;


/**
 * 
 * Get the PublicKey or PrivateKey from key file or key string
 *
 */
public class X509KeyUtils {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(X509KeyUtils.class);
	
	private static final String CRT_KEY_HEAD = "-----BEGIN CERTIFICATE-----";
	private static final String CRT_KEY_TAIL = "-----END CERTIFICATE-----";
	private static final String PUBLIC_KEY_HEAD = "-----BEGIN PUBLIC KEY-----";
	private static final String PUBLIC_KEY_TAIL = "-----END PUBLIC KEY-----";	
	private static final String PRIVATE_KEY_HEAD = "-----BEGIN RSA PRIVATE KEY-----";
	private static final String PRIVATE_KEY_TAIL = "-----END RSA PRIVATE KEY-----";
	private static final String X509KEY_INSTANCE = "X.509";
	private static final String KEY_FACTORY_INSTANCE = "RSA"; 
	
	
	/**
	 * The certificate file must contain CRT_KEY_HEAD and CRT_KEY_TAIL
	 * @param certFile
	 * @return
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static X509Certificate getCertificateFromFile(String certFile) 
	throws CertificateException, IOException{
		CertificateFactory cf;
	    FileInputStream fin = new FileInputStream(certFile);
	    cf = CertificateFactory.getInstance(X509KEY_INSTANCE);
	    X509Certificate x509Cert = (X509Certificate) cf.generateCertificate(fin);
	    fin.close();
	    return x509Cert;
	}
	/**
	 * The certificate content must contain CRT_KEY_HEAD and CRT_KEY_TAIL
	 * @param keyString
	 * @return
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static X509Certificate getCertificateFromContent(String keyString) 
	throws CertificateException, IOException{
		if (!keyString.contains(CRT_KEY_HEAD) || !keyString.contains(CRT_KEY_TAIL)) {
			return null;
		}
		ByteArrayInputStream bin = 
			new ByteArrayInputStream(keyString.getBytes());
		CertificateFactory cf;
	    
	    cf = CertificateFactory.getInstance(X509KEY_INSTANCE);
	    X509Certificate x509Cert = 
	    	(X509Certificate) cf.generateCertificate(bin);
	    bin.close();
	    return x509Cert;
	}
	/**
	 * The public key file may contain PUBLIC_KEY_HEAD and PUBLIC_KEY_TAIL
	 * It does not matter if it does not contain them.
	 * @param pubKeyFile
	 * @return
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static PublicKey getPublicKeyFromFile(String pubKeyFile) 
	throws CertificateException, IOException{	    
	    return getPublicKeyFromContent(getPublicKeyContentFromFile(pubKeyFile));
	}
	
	public static String getPublicKeyContentFromFile(String pubKeyFile) 
	throws CertificateException, IOException{
		BufferedReader in = new BufferedReader(new FileReader(pubKeyFile));
		String line;
		StringBuffer pubKeyCont = new StringBuffer("");
	    
	    line = in.readLine();
	    while (line != null) {
	    	pubKeyCont.append(line).append("\r\n");
	    	line = in.readLine();
	    }
	    in.close();
	    
	    String pubKeyString = pubKeyCont.toString();
	    
	    pubKeyString = pubKeyString.replace(PUBLIC_KEY_HEAD, "");
	    pubKeyString = pubKeyString.replace(PUBLIC_KEY_TAIL, "");
	    pubKeyString = pubKeyString.trim();
	    
	    return pubKeyString;
	}
	
	public static PublicKey getPublicKeyFromContent(String keyString) {
		KeyFactory keyFactory;
		X509EncodedKeySpec pubSpec;
	    byte [] binaryKey;
	    PublicKey pubKey = null;
	    String pubKeyString = keyString;

		try {
	        keyFactory = KeyFactory.getInstance(KEY_FACTORY_INSTANCE);
	        binaryKey = Base64.decode(pubKeyString);
	        pubSpec = new X509EncodedKeySpec(binaryKey);
	        pubKey = keyFactory.generatePublic(pubSpec);
	    } catch (NoSuchAlgorithmException e) {
        	LOG.error("getPublicKeyFromContent error!", e);
	    } catch (InvalidKeySpecException e) {
        	LOG.error("getPublicKeyFromContent error!", e);
	    }
	    return pubKey;
	}
	
	public static PrivateKey getPrivateKeyFromFile(String privateKeyFile) 
	throws IOException{	    
	    return getPrivateKeyFromContent(getPrivateKeyContentFromFile(privateKeyFile));
	}
	/**
	 * The private key file must contain PRIVATE_KEY_HEAD and PRIVATE_KEY_TAIL
	 * @param privateKeyFile
	 * @return
	 * @throws IOException
	 */
	private static String getPrivateKeyContentFromFile(String privateKeyFile) 
	throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(privateKeyFile));
		String privateKeyString = parsePrivateKey(in);
	    in.close();
	    
	    return privateKeyString;
	}
	/**
	 * The private key string must contain PRIVATE_KEY_HEAD and PRIVATE_KEY_TAIL
	 * @param privateKeyMem
	 * @return
	 * @throws IOException
	 */
	public static PrivateKey getPrivateKeyContentFromMemory(String privateKeyMem) 
	throws IOException {
		BufferedReader in = new BufferedReader(new StringReader(privateKeyMem));
		String privateKeyString = parsePrivateKey(in);
	    in.close();
	    PrivateKey privateKey = getPrivateKeyFromContent(privateKeyString);
	    return privateKey;
	}
	
	private static String parsePrivateKey(BufferedReader in) 
	throws IOException{
		String line;
		StringBuffer privateKeyCont = new StringBuffer("");
	    
	    line = in.readLine();
	    while (line != null) {
	    	privateKeyCont.append(line).append("\r\n");
	    	line = in.readLine();
	    }
	    
	    String privateKeyString = privateKeyCont.toString();
	    privateKeyString = privateKeyString.replace(PRIVATE_KEY_HEAD, "");
	    privateKeyString = privateKeyString.replace(PRIVATE_KEY_TAIL, "");
	    privateKeyString = privateKeyString.trim();
	    
	    return privateKeyString;
	}
	/**
	 * The private key string must not contain PRIVATE_KEY_HEAD and PRIVATE_KEY_TAIL
	 * @param keyString
	 * @return
	 */
	public static PrivateKey getPrivateKeyFromContent(String keyString) {
		KeyFactory keyFactory;
	    PKCS8EncodedKeySpec privSpec;
	    byte [] binaryKey;
	    PrivateKey privateKey = null;
	    String privateKeyContent = keyString;

		try {
	    	BouncyCastleProvider provider = new BouncyCastleProvider();
	        keyFactory = KeyFactory.getInstance(KEY_FACTORY_INSTANCE, provider);
	        binaryKey = Base64.decode(privateKeyContent);
	        privSpec = new PKCS8EncodedKeySpec(binaryKey);
	        privateKey = keyFactory.generatePrivate(privSpec);
	    } catch (NoSuchAlgorithmException e) {
        	LOG.error("getPrivateKeyFromContent error!", e);
	    } catch (InvalidKeySpecException e) {
        	LOG.error("getPrivateKeyFromContent error!", e);
	    }
	    return privateKey;
	}
	
	
	public static KeyPair generateKeyPair(String keyAlgorithm, int numBits) {
		try {

			// Get the public/private key pair
			KeyPairGenerator keyGen = KeyPairGenerator
					.getInstance(keyAlgorithm);
			keyGen.initialize(numBits);
			KeyPair keyPair = keyGen.genKeyPair();
			PrivateKey privateKey = keyPair.getPrivate();
			PublicKey publicKey = keyPair.getPublic();
			return new KeyPair(publicKey, privateKey);
		} catch (Exception e) {
			LOG.error("generateKeyPair error!", e);
		}
		return null;
	}
	
	public static void generateKeys(String keyAlgorithm, int numBits) {
		try {

			// Get the public/private key pair
			KeyPairGenerator keyGen = KeyPairGenerator
					.getInstance(keyAlgorithm);
			keyGen.initialize(numBits);
			KeyPair keyPair = keyGen.genKeyPair();
			PrivateKey privateKey = keyPair.getPrivate();
			PublicKey publicKey = keyPair.getPublic();

			LOG.debug("\n" + "Generating key/value pair using "
					+ privateKey.getAlgorithm() + " algorithm");

			// Get the bytes of the public and private keys
			byte[] privateKeyBytes = privateKey.getEncoded();
			byte[] publicKeyBytes = publicKey.getEncoded();

			// Get the formats of the encoded bytes
			String formatPrivate = privateKey.getFormat(); // PKCS#8
			String formatPublic = publicKey.getFormat(); // X.509

			LOG.debug("  Private Key Format : " + formatPrivate);
			LOG.debug("  Public Key Format  : " + formatPublic);

			// The bytes can be converted back to public and private key objects
			KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
			EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
					privateKeyBytes);
			PrivateKey privateKey2 = keyFactory.generatePrivate(privateKeySpec);

			EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
					publicKeyBytes);
			PublicKey publicKey2 = keyFactory.generatePublic(publicKeySpec);

			// The original and new keys are the same
			LOG.debug("  Are both private keys equal? "
					+ privateKey.equals(privateKey2));

			LOG.debug("  Are both public keys equal? "
					+ publicKey.equals(publicKey2));

		} catch (InvalidKeySpecException e) {
        	LOG.error("generateKeys error!", e);
		} catch (NoSuchAlgorithmException e) {
			LOG.error("generateKeys error!", e);
		}
	}

	public static void main(String[] args) {

		// Generate a 1024-bit Digital Signature Algorithm (DSA) key pair
		generateKeys("DSA", 1024);

		// Generate a 576-bit DH key pair
		generateKeys("DH", 576);

		// Generate a 1024-bit RSA key pair
		generateKeys("RSA", 1024);
	}

}


