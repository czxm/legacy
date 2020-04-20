package com.intel.ca360.loadmeter.driver;

import java.util.List;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.GenericDriver;

public class ADDriver extends GenericDriver{
	@Override
	protected void prepare(List<ParamType> params){		
		for(ParamType param : params){
			if(param.getName().equals("java.security.auth.login.config")){
				System.setProperty("java.security.auth.login.config", param.getValue());	
			}
			else if(param.getName().equals("java.security.krb5.conf")){
				System.setProperty("java.security.krb5.conf", param.getValue());
			}
			else if(param.getName().equals("sun.security.krb5.debug")){
				System.setProperty("sun.security.krb5.debug", param.getValue());
			}
			else if(param.getName().equals("javax.security.auth.useSubjectCredsOnly")){
				System.setProperty("javax.security.auth.useSubjectCredsOnly", param.getValue());
			}
		}
	}
}
