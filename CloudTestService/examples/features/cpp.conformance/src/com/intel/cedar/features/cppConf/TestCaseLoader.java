package com.intel.cedar.features.cppConf;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.engine.model.DataModelException;
import com.intel.cedar.engine.model.IDataModelDocument;
import com.intel.cedar.engine.model.loader.ModelLoader;
import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.StandardNames;
import com.intel.cedar.engine.xml.iterator.AxisIterator;
import com.intel.cedar.engine.xml.loader.DocumentLoader;
import com.intel.cedar.engine.xml.model.DocumentImpl;
import com.intel.cedar.engine.xml.model.Element;
import com.intel.cedar.service.client.feature.model.Variable;

public class TestCaseLoader extends ModelLoader {
	
	protected IDataModelDocument document;
	protected NamePool namePool;
	protected CaseParser parser;
	
	private static Logger LOG = LoggerFactory.getLogger(TestCaseLoader.class);
	
	public static void main(String args[]){
		System.out.println("TestCaseLoader Test end...");
		try{
			TestCaseLoader loader = new TestCaseLoader();
			loader.load("C:\\workspace\\cloud\\testCase.xml");
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("TestCaseLoader Test end...");
	}
	
	public TestCaseLoader(){
		parser = new CaseParser();
	}
	
	public TestCaseDoc load(String file) throws DataModelException {
		DocumentLoader loader=new DocumentLoader();
		DocumentImpl doc=loader.load(file);
		return load(doc);
	}
	
	public TestCaseDoc load(InputStream inputStream) throws DataModelException {
		DocumentLoader loader=new DocumentLoader();
		DocumentImpl doc=loader.load(inputStream);	
		return load(doc);
	}

	public TestCaseDoc load(DocumentImpl doc) throws DataModelException {
		LOG.info("load case begin...");
		
		if(doc == null ||
				doc.getDocumentElement() == null)
					throw new DataModelException("Unexpected error in document.");
		
		TestCaseDoc testCasedoc = new TestCaseDoc();
		Element rootNode = doc.getDocumentElement();
		
		//store the document for later use
		document = testCasedoc;
		namePool = rootNode.getNamePool();
		
		load(rootNode, testCasedoc.getTestCaseData());
		
		//call post load of the the document
		testCasedoc.onLoaded();
		
		LOG.info("load case successful...");
		return testCasedoc;
	}
	
	protected void load(Element rootNode, TestCase testcaseData)throws DataModelException{
		//name
		String name = getAttributeValue(rootNode, StandardNames.CEDAR_NAME, namePool);
		if(name!=null){
			testcaseData.setName(name);
		}
		
		//version
		String version = getAttributeValue(rootNode, StandardNames.CEDAR_VERSION, namePool);
		if(version!=null){
			testcaseData.setVersion(version);
		}
		
		
		AxisIterator matchIter = getElements(rootNode, StandardNames.CEDAR_TESTCASE, namePool);
		while(true) {
			Element matchNode = (Element) matchIter.next();
			if(matchNode==null){
				break;
			}
			
			load(matchNode, testcaseData.getCaseVar());
		}
	}
	
	protected void load(Element element)throws DataModelException{
		AxisIterator matchIter = getElements(element, StandardNames.CEDAR_PARAM, namePool);
		while(true) {
			Element matchNode = (Element) matchIter.next();
			if(matchNode==null){
				break;
			}
			
			String name = getAttributeValue(matchNode, StandardNames.CEDAR_NAME, namePool);
			String value = getAttributeValue(matchNode, StandardNames.CEDAR_VALUE, namePool);
			if(name==null || value==null){
				continue;
			}
			parser.addSysParam(name, value);
		}
	}
	
	protected void load(Element testcaseNode, Variable caseVar)throws DataModelException{
		parser.cleanSysParam();
		Element defaultParams = getElement(testcaseNode, StandardNames.CEDAR_PARAMS, namePool);
		load(defaultParams);
		
		//load cases
		Element casesElement = getElement(testcaseNode, StandardNames.CEDAR_CASES, namePool);
		String value = getTextContent(casesElement);
		value = value.trim();
		String[] cases = value.split("\\n");
		for(String c: cases){			
			caseVar.addVarValues(parser.parseCase(c));
		}
	}
}
