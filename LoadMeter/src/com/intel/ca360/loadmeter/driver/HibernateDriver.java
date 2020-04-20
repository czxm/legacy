package com.intel.ca360.loadmeter.driver;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.connection.C3P0ConnectionProvider;
import org.hibernate.connection.ConnectionProvider;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.config.TransactionType;
import com.intel.ca360.loadmeter.GenericDriver;
import com.intel.ca360.loadmeter.Transaction;

public class HibernateDriver extends GenericDriver {
	private Properties properties = new Properties();
    private C3P0ConnectionProvider connectionPool = null;
    
	@Override
	protected void prepare(List<ParamType> params){		
		for(ParamType param : params){
			properties.put(param.getName(), param.getValue());
		}
    	start();
	}
	
	@Override
	protected void shutDown(){
		stop();
	}
	
	public List<Transaction> createTransactions(List<TransactionType> transactions) {
		ArrayList<Transaction> trans = new ArrayList<Transaction>();
		for(TransactionType tt : transactions){
			try{
				Class<?> cls = Class.forName(GenericDriver.class.getPackage().getName() + ".transaction." + tt.getName() + "Transaction");
				Constructor con = cls.getConstructor(List.class, ConnectionProvider.class, Properties.class);
				Transaction tran = (Transaction)con.newInstance(tt.getParam(), connectionPool, properties);
				trans.add(tran);
			}
			catch(Throwable t){
				t.printStackTrace();
			}
		}
		return trans;
	}
    
    public void start() {
    	if(connectionPool != null)
    		return;
    	
    	connectionPool = new C3P0ConnectionProvider();
		connectionPool.configure(properties);
    }
    
    public void stop() {
    	if(connectionPool == null)
    		return;
    	
    	try {
    			shutdown();
    	} catch(Exception e) {}
    	
    	connectionPool.close();
    	connectionPool = null;
    	
    }
	
	protected void shutdown() throws Exception {
		Connection connection = connectionPool.getConnection();

		try {
			shutdownDatabase(connection);
		} catch(SQLException e) {
		} finally {
			connection.close();
		}
	}
	
	protected void shutdownDatabase(Connection connection) throws SQLException {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			stmt.execute("SHUTDOWN");
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}
}
