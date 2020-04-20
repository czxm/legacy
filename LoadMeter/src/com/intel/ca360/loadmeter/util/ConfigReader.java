package com.intel.ca360.loadmeter.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class ConfigReader<T> {

	public T load(InputStream is, Class<T> t){
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] buf = new byte[4096];
			int len = 0;
			while((len = is.read(buf)) > 0){
				os.write(buf, 0, len);
			}
			ByteArrayInputStream ins = new ByteArrayInputStream(os.toByteArray());
			if(validate(ins)){
				ins = new ByteArrayInputStream(os.toByteArray());
				return JAXBuddy.deserializeXMLStream(t, ins);
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	public T load(String file, Class<T> t){
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return load(fis, t);
		} catch (Exception e) {
		} finally{
			if(fis != null)
				try{
					fis.close();
				}
				catch(Exception e){					
				}
		}
		return null;		
	}
	
	protected boolean validate(InputStream ins){
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Source schemaFile = new StreamSource(ConfigReader.class.getClassLoader().getResourceAsStream("config.xsd"));
			Schema schema = factory.newSchema(schemaFile);
			Validator validator = schema.newValidator();
		    validator.validate(new StreamSource(ins));
		    return true;
		} catch (Exception e) {
			return false;
		}
	}
}
