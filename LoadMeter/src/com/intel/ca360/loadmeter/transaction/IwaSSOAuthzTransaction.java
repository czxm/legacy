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


public class IwaSSOAuthzTransaction extends AbstractHttpTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(IwaSSOAuthzTransaction.class);
	private AuthzData authz = null;
	private Credentials cred = null;
	private LoginContext context = null;
	private String iceServerSSOUrl = "unreachable";
	private String logoutURL = "unreachable";
	private String saas = "unreachable";
	private String protectedURL = "unreachable";
	private boolean doLogout = true;
	private boolean isManager = false;
	private int tenances = -1;
	private long startTimestamp = 0;
	private boolean multiIWA = false;
	private String realm = "@ACME.COM";
	
	public IwaSSOAuthzTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		for(ParamType p : params){
			if(p.getName().equals("iceServerSSOUrl")){
				iceServerSSOUrl = p.getValue();
			}
			else if(p.getName().equals("logoutURL")){
				logoutURL = p.getValue();
			}
			else if(p.getName().equals("SaaS")){
				saas = p.getValue();
			}
			else if(p.getName().equals("tenances")){
				tenances = Integer.parseInt(p.getValue());
			}
			else if(p.getName().equals("multiIWA")){
				multiIWA = Boolean.parseBoolean(p.getValue());
			}
		}
	}


	@Override
	public boolean execute(boolean negative) {
		boolean result = false;
		try{			
			result = getRequest(iceServerSSOUrl, "Consumer WebApp", cred) &&
				   	getRequest(protectedURL, isManager ? "Protected" : "403 Forbidden") &&
			   		(!doLogout || getRequest(logoutURL, "You have successfully logged out"));
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		finally{
			if(!result || !doLogout){
				clearCookies();
			}
			if(System.currentTimeMillis() - startTimestamp > 8 * 3600 * 1000){
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
    	if(Integer.parseInt(authz.getUserName().replace("user", "")) % 2 == 1){
    		isManager = true;
    	}
    	if(logoutURL.equals("unreachable"))
    		doLogout = false;
    	this.protectedURL = saas + "protected.jsp";
    	this.authz = authz;
		int userIndex = Integer.parseInt(authz.getUserName().replace("user", ""));    	
		if(tenances > 0){
			int tenanceIndex = userIndex % tenances + 1;
			String tenanceName = "domain" + tenanceIndex;
			iceServerSSOUrl = iceServerSSOUrl.replace("splat", tenanceName);
			iceServerSSOUrl = iceServerSSOUrl.replace("8443", Integer.toString(8443 + (userIndex % tenances)));
			logoutURL = logoutURL.replace("splat", tenanceName);
			logoutURL = logoutURL.replace("8443", Integer.toString(8443 + (userIndex % tenances)));
			port = Integer.toString(Integer.parseInt(port) + (userIndex % tenances));
		}
    	this.setAutoPostResponse(true);

		if(multiIWA && userIndex % 2 == 1){
			realm = "@ECA.COM";
			iceServerSSOUrl = iceServerSSOUrl.replace("ACME-AD", "ECA-AD");
			iceServerSSOUrl = iceServerSSOUrl.replace("consumer-acme-auth", "consumer-eca-authz");
			logoutURL = logoutURL.replace("ACME-AD", "ECA-AD");
			logoutURL = logoutURL.replace("consumer-acme-auth", "consumer-eca-authz");
		}
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
