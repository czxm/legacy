package com.intel.ca360.loadmeter.transaction.Landmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;

import com.intel.ca360.config.ParamType;

public class MapUserAppTransaction extends LandmarkRestTransaction {

	public MapUserAppTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
	}
	
	class MyAction extends DynamicAction {
		String username;
		APIChecker authnChecker;
		APIChecker theChecker;
		
		public MyAction(APIChecker authnChecker, APIChecker theChecker, HTTP_METHOD m, String u) {
			super(m, u);
			this.authnChecker = authnChecker;
			this.theChecker = theChecker;
		}
		@Override
		public Object[] getArguments(){
			int userIndex = new Random().nextInt(userCount) + 1;
			username = String.format(userPattern, userIndex);
			return new String[]{idp, provAppId, authnChecker.getResponse(), "1", "10000", username };
		}
		@Override
		public Object getCheck(){
			return theChecker;
		}	
		public String getUserName(){
			return this.username;
		}
	}
	
	@Override
	protected List<Action> createActions(Action start){
		List<Action> result = new ArrayList<Action>();
		final APIChecker authnChecker = (APIChecker)start.getCheck();	
		result.add(new DynamicAction(HTTP_METHOD.GET, "getAppById") {
			@Override
			public Object[] getArguments(){
				return new String[]{idp, provAppId, authnChecker.getResponse()};
			}
			@Override
			public Header[] getHeaders(){
				return responseJSONHeaders;
			}
			@Override
			public Object getCheck(){
				return new APIChecker("\"Value\":\"" + provAppId + "\"");
			}
		});
		
		final APIChecker users = new APIChecker();
		final MyAction a = new MyAction(authnChecker, users, HTTP_METHOD.GET, "getUsersOfApp");
		result.add(a);
		
		final APIChecker check = new APIChecker();
		check.setCheck("successful");
		result.add(new DynamicAction(HTTP_METHOD.POST, "assignUsersToApp") {
			@Override
			public boolean skipped(){
				return users.getResponse().contains("\"total\":1");
			}
			@Override
			public Object[] getArguments(){
				return new String[]{idp, provAppId, authnChecker.getResponse()};
			}
			@Override
			public String getRequestBody(){
				return String.format(usersJSON, a.getUserName());
			}
			@Override
			public Header[] getHeaders(){
				return requestFORMHeaders;
			}
			@Override
			public Object getCheck(){
				return check;
			}
		});
		
		result.add(new DynamicAction(HTTP_METHOD.DELETE, "deleteUserOfApp") {
			@Override
			public boolean skipped(){
				return users.getResponse().contains("\"total\":0");
			}
			@Override
			public Object[] getArguments(){
				return new String[]{idp, provAppId, authnChecker.getResponse(),  a.getUserName()};
			}
			@Override
			public Header[] getHeaders(){
				return responseTextHeaders;
			}
			@Override
			public Object getCheck(){
				return check;
			}
		});
		
		return result;
	}
}
