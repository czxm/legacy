package com.intel.soak.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXParseException;

import com.intel.soak.utils.JAXBuddy;

public class ConfigReader<T> {
    protected static Log LOG = LogFactory.getLog(ConfigReader.class);
    
	public T load(InputStream is, Class<T> t) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int len = 0;
			while ((len = is.read(buffer)) > 0) {
				os.write(buffer, 0, len);
			}
			ByteArrayInputStream ins = new ByteArrayInputStream(os.toByteArray());
			if (validate(ins)) {
				ins = new ByteArrayInputStream(os.toByteArray());
				return JAXBuddy.deserializeXMLStream(t, ins);
			}
		} catch (ClassCastException e){
		    LOG.debug(e.getMessage(), e);
		    return null;
		} catch (Throwable e) {
			LOG.error(e.getMessage());
		}
		return null;
	}

	public T load(String file, Class<T> t) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return load(fis, t);
		} catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
		            LOG.error(e.getMessage());
				}
			}
		}
		return null;
	}
	
	protected boolean validate(InputStream ins) {
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            ClassPathResource configRes = new ClassPathResource("config.xsd",
                    ConfigReader.class.getClassLoader());
			Source schemaFile = new StreamSource(configRes.getInputStream());
			Schema schema = factory.newSchema(schemaFile);
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(ins));
			return true;
		} catch (SAXParseException e) {
            LOG.error(e.getMessage());
			return false;
		} catch (Throwable e){
		    LOG.warn(e.getMessage());
		    return false;
		}		
	}

}
