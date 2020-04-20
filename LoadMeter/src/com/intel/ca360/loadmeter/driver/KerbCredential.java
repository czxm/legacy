package com.intel.ca360.loadmeter.driver;

import java.security.Principal;

import javax.security.auth.Subject;

import org.apache.http.auth.Credentials;

public abstract class KerbCredential implements Credentials {
	public abstract Subject getSubject();

	@Override
	public String getPassword() {
		//unused
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		//unused
		return null;
	}
}
