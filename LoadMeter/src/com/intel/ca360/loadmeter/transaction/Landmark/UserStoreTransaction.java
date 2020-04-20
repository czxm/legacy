package com.intel.ca360.loadmeter.transaction.Landmark;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.hibernate.connection.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.AuthzData;
import com.intel.ca360.loadmeter.transaction.HibernateTransaction;

public class UserStoreTransaction extends HibernateTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(UserStoreTransaction.class);
	
	private HashSet<Integer> added = new HashSet<Integer>();
	private String table = "TestUserStore";
	private String idp = "idpsf";
	private int maxAdd = 10;
	private int maxDel = 10;
	private int maxUpdate = 10;
	private int totalUsers = 10;
	private int keepUsers = 10;
	private Random rand = new Random();
	
	public UserStoreTransaction(List<ParamType> params,
			ConnectionProvider provider, Properties props) {
		super(params, provider, props);
		for(ParamType p : params){
			if(p.getName().equals("totalUsers")){
				totalUsers = Integer.parseInt(p.getValue());
			}
			else if(p.getName().equals("keepUsers")){
				keepUsers = Integer.parseInt(p.getValue());
			}
			else if(p.getName().equals("maxAdd")){
				maxAdd = Integer.parseInt(p.getValue());
			}
			else if(p.getName().equals("maxDel")){
				maxDel = Integer.parseInt(p.getValue());
			}	
			else if(p.getName().equals("maxUpdate")){
				maxUpdate = Integer.parseInt(p.getValue());
			}
			else if(p.getName().equals("table")){
				table = p.getValue();
			}
			else if(p.getName().equals("idp")){
				idp = p.getValue();
			}				
		}
	}

	@Override
	public boolean execute(boolean negative) {
		int toAdd = (int)(maxAdd * rand.nextFloat());
		int toDel = (int)(maxDel * rand.nextFloat());
		int toUpdate = (int)(maxUpdate * rand.nextFloat());
		for(int i = 0; i < toAdd; i++){
			for(int c = keepUsers + 1; c <= totalUsers; c++){
				if(!added.contains(c)){
					added.add(c);
					createUser("user" + c, negative);
				}
			}
		}
		
		if(toDel >= added.size()){
			toDel = added.size();
		}
		if(toDel > 0){
			int[] dels = new int[toDel];
			Iterator<Integer> it = added.iterator();
			while(toDel > 0 && it.hasNext()){
				int c = it.next();
				deleteUser("user" + c);
				toDel--;
				dels[toDel] = c;
			}
			for(int c : dels){
				added.remove(c);
			}
		}
		
		if(toUpdate >= totalUsers){
			toUpdate = totalUsers;
		}
		if(toUpdate > 0){
			while(toUpdate > 0){
				int n = (int)(totalUsers * rand.nextFloat());
				if(existUser("user" + n)){
					activateUser("user" + n, rand.nextBoolean());
					toUpdate--;
				}
			}
		}
		return true;
	}
	
	private void createUser(final String user, final boolean isManager) {	
		try {
			this.doInTransaction(new TransactionalCallback(){

				@Override
				public Object execute(Connection connection) throws Exception {
					StringBuilder sql = new StringBuilder();
					sql.append("insert into ");
					appendSQLTable(sql, table);
					sql.append(" (USER_ID, IDP_ID, FIRST_NAME, LAST_NAME, USER_NAME, EMAIL, TITLE, IS_ACTIVE, CREATED_TIME, UPDATED_TIME) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");		
					PreparedStatement ps = connection.prepareStatement(sql.toString());
					ps.setString(1, user);
					ps.setString(2, idp);
					ps.setString(3, user);
					ps.setString(4, "ACME");
					ps.setString(5, user);
					ps.setString(6, user + "@acme.com");
					ps.setString(7, isManager ? "Manager" : "Employee");
					ps.setString(8, "true");
					Timestamp t = new Timestamp(System.currentTimeMillis());
					ps.setTimestamp(9, t);
					ps.setTimestamp(10, t);
					ps.executeUpdate();
					ps.close();
					return null;
				}
			});
		}
		catch (Exception e) {
			LOG.error(e.getMessage());
		}			
	}
	
	private void createUserPreRelease(final String user, final boolean isManager) {	
		try {
			this.doInTransaction(new TransactionalCallback(){

				@Override
				public Object execute(Connection connection) throws Exception {
					StringBuilder sql = new StringBuilder();
					sql.append("insert into ");
					appendSQLTable(sql, table);
					sql.append(" (USER_ID, FIRST_NAME, LAST_NAME, USER_NAME, EMAIL, TITLE, IS_ACTIVE, CREATED_TIME, UPDATED_TIME) values(?, ?, ?, ?, ?, ?, ?, ?, ?)");		
					PreparedStatement ps = connection.prepareStatement(sql.toString());
					ps.setString(1, user);
					ps.setString(2, user);
					ps.setString(3, "ACME");
					ps.setString(4, user);
					ps.setString(5, user + "@acme.com");
					ps.setString(6, isManager ? "Manager" : "Employee");
					ps.setString(7, "true");
					Timestamp t = new Timestamp(System.currentTimeMillis());
					ps.setTimestamp(8, t);
					ps.setTimestamp(9, t);
					ps.executeUpdate();
					ps.close();
					return null;
				}
			});
		}
		catch (Exception e) {
			LOG.error(e.getMessage());
		}			
	}
	
	private void activateUser(final String user, final boolean active) {
		try {
			this.doInTransaction(new TransactionalCallback(){

				@Override
				public Object execute(Connection connection) throws Exception {
					StringBuilder sql = new StringBuilder();
					sql.append("update ");
					appendSQLTable(sql, table);
					sql.append(" set IS_ACTIVE='" + active + "' where USER_ID='" + user + "'");		
					Statement ps = connection.createStatement();
					ps.executeUpdate(sql.toString());
					ps.close();
					return null;
				}
			});
		}
		catch (Exception e) {
			LOG.error(e.getMessage());
		}		
	}
	
	private void deleteUser(final String user) {
		try {
			this.doInTransaction(new TransactionalCallback(){

				@Override
				public Object execute(Connection connection) throws Exception {
					StringBuilder sql = new StringBuilder();
					sql.append("delete from ");
					appendSQLTable(sql, table);
					sql.append(" where USER_ID='" + user + "'");		
					Statement ps = connection.createStatement();
					ps.executeUpdate(sql.toString());
					ps.close();
					return null;
				}
			});
		}
		catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}
	
	private boolean existUser(final String user) {
		try {
			return (Boolean)this.doInTransaction(new TransactionalCallback(){

				@Override
				public Object execute(Connection connection) throws Exception {
					StringBuilder sql = new StringBuilder();
					sql.append("select * from ");
					appendSQLTable(sql, table);
					sql.append(" where USER_ID='" + user + "'");		
					Statement ps = connection.createStatement();
					ps.execute(sql.toString());
					boolean result = ps.getResultSet().first();
					ps.close();
					return result;
				}
			});
		}
		catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return false;
	}

	@Override
	public void setup(AuthzData authz) {
		for(int i = 1; i <= totalUsers; i++){
			String user = "user" + i;
			deleteUser(user);
		}
		for(int i = 1; i <= keepUsers; i++){
			String user = "user" + i;
			if(!existUser(user)){
				createUser(user, rand.nextBoolean());
			}
		}
		if(!existUser("samlUser"))
			createUser("samlUser", rand.nextBoolean());
	}
	
}
