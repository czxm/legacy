package com.intel.cedar.features.cppConf;

import java.util.Iterator;

import com.intel.cedar.engine.model.DataModelDocument;
import com.intel.cedar.engine.model.IDataModel;
import com.intel.cedar.engine.util.MonoIterator;
import com.intel.cedar.service.client.feature.model.Variable;

public class TestCaseDoc extends DataModelDocument {
	private TestCase testcaseData;
	
	public TestCaseDoc() {
		testcaseData = new TestCase(this);
	}
	
	public TestCase getTestCaseData(){
		return this.testcaseData;
	}
	
	public Variable getTestCaseMap(){
		return testcaseData.getCaseVar();
	}
	
	public Iterator<IDataModel> iterate(){
		return new MonoIterator<IDataModel>(testcaseData);
	}
}
