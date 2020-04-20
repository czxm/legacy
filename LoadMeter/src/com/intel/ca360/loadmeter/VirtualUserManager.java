package com.intel.ca360.loadmeter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import com.intel.ca360.loadmeter.util.Util;

public class VirtualUserManager {
	private ConcurrentHashMap<VirtualUser, ArrayList<TransactionData>> dataList = new ConcurrentHashMap<VirtualUser, ArrayList<TransactionData>>();
	private volatile CountDownLatch latch;
	private boolean stopped = false;
	
	public void allocVirtualUserData(VirtualUser user){
		ArrayList<TransactionData> list = new ArrayList<TransactionData>();
		dataList.put(user, list);
		for(int i = 0; i < user.getTransactions().size(); i++){
			TransactionData data = new TransactionData();
			list.add(data);
		}
	}
	
	public void releaseVirtualUserData(VirtualUser user){
		dataList.remove(user);
	}
	
	public List<TransactionData> getTransactionDataList(VirtualUser user){
		return this.dataList.get(user);
	}
	
	public Collection<ArrayList<TransactionData>> getAllTransactionDataList(){
		return this.dataList.values();
	}
	
	public int getActiveUsers(){
		return this.dataList.size();
	}
	
	public String[] getTransactionNames(){
		Iterator iter = this.dataList.keySet().iterator();
		if(iter.hasNext()){
			VirtualUser user = (VirtualUser)iter.next();
			String[] names = new String[user.getTransactions().size()];
			for(int i = 0; i < names.length; i++){
				Transaction t = user.getTransactions().get(i);
				String name = t.getClass().getSimpleName();
				names[i] = name.replace("Transaction", "");
			}
			return names;
		}
		return new String[]{};
	}
	
	public void waitForReach(int users){
		try {
			if(latch == null)
				latch = new CountDownLatch(users);
			latch.await();
			synchronized(latch){
				latch.notifyAll();
				latch = new CountDownLatch(users);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void reach() throws Throwable{
		while(latch == null || latch.getCount() == 0){
			Util.sleep(1);
		}
		synchronized(latch){
			latch.countDown();
			latch.wait();
		}
	}
	
	public void shutdown(){
		this.stopped = true;
	}
	
	public boolean isShutdown(){
		return this.stopped;
	}
}
