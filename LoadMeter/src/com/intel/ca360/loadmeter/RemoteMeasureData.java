package com.intel.ca360.loadmeter;

import java.util.ArrayList;
import java.util.List;

public class RemoteMeasureData {
	private int index;
	private List<Float> dataList;
	private int mergeCount;
	
	public RemoteMeasureData(){
		this.dataList = new ArrayList<Float>();
	}
	
	public Object getByIndex(int index){
		if(index == 0){
			return this.index;
		}
		if(index > 0 && index < dataList.size() + 1){
			return dataList.get(index -1);
		}
		return null;
	}
	
	public void beginAverage(){
		mergeCount = 0;
	}
	
	public void merge(RemoteMeasureData d){
		if(dataList.size() < d.dataList.size()){
			for(Float f : d.dataList){
				dataList.add(f);
			}
		}
		else{
			for(int i = 0; i < dataList.size(); i++){
				Float f = dataList.get(i);
				Float nf = d.dataList.get(i);
				dataList.set(i, f + nf);
			}
		}
		mergeCount++;
	}
	
	public void endAverage(){
		for(int i = 0; i < dataList.size(); i++){
			Float f = dataList.get(i);
			dataList.set(i, f / mergeCount);
		}
	}
	
	public RemoteMeasureData(String data){
		dataList = new ArrayList<Float>();
		String[] args = data.split(",");
		index = Integer.parseInt(args[0]);
		for(int i = 1; i < args.length; i++){
			this.dataList.add(Float.parseFloat(args[i]));
		}
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(index);
		for(Float t : dataList){
			sb.append(String.format(",%.2f", t));
		}
		return sb.toString();
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
	public List<Float> getDataList(){
		return this.dataList;
	}
}
