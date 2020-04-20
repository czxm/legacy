package com.intel.ca360.loadmeter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.intel.ca360.config.LoadConfig;
import com.intel.ca360.config.ParamType;
import com.intel.ca360.config.TransactionType;

public abstract class GenericDriver {
	protected LoadConfig config;
	
	protected abstract void prepare(List<ParamType> params);
	
	protected void shutDown(){
	}

	public List<Transaction> createTransactions(List<TransactionType> transactions) {
		ArrayList<Transaction> trans = new ArrayList<Transaction>();
		for(TransactionType tt : transactions){
			try{
				Class<?> cls = Class.forName(GenericDriver.class.getPackage().getName() + ".transaction." + tt.getName() + "Transaction");
				Constructor con = cls.getConstructor(List.class);
				Transaction tran = (Transaction)con.newInstance(tt.getParam());
				trans.add(tran);
			}
			catch(Throwable t){
				t.printStackTrace();
			}
		}
		return trans;
	}
	
	protected void setup(LoadConfig config){
		this.config = config;
		prepare(config.getTaskConfig().getTaskDriver().getParam());
	}		
}
