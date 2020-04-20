package com.intel.ca360.loadmeter.util;

import java.io.FileOutputStream;

public class UserJSONGenerator {
	public static void main(String[] args) throws Exception{
		if(args.length != 4){
			System.out.println("Usage: user%d 1 100 jsonfile");
			System.exit(1);
		}
		
		String userPattern = args[0];
		int start = Integer.parseInt(args[1]);
		int count = Integer.parseInt(args[2]);
		String outfile = args[3];
		
		StringBuilder users = new StringBuilder();
		users.append("{\"users\":[\"");
		users.append(String.format(userPattern, start));
		users.append("\"");
		for(int i = start + 1; i <= count; i++){
			users.append(",\"");
			users.append(String.format(userPattern, i));
			users.append("\"");
		}
		users.append("]}");
		FileOutputStream os = new FileOutputStream(outfile);
		os.write(users.toString().getBytes());
		os.close();
	}
}

