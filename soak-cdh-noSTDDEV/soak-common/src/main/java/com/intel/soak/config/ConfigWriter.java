package com.intel.soak.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.intel.soak.utils.JAXBuddy;
import com.intel.soak.utils.PrettyPrinter;


public class ConfigWriter {
	
	public static void write(String file, Class<?> clz, Object config){
		try {
			FileOutputStream fos = new FileOutputStream(file);
			InputStream is = JAXBuddy.serializeXMLFile(clz, config);
		    String prettyOutput = PrettyPrinter.prettyPrint(is);
			fos.write(prettyOutput.getBytes());
			fos.close();
		}catch (Exception e) {
		}
	}
	
	public static void write(File file, Class<?> clz, Object config){
		try {
			FileOutputStream fos = new FileOutputStream(file);
			InputStream is = JAXBuddy.serializeXMLFile(clz, config);
		    String prettyOutput = PrettyPrinter.prettyPrint(is);
			fos.write(prettyOutput.getBytes());
			fos.close();
		}catch (Exception e) {
		}
	}
	
	public static InputStream toInputStream(Class<?> clz, Object config){
        try {
            InputStream is = JAXBuddy.serializeXMLFile(clz, config);
            String prettyOutput = PrettyPrinter.prettyPrint(is);
            return new ByteArrayInputStream(prettyOutput.getBytes());
        }catch (Exception e) {
        }
        return null;
    }
}