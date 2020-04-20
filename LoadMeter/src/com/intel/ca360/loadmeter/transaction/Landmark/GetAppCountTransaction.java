package com.intel.ca360.loadmeter.transaction.Landmark;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;

import com.intel.ca360.config.ParamType;

public class GetAppCountTransaction extends LandmarkRestTransaction {
	
	public GetAppCountTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
	}
	
	@Override
	protected List<Action> createActions(Action start){
		List<Action> result = new ArrayList<Action>();
		final APIChecker checker = (APIChecker)start.getCheck();
		Action a = new DynamicAction(HTTP_METHOD.GET, "getAppCount") {
			@Override
			public Object[] getArguments(){
				return new String[]{INSTANCE.idp, checker.result};
			}
			@Override
			public Header[] getHeaders(){
				return responseJSONHeaders;
			}
		};
		result.add(a);
		return result;
	}
}
