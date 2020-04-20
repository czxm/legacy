package com.intel.ca360.loadmeter.util;

import java.io.FileOutputStream;
import java.io.InputStream;

public class ADUserGenerator {

	public static void main(String[] args) {
		String userName = "user";
		int total = 10000;
		StringBuilder sb = new StringBuilder();
		for(int i = 1; i <= total; i++){
			String changeType = "add";
			String user = userName + i;
			String sname = user.toUpperCase();
			String gname = "ECA" + i;

			sb.append(String.format("dn: CN=%s,CN=Users,DC=acme,DC=com", user)).append("\n");
			sb.append(String.format("changetype: %s", changeType)).append("\n");
			sb.append(String.format("cn: %s", user)).append("\n");
			sb.append("objectClass: user").append("\n");
			sb.append(String.format("samAccountName: %s", user)).append("\n");
			sb.append(String.format("sn: %s", sname)).append("\n");
			sb.append(String.format("givenname: %s", gname)).append("\n");	
			sb.append(String.format("name: %s", user)).append("\n");
			sb.append(String.format("userPrincipalName: %s@acme.com", user)).append("\n");
			sb.append(String.format("mail: %s@acme.com", user)).append("\n");
			sb.append("\n");
		}
		try{
			FileOutputStream fos = new FileOutputStream("segment.ldif");
			fos.write(sb.toString().getBytes());
			fos.close();
			
			fos = new FileOutputStream("enableUsers.vbs");
			InputStream is = ADUserGenerator.class.getResourceAsStream("enableUsers.vbs");
			byte[] buf = new byte[1024];
			int len = 0;
			while((len = is.read(buf)) > 0){
				fos.write(buf, 0, len);
			}
			fos.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}