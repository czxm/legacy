package com.intel.ca360.loadmeter.transaction;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.connection.C3P0ConnectionProvider;

import com.intel.ca360.config.ParamType;

public class HibernateHelper{	
	private Properties properties = new Properties();
    private C3P0ConnectionProvider connectionPool = null;
    private SimpleAuthenticator authenticator = new SimpleAuthenticator();
    
    private static class SimpleAuthenticator extends Authenticator{
    	private String user = "";
		private String passwd = "";
    	
    	public void setUser(String username){
    		this.user = username;
    	}
    	
    	public void setPassword(String passwd){
    		this.passwd = passwd;
    	}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, passwd.toCharArray());
		}
		
		public boolean isValid(){
			return user.length() > 0 && passwd.length() > 0;
		}
    };
    
    public HibernateHelper(List<ParamType> params){
        properties.put("hibernate.c3p0.max_size","50");    
        properties.put("hibernate.c3p0.min_size","0"); 
        properties.put("hibernate.c3p0.timeout","50");
        properties.put("hibernate.c3p0.idle_test_period","5");   
        properties.put("hibernate.c3p0.max_statements","0");    
        properties.put("hibernate.c3p0.acquire_increment","1");
    	
		for(ParamType param : params){
			if(param.getName().startsWith("hibernate"))
				properties.put(param.getName(), param.getValue());
			if(param.getName().equals("socksProxyHost")){
				String value = param.getValue();
				if(value != null && value.length() > 0){
					properties.put(param.getName(), value);
				}
			}
			if(param.getName().equals("socksProxyPort")){
				String value = param.getValue();
				if(value != null && value.length() > 0){
					properties.put(param.getName(), value);
				}
			}
			if(param.getName().equals("socksProxyUser")){
				String value = param.getValue();
				if(value != null && value.length() > 0){
					authenticator.setUser(value);
				}
			}		
			if(param.getName().equals("socksProxyPassword")){
				String value = param.getValue();
				if(value != null && value.length() > 0){
					authenticator.setPassword(value);
				}
			}			
		}
		String url = properties.getProperty("hibernate.connection.url");
		if(url != null && url.startsWith("jdbc:mysql")){
			if(properties.getProperty("hibernate.connection.driver_class") == null)
				properties.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
	        if(properties.getProperty("hibernate.dialect") == null)
	        	properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect"); 
		}
		
    	connectionPool = new C3P0ConnectionProvider();
		connectionPool.configure(properties);
		
		if(authenticator.isValid()){
			Authenticator.setDefault(authenticator);
		}
	}

	protected Connection getConnection() throws Exception {
		try {
			if(properties.getProperty("socksProxyHost") != null){
				System.setProperty("socksProxyHost", properties.getProperty("socksProxyHost"));
			}
			if(properties.getProperty("socksProxyPort") != null){
				System.setProperty("socksProxyPort", properties.getProperty("socksProxyPort"));
			}
			return connectionPool.getConnection();
		} catch(SQLException e) {
    		throw new Exception("Failed to get connection.", e);
		}
	}

	protected void closeConnection(Connection connection) {
		try {
			connectionPool.closeConnection(connection);
			System.clearProperty("socksProxyHost");
			System.clearProperty("socksProxyPort");
		} catch(SQLException e) {

		}
	}
	
	protected void commitTransaction(Connection connection)  throws SQLException {
		connection.commit();
	}
	
	protected void rollbackTransaction(Connection connection)  throws Exception {
		try {
			connection.rollback();
		} catch(SQLException e) {
			throw new Exception("Failed to rollback transaction.", e);
		}
	}
	
	protected StringBuilder appendSQLTable(StringBuilder sql, String table) {
		sql.append('"');
		sql.append(table);
		sql.append('"');
		return sql;
	}
	
	protected boolean isTableExists(Connection connection, String table) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("select count(*) from ");
			appendSQLTable(sql, table); 
			
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql.toString());
		}
		catch (SQLException e ) {
			return false;
		} finally {
			if(rs != null)
				rs.close();
			
			if(stmt != null)
				stmt.close();
		}
		
		return true;
	}
	
	protected List<String> getTables(Connection connection) throws SQLException{
		List<String> tables=new ArrayList<String>();
		
		DatabaseMetaData metaData = connection.getMetaData();
		if(metaData == null)
			return tables;
		
		ResultSet rs=null;
		
		try {
			rs = metaData.getTables(null, null, null, new String[]{ "TABLE" });
			while (rs.next()) {
				tables.add(rs.getString(3));
			}
		} finally {
			if(rs != null)
				rs.close();
		}
		
		return tables;
	}
	
	public Object doInTransaction(TransactionalCallback callback) throws Exception{
		Connection connection = getConnection();
    	
    	try {
    		Object result = callback.execute(connection);
    		commitTransaction(connection);
    		return result;
    	} catch(Exception e) {
    		rollbackTransaction(connection);
    		throw new Exception("operation failed.", e);
    	} finally {
    		closeConnection(connection);
    	}
	}
	
    public static interface TransactionalCallback {
    	Object execute(Connection connection) throws Exception;
    }
}
