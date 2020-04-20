package com.intel.e360.identityservice.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class Compression {
    public static byte[] compress(byte[] buffer) throws IOException {
        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
		deflaterOutputStream.write(buffer);
		deflaterOutputStream.close();
		deflater.end();
		return byteArrayOutputStream.toByteArray();
    }
    
    public static byte[] decompress(byte[] compressedByteBuffer) throws IOException {
        Inflater inflater = new Inflater(true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(byteArrayOutputStream, inflater);
        inflaterOutputStream.write(compressedByteBuffer);
        inflaterOutputStream.close();
        inflater.end();
       	return byteArrayOutputStream.toByteArray();
    }
}
