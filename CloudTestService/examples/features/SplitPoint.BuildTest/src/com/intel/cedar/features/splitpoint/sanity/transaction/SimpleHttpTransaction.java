package com.intel.cedar.features.splitpoint.sanity.transaction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.features.splitpoint.sanity.ParamType;


public class SimpleHttpTransaction extends AbstractHttpTransaction {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpTransaction.class);
	
	protected static enum HTTP_METHOD{
		GET,
		POST,
		PUT,
		DELETE,
		HEAD;
		
		public static boolean matches(String v){
			for(HTTP_METHOD s : HTTP_METHOD.values()){
				if(s.name().equals(v))
					return true;
			}
			return false;
		}
	}
	
	protected static interface Action{
		public HTTP_METHOD getMethod();
		public String getUrl();
		public Object[] getArguments();
		public String getRequestContentType();
		public String getRequestBody();
		public Object getCheck();
		public Credentials getCredential();	
	}	
		
	protected static class StaticAction implements Action{		
		HTTP_METHOD method;
		String url;
		Object[] args;
		String contentType;
		String body;
		Object check;
		Credentials cred;
		
		public StaticAction(HTTP_METHOD m, String u, Object[] args, String contentType, String body, Object check, Credentials cred){
			this.method = m;
			this.url = u;
			this.args = args;
			this.contentType = contentType;
			this.body = body;
			this.check = check;
			this.cred = cred;
		}
		
		public StaticAction(HTTP_METHOD m, String u, Object[] args, String contentType, String body, Object check){
			this(m, u, args, contentType, body, check, null);
		}
		
		public StaticAction(HTTP_METHOD m, String u, Object[] args, String contentType, String body){
			this(m, u, args, contentType, body, null, null);
		}
		
		public StaticAction(HTTP_METHOD m, String u, Object[] args){
			this(m, u, args, null, null, null);
		}
		
		public StaticAction(HTTP_METHOD m, String u){
			this(m, u, null, null, null, null);
		}

		public HTTP_METHOD getMethod() {
			return method;
		}

		public String getUrl() {
			return url;
		}

		public Object[] getArguments() {
			return args;
		}

		public String getRequestContentType() {
			return contentType;
		}

		public String getRequestBody() {
			return body;
		}

		public Object getCheck() {
			return check;
		}

		public Credentials getCredential() {
			return cred;
		}		
	}

	protected static class DynamicAction extends StaticAction {
		public DynamicAction(HTTP_METHOD m, String u) {
			super(m, u);
		}
	}
	
	protected static HashMap<HTTP_METHOD, List<Method>> httpMethods;
	static{
		httpMethods = new HashMap<HTTP_METHOD, List<Method>>();
		for(HTTP_METHOD s : HTTP_METHOD.values()){
			String mn = s.name().toLowerCase() + "Request";
			for(Method m : AbstractHttpTransaction.class.getMethods()){
				if(m.getName().equals(mn)){
					List<Method> methods = httpMethods.get(s);
					if(methods == null){
						methods = new ArrayList<Method>();
						httpMethods.put(s, methods);
					}
					
					Class<?>[] params = m.getParameterTypes();
					if(params.length > 1 && params[1].equals(HashMap.class))
						continue;
					methods.add(m);
				}
			}
		}
	}
	
	protected String base = null;
	protected List<Action> actions;
	
	public SimpleHttpTransaction(HttpClient client, List<ParamType> params) {
		super(client, params);
		actions = new ArrayList<Action>();
		// determine the baseURL, use the last defined
		for(ParamType p : params){
			if(p.getName().equals("base")){
				base = p.getValue();
			}
		}
		for(int i = 0; i < params.size(); i++){		
			ParamType p = params.get(i);
			if(HTTP_METHOD.matches(p.getName())){
				Action a = parseAction(params, i);
				if(a != null){
					actions.add(a);
				}
			}	
		}
	}
	
	protected String encodeURL(String u){
		u = u.replace("{", "%7B");
		u = u.replace("\"", "%22");
		u = u.replace(":", "%3A");
		u = u.replace("}", "%7D");
		return u;
	}
	
	protected String getFullURL(String url, Object[] args){
		if(url == null){
			return base;
		}
		String relative = url;
		if(args != null)
			relative = String.format(url, args);
		relative = encodeURL(relative);
		if(base != null){
			if(!base.endsWith("/") && !relative.startsWith("/"))
				return base + "/" + relative;
			else
				return base + relative;
		}
		return relative;
	}
	
	protected Action parseAction(List<ParamType> params, int index){
		Action a = null;
		ParamType bp = params.get(index);
		HTTP_METHOD m = HTTP_METHOD.valueOf(bp.getName());
		String endTag = "/" + m.name();
		String body = null;
		String[] args = null;
		String check = null;
		String contentType = null;
		Credentials cred = null;
		boolean correct = false;
		for(int i = index + 1; i < params.size(); i++){
			ParamType p = params.get(i);
			if(p.getName().equals("body")){
				if(p.getValue() != null && p.getValue().length() > 0)
					body = p.getValue();
			}
			else if(p.getName().equals("ContentType")){
				if(p.getValue() != null && p.getValue().length() > 0)
					contentType = p.getValue();
			}
			else if(p.getName().equals("args")){
				if(p.getValue() != null && p.getValue().length() > 0)
					args = p.getValue().split(" ");
			}
			else if(p.getName().equals(endTag)){
				correct = true;
				if(p.getValue() != null && p.getValue().length() > 0)
					check = p.getValue();
				break;
			}
		}
		if(correct){
			a = new StaticAction(m, bp.getValue(), args, contentType, body, check, cred);
		}
		return a;
	}
	
	protected boolean doAction(Action a) throws Exception{
		Method httpMethod = null;
		HTTP_METHOD md = a.getMethod();
		Object[] args = a.getArguments();
		String body = a.getRequestBody();
		String contentType = a.getRequestContentType();
		Object check = a.getCheck();
		Credentials cred = a.getCredential();
		//skip this action
		if(md == null)
			return true;
		
		if(httpMethods.get(md) != null){
			for(Method m : httpMethods.get(md)){
				if(body != null){
					if(m.getParameterTypes().length == (cred != null ? 4 : 3)){
						httpMethod = m;
						break;
					}
				}
				else{
					if(m.getParameterTypes().length == (cred != null ? 3 : 2)){
						httpMethod = m;
						break;
					}
				}
			}
		}
		if(httpMethod == null){
			LOG.error(md.name() + " Method implemention not found!");
			return false;
		}
		Object[] theArgs =  new Object[httpMethod.getParameterTypes().length];
		theArgs[0] = getFullURL(a.getUrl(), args);
		if(body != null){
			theArgs[1] = body;
			theArgs[2] = check;
		}
		else{
			theArgs[1] = check;
		}
		if(cred != null){
			theArgs[args.length - 1] = cred;
		}
		this.setContentType(contentType);
		Object ret = httpMethod.invoke(this, theArgs);
		if(ret instanceof Boolean){
			return (Boolean)ret;
		}
		return false;
	}
	
	@Override
	public boolean execute(boolean negative) {
		if(actions.size() == 0){
			LOG.error("No actions defined!");
			return false;
		}
		boolean result = true;
		try{
			for(Action a : actions){
				if(!doAction(a))
					result = false;
			}
		}
		catch(Exception e){
			result = false;
			LOG.error(e.getMessage());
		}
		return result;
	}
}
