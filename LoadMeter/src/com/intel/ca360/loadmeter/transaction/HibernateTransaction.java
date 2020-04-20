package com.intel.ca360.loadmeter.transaction;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.connection.ConnectionProvider;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.Transaction;

public abstract class HibernateTransaction implements Transaction {    
    private ConnectionProvider connectionPool = null;
    private boolean isMySQL = false;
    
	public HibernateTransaction(List<ParamType> params, ConnectionProvider provider, Properties props){
		this.connectionPool = provider;
        String jdbcDriverClass = props.getProperty("hibernate.connection.driver_class");
        if(jdbcDriverClass.contains("mysql")){
        	isMySQL = true;
        }
	}

	protected Connection getConnection() throws Exception {
		try {
			return connectionPool.getConnection();
		} catch(SQLException e) {
    		throw new Exception("Failed to get connection.", e);
		}
	}

	protected void closeConnection(Connection connection) {
		try {
			connectionPool.closeConnection(connection);
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
		if(isMySQL)
			sql.append("`");
		else
			sql.append('"');
		sql.append(table);
		if(isMySQL)
			sql.append("`");
		else
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
	
	protected Object doInTransaction(TransactionalCallback callback) throws Exception{
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
	
    protected interface TransactionalCallback {
    	Object execute(Connection connection) throws Exception;
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
	public boolean startup() {
		return false;
	}
	
	@Override
	public void shutdown() {
	}
}
