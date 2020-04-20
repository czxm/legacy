package com.intel.ca360.loadmeter.transaction;

import java.util.HashMap;

import com.unboundid.ldap.sdk.GSSAPIBindRequest;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.SimpleBindRequest;

public class LdapConnPoolManager {

	private static LdapConnPoolManager singleton;
	static{
		singleton = new LdapConnPoolManager();
	}
	
	public static LdapConnPoolManager getInstance(){
		return singleton;
	}
	
	private HashMap<LdapConnPoolRequest, LDAPConnectionPool> pools;
	
	private LdapConnPoolManager(){
		pools = new HashMap<LdapConnPoolRequest, LDAPConnectionPool>();
	}
	
	protected LDAPConnectionPool createPool(LdapConnPoolRequest request){
		try{
			if(request.getType().equals(LdapAuthnType.GSSAPI)){
				LDAPConnection connection = new LDAPConnection(request.getServer(), request.getPort());
				GSSAPIBindRequest bindRequest = new GSSAPIBindRequest(request.getAuthnId(), null, request.getPasswd().getBytes(), null, null, "resource/unboundid.conf");
				connection.bind(bindRequest);
				return new LDAPConnectionPool(connection, 512);
			}
			else if(request.getType().equals(LdapAuthnType.SIMPLE)){
				LDAPConnection connection = new LDAPConnection(request.getServer(), request.getPort());
				SimpleBindRequest bindRequest = new SimpleBindRequest(request.getAuthnId(), request.getPasswd().getBytes());
				connection.bind(bindRequest);
				return new LDAPConnectionPool(connection, 512);				
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public LDAPConnectionPool getConnectionPool(LdapConnPoolRequest request){
		LDAPConnectionPool pool = pools.get(request);
		if(pool == null){
			synchronized(pools){
				pool = pools.get(request);
				if(pool == null){
					pool = createPool(request);
					if(pool != null){
						pools.put(request, pool);
					}
				}
			}
		}
		return pool;
	}
}
