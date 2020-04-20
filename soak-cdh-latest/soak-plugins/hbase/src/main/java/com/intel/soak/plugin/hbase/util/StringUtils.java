package com.intel.soak.plugin.hbase.util;

/**
 * Created with IntelliJ IDEA.
 * User: xhao1
 * Date: 11/22/13
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class StringUtils {
    public static boolean isNullOrEmpty(String str) {
        if(str == null || str.isEmpty()){
            return true;
        }else{
            return false;
        }

    }
    public static String setAsDefaultValueIfEmpty(String key, String defaultValue) {
        if(isNullOrEmpty(key)){
            return defaultValue;
        }else{
            return key;
        }

    }
    public static String stringReplace(String str, String oldSubStr, String newSubStr) {

        String returnStr = "";

        int oldSubStrLen, newSubStrLen, strLen, startIndex,i;
        oldSubStrLen = oldSubStr.length();
        newSubStrLen = newSubStr.length();
        startIndex = 0;

        while ((i = str.indexOf(oldSubStr, startIndex)) > -1) {
            strLen = str.length();
            if (i == 0) {
                str = newSubStr + str.substring(oldSubStrLen, strLen);
                startIndex = i + newSubStrLen;
            } else {
                str = str.substring(0, i) + newSubStr + str.substring(i + oldSubStrLen, strLen);
            }
            startIndex = i + newSubStrLen;
        }
        returnStr = str;

        return returnStr;

    }

    public static void stringReplace(StringBuffer strBuf, String oldSubStr, String newSubStr) {

        int oldSubStrLen, newSubStrLen, fromIndex,i;
        oldSubStrLen = oldSubStr.length();
        newSubStrLen = newSubStr.length();
        fromIndex = 0;

        while ((i = strBuf.indexOf(oldSubStr, fromIndex)) > -1) {
            strBuf.replace(i, i+oldSubStrLen, newSubStr);
            fromIndex = i + newSubStrLen;
        }
    }

    public static void stringReplaceInAllFiles(String pathStr, String[] fileType,String oldStr, String newStr) {
        System.out.println("** start recursive **");
        File rootDir = new File(pathStr);
        if (!rootDir.exists()) {
            System.out.println("Directory " + pathStr + "doesn't exist");
            return;
        }
        if (!rootDir.isDirectory()) {
            System.out.println("File " + pathStr + " is not a directory");
            return;
        }
        String[] dirlist = rootDir.list();

        int i1 = 0;
        for(i1=0;i1<dirlist.length;i1++)
        {
            System.out.println("File list: "+dirlist[i1]);
        }
        for (i1 = 0; i1 < dirlist.length; i1++)
        {
            File chiFile = new File(pathStr + File.separator + dirlist[i1]);
            if (chiFile.isDirectory()) {
                stringReplaceInAllFiles(pathStr + File.separator + dirlist[i1] + File.separator, fileType, oldStr, newStr);
            } else {
                if (!chiFile.canRead()) {
                    continue;
                }
                int i2 = dirlist[i1].lastIndexOf('.');
                String suffix = "";
                if (i2 < 0){//no suffix
                    // do nothing
                } else {
                    suffix = dirlist[i1].substring(i2+1); // get suffix
                }

                if (!isInStrList(suffix, fileType)) {

                    continue;
                }

                StringBuffer bstr = new StringBuffer();
                String tempstr;
                try {
                    FileReader fr = new FileReader(chiFile);
                    BufferedReader br = new BufferedReader(fr);
                    while (true) {
                        tempstr = br.readLine();
                        if (tempstr == null)
                        {
                            break;
                        }
                        bstr.append(tempstr);
                    }
                    StringUtils.stringReplace(bstr,oldStr,newStr);
                    System.out.println(bstr);
                    fr.close();
                    br.close();
                    OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(chiFile));
                    osw.write(bstr.toString().toCharArray(),0,bstr.length());
                    osw.flush();
                    osw.close();
                } catch (FileNotFoundException e) {
                    System.out.println("File does not exist");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("File read error");
                    e.printStackTrace();
                }
            }
        }
        System.out.println("** stop recursive **");
    }

    public static boolean isInStrList(String str, String[] StrList) {
        boolean temp = false;
        int lenoflist = StrList.length;
        for (int i = 0; i < lenoflist; i++) {
            if (0 == str.compareToIgnoreCase(StrList[i])) {
                return true;
            }
        }
        return temp;
    }

    public static void main(String[] args)
    {
        String str = "FGABCDEFGHIGKLMNFGOPQFG";
        String str2 = StringUtils.stringReplace(str, "FG", "XXXX");
        System.out.println(str);
        System.out.println(str2);


        StringBuffer strBuf = new StringBuffer("FGABCDEFGHIGKLMNFGOPQFG");
        System.out.println(strBuf);
        StringUtils.stringReplace(strBuf, "FG", "XXXX");
        System.out.println(strBuf);

        System.out.println("________________________begin________________________");
        String[] fileType = { "xml", "txt"};
        StringUtils.stringReplaceInAllFiles("C:\\xhao1\\test", fileType, "FG", "XXXX");
        System.out.println("_________________________end_________________________");

    }
}
