package com.intel.ca360.loadmeter.transaction;

import java.util.List;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;
import com.intel.ca360.loadmeter.Transaction;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

public class UnboundidLdapAuthnTransaction implements Transaction {
	private String admin;
	private String password;
	private String server;
	private String port;
	private String baseDN;
	private String searchAttr;
	private SearchScope ubdSearchScope;
	private AuthzData authz = null;
	LDAPConnectionPool pool = null;
	
	public UnboundidLdapAuthnTransaction(List<ParamType> params){
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
				password = param.getValue();
			}
			else if(param.getName().equals("baseDN")){
				baseDN = param.getValue();
			}
			else if(param.getName().equals("searchAttr")){
				searchAttr = param.getValue();
			}
			else if(param.getName().equals("searchScope")){
				ubdSearchScope = SearchScope.SUB;
			}			
		}
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
		LDAPConnection connection = null;
		try{
			connection = pool.getConnection();
		    String searchFilter = searchAttr + "=" + authz.getUserName();
		    SearchResult result = connection.search(baseDN, ubdSearchScope, searchFilter, searchAttr);
		    if(result.getEntryCount() == 1){
		    	SearchResultEntry entry = result.getSearchEntries().get(0);
		    	return entry.getAttributeValue(searchAttr).equals(authz.getUserName());
		    }
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(connection != null){
				pool.releaseConnection(connection);
			}
		}
		return false;
	}

	@Override
	public void setup(final AuthzData authz) {
		this.authz = authz;
		LdapConnPoolRequest request = new LdapConnPoolRequest();
		request.setServer(server);
		request.setPasswd(password);
		request.setAuthnId(admin);
		request.setPort(Integer.parseInt(port));
		request.setType(LdapAuthnType.SIMPLE);
		pool = LdapConnPoolManager.getInstance().getConnectionPool(request);
	}

	@Override
	public void shutdown() {
	}

	@Override
	public boolean startup() {
		return true;
	}
}
