package com.intel.cedar.features.cppConf;

import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.service.client.feature.model.Params;
import com.intel.cedar.service.client.feature.model.VarValue;


public class CaseParser  {
	private Params sysParameters;
	
	public static final String FILEPATH 	= "filePath";
	public static final String DRIVER 		= "dri";
	public static final String TSUITE 		= "tsu";
	public static final String MODE			= "mod";
	public static final String UNTAR		= "untar";
	public static final String PLATFORM 	= "plf";
	public static final String COP			= "cop";
	public static final String EVT			= "evt";
	public static final String THD			= "thd";
	public static final String ITERATION    = "iteration";

	public CaseParser() {
	}


	public List<VarValue> parseCase(String v) {
		List<String> cases = splitCase(v);
		
		List<VarValue> testCases = new ArrayList<VarValue>();
		for(String c : cases){
			Params params = parseParam(c);
			VarValue testcase = new VarValue(c, params);
			testcase.refresh();
			testCases.add(testcase);
		}
		
		return testCases;
	}

	public Params parseParam(String c) {
		Params parameters = new Params();
		
		fillSysParameters(parameters);
		
		parseFilePath(parameters,  c, FILEPATH);
		internalParser(parameters, c, DRIVER);
		internalParser(parameters, c, TSUITE);
		internalParser(parameters, c, MODE);
		internalParser(parameters, c, UNTAR);
		internalParser(parameters, c, PLATFORM);
		internalParser(parameters, c, COP);
		internalParser(parameters, c, EVT);
		internalParser(parameters, c, THD);
		internalParser(parameters, c, ITERATION);
		
		return parameters;
	}	

	public void addSysParam(String param, String value) {
		if(sysParameters==null){
			sysParameters = new Params();
		}
		
		sysParameters.addParam(param, value);
	}

	public void cleanSysParam() {
		if(sysParameters==null){
			sysParameters = new Params();
		}
		sysParameters.clear();
	}

	protected List<String> splitCase(String c){
		List<String> cases = new ArrayList<String>();
		String [] spices = c.split("\\s");
		for(String spice: spices){
			int pindex = spice.indexOf("=");
			if(pindex==-1){
				cases = append(cases, spice);
				continue;
			}
			
			String pname = spice.substring(0, pindex+1);
			spice = spice.substring(pindex+1, spice.length());
			String [] params = spice.split("#");
			List<String> cache = new ArrayList<String>();
			for(String param : params){
				String append = pname + param;
				List<String> l = append(cases, append);
				cache.addAll(l);
			}
			cases = cache;
		}
		return cases;
	}
	
	protected List<String> append(List<String> in, String append){
		List<String> out = new ArrayList<String>();
		if(in.isEmpty()){
			out.add(append);
			return out;
		}
		
		for(String c : in){
			String v = c+" "+ append;
			out.add(v);
		}
		
		return out;
	}
	
	protected void parseFilePath(Params parameters, String cases, String param){
		int i = cases.indexOf(".pl");
		if(i==-1){
			return ;
		}
		
		String filepath = cases.substring(0, i+3);
		parameters.addParam(param, filepath);
	}
	
	protected void internalParser(Params parameters, String cases, String param){
		String mark = param + "=";

		for(String c: cases.split("\\s")){
			if(c.startsWith(mark)){				
				String v = c.substring(mark.length());
				parameters.addParam(param, v);
				break;
			}
		}
	}
	
	protected void fillSysParameters(Params parameters){
		if(sysParameters ==null){
			return ;
		}
		
		List<String> params = sysParameters.getNames();
		for(String p:params){
			String v = sysParameters.getValue(p);
			parameters.addParam(p, v);
		}
	}
}
