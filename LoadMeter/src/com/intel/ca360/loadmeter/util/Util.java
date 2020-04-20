package com.intel.ca360.loadmeter.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private static Map<String, Pattern> patternCache = new ConcurrentHashMap<String, Pattern>(); 
	
	public static String getNextFileName(String name){
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		for(int i = 1; i < 1000 ;i++){
			sb.setLength(name.length());
			sb.append(".");
			sb.append(i);
			if(!new File(sb.toString()).exists())
				return sb.toString();					
		}
		return name + ".ERROR";
	}
	
	public static void sleep(int seconds){
		if(seconds > 0){
			try{
				Thread.sleep(seconds * 1000);
			}
			catch(Exception e){
			}
		}
	}
	
	public static void usleep(int mill){
		try{
			Thread.sleep(mill);
		}
		catch(Exception e){
		}
	}
	
	public static String stringRegexMatch(String regexPattern, String input) {
		Pattern cachedPattern = patternCache.get(regexPattern);
		if(cachedPattern == null){
			cachedPattern = Pattern.compile(regexPattern);
			patternCache.put(regexPattern, cachedPattern);
		}
		Matcher matcher = cachedPattern.matcher(input);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
