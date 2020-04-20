package com.intel.ca360.loadmeter;

import java.util.ArrayList;
import java.util.List;

public class MeasureData {
	private int index;
	private int activeUsers;
	private float tps;
	private float avgResponseTime;
	private float errorRate;
	
	private long totalTransactions;
	private long errorTransactions;
	private long mergeCount;
	private long effectiveResponseCount;
	private List<TransactionData> dataList;
	
	public MeasureData(){
		this.dataList = new ArrayList<TransactionData>();
	}
	
	public MeasureData(String data){
		String[] args = data.split(",");
		index = Integer.parseInt(args[0]);
		activeUsers = Integer.parseInt(args[1]);
		tps = Float.parseFloat(args[2]);
		avgResponseTime = Float.parseFloat(args[3]);
		errorRate = Float.parseFloat(args[4]);
		this.dataList = new ArrayList<TransactionData>();
		for(int i = 0; i < args.length/5 - 1; i++){
			this.dataList.add(new TransactionData(Long.parseLong(args[5 + 5*i]), Integer.parseInt(args[6 + 5*i]), Float.parseFloat(args[7 + 5*i]), Integer.parseInt(args[8 + 5*i]), Long.parseLong(args[9 + 5*i])));
		}
	}
	
	public MeasureData(int index){
		this.index = index;
	}
	
	public int getColumns(){
		return 5 + dataList.size() * 5;
	}
	
	public List<TransactionData> getDataList(){
		return this.dataList;
	}
	
	public void beginMerge(List<TransactionData> dataList){
		this.totalTransactions = 0;
		this.errorTransactions = 0;
		this.dataList = new ArrayList<TransactionData>();
		for(TransactionData t : dataList){
			this.totalTransactions += t.getTotalTransactions();
			this.errorTransactions += t.getErrorTransactions();
			this.dataList.add(new TransactionData(t));
		}
		mergeCount = 1;
	}
	
	public void beginAverage(){
		mergeCount = 0;
		effectiveResponseCount = 0;
	}
	
	public void beginMerge(){
		mergeCount = 1;
	}
	
	public void merge(List<TransactionData> dataList){
		for(int i = 0; i < dataList.size(); i++){
			TransactionData t = dataList.get(i);
			this.totalTransactions += t.getTotalTransactions();
			this.errorTransactions += t.getErrorTransactions();
			TransactionData d = this.dataList.get(i);
			d.merge(t);
		}
		mergeCount++;
	}
	
	public void merge(MeasureData data){
		if(index == data.index){
			activeUsers += data.activeUsers;
			tps += data.tps;
			avgResponseTime += data.avgResponseTime;
			errorRate += data.errorRate;
			for(int i = 0; i < dataList.size(); i++){
				TransactionData t = dataList.get(i);
				TransactionData d = data.dataList.get(i);
				t.merge(d);
			}
		}
		else{
			tps += data.tps;
			avgResponseTime += data.avgResponseTime;
			errorRate += data.errorRate;
			if(this.dataList.size() < data.dataList.size()){
				for(TransactionData d : data.dataList){
					dataList.add(new TransactionData(d.getTotalTransactions(), d.getMinResponseTime(), d.getAvgResponseTime(), d.getMaxResponseTime(), d.getErrorTransactions()));
				}
			}
			else{
				for(int i = 0; i < dataList.size(); i++){
					TransactionData d = dataList.get(i);
					TransactionData nd = data.dataList.get(i);
					d.setAvgResponseTime(d.getAvgResponseTime() + nd.getAvgResponseTime());
					d.setTotalTransactions(d.getTotalTransactions() + nd.getTotalTransactions());
					d.setErrorTransactions(d.getErrorTransactions() + nd.getErrorTransactions());
					d.setMaxResponseTime(d.getMaxResponseTime() + nd.getMaxResponseTime());
					d.setMinResponseTime(d.getMinResponseTime() + nd.getMinResponseTime());
				}
			}
			if(data.avgResponseTime > 0.01)
				effectiveResponseCount++;
		}
		mergeCount++;
	}
	
	public void endMerge(){
		avgResponseTime = avgResponseTime / mergeCount;
		errorRate = errorRate / mergeCount;
		for(TransactionData t : dataList){
			t.setAvgResponseTime(t.getAvgResponseTime() / mergeCount);
		}
	}
	
	public void endAverage(){
		avgResponseTime = avgResponseTime / effectiveResponseCount;
		errorRate = errorRate / mergeCount;
		tps = tps / mergeCount;
		for(TransactionData t : dataList){
			t.setTotalTransactions(t.getTotalTransactions() / mergeCount);
			t.setAvgResponseTime(t.getAvgResponseTime() / mergeCount);
			t.setErrorTransactions(t.getErrorTransactions() / mergeCount);
			t.setMinResponseTime(t.getMinResponseTime() / mergeCount);
			t.setMaxResponseTime(t.getMaxResponseTime() / mergeCount);
		}
	}
	
	public void endMerge(float interval){
		this.activeUsers =  (int)this.mergeCount;
		this.tps = (this.totalTransactions - this.errorTransactions)/ interval;
		this.errorRate = this.totalTransactions > 0 ? this.errorTransactions / ((float)this.totalTransactions) : 0;
		for(int i = 0; i < dataList.size(); i++){
			TransactionData t = dataList.get(i);
			long succTrans = t.getTotalTransactions() - t.getErrorTransactions();
			t.setTotalTransactions(succTrans);
			t.setAvgResponseTime(succTrans > 0 ? t.getTotalResponseTime() / ((float)succTrans) : 0);
			this.avgResponseTime += t.getAvgResponseTime();
		}
		this.avgResponseTime = this.avgResponseTime / dataList.size();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%d,%d,%.2f,%.2f,%.2f", index, activeUsers, tps, avgResponseTime, errorRate));
		for(TransactionData t : dataList){
			sb.append(String.format(",%d,%d,%.2f,%d,%d", t.getTotalTransactions(), t.getMinResponseTime() == Long.MAX_VALUE ? 0 : t.getMinResponseTime(), t.getAvgResponseTime(), t.getMaxResponseTime(), t.getErrorTransactions()));
		}
		return sb.toString();
	}
	
	public String toHTMLString(){
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("<TD>%d</TD><TD>%d</TD><TD>%.2f</TD><TD>%.2f</TD><TD>%.2f</TD>", index, activeUsers, tps, avgResponseTime, errorRate));
		for(TransactionData t : dataList){
			sb.append(String.format("<TD>%d</TD><TD>%d</TD><TD>%.2f</TD><TD>%d</TD><TD>%d</TD>", t.getTotalTransactions(), t.getMinResponseTime() == Long.MAX_VALUE ? 0 : t.getMinResponseTime(), t.getAvgResponseTime(), t.getMaxResponseTime(), t.getErrorTransactions()));
		}
		return sb.toString();
	}
	
	public Object getByIndex(int index){
		if(index < 5){
			switch(index){
			case 0:
				return this.index;
			case 1:
				return activeUsers;
			case 2:
				return tps;
			case 3:
				return avgResponseTime;
			case 4:
				return errorRate;
			default:
				return null;
			}
		}
		else{
			TransactionData d = dataList.get((index - 5) / 5);
			return d.getByIndex((index - 5) % 5);
		}
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getActiveUsers() {
		return activeUsers;
	}
	public void setActiveUsers(int activeUsers) {
		this.activeUsers = activeUsers;
	}
	public float getTps() {
		return tps;
	}
	public void setTps(float tps) {
		this.tps = tps;
	}
	public float getAvgResponseTime() {
		return avgResponseTime;
	}
	public void setAvgResponseTime(float avgResponseTime) {
		this.avgResponseTime = avgResponseTime;
	}
	public float getErrorRate() {
		return errorRate;
	}
	public void setErrorRate(float errorRate) {
		this.errorRate = errorRate;
	}
}
