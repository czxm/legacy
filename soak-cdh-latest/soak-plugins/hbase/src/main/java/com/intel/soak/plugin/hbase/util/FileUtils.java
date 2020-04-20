package com.intel.soak.plugin.hbase.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtils {
    /**
     * Copy all available input to the given OutputStream.
     * The caller is responsible for closing the streams.
     * This method will not close the input or output streams.
     * @param input
     * @param outputStream
     * @throws IOException
     */
    public static void copyStream(InputStream input, OutputStream outputStream) throws IOException {
        byte[] array = new byte[1024];
        int ch;
        while ((ch = input.read(array)) >= 0){
            outputStream.write(array, 0, ch);
        }
    }

    /**
     * Close the stream, ignoring any exceptions when doing so (which
     * callers usually can do nothing about).  Skips if the stream is null.
     * @param outputStream
     */
    public static void closeOutputStream(OutputStream outputStream) {
        closeStream(outputStream);
    }

    public static void closeStream(Closeable closeableStream) {
        try {
            if (closeableStream != null) {
                closeableStream.close();
            }
        } catch (IOException e) {
            // Ignore
        }
    }

    public static void deleteFolderContents(File folder) {
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                if (!file.isDirectory()) {
                    file.delete();
                } else {
                    deleteFolderContents(file);
                    file.delete();
                }
            }
        }
    }

    public static void deleteFolderAndContents(File folder) {
        deleteFolderContents(folder);
        folder.delete();
    }

    public static File createFolder(File parentFolder, String newFolderName) {
        File newFolder = new File(parentFolder, newFolderName);
        FileUtils.ensureFolderExists(newFolder);
        return newFolder;
    }

    public static boolean ensureFolderExists(File dir) {
        if (!dir.exists()) {
            ensureFolderExists(dir.getParentFile());
            return dir.mkdir();
        } else if (dir.isFile()) {
            dir.delete();
            return dir.mkdir();
        } else {
            return true;
        }
    }

    public static File createTempFolder(String prefix, String suffix)
            throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        return createTempFolderInFolder(tempDir, prefix, suffix);
    }

    public static File createTempFolderInFolder(File tempDir, String prefix, String suffix)
            throws IOException {
        // Java doesn't allow you to create a temporary directory, only files.
        // Create a file, then delete it and use that name to create a directory
        // There is a tiny chance that someone else creates a file/directory
        // with the same name during the small window of time after the temporary
        // file is created.  Try a few times to make this chance even smaller,
        // then give up.
        for (int i = 0; i < 10; i++) {
            File tempFile = File.createTempFile(prefix, suffix, tempDir);
            File newFolder = createFolder(tempDir, tempFile.getName());
            if (newFolder.exists() && newFolder.isDirectory()) {
                return newFolder;
            }
        }
        throw new IOException("Unable to create temporary directory");
    }

    /**
     * Unzips the given stream to the specified folder.
     * The stream is closed on completion.
     * @param inputStream
     * @param folder
     * @throws IOException
     */
    public static void unzip(InputStream inputStream, File folder) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
//		FileUtils.deleteFolderContents(folder);
        try {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()){
                    String zipEntryName = zipEntry.getName();
                    File outputFile = new File(folder, zipEntryName);
                    File parentDir = outputFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(outputFile);
                        FileUtils.copyStream(zipInputStream, outputStream);
                        outputStream.close();
                    } finally {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                }
            }
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    // Ok to ignore
                }
            }
        }
    }


    public static boolean isValidUri(String uri) {
        boolean result = true;
        try {
            new URI(uri);
        } catch (URISyntaxException e) {
            result = false;
        }
        return result;
    }

    public static void deleteFolderAndContentsOnExit(File file) {
        if (file == null) {
            return;
        }
        file.deleteOnExit();
        if (file.isDirectory()) {

            String[] list = file.list();
            for (String fileName : list) {
                deleteFolderAndContentsOnExit(new File(file, fileName));
            }
        }
    }

    public static String extractFolderBaseName(File folder) {
        String path = folder.getAbsolutePath().replace("\\", "/");
        return path.substring(path.lastIndexOf("/") + 1);
    }


    public static boolean FileExists(String fileName){
        File file = new File(fileName);
        if(!file.exists() || !file.isFile()){
            return false;
        }
        return true;
    }

    public static boolean DirExists(String dirName){
        File dir = new File(dirName);
        if(!dir.exists() || !dir.isDirectory()){
            return false;
        }
        return true;
    }

    public static int CreateFile(String destFileName) {
        File file = new File(destFileName);

        if (file.exists()) {
            return 1;
        }

        if (destFileName.endsWith(File.separator)) {
            return 2;
        }

        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                return 3;
            }
        }

        try {
            if (file.createNewFile()) {
                return 0;
            } else {
                return 4;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 5;
        }
    }

    public static int createDir(String destDirName) {
        if(destDirName == null || destDirName.isEmpty()){
            return 3;
        }
        File dir = new File(destDirName);
        if(dir.exists()) {
            return 1;
        }
        if(!destDirName.endsWith(File.separator))
            destDirName = destDirName + File.separator;

        if(dir.mkdirs()) {
            return 0;
        } else {
            return 2;
        }
    }

    public static void copyFile(String src, String dst) throws IOException{
        File srcFile = new File(src);
        File destFile = new File(dst);

        FileInputStream input = new FileInputStream(srcFile);
        try {
            FileOutputStream output = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int n = 0;
                while (-1 != (n = input.read(buffer))) {
                    output.write(buffer, 0, n);
                }
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ioe) {
                    // ignore
                }
            }
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    public static boolean copyDir(String srcDirName, String dstDirName)
    {
        try
        {
            File dstDir = new File(dstDirName);
            if(dstDir.exists() && !dstDir.isDirectory()){
                System.out.println(dstDirName + " is not a directory!");
                return false;
            }
            if (!dstDir.exists()){
                if (!dstDir.mkdir()){
                    System.out.println("mkdir failed for " + dstDirName + "!");
                    return false;
                }
            }
            File srcDir = new File(srcDirName);
            if(!srcDir.exists()){
                System.out.println("Source dir " + srcDirName + " does not exist!");
                return false;
            }
            boolean flag = true;
            String from = "";
            String to = "";
            if(!srcDir.isDirectory()){
                try{
                    from = srcDirName;
                    to = dstDirName + File.separator + srcDir.getName();
                    FileUtils.copyFile(from, to);
                }catch(Exception e){
                    System.out.println("Failed to copy file from " + from + " to " + to + " !");
                    return false;
                }
                return true;
            }
            File[] allFile = srcDir.listFiles();
            for (int i = 0; i < allFile.length; i++)
            {
                if (!allFile[i].isDirectory())
                {
                    from = allFile[i].toString();
                    to = dstDirName + File.separator + allFile[i].getName();
                    try{
                        FileUtils.copyFile(from, to);
                    }catch(Exception e){
                        System.out.println("Failed to copy file from " + from + " to " + to + " !");
                        flag = false;
                    }
                }
                else
                {
                    if (copyDir(allFile[i].getPath().toString(), dstDirName + File.separator + allFile[i].getName().toString())){
                        //do nothing;
                    }
                    else {
                        System.out.println("Failed to copy sub directory " + allFile[i].getPath().toString() + " !");
                        flag = false;
                    }
                }
            }
            return flag;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static void moveFile(String src, String dst) throws IOException{
        FileUtils.copyFile(src,dst);
        FileUtils.deleteFolderAndContents(new File(src));
    }

    public static boolean copyRequierdFiles(String prefix, String suffix, String srcDirStr, String dstDirStr) {
        File srcDir = new File(srcDirStr);
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            return false;
        }
        File dstDir = new File(dstDirStr);
        if (!dstDir.exists()) {
            if(createDir(dstDirStr)>1){
                return false;
            }
        }

        String[] dirlist = srcDir.list();
        for (int i = 0; i < dirlist.length; i++)
        {
            //System.out.println(dirlist[i]);
            File chiFile = new File(srcDirStr + File.separator + dirlist[i]);
            if (!chiFile.isDirectory() && chiFile.canRead()){
                if(dirlist[i].startsWith(prefix) && dirlist[i].endsWith(suffix)){
                    try {
                        copyFile(srcDirStr + File.separator + dirlist[i], dstDirStr + File.separator + dirlist[i]);
                    } catch (IOException e) {
                    }
                }

            }
        }
        return true;
    }

    public static boolean deleteRequierdFiles(String prefix, String suffix, String inDirStr) {
        File srcDir = new File(inDirStr);
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            return false;
        }

        String[] dirlist = srcDir.list();
        for (int i = 0; i < dirlist.length; i++)
        {
            //System.out.println(dirlist[i]);
            File chiFile = new File(inDirStr + File.separator + dirlist[i]);
            if (!chiFile.isDirectory() && chiFile.canWrite()){
                if(dirlist[i].startsWith(prefix) && dirlist[i].endsWith(suffix)){
                    chiFile.delete();
                }

            }
        }
        return true;
    }

    public static String concatRequierdFileNames(String prefix, String suffix, String inDirStr, String separator) {
        String returnStr = "";
        File srcDir = new File(inDirStr);
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            return "";
        }

        String[] dirlist = srcDir.list();
        for (int i = 0; i < dirlist.length; i++)
        {
            //System.out.println(dirlist[i]);
            File chiFile = new File(inDirStr + File.separator + dirlist[i]);
            if (!chiFile.isDirectory() && chiFile.canWrite()){
                if(dirlist[i].startsWith(prefix) && dirlist[i].endsWith(suffix)){
                    if(returnStr.equalsIgnoreCase("")){
                        returnStr = dirlist[i];
                    }else{
                        returnStr = returnStr + separator + dirlist[i];
                    }
                }

            }
        }
        return returnStr;
    }

    public static File unzipFile(
            File destFile,
            ZipInputStream zis) throws IOException{
        if(destFile == null)
            destFile = new File("tempUnzipped");

        if(!destFile.exists())
            destFile.mkdir();

        //destFile.mkdir();

        ZipEntry entry = null;
        while((entry = zis.getNextEntry()) != null) {
            if(entry.isDirectory()) {
                File f = new File(destFile, entry.getName());
                if(!f.exists()) {
                    f.mkdir();
                }
                continue;
            }

            File f = new File(destFile, entry.getName());
            if(!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fout = new FileOutputStream(f);
            writeOutput(zis,fout);
            fout.close();
        }
        return destFile;
    }

    public static int writeOutput(File sourceFile, File destFile)
            throws IOException {
        int totallen = 0;
        if(sourceFile != null &&
                sourceFile.exists() &&
                sourceFile.isFile() &&
                destFile != null) {
            if(!destFile.exists())
                destFile.createNewFile();

            FileInputStream fin = new FileInputStream(sourceFile);
            FileOutputStream fout = new FileOutputStream(destFile);
            totallen = writeOutput(fin, fout);
            fin.close();
            fout.close();
        }
        return totallen;
    }

    public static int writeOutput(File sourceFile, OutputStream writer)
            throws IOException {
        int totallen = 0;
        if(sourceFile != null &&
                sourceFile.exists() &&
                sourceFile.isFile()) {
            FileInputStream reader = new FileInputStream(sourceFile);
            totallen = writeOutput(reader, writer);
            reader.close();
        }
        return totallen;
    }

    public static final int writeOutput(InputStream in, OutputStream out)
            throws IOException
    {
        byte[] buffer = new byte[1024];
        int len;

        int totalLen = 0;

        while((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
            totalLen += len;
        }

        out.flush();

        return totalLen;
    }

    public static void deleteDir(File file)throws Exception {
        if(file.isDirectory()) {
            File[] children = file.listFiles();
            if(children != null) {
                for(int i=0; i<children.length; i++) {
                    deleteDir(children[i]);
                }
            }
        }

        boolean result = file.delete();

        if(!result){
            throw new Exception("Cannot delete file " +
                    file.getAbsolutePath());
        }
    }

    public static int zipFile(File sourceFile ,
                              String parent,
                              ZipOutputStream zipOutStream) throws IOException {
        int totallen = 0;

        if(sourceFile == null || !sourceFile.exists() || zipOutStream == null)
            return totallen;

        if(sourceFile.isFile()) {
            zipOutStream.putNextEntry(new ZipEntry(
                    parent + sourceFile.getName()));

            totallen = writeOutput(sourceFile,
                    zipOutStream);
        } else if(sourceFile.isDirectory()) {
            String dir = parent + sourceFile.getName() + "/";

            zipOutStream.putNextEntry(new ZipEntry(dir));

            File[] children = sourceFile.listFiles();

            for (int i=0; i<children.length; i++) {
                totallen += zipFile(children[i], dir, zipOutStream);
            }
        }
        return totallen;
    }

    public static long getFileSizes(File f) throws IOException{
        long s=0;
        if (f.exists() && f.isFile()) {
            FileInputStream fis = null;
            fis = new FileInputStream(f);
            s= fis.available();
            fis.close();
        }
        return s;
    }

    public static long getFileTotalSize(File f) throws IOException{
        long size = 0;
        if(!f.exists() || !f.canRead())
            return 0;

        if(f.isFile())
            size = getFileSizes(f);
        else {
            File flist[] = f.listFiles();
            for (int i = 0; i < flist.length; i++)
            {
                if (flist[i].isDirectory())
                {
                    size = size + getFileTotalSize(flist[i]);
                } else
                {
                    size = size + getFileSizes(flist[i]);
                }
            }
        }


        return size;
    }

    public static boolean writeFileWithContect(String filePath, String content){
        boolean result=true;
        PrintWriter pw = null;
        try {
            if(CreateFile(filePath)>1){
                result = false;
            }
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filePath)),true);
            pw.print(content);
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        } finally{
            if(pw != null){
                pw.close();
            }
        }
        return result;
    }

    public static void closeFISQuietly(FileInputStream fis){
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
            }
        }
    }

    public static void closeFOSQuietly(FileOutputStream fos){
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
            }
        }
    }

}
