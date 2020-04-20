package com.intel.splat.identityservice.utils;


import java.security.NoSuchAlgorithmException;

import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
public class GenSecurityID {
	private static SecureRandomIdentifierGenerator _generator = null;
	
	/**
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public static void Initialize() 
	throws NoSuchAlgorithmException{
		if (_generator == null) {
			_generator = new SecureRandomIdentifierGenerator ();
		}
	}
	/**
	 * 
	 * @return
	 */
	public static String getSecurityID() {
        return _generator.generateIdentifier();
    }
}
