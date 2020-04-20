package com.intel.ca360.loadmeter.transaction;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.GenericDriver;
import com.intel.ca360.loadmeter.Transaction;

public class CompositeHttpTransaction extends CompositeTransaction {
	protected List<ParamType> tranDefines;
	protected List<ParamType> commParams;
	protected HttpClient client;
	
	public CompositeHttpTransaction(HttpClient client, List<ParamType> params) {
		super(params);
		this.client = client;
		this.tranDefines = new ArrayList<ParamType>();
		this.commParams = new ArrayList<ParamType>();
		for(int i = 0; i < params.size(); i++){
			ParamType p = params.get(i);
			if(p.getName().equals("transaction")){
				tranDefines.add(p);
				i++;
				while(i < params.size()){
					tranDefines.add(params.get(i));
					i++;
				}
				break;
			}
			commParams.add(p);
		}
	}

	@Override
	protected List<Transaction> createSubTransactions() {
		List<Transaction> trans = new ArrayList<Transaction>();
		List<ParamType> tranParams = null;
		for(int i = 0; i < tranDefines.size();){
			ParamType p = tranDefines.get(i);
			if(p.getName().equals("transaction")){
				tranParams = new ArrayList<ParamType>();
				tranParams.addAll(commParams);
				i++;
				while(i < tranDefines.size() && !tranDefines.get(i).getName().equals("transaction")){
					tranParams.add(tranDefines.get(i));
					i++;
				}
				try{
					Class<?> cls = Class.forName(GenericDriver.class.getPackage().getName() + ".transaction." + p.getValue() + "Transaction");
					Constructor con = cls.getConstructor(HttpClient.class, List.class);
					Transaction tran = (Transaction)con.newInstance(client, tranParams);
					trans.add(tran);
				}
				catch(Throwable t){
					t.printStackTrace();
				}
				continue;
			}
			i++;
		}
		return trans;
	}

}
