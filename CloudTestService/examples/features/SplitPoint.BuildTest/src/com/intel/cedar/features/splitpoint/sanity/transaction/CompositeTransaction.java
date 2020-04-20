package com.intel.cedar.features.splitpoint.sanity.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.intel.cedar.features.splitpoint.sanity.ParamType;
import com.intel.cedar.features.splitpoint.sanity.Transaction;

public abstract class CompositeTransaction implements Transaction {
	
	protected abstract List<Transaction> createSubTransactions();
	
	protected List<Transaction> trans;
	protected boolean ordered;
	protected boolean sequential;
	protected Random random;
	protected int nextItem = -1;
	protected int wait = 0;
	
	public CompositeTransaction(List<ParamType> params){
		random = new Random();
		trans = new ArrayList<Transaction>();
		for(ParamType p : params){
			if(p.getName().equals("ordered")){
				ordered = Boolean.parseBoolean(p.getValue());
			}
			else if(p.getName().equals("sequential")){
				sequential = Boolean.parseBoolean(p.getValue());
			}
			else if(p.getName().equals("wait")){
				try{
					wait = Integer.parseInt(p.getValue()) * 1000;
				}
				catch(Exception e){					
				}
			}
		}
	}
	
	protected void setSequential(boolean flag){
		this.sequential = flag;
	}
	
	protected boolean isSequential(){
		return this.sequential;
	}
	
	protected void setOrdered(boolean flag){
		this.ordered = flag;
	}
	
	protected boolean isOrdered(){
		return this.ordered;
	}
	
	@Override
	public void afterExecute() {
		if(trans.size() == 0)
			return;
		if(!sequential){
			trans.get(nextItem).afterExecute();
		}
	}

	@Override
	public void beforeExecute() {
		if(trans.size() == 0)
			return;
		if(!sequential){
			if(ordered){
				nextItem++;
				if(nextItem == trans.size()){
					nextItem = 0;
				}
			}
			else{
				nextItem = (int)(random.nextFloat() * trans.size());
				if(nextItem >= trans.size()){
					nextItem = trans.size() - 1;
				}
			}
			trans.get(nextItem).beforeExecute();
		}
	}

	@Override
	public boolean execute(boolean negative) {
		if(trans.size() == 0)
			return true;
		boolean result = true;
		if(sequential){
			for(Transaction t : trans){
				t.beforeExecute();
				if(!t.execute(negative))
					result = false;				
				t.afterExecute();
				if(wait > 0){
					try {
						Thread.sleep(wait);
					}
					catch (InterruptedException e) {
					}
				}
			}
		}
		else{
			result = trans.get(nextItem).execute(negative);
		}
		return result;
	}

	@Override
	public void setup() {
		trans = this.createSubTransactions();
		for(Transaction t : trans){
			t.setup();
		}
	}

	@Override
	public void shutdown() {
		for(Transaction t : trans){
			t.shutdown();
		}
	}

	@Override
	public boolean startup() {
		boolean result = true;
		for(Transaction t : trans){
			if(!t.startup())
				result = false;
		}
		return result;
	}

}
