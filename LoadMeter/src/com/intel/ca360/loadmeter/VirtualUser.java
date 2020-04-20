package com.intel.ca360.loadmeter;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.ca360.config.LoadConfig;
import com.intel.ca360.loadmeter.util.Util;

public class VirtualUser implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(LoadScheduler.class);
	
	private VirtualUserManager userManager;
	private LoadConfig config;
	private String userName;
	private int userIndex;
	private List<Transaction> transactions;
	
	public VirtualUser(VirtualUserManager userManager, LoadConfig config, String userName, int userIndex, List<Transaction> transactions){
		this.userManager = userManager;
		this.config = config;
		this.userName = userName;
		this.userIndex = userIndex;
		this.transactions = transactions;
	}
	
	public String getUser(){
		return this.userName;
	}
	
	public int getUserIndex(){
		return this.userIndex;
	}
	
	public List<Transaction> getTransactions(){
		return this.transactions;
	}
	
	@Override
	public void run() {
		userManager.allocVirtualUserData(this);
		String password = "";
		if(config.getVirtualUserConfig().getIndexedCredential() != null){
			password = config.getVirtualUserConfig().getIndexedCredential().getPassword();
		}
		else{
			password = config.getVirtualUserConfig().getFixedCredential().getPassword();
		}
		AuthzData authz = new AuthzData(userName, password);
		if(transactions.size() == 0){
			LOG.info("No transactions defined!");
			return;
		}
		LOG.info("Started Virtual User ({}, {})", new Object[]{this.getUser(), this.getUserIndex()});
		try{			
			for(Transaction t : transactions)
				t.setup(authz);
			
			List<TransactionData> dataList = userManager.getTransactionDataList(this);
			for(TransactionData d : dataList)
				d.reset();
			
			if(config.getTaskConfig().isSyncStartup() != null && config.getTaskConfig().isSyncStartup() && config.getBatchConfig() == null){
				userManager.reach();
				for(int t = 0; t < transactions.size(); t++){
					Transaction tran = transactions.get(t);
					long start = System.currentTimeMillis();
					boolean suc = tran.startup();
					long resp = System.currentTimeMillis() - start;
					TransactionData d = dataList.get(t);
					d.incTotalTransactions();
					if(suc){
						if(resp > 0){
							d.incTotalResponseTime(resp);
							if(d.getMaxResponseTime() < resp)
								d.setMaxResponseTime(resp);
							if(d.getMinResponseTime() > resp)
								d.setMinResponseTime(resp);
						}
					}
					else{
						d.incErrorTransactions();
					}
				}
				userManager.reach();
				userManager.reach();
			}
			else{
				for(Transaction t : transactions){
					t.startup();
				}
			}
			int maxCount = Integer.MAX_VALUE;
			int negCount = 0;
			if(config.getTaskConfig().getNegRate() instanceof Float && ((Float)config.getTaskConfig().getNegRate()) > 0){
				negCount = (int)(1 / config.getTaskConfig().getNegRate());
			}
			if(config.getTaskConfig().getIterations() > 0)
				maxCount = config.getTaskConfig().getIterations();
			Random random = new Random();
			Boolean isOrdered = config.getTaskConfig().getTaskDriver().isOrdered();
			for(int i = 0; i < maxCount; i++){
				int t = i % transactions.size();
				if(isOrdered != null && !isOrdered){
					t = (int)(random.nextFloat() * transactions.size());
					if(t >= transactions.size()){
						t = transactions.size() - 1;
					}
				}
				Transaction tran = transactions.get(t);
				TransactionData d = dataList.get(t);
				boolean negative = (negCount > 0 && i % negCount == 0);
				boolean suc = tran.beforeExecute();
				long start = System.currentTimeMillis();
				suc = tran.execute(negative) && suc;
				long resp = System.currentTimeMillis() - start;
				suc = tran.afterExecute() && suc;
				if(suc){
					if(!negative){
						d.incTotalResponseTime(resp);
						if(d.getMaxResponseTime() < resp)
							d.setMaxResponseTime(resp);
						if(d.getMinResponseTime() > resp && resp > 0)
							d.setMinResponseTime(resp);
					}
				}
				else{
					d.incErrorTransactions();
				}
				d.incTotalTransactions();
				
				if(userManager.isShutdown())
					break;
				if(config.getTaskConfig().getDelay().getFixDelay() != null){
					Util.sleep(config.getTaskConfig().getDelay().getFixDelay().getDelay());
				}
				else if(config.getTaskConfig().getDelay().getVariableDelay() != null){
					int min = config.getTaskConfig().getDelay().getVariableDelay().getMinDelay();
					int max = config.getTaskConfig().getDelay().getVariableDelay().getMaxDelay();
					int r = new Random().nextInt(max - min);
					Util.sleep(r + min);
				}
			}
			for(Transaction t : transactions){
				t.shutdown();
			}
		}
		catch(Throwable t){
			LOG.info(t.getMessage(), t);
		}
		finally{
			userManager.releaseVirtualUserData(this);
			LOG.info("Virtual User ({}, {}) finished", new Object[]{this.getUser(), this.getUserIndex()});
		}
	}

}
