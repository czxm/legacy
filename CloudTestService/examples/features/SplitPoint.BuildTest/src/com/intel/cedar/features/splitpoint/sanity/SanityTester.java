package com.intel.cedar.features.splitpoint.sanity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.intel.cedar.features.splitpoint.sanity.driver.SimpleHttpDriver;

public class SanityTester {
	
	protected Document newDocument(InputStream ins) throws Exception{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		return docBuilder.parse(ins);
	}
	
	public boolean run(InputStream config) throws Exception{
		if(config == null){
			config = SanityTester.class.getResourceAsStream("sanity.xml");
		}
		List<ParamType> params = new ArrayList<ParamType>();
		Document doc = newDocument(config);
		NodeList eleList = doc.getElementsByTagName("Param");
		for(int i = 0; i < eleList.getLength(); i++){
			Node n = eleList.item(i);
			if(n instanceof Element){
				String name = ((Element)n).getAttribute("name");
				String value = ((Element)n).getTextContent();
				params.add(new ParamType(name, value));
			}
		}
		
		GenericDriver driver = new SimpleHttpDriver();
		driver.prepare(params);
		Transaction t = driver.createTransaction("CompositeHttp");
		t.setup();
		t.startup();
		t.beforeExecute();
		boolean ret = t.execute(false);
		t.afterExecute();
		t.shutdown();
		driver.shutDown();
		return ret;
	}
	
	public static void main(String[] args) throws Exception{
		new SanityTester().run(null);
	}
}
