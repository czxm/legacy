package com.intel.xml.rss.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.logging.Logger;

/**
 * Author Shen, Han
 */
public class Routine {

    /**
     * Read lines from the reader, take special handling of the "\" at the line
     * end
     * 
     * @param reader
     * @return
     */
    public static String readSpanningLine(BufferedReader reader)
            throws IOException {
        String line = reader.readLine();
        if (line == null || !line.endsWith("\\")) {
            return line;
        }
        StringBuffer strbuf = new StringBuffer(line.substring(0,
                line.length() - 1));
        line = reader.readLine();
        do {
            strbuf.append(line);
            if (line != null && line.endsWith("\\")) {
                strbuf.deleteCharAt(strbuf.length() - 1);
            } else {
                break;
            }
            line = reader.readLine();
        } while (true);

        return strbuf.toString();

    }

    public static String formatTime(long timeMillis) {
        int millis = (int) (timeMillis) % 1000;
        timeMillis /= 1000;
        int seconds = (int) (timeMillis) % 60;
        timeMillis /= 60;
        int minutes = (int) (timeMillis) % 60;
        timeMillis /= 60;
        int hours = (int) timeMillis;
        StringBuilder sb = new StringBuilder();
        sb.append(hours);
        sb.append(":");
        sb.append(minutes);
        sb.append(":");
        sb.append(seconds);
        sb.append(".");
        sb.append(millis);
        return new String(sb);
    }

    public static String getFileNameWithoutSuffix(String name) {
        int j = name.lastIndexOf(".");
        if (j > 0) {
            return name.substring(0, j);
        }
        return name;
    }

    public static String getFileNameOnly(String name) {
        int j = name.lastIndexOf('/');
        int j1 = name.lastIndexOf('\\');
        int t = (j > j1) ? j : j1;
        if (t < 0) {
            return name;
        }
        return name.substring(t + 1);
    }

    public static String getFileDirOnly(String name) {
        int j = name.lastIndexOf('/');
        int j1 = name.lastIndexOf('\\');
        int t = (j > j1) ? j : j1;
        if (t < 0) {
            return name;
        }
        return name.substring(0, t);
    }

    public static String replaceFileSuffix(String fileName, String newSuffix) {
        int j = fileName.lastIndexOf(".");
        int k0 = fileName.lastIndexOf("/");
        int k1 = fileName.lastIndexOf("\\");
        int k = k0 >= k1 ? k0 : k1;
        if (j > k) {
            return fileName.substring(0, j) + newSuffix;
        }
        return fileName;
    }

    public static String getFileNameSuffix(String name) {
        int j = name.lastIndexOf(".");
        int k0 = name.lastIndexOf("/");
        int k1 = name.lastIndexOf("\\");
        int k = k0 >= k1 ? k0 : k1;
        if (j > k) {
            return name.substring(j);
        }
        return null;
    }

    public static byte[] readFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        int fSize = (int) file.length();
        byte[] content = new byte[fSize];
        int offset = 0;
        do {
            int tt = fis.read(content, offset, fSize);
            fSize -= tt;
            offset += tt;
        } while (fSize > 0);
        fis.close();
        return content;
    }

    public static void writeFile(File file, byte[] content) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    public static boolean deleteTempFile(File file, Logger lg) {
        if (!file.delete()) {
            lg.warning("Failed to delete temp file " + file.getPath());
            return false;
        }
        return true;
    }

    public static String getStackTraceString(Exception e) {
        StringWriter strwriter = new StringWriter();
        PrintWriter writer = new PrintWriter(strwriter, true);
        e.printStackTrace(writer);
        return new String(strwriter.getBuffer());
    }

    public static String getExceptionLogInfo(String desc, Exception e) {
        StringBuilder sb = new StringBuilder(desc).append(": ").append(
                e.getClass().getName());
        sb.append(": ").append(e.getMessage()).append("\n");
        sb.append("Stack trace: ").append(getStackTraceString(e)).append("\n");
        return new String(sb);

    }

    public static String getExceptionLogInfo(Exception e) {
        return getExceptionLogInfo("", e);
    }

    public static String getRelativeNameTo(File parent, File child) {
        String pn = parent.getPath();
        String cn = child.getPath();
        assert (cn.startsWith(pn));
        return cn.substring(pn.length()
                + (pn.endsWith("/") || pn.endsWith("\\") ? 0 : 1));
    }

    public static String join(AbstractCollection<String> strings, String sep) {
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            sb.append(str);
            sb.append(sep);
        }
        if (strings.size() > 0) {
            sb.delete(sb.length() - sep.length(), sb.length());
        }
        return new String(sb);
    }

    public static String join(String[] strarray, String sep) {
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

    public static boolean isNewerThan(File fileA, File fileB) {
        if (!fileA.exists()) {
            return false;
        }
        if (!fileB.exists()) {
            return true;
        }
        long al = fileA.lastModified();
        long bl = fileB.lastModified();
        return al - bl > 1000;
    }

    /**
     * Current support only windows!
     * 
     * @param vname
     *            variable name to retrieve
     * @return the environment value, if any thing wrong, will return null
     */
    public static String getEnvironmentVariable(String vname) {
        SimpleCmdExecutor executor = new SimpleCmdExecutor();
        String cmd = "echo %" + vname + "%";
        try {
            String d = executor.execute(new String[] { "cmd.exe", "/C", cmd }).log;
            int j = d.indexOf(System.getProperty("line.separator"));
            if (j >= 0) {
                return d.substring(0, j);
            }
            return d;
        } catch (CmdExecutionException e) {
            return null;
        }
    }

    public static String findExecutableInPath(String executableName) {
        String pathString = getEnvironmentVariable("PATH");
        String[] pathArray = pathString.split(File.pathSeparator);
        for (String path : pathArray) {
            File file = new File(path, executableName);
            if (file.exists()) {
                return file.getPath();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        System.err.println(findExecutableInPath("iccmsa.exe"));
    }

    public static void copyFile(File sourceFile, File destFile)
            throws IOException {
        if (destFile.exists()) {
            destFile.delete();
        }
        InputStream ins = new FileInputStream(sourceFile);
        OutputStream ous = new FileOutputStream(destFile);
        byte[] buffer = new byte[512 * 1024];
        int read;
        while ((read = ins.read(buffer)) != -1) {
            ous.write(buffer, 0, read);
        }
        ous.close();
        ins.close();
    }

    public static String toLogString(String desc, Throwable t) {
        String l = System.getProperty("line.separator", "\n");
        StringWriter sw = new StringWriter();
        sw.write(desc);
        sw.write(l);
        t.printStackTrace(new PrintWriter(sw));
        return sw.getBuffer().toString();
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] mergeArray(T[] arr1, T[] arr2) {
        if (arr1 == null && arr2 == null) {
            return null;
        }
        if (arr1 == null) {
            return arr2;
        }
        if (arr2 == null) {
            return arr1;
        }
        int l1 = arr1.length;
        int l2 = arr2.length;
        T[] result = (T[]) Array.newInstance(
                arr1.getClass().getComponentType(), l1 + l2);

        int i = -1;
        for (T t : arr1) {
            result[++i] = t;
        }
        for (T t : arr2) {
            result[++i] = t;
        }
        return result;
    }

}
