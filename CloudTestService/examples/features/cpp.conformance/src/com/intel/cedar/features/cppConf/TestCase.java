package com.intel.cedar.features.cppConf;



import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;
import com.intel.cedar.service.client.feature.model.Variable;

public class TestCase extends DataModel {
	private String 			name;
	private String 			version;
	protected Variable 		caseVar;	
	
	public TestCase(IDataModelDocument document) {
		super(document);
		caseVar 	= new Variable();	
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setVersion(String version){
		this.version = version;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getVersion(){
		return this.version;
	}
	
	public Variable getCaseVar(){
		return this.caseVar;
	}
}
