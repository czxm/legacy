package com.intel.ca360.loadmeter.transaction;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


public class LdapHelper {

    private static DirContext ctx;

    @SuppressWarnings(value = "unchecked")
   public static DirContext getCtx() {
//        if (ctx != null ) {
//            return ctx;
//        }
       String account = "Manager"; //binddn 
       String password = "pwd";    //bindpwd
       String root = "dc=scut,dc=edu,dc=cn"; // root
       Hashtable env = new Hashtable();
       env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
       env.put(Context.PROVIDER_URL, "ldap://localhost:389/" + root);
       env.put(Context.SECURITY_AUTHENTICATION, "simple");
       env.put(Context.SECURITY_PRINCIPAL, "cn="+account );
       env.put(Context.SECURITY_CREDENTIALS, password);
       try {
           ctx = new InitialDirContext(env);
       }
       catch (Exception e) {
           e.printStackTrace();
       }
       return ctx;
   }
   
   public static void closeCtx(){
       try {
           ctx.close();
       } catch (NamingException ex) {
           Logger.getLogger(LdapHelper.class.getName()).log(Level.SEVERE, null, ex);
       }
   }
   
   @SuppressWarnings(value = "unchecked")
   public static boolean verifySHA(String ldappw, String inputpw)
           throws NoSuchAlgorithmException {
       MessageDigest md = MessageDigest.getInstance("SHA-1");
       if (ldappw.startsWith("{SSHA}")) {
           ldappw = ldappw.substring(6);
       } else if (ldappw.startsWith("{SHA}")) {
           ldappw = ldappw.substring(5);
       }
       byte[] ldappwbyte = Base64.decode(ldappw);
       byte[] shacode;
       byte[] salt;
       if (ldappwbyte.length <= 20) {
           shacode = ldappwbyte;
           salt = new byte[0];
       } else {
           shacode = new byte[20];
           salt = new byte[ldappwbyte.length - 20];
           System.arraycopy(ldappwbyte, 0, shacode, 0, 20);
           System.arraycopy(ldappwbyte, 20, salt, 0, salt.length);
       }
       md.update(inputpw.getBytes());
       md.update(salt);
       byte[] inputpwbyte = md.digest();
       return MessageDigest.isEqual(shacode, inputpwbyte);
   }

    public static void main(String[] args) {
       getCtx();
   }
 
}