package com.intel.ca360.loadmeter.transaction.Landmark;

import java.util.List;

import org.apache.http.client.HttpClient;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.transaction.CompositeHttpTransaction;

// this class is only used to wrapper the Landmark APIs designed for managing applications
public class EditAppTransaction extends CompositeHttpTransaction {	
	public EditAppTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
	}
}
