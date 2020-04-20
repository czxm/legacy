package com.intel.splat.identityservice.certificate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import sun.misc.BASE64Encoder;
import sun.security.provider.X509Factory;

import com.intel.splat.identityservice.utils.KeyStoreUtils;



/**
 * This class will be responsible for managing all the key materials used by Split Point
 * runtime, includes the SSL connection Certificate, and those certificates/private key pairs
 * used for SAML communication, etc. 
 * Since key materials accessing is usually on the transaction critical path, the performance
 * and scalability should be considered in this class.  
 * Please note that the KeyModel conversion will be handled by the RPC, instead of here.
 * @author Administrator
 *
 */
public class CertificateManager {
	
	// currently refering the keyManager used by Jetty, which has a strange design assumption
	// that the keyStore only contains one key path
	private KeyStore _sslKeyStore; 	
	private KeyStore _keyStore;
	private static CertificateManager _instance = null;
	private static boolean _isInitialized = false;
	
	private CertificateManager() {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public synchronized static CertificateManager getInstance() throws Exception{
		if (_instance == null) {
			_instance = new CertificateManager();
			try {
				_instance.initialize();
			} catch (Exception e) {
				throw e;
			}
		}	
		return _instance;
	}

	/**
	 * load Split Point keystore from the configuration directory. Create one if 
	 * not existed.
	 * @throws KeyStoreException 
	 */
	private synchronized void initialize() throws KeyStoreException {
		if (_isInitialized == true)
			return;
		
		_keyStore = KeyStoreUtils.getKeyStore(getKeyStoreStream(), getKeyStorePass());		
	}
	
	
	public List<String> getAllAliases() throws KeyStoreException {
		return KeyStoreUtils.getAliases(_keyStore);
	}
	

	public PrivateKey getPrivateKey(String alias) throws GeneralSecurityException {
		return (PrivateKey) KeyStoreUtils.getKey(_keyStore, alias, getKeyStorePass());
	}
	/**
	 * check whether one key is certificate entry or key entry
	 * @param alias
	 * @return
	 */
    public boolean isKeyEntry(String alias) {
    	try {
			return _keyStore.isKeyEntry(alias);
		} catch (KeyStoreException e) {
			return false;
		}
    }
	
    
    public String getCertificateAsPEM(String alias) throws KeyStoreException {
    	X509Certificate cert = getCertificate(alias, false);	// do not check status when exporting cert
        BASE64Encoder encoder = new BASE64Encoder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try{
        	encoder.encodeBuffer(cert.getEncoded(), out);
        } catch (Exception e) {
        	throw new KeyStoreException("cert encoding error, alias " + alias, e);
        }
        return  X509Factory.BEGIN_CERT + "\n" + out.toString() + X509Factory.END_CERT + "\n";
    }


    /**
	 * @param alias
	 * @return
	 * @throws KeyStoreException
	 */
	public X509Certificate getCertificate(String alias, boolean check) throws KeyStoreException {
		java.security.cert.Certificate cert = _keyStore.getCertificate(alias);
		if (cert instanceof X509Certificate) {
			X509Certificate x509Cert = (X509Certificate)cert;
			return x509Cert;
		}
		return null;
	}


    /**
	 * @param alias
	 * @return
	 * @throws KeyStoreException
	 */
	public X509Certificate getCertificateWithStatus(String alias) throws KeyStoreException {
		return getCertificate(alias, true);
	}
	
	/**
	 * some may need the keystore object
	 * @return
	 */
	public KeyStore getKeyStore() {
		return _keyStore;
	}
	
	public InputStream getKeyStoreStream(){
		return CertificateManager.class.getClassLoader().getSystemResourceAsStream("keystore.intel");
	}
	
	public String getKeyStorePass() {
		return "<redact>";
	}
}
