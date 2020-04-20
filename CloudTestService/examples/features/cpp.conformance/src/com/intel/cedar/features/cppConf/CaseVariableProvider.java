package com.intel.cedar.features.cppConf;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.engine.model.feature.IVarValueProvider;
import com.intel.cedar.feature.impl.FeatureJar;
import com.intel.cedar.service.client.feature.model.VarValue;


public class CaseVariableProvider implements IVarValueProvider {
	
	private static final String CASEFILE = "conf/testCase.xml";
	private static final List<VarValue>  EMPTY = new ArrayList<VarValue>();
	
	public CaseVariableProvider() {
	}

	@Override
	public List<VarValue> getVarValues(FeatureJar featureJar) throws Exception {
		List<VarValue> varValues = new ArrayList<VarValue>();
		if(featureJar==null){
			return EMPTY;
		}
		InputStream is = getCaseInput(featureJar);
		if(null==is){
			return EMPTY;
		}
		
		TestCaseLoader loader = new TestCaseLoader();
		TestCaseDoc doc = loader.load(is);
		
		return doc.getTestCaseData().getCaseVar().getVarValues();
	}

	protected InputStream getCaseInput(FeatureJar featureJar)throws Exception{
		return featureJar.getResourceStream(CASEFILE);
	}
	
	
}
