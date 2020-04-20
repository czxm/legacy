package com.intel.ca360.loadmeter.transaction.Landmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.util.Util;

public class DeactivateAppTransaction extends LandmarkRestTransaction {
	public DeactivateAppTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
	}
	
	@Override
	protected List<Action> createActions(Action start){
		List<Action> result = new ArrayList<Action>();
		final APIChecker authnChecker = (APIChecker)start.getCheck();
		final APIChecker appActive = new APIChecker();		
		result.add(new DynamicAction(HTTP_METHOD.GET, "getAppSum") {
			@Override
			public Object[] getArguments(){
				return new String[]{INSTANCE.idp, authnChecker.getResponse(), "", "", "ACTIVE"};
			}
			@Override
			public Header[] getHeaders(){
				return responseJSONHeaders;
			}
			@Override
			public Object getCheck(){
				return appActive;
			}
		});
				
		result.add(new DynamicAction(HTTP_METHOD.PUT, "updateApp"){
			private String app = null;
			private int appIndex = 0;
			@Override
			public boolean skipped(){
				appIndex = new Random().nextInt(appCount) + 1;
				String theAppIdMatcher = String.format(appIdMatcher, subject, appIndex);
				app = Util.stringRegexMatch(theAppIdMatcher, appActive.getResponse());
				return app == null;
			}
			@Override
			public Object[] getArguments(){
				return new String[]{idp, app, authnChecker.getResponse()};
			}
			@Override
			public String getRequestBody(){
				return String.format(updateAppJSON, app, "INACTIVE");
			}			
			@Override
			public Header[] getHeaders(){
				return allJSONHeaders;
			}
			@Override
			public Object getCheck(){
				return new APIChecker("INACTIVE");
			}
		});
		return result;
	}
}
