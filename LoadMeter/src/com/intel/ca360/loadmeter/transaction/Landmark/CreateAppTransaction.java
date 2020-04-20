package com.intel.ca360.loadmeter.transaction.Landmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;

import com.intel.ca360.config.ParamType;
import com.intel.ca360.loadmeter.transaction.Landmark.LandmarkRestTransaction.APIStatusChecker;
import com.intel.ca360.loadmeter.util.Util;

public class CreateAppTransaction extends LandmarkRestTransaction {

	public CreateAppTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
	}
	
	@Override
	protected List<Action> createActions(Action start){
		List<Action> result = new ArrayList<Action>();
		final APIChecker authnChecker = (APIChecker)start.getCheck();
		final APIChecker appInactiveChecker = new APIChecker();		
		result.add(new DynamicAction(HTTP_METHOD.GET, "getAppSum") {
			@Override
			public Object[] getArguments(){
				return new String[]{idp, authnChecker.getResponse(), "", "", "INACTIVE"};
			}
			@Override
			public Header[] getHeaders(){
				return responseJSONHeaders;
			}
			@Override
			public Object getCheck(){
				return appInactiveChecker;
			}
		});
		
		final APIChecker appActiveChecker = new APIChecker();		
		result.add(new DynamicAction(HTTP_METHOD.GET, "getAppSum") {
			@Override
			public Object[] getArguments(){
				return new String[]{idp, authnChecker.getResponse(), "", "", "ACTIVE"};
			}
			@Override
			public Header[] getHeaders(){
				return responseJSONHeaders;
			}
			@Override
			public Object getCheck(){
				return appActiveChecker;
			}
		});
		
		
		final APIChecker appPartialChecker = new APIChecker();		
		result.add(new DynamicAction(HTTP_METHOD.GET, "getAppSum") {
			@Override
			public Object[] getArguments(){
				return new String[]{idp, authnChecker.getResponse(), "", "", "INCOMPLETE"};
			}
			@Override
			public Header[] getHeaders(){
				return responseJSONHeaders;
			}
			@Override
			public Object getCheck(){
				return appPartialChecker;
			}
		});
		
		result.add(new DynamicAction(HTTP_METHOD.POST, "createApp") {
			private String app = null;
			private int appIndex = 0;
			@Override
			public boolean skipped(){
				appIndex = new Random().nextInt(appCount) + 1;
				String theAppIdMatcher = String.format(appIdMatcher, subject, appIndex);
				app = Util.stringRegexMatch(theAppIdMatcher, appInactiveChecker.getResponse());
				if(app == null)
					app = Util.stringRegexMatch(theAppIdMatcher, appActiveChecker.getResponse());
				if(app == null)
					app = Util.stringRegexMatch(theAppIdMatcher, appPartialChecker.getResponse());
				return app != null;
			}
			@Override
			public Object[] getArguments(){
				return new String[]{idp, authnChecker.getResponse()};
			}
			@Override
			public String getRequestBody(){
				return String.format(createAppJSON, subject, appIndex);
			}
			@Override
			public Header[] getHeaders(){
				return requestJSONHeaders;
			}
			@Override
			public Object getCheck(){
				return new APIChecker(app);
			}
		});
		return result;
	}
}
