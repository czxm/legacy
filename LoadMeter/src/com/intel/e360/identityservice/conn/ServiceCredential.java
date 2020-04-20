package com.intel.e360.identityservice.conn;

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the credential structure used by service connection.
 */
public class ServiceCredential  {
	/**
	 * This class represents the credential attribute structure.
	 */
	public class Attribute {
		private String _name;
		private String _type;
		private String[] _value;
		
		private Attribute(String name, String type, String[] value) {
			_name = name;
			_type = type;
			_value = value;
		}
		
		/**
		 * Method to get the credential attribute name.
		 * @return	the credential attribute name.
		 */
		public String getName() {
			return _name;
		}
		
		/**
		 * Method to get the credential attribute type.
		 * @return	the credential attribute type.
		 */
		public String getType() {
			return _type;
		}
		
		/**
		 * Method to get the credential attribute values.
		 * @return	the string array contains the credential attribute value.
		 */
		public String[] getValue() {
			return _value;
		}
	}
	
	private String _subject;
	
	private List<Attribute> _attrList;
	
	/**
	 * Constructor for ServiceCredential class.
	 */
	public ServiceCredential() {
		_attrList = new ArrayList<Attribute>();
	}
	
	/**
	 * Method to get the service credential subject.
	 * @return	the service credential subject.
	 */
	public String getSubject() {
		return _subject;
	}
	
	/**
	 * Method to set the service credential subject.
	 * @param subject	the service credential subject.
	 */
	public void setSubject(String subject) {
		_subject = subject;
	}
	
	/**
	 * Method to set the service credential attribute list.
	 * @return	the service credential attribute list.
	 */
	public List<Attribute> getAttributes() {
		return _attrList;
	}
	
	/**
	 * Method to add a new attribute into the credential attribute list.
	 * @param name	the credential attribute name. 
	 * @param type	the credential attribute type.
	 * @param value	the credential attribute value.
	 */
	public void addAttribute(String name, String type, String[] value) {
		Attribute attr = new Attribute(name, type, value);
		_attrList.add(attr);
	}
}
