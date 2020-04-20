/*
 * Copyright (C) 2001 Ciaran Treanor <ciaran.treanor-@T-gmail.com>
 *
 * Distributable under Apache Software License, Version 2.0.
 * See terms of license at http://www.apache.org/licenses/LICENSE-2.0.txt.
 *
 */
package net.sourceforge.jrrd;

/**
 * This exception may be throw if an error occurs while operating on an RRD
 * object.
 * 
 */
public class RRDException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6096979703241286756L;

	/**
	 * Constructs an RRDException with no detail message.
	 */
	public RRDException() {
		super();
	}

	/**
	 * Constructs an RRDException with the specified detail message.
	 * 
	 * @param reason
	 */
	public RRDException(String message) {
		super(message);
	}
}
