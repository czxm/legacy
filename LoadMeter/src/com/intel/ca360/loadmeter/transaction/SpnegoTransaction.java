package com.intel.ca360.loadmeter.transaction;

import java.security.Principal;
import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;

import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;
import com.intel.ca360.loadmeter.driver.KerbCredential;


public class SpnegoTransaction extends AbstractHttpTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(SpnegoTransaction.class);
	private AuthzData authz = null;
	private Credentials cred = null;
	private LoginContext context = null;
	private String loginURL = "unreachable";
	private String appURL = "unreachable";
	private String appMatch = "";
	private String realm = "@ACME.COM";
	private long startTimestamp;
	
	public SpnegoTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		for(ParamType p : params){
			if(p.getName().equals("loginURL")){
				loginURL = p.getValue();
			}
			else if(p.getName().equals("appURL")){
				appURL = p.getValue();
			}
			else if(p.getName().equals("appMatch")){
				appMatch = p.getValue();
			}
			else if(p.getName().equals("user")){
				if(authz == null){
					authz = new AuthzData("N/A", "N/A");
				}
				authz.setUserName(p.getValue());
			}
			else if(p.getName().equals("passwd")){
				if(authz == null){
					authz = new AuthzData("N/A", "N/A");
				}
				authz.setPassword(p.getValue());
			}
			else if(p.getName().equals("realm")){
				realm = "@" + p.getValue();
			}
		}
	}

	@Override
	public boolean execute(boolean negative) {
		boolean result = false;
		try{
			if(!loginURL.equals("unreachable")){
				result = getRequest(loginURL, authz.getUserName(), cred);
			}
			if(!appURL.equals("unreachable")){
				result = getRequest(appURL, appMatch);
			}
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		finally{
			if(!result || System.currentTimeMillis() - startTimestamp > 8 * 3600 * 1000){
				clearCookies();
				logout();
				login();
			}
		}
		return result;
	}

	@Override
	public void shutdown() {
		logout();
	}

	@Override
	public void setup(final AuthzData authz) {
		if(this.authz == null)
			this.authz = authz;
		login();
		startTimestamp = System.currentTimeMillis();
	}
	
	protected void logout(){
		if(context != null){
			try{
				context.logout();
			}
			catch(Exception e){
		    	e.printStackTrace();
			}
		}
	}
	
	protected void login(){
		try{
		    context = new LoginContext("com.sun.security.jgss.login", getUsernamePasswordHandler(authz));
	    	context.login();
		
			cred = new KerbCredential(){
				@Override
				public Subject getSubject() {
					return context.getSubject();
				}			
			};
	    }
	    catch(Exception e){
	    	context = null;
	    	e.printStackTrace();
	    }
	}

    protected CallbackHandler getUsernamePasswordHandler(final AuthzData authz){
        return new CallbackHandler() {
            public void handle(Callback[] callback)
            {
                for(int i = 0; i < callback.length; i++){
                    if(callback[i] instanceof NameCallback)
                    {
                        NameCallback nameCallback = (NameCallback)callback[i];
                        nameCallback.setName(authz.getUserName() + realm);
                    } else
                    if(callback[i] instanceof PasswordCallback)
                    {
                        PasswordCallback passCallback = (PasswordCallback)callback[i];
                        passCallback.setPassword(authz.getPasswords().length == 1 ? authz.getPassword().toCharArray() : authz.getPasswords()[1].toCharArray());
                    }
                }
            }
        };
    }
}
