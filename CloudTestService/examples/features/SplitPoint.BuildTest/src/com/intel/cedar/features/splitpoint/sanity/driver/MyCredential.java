package com.intel.cedar.features.splitpoint.sanity.driver;

import javax.security.auth.Subject;

import org.apache.http.auth.Credentials;

public interface MyCredential extends Credentials {
	public Subject getSubject();
}
