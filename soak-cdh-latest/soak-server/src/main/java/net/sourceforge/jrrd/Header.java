/*
 * Copyright (C) 2001 Ciaran Treanor <ciaran.treanor-@T-gmail.com>
 *
 * Distributable under Apache Software License, Version 2.0.
 * See terms of license at http://www.apache.org/licenses/LICENSE-2.0.txt.
 *
 */
package net.sourceforge.jrrd;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Instances of this class model the header section of an RRD file.
 * 
 */
public class Header implements Constants {

	static final long offset = 0;

	private final Log logger = LogFactory.getLog(Header.class);

	long size;

	String version = UNDEFINED_VERSION;

	int iVersion = UNDEFINED_VERSION_AS_INT;

	int dsCount;

	int rraCount;

	int pdpStep;

	Header(RRDFile file) throws IOException {
		if (!file.readString(4).equals(COOKIE)) {
			throw new IOException("Invalid COOKIE");
		}
		version = file.readString(5);
		try {
			iVersion = Integer.parseInt(version);
		} catch (NumberFormatException e) {
			throw new IOException("Unsupported RRD version (" + version + ")");
		}
		if (iVersion > MAX_SUPPORTED_VERSION) {
			throw new IOException("Unsupported RRD version (" + version + ")");
		}

		file.align();

		// Consume the FLOAT_COOKIE
		file.readDouble();

		dsCount = (int) file.readInt();
		rraCount = (int) file.readInt();
		pdpStep = (int) file.readInt();

		file.align();
		file.skipBytes(80);

		size = file.getFilePointer() - offset;
	}

	/**
	 * Returns the version of the database.
	 * 
	 * @return the version of the database.
	 */
	public String getVersion() {
		return version;
	}

	public int getVersionAsInt() {
		return iVersion;
	}

	/**
	 * Returns the number of <code>DataSource</code>s in the database.
	 * 
	 * @return the number of <code>DataSource</code>s in the database.
	 */
	public int getDSCount() {
		return dsCount;
	}

	/**
	 * Returns the number of <code>Archive</code>s in the database.
	 * 
	 * @return the number of <code>Archive</code>s in the database.
	 */
	public int getRRACount() {
		return rraCount;
	}

	/**
	 * Returns the primary data point interval in seconds.
	 * 
	 * @return the primary data point interval in seconds.
	 */
	public int getPDPStep() {
		return pdpStep;
	}

	/**
	 * Returns an explicit description of the header
	 * 
	 * @return an explicit description of the header
	 */
	public String describe() {

		StringBuffer sb = new StringBuffer("[Header: OFFSET=0x00, SIZE=0x");

		sb.append(Long.toHexString(size));
		sb.append(", version=");
		sb.append(version);
		sb.append(", dsCount=");
		sb.append(dsCount);
		sb.append(", rraCount=");
		sb.append(rraCount);
		sb.append(", pdpStep=");
		sb.append(pdpStep);
		sb.append("]");

		return sb.toString();
	}

	/**
	 * Returns a summary of the contents of this header.
	 * 
	 * @return a summary of the information contained in this header.
	 */
	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer("[Header: ");
		sb.append(" version=");
		sb.append(version);
		sb.append(", dsCount=");
		sb.append(dsCount);
		sb.append(", rraCount=");
		sb.append(rraCount);
		sb.append(", pdpStep=");
		sb.append(pdpStep);
		sb.append("]");

		return sb.toString();
	}
}
