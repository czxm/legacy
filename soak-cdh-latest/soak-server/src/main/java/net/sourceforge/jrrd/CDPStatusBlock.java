/*
 * Copyright (C) 2001 Ciaran Treanor <ciaran.treanor-@T-gmail.com>
 *
 * Distributable under Apache Software License, Version 2.0.
 * See terms of license at http://www.apache.org/licenses/LICENSE-2.0.txt.
 *
 */
package net.sourceforge.jrrd;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Instances of this class model the consolidation data point status from an RRD
 * file.
 * 
 */
public class CDPStatusBlock {
	private final Log logger = LogFactory.getLog(CDPStatusBlock.class);
	long offset;
	long size;
	int unknownDatapoints;
	double value;

	CDPStatusBlock(RRDFile file, boolean b64) throws IOException {
		offset = file.getFilePointer();
		value = file.readDouble();
		unknownDatapoints = (int) file.readInt();

		// Skip rest of cdp_prep_t.scratch
		if (!b64) {
			file.skipBytes(68);
		} else {
			file.skipBytes(64);
		}

		size = file.getFilePointer() - offset;
	}

	/**
	 * Returns the number of unknown primary data points that were integrated.
	 * 
	 * @return the number of unknown primary data points that were integrated.
	 */
	public int getUnknownDatapoints() {
		return unknownDatapoints;
	}

	/**
	 * Returns the value of this consolidated data point.
	 * 
	 * @return the value of this consolidated data point.
	 */
	public double getValue() {
		return value;
	}

	void toXml(PrintStream s) {
		s.print("\t\t\t<ds><value> ");
		s.print(value);
		s.print(" </value>  <unknown_datapoints> ");
		s.print(unknownDatapoints);
		s.println(" </unknown_datapoints></ds>");
	}

	/**
	 * Returns a summary the contents of this CDP status block.
	 * 
	 * @return a summary of the information contained in the CDP status block.
	 */
	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer("[CDPStatusBlock: OFFSET=0x");

		sb.append(Long.toHexString(offset));
		sb.append(", SIZE=0x");
		sb.append(Long.toHexString(size));
		sb.append(", unknownDatapoints=");
		sb.append(unknownDatapoints);
		sb.append(", value=");
		sb.append(value);
		sb.append("]");

		return sb.toString();
	}
}
