package com.intel.ca360.loadmeter.util;

import java.io.FileOutputStream;
import java.util.Random;

public class LdapUserGenerator {

	public static void main(String[] args) {
		String userName = "user";
		String password = "123456";
		int total = 10000;
		StringBuilder sb = new StringBuilder();
		for(int i = 1; i <= total; i++){
			String user = userName + i;
			String cname = "ECA" + i + " " + user;
			String sname = user.toUpperCase();
			String gname = "ECA" + i;
			String phone = Integer.toString(new Random().nextInt(40000000));
			String room = Integer.toString(new Random().nextInt(1000));			
			
			sb.append(String.format("dn: uid=%s, ou=People, dc=acme,dc=com", user)).append("\n");
			sb.append(String.format("cn: %s", cname)).append("\n");
			sb.append(String.format("sn: %s", sname)).append("\n");
			sb.append(String.format("givenname: %s", gname)).append("\n");
			sb.append("Objectclass: top").append("\n");
			sb.append("Objectclass: person").append("\n");
			sb.append("Objectclass: organizationalPerson").append("\n");
			sb.append("Objectclass: inetOrgPerson").append("\n");
			sb.append("ou: Accounting").append("\n");
			sb.append("ou: People").append("\n");			
			sb.append("l: Sunnyvale").append("\n");			
			sb.append(String.format("uid: %s", user)).append("\n");
			sb.append(String.format("mail: %s@acme.com", user)).append("\n");
			sb.append(String.format("telephonenumber: %s", phone)).append("\n");
			sb.append(String.format("facsimiletelephonenumber: %s", phone)).append("\n");
			sb.append(String.format("roomnumber: %s", room)).append("\n");
			sb.append(String.format("userpassword: %s", password)).append("\n");
			sb.append("\n");
		}
		try{
			FileOutputStream fos = new FileOutputStream("segment.ldif");
			fos.write(sb.toString().getBytes());
			fos.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}