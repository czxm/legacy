package com.intel.ca360.loadmeter.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
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
}
