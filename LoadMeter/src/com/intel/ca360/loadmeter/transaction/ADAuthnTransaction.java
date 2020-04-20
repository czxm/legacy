package com.intel.ca360.loadmeter.transaction;

import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;
import com.intel.ca360.loadmeter.Transaction;

public class ADAuthnTransaction implements Transaction {
	protected static final String CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    protected static final String GSS_AUTHENTICATION = "GSSAPI";
    private static final String CONNECTION_POOL 		= "com.sun.jndi.ldap.connect.pool";
	private static final String TIMEOUT 				= "com.sun.jndi.ldap.connect.timeout";
	private static final String	DEFAULT_TIMEOUT 		= "5000"; // 5 seconds
	private static final String CONNECTION_POOL_TIMEOUT = "com.sun.jndi.ldap.connect.pool.timeout";
	private static final String	DEFAULT_POOL_TIMEOUT 	= "300000"; // 300 seconds

    private LdapContext ldapContext = null;
	private LoginContext context = null;
	private String serviceAccount;
	private String password;
	private String realm;
	private String server;
	private String port;
	private String baseDN;
	private String searchAttr;
	private String searchScope;
	private AuthzData authz = null;
	
	public ADAuthnTransaction(List<ParamType> params){
		for(ParamType param : params){
			if(param.getName().equals("serviceAccount")){
				serviceAccount = param.getValue();
			}
			else if(param.getName().equals("passwd")){
				password = param.getValue();
			}
			else if(param.getName().equals("realm")){
				realm = param.getValue();
			}
			else if(param.getName().equals("server")){
				server = param.getValue();				
			}
			else if(param.getName().equals("port")){
				port = param.getValue();
			}
			else if(param.getName().equals("baseDN")){
				baseDN = param.getValue();
			}
			else if(param.getName().equals("searchAttr")){
				searchAttr = param.getValue();
			}
			else if(param.getName().equals("searchScope")){
				searchScope = param.getValue();
			}						
		}
	}
	
	public LdapContext createLdapContext(Subject subject, String realm, String port, String kdc) 
	throws NamingException {
		LDAPBindingWithGSSAPI ldapBinding = new LDAPBindingWithGSSAPI(realm, port, kdc);
		LdapContext ldapCtx = Subject.doAs(subject, ldapBinding);
		NamingException ne = ldapBinding.getNamingException();
		if (ldapCtx == null && ne != null) {
			throw ne;
		}
		return ldapCtx;
	}
	
	public class LDAPBindingWithGSSAPI implements java.security.PrivilegedAction<LdapContext> {
		String _server;
		String _port;
		String _kdcIp;
		NamingException _namingException;
		
		public NamingException getNamingException() {
			return _namingException;
		}
		
		public LDAPBindingWithGSSAPI(String server, String port, String kdcIp) {
			_server = server;
			_port = port;
			_kdcIp = kdcIp;
		}
		
		@Override
		public LdapContext run() {
			StringBuffer ldapURL = new StringBuffer();
			ldapURL.append("ldap://");
			if (_kdcIp != null) {
				ldapURL.append(_kdcIp);
			} else {
				ldapURL.append(_server);
			}
			ldapURL.append(":").append(_port);
			Hashtable<String, Object> env = new Hashtable<String, Object>();
	        env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
			env.put(CONNECTION_POOL, "true");
			env.put(TIMEOUT, DEFAULT_TIMEOUT);
			env.put(CONNECTION_POOL_TIMEOUT, DEFAULT_POOL_TIMEOUT);
			env.put(Context.PROVIDER_URL, ldapURL.toString());
			env.put(Context.SECURITY_AUTHENTICATION, GSS_AUTHENTICATION);
			
			env.put("java.naming.ldap.attributes.binary","objectGUID");
			LdapContext ctx = null;
			try {
				ctx = new InitialLdapContext(env, null);
			} catch (NamingException e) {
				_namingException = e;
				e.printStackTrace();
			}
			return ctx;
		}
	}
	
	class Action2QueryADEntriesByFilter implements java.security.PrivilegedAction<NamingEnumeration<SearchResult>> {
		private String _baseDN;
		private String _filter;
		private int _searchScope;
		Action2QueryADEntriesByFilter(
				String baseDN,
				String filter,
				int searchScope) {
			_baseDN = baseDN;
			_filter = filter;
			_searchScope = searchScope;
		}
		
		protected NamingEnumeration<SearchResult> searchEntryByFilter(String filter) 			
			throws NamingException {
			SearchControls constraints = new SearchControls();
		    constraints.setSearchScope(_searchScope);
			return ldapContext.search(_baseDN, filter, constraints);
		}
		
		public NamingEnumeration<SearchResult> run() {
			NamingEnumeration<SearchResult> ne = null;
			
			try {
				ne = searchEntryByFilter(_filter);
			} catch (Exception e) {
				ldapContext = loginAD();
			}
			
			if (ldapContext == null) {
				return null;
			}
			try {
				ne = searchEntryByFilter(_filter);
				return ne;
			} catch (NamingException e) {
				e.printStackTrace();
			}
			return ne;
		}
	}
	
	
	private NamingEnumeration<SearchResult> searchResult() {
		NamingEnumeration<SearchResult> searchResult = null;
		String searchFilter = searchAttr + "=" + authz.getUserName();
		searchResult =
				Subject.doAs(context.getSubject(), 
				new Action2QueryADEntriesByFilter(
					baseDN, searchFilter, getSearchScope(searchScope)));
		return searchResult;
	}
	
	private int getSearchScope(String searchScope) {
		int ss = -1;
		if ("SUBTREE".equals(searchScope)) {
			ss = SearchControls.SUBTREE_SCOPE;
		} else if ("OBJECT".equals(searchScope)) {
			ss = SearchControls.OBJECT_SCOPE;
		} else if ("ONELEVEL".equals(searchScope)) {
			ss = SearchControls.ONELEVEL_SCOPE;
		}
		return ss;
	}

	@Override
	public boolean afterExecute() {
		return true;
	}

	@Override
	public boolean beforeExecute() {
		return true;
	}

	@Override
	public boolean execute(boolean negative) {
		try{
	    	this.ldapContext = createLdapContext(context.getSubject(), realm, port, server);
			NamingEnumeration<SearchResult> result = searchResult();
	    	while (result != null && result.hasMoreElements()) {
	    		Object obj = result.nextElement();
				if (obj instanceof SearchResult) {
					SearchResult si = (SearchResult) obj;
					if(si.getAttributes().get("cn").get().equals(authz.getUserName()))
						return true;
				}
	    	}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void setup(final AuthzData authz) {
		this.authz = authz;
		this.ldapContext = loginAD();
	}
	
	private LdapContext loginAD(){
	    try{
		    context = new LoginContext("com.sun.security.jgss.login", getUsernamePasswordHandler());
	    	context.login();
	    	return createLdapContext(context.getSubject(), realm, port, server);
	    }
	    catch(Exception e){
	    	context = null;
	    	e.printStackTrace();
	    	return null;
	    }
	}

    protected CallbackHandler getUsernamePasswordHandler(){
        return new CallbackHandler() {
            public void handle(Callback[] callback)
            {
                for(int i = 0; i < callback.length; i++){
                    if(callback[i] instanceof NameCallback)
                    {
                        NameCallback nameCallback = (NameCallback)callback[i];
                        nameCallback.setName(serviceAccount);
                    } else
                    if(callback[i] instanceof PasswordCallback)
                    {
                        PasswordCallback passCallback = (PasswordCallback)callback[i];
                        passCallback.setPassword(password.toCharArray());
                    }
                }
            }
        };
    }

	@Override
	public void shutdown() {
		if(ldapContext != null){
			try{
				ldapContext.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		if(context != null){
			try{
				context.logout();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean startup() {
		return true;
	}
}
