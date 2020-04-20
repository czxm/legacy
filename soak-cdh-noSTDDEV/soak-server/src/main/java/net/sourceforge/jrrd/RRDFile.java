/*
 * Copyright (C) 2001 Ciaran Treanor <ciaran@codeloop.com>
 *
 * Distributable under Apache Software License, Version 2.0.
 * See terms of license at http://www.apache.org/licenses/LICENSE-2.0.txt.
 *
 */
package net.sourceforge.jrrd;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is a quick hack to read information from an RRD file. Writing to
 * RRD files is not currently supported. As I said, this is a quick hack. Some
 * thought should be put into the overall design of the file IO.
 * <p>
 * Currently this can read RRD files that were generated on Solaris (Sparc) and
 * Linux (x86).
 * 
 */
public class RRDFile implements Constants {

	private final Log logger = LogFactory.getLog(RRDFile.class);
	boolean bigEndian;

	int alignment;
	/*
	 * was the file produced on a 64bits OS
	 */
	boolean f64 = false;

	RandomAccessFile ras;

	byte[] buffer;

	RRDFile(String name) throws IOException {
		this(new File(name), false);
	}

	RRDFile(String name, boolean b64) throws IOException {
		this(new File(name), b64);
	}

	RRDFile(File file) throws IOException {
		this(file, false);
	}

	RRDFile(File file, boolean b64) throws IOException {
		ras = new RandomAccessFile(file, "r");
		this.f64 = b64;
		buffer = new byte[128];
		initDataLayout(file);
	}

	private void initDataLayout(File file) throws IOException {

		if (file.exists()) { // Load the data formats from the file
			ras.read(buffer, 0, 24);

			int index;

			if ((index = indexOf(FLOAT_COOKIE_BIG_ENDIAN, buffer)) != -1) {
				bigEndian = true;
			} else if ((index = indexOf(FLOAT_COOKIE_LITTLE_ENDIAN, buffer)) != -1) {
				bigEndian = false;
			} else {
				throw new IOException("Invalid RRD file");
			}
			switch (index) {

			case 12:
				alignment = 4;
				break;

			case 16:
				alignment = 8;
				break;

			default:
				throw new RuntimeException("Unsupported architecture");
			}
		} else { // Default to data formats for this hardware architecture
		}

		ras.seek(0); // Reset file pointer to start of file
	}

	private int indexOf(byte[] pattern, byte[] array) {
		return new String(array).indexOf(new String(pattern));
	}

	boolean isBigEndian() {
		return bigEndian;
	}

	int getAlignment() {
		return alignment;
	}

	double readDouble() throws IOException {

		// LOGGER.debug("readDouble(): current pos <" + ras.getFilePointer() +
		// ">");
		double value;
		int prec = 8;// _64 ? 16 : 8;
		byte[] tx = new byte[prec];

		ras.read(buffer, 0, prec);

		if (bigEndian) {
			tx = buffer;
		} else {
			for (int i = 0; i < prec; i++) {
				tx[prec - 1 - i] = buffer[i];
			}
		}

		DataInputStream reverseDis = new DataInputStream(
				new ByteArrayInputStream(tx));

		return reverseDis.readDouble();
	}

	long readInt() throws IOException {
		return readInt(false);
	}

	long readInt(boolean dump) throws IOException {
		// LOGGER.debug("readInt(): current pos <" + ras.getFilePointer() +
		// ">");
		ras.read(buffer, 0, f64 ? 8 : 4);
		// for (int i = 0; i < (_64 ? 8 : 4); i++) {
		// LOGGER.debug("readInt(): " + i + " " + (0xFF & buffer[i]));
		// }
		long value;

		if (f64) {
			if (bigEndian) {
				value = 0xFF & buffer[7] | (0xFF & buffer[6]) << 8
						| (0xFF & buffer[5]) << 16 | (0xFF & buffer[4]) << 24
						| (0xFF & buffer[3]) << 32 | (0xFF & buffer[2]) << 40
						| (0xFF & buffer[1]) << 48 | (0xFF & buffer[0]) << 56;
			} else {
				value = 0xFF & buffer[0] | (0xFF & buffer[1]) << 8
						| (0xFF & buffer[2]) << 16 | (0xFF & buffer[3]) << 24
						| (0xFF & buffer[4]) << 32 | (0xFF & buffer[5]) << 40
						| (0xFF & buffer[6]) << 48 | (0xFF & buffer[7]) << 56;
			}
		} else {
			if (bigEndian) {
				value = 0xFF & buffer[3] | (0xFF & buffer[2]) << 8
						| (0xFF & buffer[1]) << 16 | (0xFF & buffer[0]) << 24;
			} else {
				value = 0xFF & buffer[0] | (0xFF & buffer[1]) << 8
						| (0xFF & buffer[2]) << 16 | (0xFF & buffer[3]) << 24;
			}
		}
		return value;
	}

	String readString(int maxLength) throws IOException {
		// LOGGER.debug("readString(): current pos <" + ras.getFilePointer() +
		// ">");
		ras.read(buffer, 0, maxLength);

		return new String(buffer, 0, maxLength).trim();
	}

	void skipBytes(int n) throws IOException {
		ras.skipBytes(n);
	}

	int align(int boundary) throws IOException {

		int skip = (int) (boundary - ras.getFilePointer() % boundary)
				% boundary;
		// LOGGER.debug("align(): skipping <" + skip + "> bytes [boundary=" +
		// boundary
		// + "][alignement=" + alignment + "][ras.getFilePointer()=" +
		// ras.getFilePointer()
		// + "]");
		if (skip != 0) {
			ras.skipBytes(skip);
		}

		return skip;
	}

	int align() throws IOException {
		return align(alignment);
	}

	long info() throws IOException {
		return ras.getFilePointer();
	}

	long getFilePointer() throws IOException {
		return ras.getFilePointer();
	}

	void close() throws IOException {
		ras.close();
	}

	public boolean is64bitsRRDFile() {
		return f64;
	}

	public void set64bitsTag(boolean b) {
		this.f64 = b;
	}
}
