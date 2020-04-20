package com.intel.soak.utils;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
    /**
     * Close the stream, ignoring any exceptions when doing so (which callers
     * usually can do nothing about). Skips if the stream is null.
     * 
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

    public static File createTempFolderInFolder(File tempDir, String prefix,
            String suffix) throws IOException {
        // Java doesn't allow you to create a temporary directory, only files.
        // Create a file, then delete it and use that name to create a directory
        // There is a tiny chance that someone else creates a file/directory
        // with the same name during the small window of time after the
        // temporary
        // file is created. Try a few times to make this chance even smaller,
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

	public static String getNextFileName(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		for (int i = 1; i < 1000; i++) {
			sb.setLength(name.length());
			sb.append(".");
			sb.append(i);
			if (!new File(sb.toString()).exists())
				return sb.toString();
		}
		return name + ".ERROR";
	}
	
	public static void zip(String zipFileName, List<String> files) throws Exception {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
				zipFileName));
		for(String f : files){
			out.putNextEntry(new ZipEntry(new File(f).getName()));
			FileInputStream in = new FileInputStream(f);
			copyStream(in, out);
			in.close();
		}
		out.close();
	}
	
	public static void zip(String zipFileName, File folder) throws Exception {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
				zipFileName));
		zip(out, folder, "");
		out.close();
	}
	
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
	
	private static void zip(ZipOutputStream out, File f, String base) throws Exception {
		if (f.isDirectory()) {
			base = base.length() == 0 ? f.getName() + "/" : base + "/";
			File[] fl = f.listFiles();
			out.putNextEntry(new ZipEntry(base));
			for (int i = 0; i < fl.length; i++) {
				zip(out, fl[i], base + fl[i].getName());
			}
		} else {
			out.putNextEntry(new ZipEntry(base));
			FileInputStream in = new FileInputStream(f);
			copyStream(in, out);
			in.close();
		}
	}


    public static synchronized List<String> listFileDirs(File rootDir) {
        List<String> result = new ArrayList<String>();
        String[] list = rootDir.list();
        for (String path : list) {
            File f = new File(path);
            if (f.isDirectory()) {
                result.addAll(listFileDirs(f));
            } else {
                result.add(path);
            }
        }
        return result;
    }

    public static synchronized List<File> listFiles(File rootDir) {
        List<File> result = new ArrayList<File>();
        File[] list = rootDir.listFiles();
        for (File file : list) {
            if (file.isDirectory()) {
                result.addAll(listFiles(file));
            } else {
                result.add(file);
            }
        }
        return result;
    }
    
    public static synchronized List<File> listFiles(File rootDir, String pattern) {
        List<File> result = new ArrayList<File>();
        File[] list = rootDir.listFiles();
        if(list != null){
            for (File file : list) {
                if (file.isDirectory()) {
                    result.addAll(listFiles(file, pattern));
                } else if(file.getName().matches(pattern)) {
                    result.add(file);
                }
            }
        }
        return result;
    }

    public static String applyTemplate(String file, HashMap<String, Object> vars){
        //set the veloctity properties
        Properties properties = new Properties();
        properties.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS,"org.apache.velocity.runtime.log.NullLogSystem");
        properties.setProperty(Velocity.RESOURCE_LOADER,"class");
        properties.setProperty("class.resource.loader.class","org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        VelocityEngine velocityEngine = new VelocityEngine(properties);
        velocityEngine.init();
        Template template = null;
        try {
            template = velocityEngine.getTemplate(file);
            VelocityContext context = new VelocityContext();
            for(String key : vars.keySet()){
                context.put(key, vars.get(key));
            }

            StringWriter writer = new StringWriter();
            template.merge(context,writer);
            return writer.toString();  
        }
        catch (Exception e){
        }
        return null;
    }

    public static String applyStringTemplate(String content, HashMap<String, Object> vars){
        //set the veloctity properties
        Properties properties = new Properties();
        properties.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS,"org.apache.velocity.runtime.log.NullLogSystem");
        properties.setProperty(Velocity.RESOURCE_LOADER,"string");
        properties.setProperty("string.resource.loader.class","org.apache.velocity.runtime.resource.loader.StringResourceLoader");

        VelocityEngine velocityEngine = new VelocityEngine(properties);
        velocityEngine.init();
        Template template = null;
        StringResourceRepository repo = StringResourceLoader.getRepository();
        String rndName = Integer.toString(new Random().nextInt());
        try {
            repo.putStringResource(rndName, content);
            template = velocityEngine.getTemplate(rndName);
            VelocityContext context = new VelocityContext();
            for(String key : vars.keySet()){
                context.put(key, vars.get(key));
            }

            StringWriter writer = new StringWriter();
            template.merge(context,writer);
            return writer.toString();
        }
        catch (Exception e){
        }
        finally{
            repo.removeStringResource(rndName);
        }
        return null;
    }
}
