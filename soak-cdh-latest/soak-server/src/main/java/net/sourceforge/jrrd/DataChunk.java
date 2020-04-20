/*
 * Copyright (C) 2001 Ciaran Treanor <ciaran.treanor-@T-gmail.com>
 *
 * Distributable under Apache Software License, Version 2.0.
 * See terms of license at http://www.apache.org/licenses/LICENSE-2.0.txt.
 *
 */
package net.sourceforge.jrrd;

import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Models a chunk of result data from an RRDatabase.
 * 
 */
public class DataChunk {

	private static final String NEWLINE = System.getProperty("line.separator");
	long startTime;
	int start;
	int end;
	long step;
	int dsCount;
	double[][] data;
	int rows;

	DataChunk(long startTime, int start, int end, long step, int dsCount,
			int rows) throws RRDException {
		this.startTime = startTime;
		this.start = start;
		this.end = end;
		this.step = step;
		this.dsCount = dsCount;
		this.rows = rows;
		if (rows <= 0 || dsCount <= 0 || step < 1) {
			throw new RRDException("Invalid chunk definition rows <" + rows
					+ ">, dsCount <" + dsCount + ">, step <" + step + ">");
		}
		data = new double[rows][dsCount];
	}

	/**
	 * Returns a summary of the contents of this data chunk. The first column is
	 * the time (RRD format) and the following columns are the data source
	 * values.
	 * 
	 * @return a summary of the contents of this data chunk.
	 */
	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer();
		long time = startTime;

		for (int row = 0; row < rows; row++, time += step) {
			sb.append(time);
			sb.append(": ");

			for (int ds = 0; ds < dsCount; ds++) {
				sb.append(data[row][ds]);
				sb.append(" ");
			}

			sb.append(NEWLINE);
		}

		return sb.toString();
	}

	/**
	 * Returns an array of <code>Hashtable</code>, each hashtable corresponding
	 * to a datasource.
	 * 
	 */
	public Hashtable[] toArrayOfHashtable() {

		Hashtable[] resultHtArray = new Hashtable[dsCount];

		for (int i = 0; i < dsCount; i++) {
			resultHtArray[i] = new Hashtable();
		}
		long time = startTime;

		for (int row = 0; row < rows; row++, time += step) {
			for (int ds = 0; ds < dsCount; ds++) {
				resultHtArray[ds].put(new Date(time * 1000), new Double(
						data[row][ds]));
			}
		}

		return resultHtArray;
	}

	/**
	 * 
	 * @return
	 */
	public Map[] toArrayOfMap() {
		Map[] resultMapArray = new LinkedHashMap[dsCount];
		for (int i = 0; i < dsCount; i++) {
			resultMapArray[i] = new LinkedHashMap();
		}
		long time = startTime;

		for (int row = 0; row < rows; row++, time += step) {
			for (int ds = 0; ds < dsCount; ds++) {
				resultMapArray[ds].put(new Date(time * 1000), new Double(
						data[row][ds]));
			}
		}
		return resultMapArray;
	}

	/**
	 * Return the data for the DataSource with id <code>dsId</code>
	 * 
	 * @param dsId
	 * @return
	 */
	public Map toMap(int dsId) {
		Map result = new LinkedHashMap();
		if (dsId < dsCount) {
			long time = startTime;
			for (int row = 0; row < rows; row++, time += step) {
				result.put(new Date(time * 1000), new Double(data[row][dsId]));
			}
		}
		return result;
	}

}
