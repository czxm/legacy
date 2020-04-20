package com.intel.cedar.features.splitpoint.sanity;

import java.lang.reflect.Constructor;
import java.util.List;

public abstract class GenericDriver {
	protected abstract void prepare(List<ParamType> params);
	
	protected void shutDown(){
	}

	public Transaction createTransaction(String name) {
		Transaction tran = null;
		try{
			Class<?> cls = Class.forName(GenericDriver.class.getPackage().getName() + ".transaction." + name + "Transaction");
			Constructor con = cls.getConstructor();
			tran = (Transaction)con.newInstance();
		}
		catch(Throwable t){
			t.printStackTrace();
		}
		return tran;
	}	
}
