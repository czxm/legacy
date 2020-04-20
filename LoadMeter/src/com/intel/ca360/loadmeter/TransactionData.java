package com.intel.ca360.loadmeter;

public class TransactionData {
	private long totalTransactions;
	private long totalResponseTime;
	private long errorTransactions;
	private long minResponseTime;
	private long maxResponseTime;
	private float avgResponseTime;
	
	public TransactionData(){
		reset();
	}
	
	public TransactionData(TransactionData t){
		this(t.totalTransactions, t.totalResponseTime, t.errorTransactions, t.minResponseTime, t.maxResponseTime);
	}
	
	public TransactionData(long tx, long min, float avgResponseTime, long max, long errors){
		this.totalTransactions = tx;
		this.minResponseTime = min;
		this.avgResponseTime = avgResponseTime;
		this.maxResponseTime = max;
		this.errorTransactions = errors;
	}
	
	public TransactionData(long tT, long tR, long eT, long min, long max){
		this.totalTransactions = tT;
		this.totalResponseTime = tR;
		this.errorTransactions = eT;
		this.minResponseTime = min;
		this.maxResponseTime = max;
	}
	
	public int getColumns(){
		return 5;
	}
	
	public Object getByIndex(int index){
		if(index < 5){
			switch(index){
			case 0:
				return totalTransactions;
			case 1:
				return minResponseTime;
			case 2:
				return avgResponseTime;
			case 3:
				return maxResponseTime;
			case 4:
				return errorTransactions;
			}
		}
		return null;
	}
	
	public void merge(TransactionData t){
		this.errorTransactions += t.errorTransactions;
		this.totalTransactions += t.totalTransactions;
		this.totalResponseTime += t.totalResponseTime;
		this.avgResponseTime += t.avgResponseTime;
		if(this.minResponseTime > t.minResponseTime)
			this.minResponseTime = t.minResponseTime;
		if(this.maxResponseTime < t.maxResponseTime)
			this.maxResponseTime = t.maxResponseTime;
	}
	
	public void reset(){
		this.minResponseTime = Long.MAX_VALUE;
		this.maxResponseTime = 0;
		this.errorTransactions = 0;
		this.totalResponseTime = 0;
		this.totalTransactions = 0;
	}
	
	public long getTotalTransactions() {
		return totalTransactions;
	}
	public void incTotalTransactions() {
		this.totalTransactions++;
	}
	public long getTotalResponseTime() {
		return totalResponseTime;
	}
	public void incTotalResponseTime(long totalResponseTime) {
		this.totalResponseTime += totalResponseTime;
	}
	public long getErrorTransactions() {
		return errorTransactions;
	}
	public void incErrorTransactions() {
		this.errorTransactions++;
	}
	public long getMinResponseTime() {
		return minResponseTime;
	}
	public void setMinResponseTime(long minResponseTime) {
		this.minResponseTime = minResponseTime;
	}
	public long getMaxResponseTime() {
		return maxResponseTime;
	}
	public void setMaxResponseTime(long maxResponseTime) {
		this.maxResponseTime = maxResponseTime;
	}

	public void setTotalTransactions(long totalTransactions) {
		this.totalTransactions = totalTransactions;
	}
		
	public void setErrorTransactions(long errorTransactions) {
		this.errorTransactions = errorTransactions;
	}

	public float getAvgResponseTime() {
		return avgResponseTime;
	}

	public void setAvgResponseTime(float avgResponseTime) {
		this.avgResponseTime = avgResponseTime;
	} 
	
}
