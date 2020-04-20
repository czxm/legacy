package com.intel.cedar.features.demo;

import com.intel.cedar.tasklet.impl.Result;

public class DemoResult extends Result {
	private int val;
	
	public void setValue(int value){
		val = value;
	}
	
	public int getValue(){
		return val;
	}
}
