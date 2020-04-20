package com.intel.ca360.loadmeter.transaction;

import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;
import com.intel.ca360.loadmeter.Transaction;

public class LdapAuthnTransaction implements Transaction {
	private static final String CONTEXT_FACTORY 		= "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String SIMPLE_AUTHENTICATION 	= "simple";
    private static final String CONNECTION_POOL 		= "com.sun.jndi.ldap.connect.pool";
	private static final String TIMEOUT 				= "com.sun.jndi.ldap.connect.timeout";
	private static final String	DEFAULT_TIMEOUT 		= "5000"; // 5 seconds
    //private boolean useSSL = false;
    private String server = null;
    private String port = null;
    private String admin = null;
    private String passwd = null;
    private String baseDN = null;
    private String searchAttr = null;
    private String searchScope = null;
    private int searchScopeFlag = -1;
    private InitialLdapContext ctx = null;
    private AuthzData authz = null;
    
	public LdapAuthnTransaction(List<ParamType> params){
		for(ParamType param : params){
			if(param.getName().equals("server")){
				server = param.getValue();				
			}
			else if(param.getName().equals("port")){
				port = param.getValue();
			}
			else if(param.getName().equals("admin")){
				admin = param.getValue();
			}
			else if(param.getName().equals("passwd")){
				passwd = param.getValue();
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
		searchScopeFlag = getSearchScope(searchScope);
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
	
	private InitialLdapContext createLdapContext(String username, String password) throws NamingException {
    	String ldapURL = "ldap://" + server + ":" + port;
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL, ldapURL);
		env.put(Context.SECURITY_AUTHENTICATION, SIMPLE_AUTHENTICATION);
		env.put(Context.SECURITY_PRINCIPAL, username);
		env.put(Context.SECURITY_CREDENTIALS, password);
		env.put(CONNECTION_POOL, "true");
		env.put(TIMEOUT, DEFAULT_TIMEOUT);
		return new InitialLdapContext(env, null);
    }
	
	private String getDistinguishedName(String name) throws Exception {
    	String dn = null;
    	NamingEnumeration<SearchResult> result = null;
		result = queryEntryByFilter(searchAttr + "=" + name);
		// extract the distinguishedName from result
    	while (result != null && result.hasMoreElements()) {
    		Object obj = result.nextElement();
			if (obj instanceof SearchResult) {
				SearchResult si = (SearchResult) obj;
				dn = si.getNameInNamespace();
				if (dn != null) {
					break;
				}
			}
    	}
    	return dn;
    }
	
	private NamingEnumeration<SearchResult> 
		queryEntryByFilter(String searchAttrFilter) throws Exception {
		InitialLdapContext ctx = null;
		NamingEnumeration<SearchResult> result = null;
		ctx = createLdapContext(admin, passwd);
    	SearchControls constraints = new SearchControls();
	    constraints.setSearchScope(searchScopeFlag);
	    result = ctx.search(baseDN, 
	    			searchAttrFilter, 
	    			constraints);
	    ctx.close();
		return result;
    }
	
	private boolean authenticate(String username, String password) throws Exception{
		boolean result = true;
		
		String dnName = getDistinguishedName(username);
		if (dnName != null) {
			InitialLdapContext ctx = createLdapContext(dnName, password);
			ctx.close();
		} else {
			result = false;
		}
		return result;
	}
	
	private boolean authenticate2(String username, String password) throws Exception{
		boolean ret = true;
    	String dn = null;
    	//*
    	SearchControls constraints = new SearchControls();
	    constraints.setSearchScope(searchScopeFlag);
	    NamingEnumeration<SearchResult> result = ctx.search(baseDN, 
	    			searchAttr + "=" + username, 
	    			constraints);
	    //ctx.close();
		// extract the distinguishedName from result
    	while (result != null && result.hasMoreElements()) {
    		Object obj = result.nextElement();
			if (obj instanceof SearchResult) {
				SearchResult si = (SearchResult) obj;
				dn = si.getNameInNamespace();
				if (dn != null) {
					String p = new String((byte[])si.getAttributes().get("userpassword").get());
					if(LdapHelper.verifySHA(p, password))
						return true;
					else
						return false;
					//break;
				}
			}
    	}
    	
    	//dn = "uid=" + username + ", ou=People, dc=acme,dc=com";
		if (dn != null) {
			InitialLdapContext ctx1 = createLdapContext(dn, password);
			ctx1.close();
		} else {
			ret = false;
		}
		return ret;
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
			return authenticate2(authz.getUserName(), authz.getPassword());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void setup(AuthzData authz) {
		this.authz = authz;
		try {
			ctx = createLdapContext(admin, passwd);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		try {
			ctx.close();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean startup() {
		return true;
	}
}
