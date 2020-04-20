package com.intel.e360.identityservice.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DocBuilderPool {

	private static Map<String,DocumentBuilder> threadData = new ConcurrentHashMap<String,DocumentBuilder>();
    public static DocumentBuilder getDocumentBuilder(String threadId) {
                if (threadData.get(threadId) == null) {
                        DocumentBuilderFactory _factory = DocumentBuilderFactory.newInstance ();
                        _factory.setNamespaceAware (true);
                        try {
                                DocumentBuilder _builder = _factory.newDocumentBuilder();
                                threadData.put(threadId, _builder);
                        } catch (Exception e) {
                                System.out.println(e.getMessage());
                        }
                }
                return (threadData.get(threadId));
        }

}
