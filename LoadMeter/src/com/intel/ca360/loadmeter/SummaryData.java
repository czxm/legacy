package com.intel.ca360.loadmeter;

import java.util.ArrayList;
import java.util.List;

public class SummaryData {
	private MeasureData data;
	private List<Float> dataList;
	
	public SummaryData(MeasureData data, List<RemoteMeasureData> dataList){
		this.dataList = new ArrayList<Float>();
		this.data = data;
		for(RemoteMeasureData d : dataList){
			for(int i = 0; i < d.getDataList().size(); i++){
				this.dataList.add(d.getDataList().get(i));
			}
		}
	}
	
	public Object getByIndex(int index){
		if(index >= data.getColumns() && index < data.getColumns() + dataList.size()){
			return dataList.get(index - data.getColumns());
		}
		return data.getByIndex(index);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(data.toString());
		for(Float t : dataList){
			sb.append(String.format(",%.2f", t));
		}
		return sb.toString();
	}
	
	public String toHTMLString(){
		StringBuilder sb = new StringBuilder();
		sb.append(data.toHTMLString());
		for(Float t : dataList){
			sb.append(String.format("<TD>%.2f</TD>", t));
		}
		return sb.toString();
	}
	
	public void setActiveUsers(int users){
		this.data.setActiveUsers(users);
	}
	
	public void setIndex(int index){
		this.data.setIndex(index);
	}
}
