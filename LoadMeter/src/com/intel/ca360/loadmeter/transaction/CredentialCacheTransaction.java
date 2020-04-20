package com.intel.ca360.loadmeter.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.hibernate.connection.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;

public class CredentialCacheTransaction extends HibernateTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(CredentialCacheTransaction.class);
    private static final String CREDENTIAL_CACHE_TABLE_PREFIX = "CC_";
    private static final String REGEX_TABLE_NAME = "[^a-zA-Z0-9.-_ ]";
    private Pattern tableNamePattern;
    private String domain = "ab - c 1 & + b";
	private AuthzData authz = null;
    
    public CredentialCacheTransaction(List<ParamType> params, ConnectionProvider provider, Properties props) {
    	super(params, provider, props);
		for(ParamType param : params){
			if(param.getName().equals("domain")){
				domain = param.getValue();
			}
		}
		tableNamePattern = Pattern.compile(REGEX_TABLE_NAME);
    }

	public synchronized void createCache(final String domainId) throws Exception {
    	doInTransaction(new TransactionalCallback() {
    		@Override
    		public Object execute(Connection connection) throws Exception {
    			final String table = getTableOfDomain(connection, domainId);
    	    	
    			if(isTableExists(connection, table))
    				throw new Exception("Credential store with domain name '" + table  + "' already exists.");

    			createCacheTable(connection, table);
    			return null;
    		}
    	});
	}

	public synchronized void deleteCache(final String domainId) throws Exception {
    	doInTransaction(new TransactionalCallback() {
    		@Override
    		public Object execute(Connection connection) throws Exception {
    			final String table = getTableOfDomain(connection, domainId);
    			if(!isTableExists(connection, table))
    				throw new Exception("Credential store with domain name '" + table  + "' doesn't exist.");

    			deleteCacheTable(connection, table);
    			return null;
    		}
    	});
	}
	
	public synchronized void renameCache(final String domainId, final String newDomainId) throws Exception {
    	doInTransaction(new TransactionalCallback() {
    		@Override
    		public Object execute(Connection connection) throws Exception {
    			final String table = getTableOfDomain(connection, domainId);
    			final String newTable = getTableOfDomain(connection, newDomainId);
    	    	
    			//same table name
    			if(table.equalsIgnoreCase(newTable))
    				return null;
    			
    			if(!isTableExists(connection, table))
    				throw new Exception("Credential store with domain name '" + table  + "' doesn't exist.");
    			renameCacheTable(connection, table, newTable);
    			updateDomainOfCredentials(connection, newTable, newDomainId);
    			return null;
    		}
    	});
	}
	
	public synchronized boolean isCacheExists(final String domainId) throws Exception {
    	Boolean result = (Boolean) doInTransaction(new TransactionalCallback() {
    		@Override
    		public Object execute(Connection connection) throws Exception {
    			final String table = getTableOfDomain(connection, domainId);
    			return new Boolean(isTableExists(connection, table));
    		}
		});
    	
    	return result.booleanValue();
	}

	public void clearCache(final String domainId) throws Exception {
		doInTransaction(new TransactionalCallback() {
    		@Override
    		public Object execute(Connection connection) throws Exception {
    			final String table = getTableOfDomain(connection, domainId);
    			deleteFromCacheTable(connection, table);
    			return null;
    		}
		});
	}

	public CachedCredential getCredential(final String domainId, final String subjectId)
			throws Exception {
		return (CachedCredential) doInTransaction(new TransactionalCallback() {
    		@Override
    		public Object execute(Connection connection) throws Exception {
    			final String table = getTableOfDomain(connection, domainId);
    			return queryCredentialBySubjectId(connection, table, subjectId);
    		}
		});
	}

	public void setCredential(final CachedCredential credential) throws Exception {
		if(credential == null)
			throw new IllegalArgumentException("Invalid credential.");
		
		doInTransaction(new TransactionalCallback() {
    		@Override
    		public Object execute(Connection connection) throws Exception {
    			final String table = getTableOfDomain(connection, credential.getDomainId());
    			saveCredential(connection, table, credential);
    			return null;
    		}
		});
		
	}

	protected String getTableOfDomain(Connection connection, String domainId) {
		if(domainId == null || 
    			domainId.isEmpty())
    		throw new IllegalArgumentException("Invalid domain name for credential store.");
		
		String tableString = tableNamePattern.matcher(domainId).replaceAll("_");
		return CREDENTIAL_CACHE_TABLE_PREFIX + tableString;
	}
	
	protected void createCacheTable(Connection connection, String table) throws SQLException {
		Statement stmt = null;
	
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("create table ");
			appendSQLTable(sql, table);
			sql.append(" (ID varchar(64) PRIMARY KEY, DOMAIN_ID varchar(255), SUBJECT_ID varchar(255) NOT NULL");
			sql.append(", USER_NAME varchar(255) NOT NULL, PASSWORD varchar(255) NOT NULL, PROPERTIES varchar(2048), UPDATED timestamp(0) NOT NULL");
			sql.append(")");
			
			stmt = connection.createStatement();
			stmt.executeUpdate(sql.toString());
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}
	
	protected void deleteCacheTable(Connection connection, String table) throws SQLException {
		Statement stmt = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("drop table ");
			appendSQLTable(sql, table);
			
			stmt = connection.createStatement();
			stmt.executeUpdate(sql.toString());
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}
	
	protected void renameCacheTable(Connection connection, String table, String newTable) throws SQLException {
		Statement stmt = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("alter table ");
			appendSQLTable(sql, table);
			sql.append(" rename to ");
			appendSQLTable(sql, newTable);
			
			stmt = connection.createStatement();
			stmt.executeUpdate(sql.toString());
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}
	
	protected void deleteFromCacheTable(Connection connection, String table) throws SQLException {
		Statement stmt = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("delete from ");
			appendSQLTable(sql, table);
			
			stmt = connection.createStatement();
			stmt.executeUpdate(sql.toString());
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}
	
	protected CachedCredential queryCredentialBySubjectId(Connection connection, String table, String subjectId) 
			throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from ");
		appendSQLTable(sql, table);
		sql.append(" where SUBJECT_ID = ?");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = connection.prepareStatement(sql.toString());
			pstmt.setString(1, subjectId);
			
			rs = pstmt.executeQuery();
			if(!rs.next())
				return null; //no credential with the subject id found
			
			return createCredential(rs);
		} finally {
			if(rs != null)
				rs.close();
			
			if(pstmt != null)
				pstmt.close();
		}
	}
	
	protected CachedCredential queryCredentialById(Connection connection, String table, String id) 
			throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from ");
		appendSQLTable(sql, table);
		sql.append(" where ID = ?");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = connection.prepareStatement(sql.toString());
			pstmt.setString(1, id);
			
			rs = pstmt.executeQuery();
			if(!rs.next())
				return null; //no credential with the subject id found
			
			return createCredential(rs);
		} finally {
			if(rs != null)
				rs.close();
			
			if(pstmt != null)
				pstmt.close();
		}
	}
	
	protected CachedCredential createCredential(ResultSet rs) throws SQLException {
		CachedCredential credential = new CachedCredential();
		credential.setId(rs.getString(1));
		credential.setDomainId(rs.getString(2));
		credential.setSubjectId(rs.getString(3));
		credential.setUserName(rs.getString(4));
		credential.setPasswordEncrypted(rs.getString(5));
		credential.setPropertiesAsString(rs.getString(6));
		credential.setUpdated(rs.getTimestamp(7));
		
		return credential;
	}
	
	protected void saveCredential(Connection connection, String table, CachedCredential credential) 
			throws SQLException {
		
		String id = credential.getId();
		if(id == null || 
				id.isEmpty()) {
			//this is an insert or update
			CachedCredential cachedCredential = queryCredentialBySubjectId(connection, table, credential.getSubjectId());
			if(cachedCredential == null) {
				//insert
				insertCredential(connection, table, credential);
			} else {	
				credential.setId(cachedCredential.getId());
				updateCredential(connection, table, credential);
			}
		} else {
			//this is an update on a given ID
			updateCredential(connection, table, credential);
		}
	}
	
	protected void insertCredential(Connection connection, String table, CachedCredential credential) 
			throws SQLException {
		
		String id = CachedCredential.generateId();
		Timestamp updated = new Timestamp(Calendar.getInstance().getTime().getTime());
		
		StringBuilder sql = new StringBuilder();
		sql.append("insert into ");
		appendSQLTable(sql, table);
		sql.append(" values(?, ?, ?, ?, ?, ?, ?)");
		
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(sql.toString());
			pstmt.setString(1, id);
			pstmt.setString(2, credential.getDomainId());
			pstmt.setString(3, credential.getSubjectId());
			pstmt.setString(4, credential.getUserName());
			pstmt.setString(5, credential.getPasswordEncrypted());
			pstmt.setString(6, credential.getPropertiesAsString());
			pstmt.setTimestamp(7, updated);
			
			pstmt.executeUpdate();
			
			//set back data
			credential.setId(id);
			credential.setUpdated(updated);
		
		} finally {
			if(pstmt != null)
				pstmt.close();
		}
	}
	
	protected void updateCredential(Connection connection, String table, CachedCredential credential) 
			throws SQLException {
		
		Timestamp updated = new Timestamp(Calendar.getInstance().getTime().getTime());
		
		StringBuilder sql = new StringBuilder();
		sql.append("update ");
		appendSQLTable(sql, table);
		sql.append(" set DOMAIN_ID = ?, SUBJECT_ID = ?, USER_NAME = ?, PASSWORD = ?, PROPERTIES = ?, UPDATED = ? where ID = ?");
		
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(sql.toString());
			
			pstmt.setString(1, credential.getDomainId());
			pstmt.setString(2, credential.getSubjectId());
			pstmt.setString(3, credential.getUserName());
			pstmt.setString(4, credential.getPasswordEncrypted());
			pstmt.setString(5, credential.getPropertiesAsString());
			pstmt.setTimestamp(6, updated);
			
			pstmt.setString(7, credential.getId());
			
			pstmt.executeUpdate();
		
			//set back data
			credential.setUpdated(updated);
		} finally {
			if(pstmt != null)
				pstmt.close();
		}
	}
	
	protected void updateDomainOfCredentials(Connection connection, String table, String domainId) 
			throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("update ");
		appendSQLTable(sql, table);
		sql.append(" set DOMAIN_ID = ?");
		
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(sql.toString());
			pstmt.setString(1, domainId);
			pstmt.executeUpdate();
			
		} finally {
			if(pstmt != null)
				pstmt.close();
		}
	}

	protected List<CachedCredential> getAllCredentials(Connection connection) throws SQLException {
		List<String> tables = getTables(connection);
		List<CachedCredential> credentials = new ArrayList<CachedCredential>();
		for(String table : tables) {
			if(!table.startsWith(CREDENTIAL_CACHE_TABLE_PREFIX))
				continue;
			
			credentials.addAll(getCredentials(connection, table));
		}
		
		return credentials;
	}
	
	protected List<CachedCredential> getCredentials(Connection connection, String table) 
			throws SQLException {
		List<CachedCredential> credentials = new ArrayList<CachedCredential>();
		
		StringBuilder sql = new StringBuilder();
		sql.append("select * from ");
		appendSQLTable(sql, table);
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = connection.prepareStatement(sql.toString());
			
			rs = pstmt.executeQuery();
			while(rs.next()) {
				CachedCredential credential = createCredential(rs);
				credentials.add(credential);
			}
		} finally {
			if(rs != null)
				rs.close();
			
			if(pstmt != null)
				pstmt.close();
		}
		
		return credentials;
	}
	
	protected void importCredentials(Connection connection, List<CachedCredential> credentials) 
			throws SQLException {
		Set<String> domains = getDomains(credentials);
		Map<String, String> domainToTable = new HashMap<String, String>();
		for(String domain : domains) {
			String table = getTableOfDomain(connection, domain);
			if(isTableExists(connection, table))
				deleteFromCacheTable(connection, table);
			else
				createCacheTable(connection, table);
			
			domainToTable.put(domain, table);
		}
		
		for(CachedCredential credential : credentials) {
			String table = domainToTable.get(credential.getDomainId());
			if(table == null || 
					table.isEmpty())
				continue;
			
			insertCredential(connection, table, credential);
		}
	}
	
	protected Set<String> getDomains(List<CachedCredential> credentials) {
		Set<String> domains = new HashSet<String>();
		for(CachedCredential credential : credentials) {
			String domainId = credential.getDomainId();
			if(domainId == null || 
					domainId.isEmpty())
				continue;
			
			String formalized = domainId.toUpperCase();
			if(domains.contains(formalized))
				continue;
			
			domains.add(formalized);
		}
		
		return domains;
	}

	@Override
	public boolean execute(boolean negative) {
    	try {
    		CachedCredential credential = this.getCredential(domain, authz.getUserName());
    		return (credential != null && 
    				authz.getUserName().equals(credential.getUserName()) && 
    				authz.getPassword().equals(credential.getPassword())
    				);
    	} catch(Exception e) {
    		LOG.info(e.getMessage(), e);
    	}
		return false;
	}

	@Override
	public void setup(AuthzData authz) {
		this.authz = authz;
		try{
			synchronized(CredentialCacheTransaction.class){
				if(!this.isCacheExists(domain))
					this.createCache(domain);
			}
    		CachedCredential credential = this.getCredential(domain, authz.getUserName());
    		if(credential == null) {
    			credential = new CachedCredential();
    			credential.setDomainId(domain);
    			credential.setSubjectId(authz.getUserName());
    			credential.setUserName(authz.getUserName());
    			credential.setPassword(authz.getPassword());
    			credential.clearProperties();
    			credential.addProperty("abc", "xyz");
    			this.setCredential(credential);
    		}
    		else {
    			credential.setUserName(authz.getUserName());
    			credential.setPassword(authz.getPassword());
    			this.setCredential(credential);
    		}
		}
		catch(Exception e){
			LOG.info(e.getMessage(), e);
		}
	}
}
