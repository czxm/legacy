/*
 * Copyright (C) 2001 Ciaran Treanor <ciaran.treanor-@T-gmail.com>
 *
 * Distributable under Apache Software License, Version 2.0.
 * See terms of license at http://www.apache.org/licenses/LICENSE-2.0.txt.
 *
 */
package net.sourceforge.jrrd;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Instances of this class model <a
 * href="http://people.ee.ethz.ch/~oetiker/webtools/rrdtool/">Round Robin
 * Database</a> (RRD) files.
 * 
 */
public class RRDatabase {

	RRDFile rrdFile;

	// RRD file name
	private String name;

	Header header;

	ArrayList dataSources;

	ArrayList archives;

	Date lastUpdate;

	boolean f64 = false;

	private final Log logger = LogFactory.getLog(RRDatabase.class);

	/**
	 * Creates a database to read from.
	 * 
	 * @param name
	 *            the filename of the file to read from.
	 * 
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 */
	public RRDatabase(String name) throws IOException {
		this(new File(name));
	}

	/**
	 * Creates a database to read from.
	 * 
	 * @param file
	 *            the file to read from.
	 * 
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 */
	public RRDatabase(File file) throws IOException {
		/*
		 * read the raw data according to the c-structure rrd_t (from rrd source
		 * distribution file rrd_format.h)
		 */
		name = file.getName();
		rrdFile = new RRDFile(file);
		header = new Header(rrdFile);

		// ok, let's have a look at the nb of rra found.
		// if it's 0, the rrd file provided is a good candidate to be a 64bits
		// files
		// there may be a safer way to discover the 64b mode but I don't know
		// it.
		if (header.getRRACount() == 0) {
			if (logger.isDebugEnabled()) {
				logger
						.debug("RRDatabase(): found rraCount=0, switching to 64bits mode");
			}
			rrdFile.set64bitsTag(true);
			this.f64 = true;
			// reset ras
			rrdFile.ras.seek(0);
			// read the header once more
			header = new Header(rrdFile);
			if (header.getRRACount() != 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("RRDatabase(): rraCount is <"
							+ header.rraCount + ">, good 64bits guess");
				}
			} else {
				// in this case, falling back to 32b mode won't help
				// but we can hope it will be safier (regarding possible
				// misreading)
				if (logger.isDebugEnabled()) {
					logger
							.debug("RRDatabase(): still found rraCount=0, switching back to 32bits mode");
				}
				rrdFile.set64bitsTag(false);
				this.f64 = false;
				header = new Header(rrdFile);
			}

		} else {
			// simply go on
			if (logger.isDebugEnabled()) {
				logger.debug("RRDatabase(): found rraCount=" + header.rraCount
						+ " at the first try");
			}
		}

		// Load the data sources
		dataSources = new ArrayList();
		for (int i = 0; i < header.dsCount; i++) {
			DataSource ds = new DataSource(rrdFile);
			dataSources.add(ds);
		}

		// Load the archives
		archives = new ArrayList();
		for (int i = 0; i < header.rraCount; i++) {
			Archive archive = new Archive(this);
			archives.add(archive);
		}

		rrdFile.align();

		int iLastUpdate = (int) rrdFile.readInt();
		lastUpdate = new Date((long) iLastUpdate * 1000);
		if (logger.isDebugEnabled()) {
			logger.debug("RRDatabase(): lastUpdate:" + iLastUpdate + ":"
					+ lastUpdate);
		}
		//
		if (header.getVersionAsInt() >= Constants.VERSION_WITH_LAST_UPDATE_SEC) {
			rrdFile.readInt();
			rrdFile.align();
		}

		// Load PDPStatus(s)
		for (int i = 0; i < header.dsCount; i++) {
			DataSource ds = (DataSource) dataSources.get(i);
			ds.loadPDPStatusBlock(rrdFile);
		}

		// Load CDPStatus(s)
		for (int i = 0; i < header.rraCount; i++) {
			Archive archive = (Archive) archives.get(i);
			archive.loadCDPStatusBlocks(rrdFile, header.dsCount);
		}

		// Load current row information for each archive
		for (int i = 0; i < header.rraCount; i++) {
			Archive archive = (Archive) archives.get(i);
			archive.loadCurrentRow(rrdFile);
			archive.computeStartAndEnd(lastUpdate.getTime());
		}

		// Now load the data
		for (int i = 0; i < header.rraCount; i++) {
			Archive archive = (Archive) archives.get(i);
			archive.loadData(rrdFile, header.dsCount);
		}
	}

	/**
	 * Returns the <code>Header</code> for this database.
	 * 
	 * @return the <code>Header</code> for this database.
	 */
	public Header getHeader() {
		return header;
	}

	/**
	 * Returns the date this database was last updated. To convert this date to
	 * the form returned by <code>rrdtool last</code> call Date.getTime() and
	 * divide the result by 1000.
	 * 
	 * @return the date this database was last updated.
	 */
	public Date getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * Returns the <code>DataSource</code> at the specified position in this
	 * database.
	 * 
	 * @param index
	 *            index of <code>DataSource</code> to return.
	 * 
	 * @return the <code>DataSource</code> at the specified position in this
	 *         database
	 */
	public DataSource getDataSource(int index) {
		return (DataSource) dataSources.get(index);
	}

	/**
	 * Returns an iterator over the data sources in this database in proper
	 * sequence.
	 * 
	 * @return an iterator over the data sources in this database in proper
	 *         sequence.
	 */
	public Iterator getDataSources() {
		return dataSources.iterator();
	}

	/**
	 * Returns the <code>Archive</code> at the specified position in this
	 * database.
	 * 
	 * @param index
	 *            index of <code>Archive</code> to return.
	 * 
	 * @return the <code>Archive</code> at the specified position in this
	 *         database.
	 */
	public Archive getArchive(int index) {
		return (Archive) archives.get(index);
	}

	/**
	 * Returns an iterator over the archives in this database in proper
	 * sequence.
	 * 
	 * @return an iterator over the archives in this database in proper
	 *         sequence.
	 */
	public Iterator getArchives() {
		return archives.iterator();
	}

	/**
	 * Returns the number of archives in this database.
	 * 
	 * @return the number of archives in this database.
	 */
	public int getNumArchives() {
		return header.rraCount;
	}

	/**
	 * Returns an iterator over the archives in this database of the given type
	 * in proper sequence.
	 * 
	 * @param type
	 *            the consolidation function that should have been applied to
	 *            the data.
	 * 
	 * @return an iterator over the archives in this database of the given type
	 *         in proper sequence.
	 */
	public Iterator getArchives(ConsolidationFunctionType type) {
		return getArchiveList(type).iterator();
	}

	ArrayList getArchiveList(ConsolidationFunctionType type) {

		ArrayList subset = new ArrayList();
		for (int i = 0; i < archives.size(); i++) {
			Archive archive = (Archive) archives.get(i);
			if (archive.getType().equals(type)) {
				subset.add(archive);
			}
		}

		return subset;
	}

	/**
	 * Closes this database stream and releases any associated system resources.
	 * 
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 */
	public void close() throws IOException {
		rrdFile.close();
	}

	/**
	 * Outputs the header information of the database to the given print stream
	 * using the default number format. The default format for
	 * <code>double</code> is 0.0000000000E0.
	 * 
	 * @param s
	 *            the PrintStream to print the header information to.
	 */
	public void printInfo(PrintStream s) {

		NumberFormat numberFormat = new DecimalFormat("0.0000000000E0");

		printInfo(s, numberFormat);
	}

	/**
	 * Returns data from the database corresponding to the given consolidation
	 * function and a step size of 1.
	 * 
	 * @param type
	 *            the consolidation function that should have been applied to
	 *            the data.
	 * 
	 * @return the raw data.
	 * 
	 * @throws RRDException
	 *             if there was a problem locating a data archive with the
	 *             requested consolidation function.
	 * @throws java.io.IOException
	 *             if there was a problem reading data from the database.
	 */
	public DataChunk getData(ConsolidationFunctionType type)
			throws RRDException, IOException {
		return getData(type, 1L);
	}

	/**
	 * 
	 * @param type
	 *            the consolidation function that should have been applied to
	 *            the data.
	 * @param dateStart
	 *            the start date for the data we want to retrieve
	 * @param dateEnd
	 *            the end date for the data we want to retrieve
	 * @param step
	 *            the step
	 * @return the raw data as a <code>DataChunk</code>
	 * @throws RRDException
	 * @throws java.io.IOException
	 */
	public DataChunk getData(ConsolidationFunctionType type, Date dateStart,
			Date dateEnd, long step) throws RRDException, IOException {
		long start = dateStart.getTime() / 1000;
		long end = dateEnd.getTime() / 1000;
		return getData(type, start, end, step);
	}

	/**
	 * 
	 * @param type
	 *            the consolidation function that should have been applied to
	 *            the data
	 * @param start
	 *            the start date for the data we want to retrieve as a long (in
	 *            second format, i.e. same as Date.getTime()/1000)
	 * @param end
	 *            the end date for the data we want to retrieve as a long (in
	 *            second format, i.e. same as Date.getTime()/1000)
	 * @param step
	 *            the step
	 * @return the raw data as a <code>DataChunk</code>
	 * @throws RRDException
	 * @throws java.io.IOException
	 */
	public DataChunk getData(final ConsolidationFunctionType type,
			final long start, final long end, final long step)
			throws RRDException, IOException {

		long aStart = start;
		long aEnd = end;
		long aStep = step;

		ArrayList possibleArchives = getArchiveList(type);

		if (possibleArchives.size() == 0) {
			throw new RRDException(
					"RRDatabase <"
							+ this.name
							+ "> does not contain an Archive of consolidation function type <"
							+ type + ">");
		}
		//
		// test if we're not trying to read something outside of the data
		// available
		//
		long lastUpdateLong = lastUpdate.getTime() / 1000;
		if (aEnd > lastUpdateLong) {
			if (logger.isDebugEnabled()) {
				logger.debug("getData(): overriding given end from <" + aEnd
						+ ":" + new Date(aEnd * 1000) + "> to lastUpdate <"
						+ lastUpdateLong + ":"
						+ new Date(lastUpdateLong * 1000) + ">");
			}
			aEnd = lastUpdateLong;
		}
		Archive archive = findBestArchive(aStart, aEnd, aStep, possibleArchives);

		// Tune the parameters now that we know what will be the real step
		aStep = header.pdpStep * archive.pdpCount;
		aStart -= aStart % aStep;

		if (aEnd % aStep != 0) {
			aEnd += aStep - aEnd % aStep;
		}

		DataChunk chunk = null;
		int rows = (int) ((aEnd - aStart) / aStep + 1);
		if (rows < 0) {
			rows = 0;
			logger
					.warn("getData(): no data matching the request, end BEFORE start");
			throw new RRDException("No archive matching the request <" + start
					+ ":" + new Date(start * 1000) + "," + end + ":"
					+ new Date(end * 1000) + "," + step + "," + type
					+ "> in RRDatabase <" + this.name + ">");
		} else {

			// Find start and end offsets
			// TODO: This is terrible - some of this should be encapsulated in
			// Archive - CT.

			long archiveEndTime = lastUpdateLong - lastUpdateLong % aStep;
			long archiveStartTime = archiveEndTime - aStep
					* (archive.rowCount - 1);
			int startOffset = (int) ((aStart - archiveStartTime) / aStep);
			int endOffset = (int) ((archiveEndTime - aEnd) / aStep);

			if (logger.isDebugEnabled()) {
				StringBuffer debug = new StringBuffer();
				debug.append("getData(): archiveEndTime:").append(
						archiveEndTime);
				debug.append(", archiveStartTime:").append(archiveStartTime);
				debug.append(", start:").append(aStart);
				debug.append(", end:").append(aEnd);
				debug.append(", startOffset:").append(startOffset);
				debug.append(", endOffset:").append(endOffset);
				debug.append(", step (computed):").append(aStep);
				debug.append(", dsCount:").append(header.dsCount);
				debug.append(", rows:").append(rows);
				logger.debug(debug.toString());
			}
			chunk = new DataChunk(aStart, startOffset, endOffset, aStep,
					header.dsCount, rows);
			archive.loadData(chunk);
		}

		return chunk;
	}

	/**
	 * Returns data from the database corresponding to the given consolidation
	 * function. Default request is "last 24 hours".
	 * 
	 * @param type
	 *            the consolidation function that should have been applied to
	 *            the data.
	 * @param step
	 *            the step size to use.
	 * 
	 * @return the raw data.
	 * 
	 * @throws RRDException
	 *             if there was a problem locating a data archive with the
	 *             requested consolidation function.
	 * @throws java.io.IOException
	 *             if there was a problem reading data from the database.
	 */
	public DataChunk getData(ConsolidationFunctionType type, long step)
			throws RRDException, IOException {

		Calendar endCal = Calendar.getInstance();

		endCal.set(Calendar.MILLISECOND, 0);

		Calendar startCal = (Calendar) endCal.clone();

		startCal.add(Calendar.DATE, -1);

		long end = endCal.getTime().getTime() / 1000;
		long start = startCal.getTime().getTime() / 1000;

		return getData(type, start, end, step);
	}

	/*
	 * This is almost a verbatim copy of the original C code by Tobias Oetiker.
	 * I need to put more of a Java style on it - CT
	 */
	private Archive findBestArchive(long start, long end, long step,
			ArrayList archives) {

		Archive archive = null;
		Archive bestFullArchive = null;
		Archive bestPartialArchive = null;
		int firstPart = 1;
		int firstFull = 1;
		long bestMatch = 0;
		long bestPartRRA = 0;
		long bestStepDiff = 0;
		long tmpStepDiff = 0;

		for (int i = 0; i < archives.size(); i++) {
			archive = (Archive) archives.get(i);

			long calEnd = archive.getEnd();
			long calStart = archive.getStart();
			long fullMatch = end - start;

			if (calEnd >= end && calStart < start) { // Best full match
				tmpStepDiff = Math
						.abs(step - header.pdpStep * archive.pdpCount);

				if (firstFull != 0 || tmpStepDiff < bestStepDiff) {
					firstFull = 0;
					bestStepDiff = tmpStepDiff;
					bestFullArchive = archive;
				}
			} else { // Best partial match
				long tmpMatch = fullMatch;

				if (calStart > start) {
					tmpMatch -= calStart - start;
				}

				if (calEnd < end) {
					tmpMatch -= end - calEnd;
				}

				if (firstPart != 0 || bestMatch < tmpMatch) {
					firstPart = 0;
					bestMatch = tmpMatch;
					bestPartialArchive = archive;
				}
			}
		}

		// See how the matching went
		// TODO: optimise this
		if (firstFull == 0) {
			archive = bestFullArchive;
		} else if (firstPart == 0) {
			archive = bestPartialArchive;
		} else {
			// returning last archive tested :D
		}

		return archive;
	}

	/**
	 * Returns the id of the DataSource matching the name <code>dsName</code>.
	 * 
	 * @param dsName
	 *            the name of the DataSource
	 * @return the id of the DataSource matching the name <code>dsName</code>
	 * @throws RRDException
	 *             thrown if the provided dsName is null or empty or if no
	 *             DataSource can be found matching the provided name
	 */
	public int getDataSourceId(String dsName) throws RRDException {
		int resultId = -1;
		if (dsName != null && !"".equals(dsName)) {
			for (int i = 0; i < header.getDSCount(); i++) {
				if (dsName.equals(((DataSource) dataSources.get(i)).getName())) {
					resultId = i;
					break;
				}
			}
			if (resultId == -1) {
				throw new RRDException("No such DataSource <" + dsName
						+ "> in RRDatabase <" + this.name + ">");
			}
		} else {
			throw new RRDException("Invalid DataSource name <" + dsName + ">");
		}
		return resultId;
	}

	/**
	 * Outputs the header information of the database to the given print stream
	 * using the given number format. The format is almost identical to that
	 * produced by <a href=
	 * "http://people.ee.ethz.ch/~oetiker/webtools/rrdtool/manual/rrdinfo.html"
	 * >rrdtool info</a>
	 * 
	 * @param s
	 *            the PrintStream to print the header information to.
	 * @param numberFormat
	 *            the format to print <code>double</code>s as.
	 */
	public void printInfo(PrintStream s, NumberFormat numberFormat) {

		s.print("filename = \"");
		s.print(name);
		s.println("\"");
		s.print("rrd_version = \"");
		s.print(header.version);
		s.println("\"");
		s.print("step = ");
		s.println(header.pdpStep);
		s.print("last_update = ");
		s.println(lastUpdate.getTime() / 1000);

		for (Iterator i = dataSources.iterator(); i.hasNext();) {
			DataSource ds = (DataSource) i.next();

			ds.printInfo(s, numberFormat);
		}

		int index = 0;

		for (Iterator i = archives.iterator(); i.hasNext();) {
			Archive archive = (Archive) i.next();

			archive.printInfo(s, numberFormat, index++);
		}
	}

	/**
	 * Outputs the content of the database to the given print stream as a stream
	 * of XML. The XML format is almost identical to that produced by <a href=
	 * "http://people.ee.ethz.ch/~oetiker/webtools/rrdtool/manual/rrddump.html"
	 * >rrdtool dump</a>
	 * 
	 * @param s
	 *            the PrintStream to send the XML to.
	 */
	public void toXml(PrintStream s) {

		s.println("<!--");
		s.println("  -- Round Robin RRDatabase Dump ");
		s.println("  -- Generated by jRRD (http://jrrd.sourceforge.net)");
		s.println("  -->");
		s.println("<rrd>");
		s.print("\t<version> ");
		s.print(header.version);
		s.println(" </version>");
		s.print("\t<step> ");
		s.print(header.pdpStep);
		s.println(" </step> <!-- Seconds -->");
		s.print("\t<lastupdate> ");
		s.print(lastUpdate.getTime() / 1000);
		s.print(" </lastupdate> <!-- ");
		s.print(lastUpdate.toString());
		s.println(" -->");
		s.println();

		for (int i = 0; i < header.dsCount; i++) {
			DataSource ds = (DataSource) dataSources.get(i);

			ds.toXml(s);
		}

		s.println("<!-- Round Robin Archives -->");

		for (int i = 0; i < header.rraCount; i++) {
			Archive archive = (Archive) archives.get(i);

			archive.toXml(s);
		}

		s.println("</rrd>");
	}

	/**
	 * Returns true if the source rrd file has been recognizes as a 64bits file
	 * 
	 * @return true if the source rrd file has been recognizes as a 64bits file
	 */
	public boolean is64bitsRRDDatabase() {
		return f64;
	}

	/**
	 * Returns true if the source rrd file has been recognizes as a big endian
	 * storage file
	 * 
	 * @return true if the source rrd file has been recognizes as a big endian
	 *         storage file
	 */
	public boolean isBigEndian() {
		return this.rrdFile.isBigEndian();
	}

	/**
	 * Returns the version (in the RRD context) of the source rrd file
	 * 
	 * @return the version (in the RRD context) of the source rrd file
	 */
	public int getVersion() {
		return this.getHeader().getVersionAsInt();
	}

	/**
	 * Returns a string shortly describing the current RRDatabase object
	 * 
	 * @return a string shortly describing the current RRDatabase object
	 * 
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("RRDatabase [");
		sb.append(name);
		sb.append(", ").append(dataSources.size()).append(" ds");
		sb.append(", ").append(archives.size()).append(" rra");
		sb.append(", lastUpdate: ").append(lastUpdate);
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Returns a summary of the contents of this database.
	 * 
	 * @return a summary of the information contained in this database.
	 */
	public String describe() {

		StringBuffer sb = new StringBuffer("\nRRDatabase file [");
		sb.append(name).append("] arch: ").append(f64 ? "64" : "32").append(
				" bits");
		sb.append(", bigEndian: ").append(rrdFile.isBigEndian());
		sb.append(", lastUpdate: ").append(lastUpdate);
		sb.append("\n");
		sb.append(header.toString());

		for (Iterator i = dataSources.iterator(); i.hasNext();) {
			DataSource ds = (DataSource) i.next();

			sb.append("\n\t");
			sb.append(ds.toString());
		}

		for (Iterator i = archives.iterator(); i.hasNext();) {
			Archive archive = (Archive) i.next();

			sb.append("\n\t");
			sb.append(archive.toString());
		}

		return sb.toString();
	}

	/**
	 * Returns a verbose summary of the contents of this database.
	 * 
	 * @return a verbose summary of the information contained in this database.
	 */
	public String verboseDescribe() {

		StringBuffer sb = new StringBuffer("\nRRDatabase file [");
		sb.append(name).append("] arch: ").append(f64 ? "64" : "32").append(
				" bits");
		sb.append(", bigEndian: ").append(rrdFile.isBigEndian());
		sb.append(", lastUpdate: ").append(lastUpdate);
		sb.append("\n");
		sb.append(header.describe());

		for (Iterator i = dataSources.iterator(); i.hasNext();) {
			DataSource ds = (DataSource) i.next();

			sb.append("\n\t");
			sb.append(ds.describe());
		}

		for (Iterator i = archives.iterator(); i.hasNext();) {
			Archive archive = (Archive) i.next();

			sb.append("\n\t");
			sb.append(archive.describe());
		}

		return sb.toString();
	}

}
