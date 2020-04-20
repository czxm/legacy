package com.intel.xml.rss.util;

public class StringRoutine {
    /**
     * This method is different from String.replaceAll() method. The
     * String.replaceAll() method takes 2 parameters, both should be a REGEXP!
     * 
     * @param str
     * @param old
     * @param replacement
     * @return
     */
    public static String plainReplaceAll(String str, String old,
            String replacement) {
        int strLen = str.length();
        int oldLen = old.length();
        StringBuilder sb = new StringBuilder();
        int index;
        int p = 0;
        while (p < strLen && (index = str.indexOf(old, p)) >= 0) {
            sb.append(str.substring(p, index));
            sb.append(replacement);
            p = index + oldLen;
        }
        if (p < strLen) {
            sb.append(str.substring(p));
        }
        return sb.toString();
    }

    /**
     * Join the string array with sep
     * 
     * @param strarray
     *            the array to be joined
     * @param sep
     *            the separator
     * @return the joined string
     */
    public static String join(String[] strarray, String sep) {
        if (strarray == null || strarray.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String str : strarray) {
            sb.append(str);
            sb.append(sep);
        }
        if (strarray.length > 0) {
            sb.delete(sb.length() - sep.length(), sb.length());
        }
        return new String(sb);
    }

    public static void main(String[] args) {
        System.out.println(plainReplaceAll("%h/dd", "%h", "/home/bin"));
    }

}
