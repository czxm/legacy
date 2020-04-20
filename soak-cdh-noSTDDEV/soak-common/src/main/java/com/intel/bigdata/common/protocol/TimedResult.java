package com.intel.bigdata.common.protocol;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class TimedResult<T> implements Serializable {
	long time;

	List<T> result;
	
	public TimedResult() {}
	
	public TimedResult(List<T> result) {
		this.result = result;
	}
	
	public List<T> getResult() {
		return result;
	}

	public void setResult(List<T> result) {
		this.result = result;
	}
	
	public void setTime(long l) {
		time = l;
	}
	
	public long getTime() {
		return time;
	}
}
