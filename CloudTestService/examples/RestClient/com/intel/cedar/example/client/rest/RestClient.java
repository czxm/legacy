package com.intel.cedar.example.client.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;


public class RestClient {
	private String server;
	private ThreadSafeClientConnManager connectionManager;
	
	public RestClient(String cloudtestServer){
		this.server = cloudtestServer;
		connectionManager = new ThreadSafeClientConnManager();
	}
	
	protected HttpClient getClient(){
		HttpParams params = new BasicHttpParams();
		DefaultHttpClient.setDefaultHttpParams(params);
		return new DefaultHttpClient(connectionManager, params);
	}
	
	public List<String> submitJobs(String feature, String launchset) throws Exception{
		return submitJobs(feature, null, launchset);
	}
	
	public List<String> submitJobs(String feature, String version, String launchset) throws Exception{
		List<String> jobIds = new ArrayList<String>();
		String url = "http://" + server + "/rest/submit";
    	HttpPost post = new HttpPost( url );
		ArrayList<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>();
		postParams.add(new BasicNameValuePair("feature", feature));
		if(version != null){
			postParams.add(new BasicNameValuePair("version", version));
		}
		postParams.add(new BasicNameValuePair("launchset", launchset));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParams, "UTF-8");
		post.setEntity(entity);
		HttpEntity resEntity = getClient().execute(post).getEntity();
		for(String jobId : EntityUtils.toString(resEntity).split("\n")){
			if(!jobId.equals(""))
				jobIds.add(jobId);
		}
		return jobIds;
	}
	
	public String queryStatus(String jobId) throws Exception{
		String url = "http://" + server + "/rest/" + jobId + "/status";
    	HttpGet get = new HttpGet( url );
		HttpEntity resEntity = getClient().execute(get).getEntity();
		return EntityUtils.toString(resEntity);
	}
	
	public String listJobs() throws Exception{
		String url = "http://" + server + "/rest/list";
    	HttpGet get = new HttpGet( url );
		HttpEntity resEntity = getClient().execute(get).getEntity();
		return EntityUtils.toString(resEntity);
	}
	
	public String killJob(String jobId) throws Exception{
		String url = "http://" + server + "/rest/" + jobId + "/kill";
    	HttpGet get = new HttpGet( url );
		HttpEntity resEntity = getClient().execute(get).getEntity();
		return EntityUtils.toString(resEntity);
	}
	
	public String createLaunchSet(String option, List<String> launches){
		StringBuilder sb = new StringBuilder();
		sb.append("<launchset>");
		sb.append(option);
		for(String l : launches){
			sb.append(l);
		}
		sb.append("</launchset>");
		return sb.toString();
	}
	
	public String createLaunchSet(String option, String launch){
		StringBuilder sb = new StringBuilder();
		sb.append("<launchset>");
		sb.append(option);
		sb.append(launch);
		sb.append("</launchset>");
		return sb.toString();
	}
	
	public String createOption(String user, boolean sendReport){
		return createOption(user, null, null, false, sendReport);
	}
	
	public String createOption(List<String> receivers, boolean sendReport){
		return createOption(null, receivers, null, false, sendReport);
	}
	
	public String createOption(String user, List<String> receivers, String comment, boolean reproducable, boolean sendReport){
		StringBuilder sb = new StringBuilder();
		sb.append("<option>");
		sb.append(String.format("<user>%s</user>", user == null ? "" : user));
		if(receivers == null){
			sb.append("<receivers></receivers>");
		}
		else{
			sb.append("<receivers>");
			int i = 0;
			for(; i < receivers.size(); i++){
				sb.append(receivers.get(i));
				if(i < receivers.size())
					sb.append(",");
			}
			sb.append("</receivers>");
		}
		sb.append(String.format("<comment>%s</comment>", comment == null ? "" : comment));
		sb.append(String.format("<reproducable>%s</reproducable>", Boolean.toString(reproducable)));
		sb.append(String.format("<sendReport>%s</sendReport>", Boolean.toString(sendReport)));
		sb.append("</option>");
		return sb.toString();
	}
	
	public String createLaunch(List<String> vars){
		StringBuilder sb = new StringBuilder();
		sb.append("<launch><variables>");
		for(String v : vars){
			sb.append(v);
		}
		sb.append("</variables></launch>");
		return sb.toString();
	}
	
	public String createVariable(String name, List<String> values){
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("<variable name=\"%s\">", name));
		sb.append("<values>");
		for(String v : values){
			sb.append(String.format("<value>%s</value>", v));
		}
		sb.append("</values>");
		sb.append("</variable>");
		return sb.toString();
	}
	
	public String createVariable(String name, String value){
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("<variable name=\"%s\">", name));
		sb.append("<values>");
		sb.append(String.format("<value>%s</value>", value));
		sb.append("</values>");
		sb.append("</variable>");
		return sb.toString();
	}
	
	public static void main(String[] args) throws Exception {
		RestClient client = new RestClient("cloudtest.sh.intel.com");
		List<String> variables = new ArrayList<String>();
		variables.add(client.createVariable("client_count", "4"));
		variables.add(client.createVariable("spServer", "splitpointperf3-linux64.sh.intel.com"));
		variables.add(client.createVariable("dummyServer", "splatqa-server.sh.intel.com"));
		variables.add(client.createVariable("thread_count", "10"));
		variables.add(client.createVariable("loop_count", "100"));
		variables.add(client.createVariable("perfloop_count", "100"));
		variables.add(client.createVariable("interval", "10"));
		variables.add(client.createVariable("engine", "TestEngineSTC"));
		variables.add(client.createVariable("driver", "LDAPFederatingDriver"));
		String option = client.createOption("xzhan27", true);
		String launchset = client.createLaunchSet(option, client.createLaunch(variables));
		List<String> jobIds = client.submitJobs("SplitPoint Reliability Test", launchset);
		Thread.sleep(60000);
		System.out.println(client.listJobs());
		for(String jobId : jobIds){
			client.killJob(jobId);
		}
		Thread.sleep(60000);
		for(String jobId : jobIds){
			System.out.println(client.queryStatus(jobId));
		}
	}
}
