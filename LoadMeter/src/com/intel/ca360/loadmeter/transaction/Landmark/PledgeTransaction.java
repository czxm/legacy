package com.intel.ca360.loadmeter.transaction.Landmark;

import java.util.List;

import org.apache.http.client.HttpClient;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.transaction.CompositeHttpTransaction;

public class PledgeTransaction extends CompositeHttpTransaction {
	public PledgeTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
	}
}
