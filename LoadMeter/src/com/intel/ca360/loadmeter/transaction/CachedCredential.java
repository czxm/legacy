package com.intel.ca360.loadmeter.transaction;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.intel.splat.identityservice.utils.PasswordUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.binary.BinaryStreamReader;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

public class CachedCredential {
	private String id;
	private String domainId;
	private String subjectId;
	
	private String userName = "";
	private String password = "";
	private Map<String, String> properties = null;
	
	private Timestamp updated;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getDomainId() {
		return domainId;
	}
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}
	
	public String getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	public Timestamp getUpdated() {
		return updated;
	}
	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}
	
	public void clearProperties() {
		if(properties == null)
			return;
		
		properties.clear();
	}
	
	public void addProperty(String name, String value) {
		if(properties == null)
			properties = new HashMap<String, String>();
		
		properties.put(name,  value);
	}
	
	public String getProperty(String name) {
		if(properties == null)
			return null;
		
		return properties.get(name);
	}
	
    
    public boolean isValid() {
        if ((userName == null || userName.isEmpty()) &&
            (password == null || password.isEmpty()) &&
            (properties == null)) {
            return false;
        }
        return true;
    }
	
	protected String getPasswordEncrypted() {
		if(password == null)
			return null;
		
		//encode password
		try {
			return PasswordUtil.encode(password);
		} catch(Exception e) {
			//empty string if error
			return "";
		}
	}
	
	protected void setPasswordEncrypted(String encryptedPassword) {
		if (encryptedPassword == null || 
				encryptedPassword.length() == 0)
			return;
		
		//decode password
		try {
			password = PasswordUtil.decode(encryptedPassword);
		} catch(Exception e) {
			//empty string if error
			password = "";
		}
	}
	
	protected String getPropertiesAsString(){
		if(properties == null)
			return null;
		else
			return new XMLStreamizer<Map<String, String>>().serialize(properties);
	}
	
	protected void setPropertiesAsString(String value){
		if(value == null)
			properties = new HashMap<String, String>();
		else
			properties = new XMLStreamizer<Map<String, String>>().deserialize(value);
	}
	
	protected static String generateId() {
		return UUID.randomUUID().toString();
	}
	
	public static class XMLStreamizer<T> {
		private XStream xs = null;

		public XMLStreamizer() {
			xs = new XStream(new DomDriver());
		}
		
		public String serialize(T model) {
			return serialize(model, false);
		}

		public String serialize(T model, boolean pretty) {
			if(pretty) {
				return xs.toXML(model);
			} else {
				StringWriter sw = new StringWriter(); 
				xs.marshal(model,  new CompactWriter(sw)); 
				return sw.toString(); 
			}
		}
		
		public void serialize(T model, OutputStream output) { 
			xs.toXML(model,  output); 
		}

		@SuppressWarnings("unchecked")
		public T deserialize(String xml) {
			return (T) xs.fromXML(xml);
		}
		
		@SuppressWarnings("unchecked")
		public T deserialize(InputStream input) {
			return (T) xs.fromXML(input);
		}
		
		public void alias(String name, Class<?> type) {
			xs.alias(name, type);
		}

	}

}
