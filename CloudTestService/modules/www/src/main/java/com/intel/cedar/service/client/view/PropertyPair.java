package com.intel.cedar.service.client.view;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class PropertyPair extends BaseModelData{
	  
	/**
	 * 
	 */
	private static final long serialVersionUID = -6453977026279458053L;
	public static final String KEY = "PropertyKey";
	public static final String VAULE = "PropertyValue";
	
	public PropertyPair(){
		
	}
	 
	public PropertyPair(String k, Object v) {
		setKey(k);
		setValue(v);
	}
	  
	public void setKey(String k) {
		set(KEY, k);
	}
	
	public String getKey() {
		return get(KEY);
	}
	
	public void setValue(Object v) {
		set(VAULE, v.toString());
	}
	
	public String getValue() {
		return get(VAULE);
	}

  }
 
