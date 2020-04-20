package com.intel.soak.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.*;
import java.util.zip.ZipEntry;

public enum JarUtils {

    INSTANCE;

    private static final Log LOG = LogFactory.getLog(JarUtils.class);

    public static Manifest getManifest(File jar) {
        JarInputStream jis = null;
        try {
            jis = new JarInputStream(new BufferedInputStream(
                    new FileInputStream(jar)));
            return jis.getManifest();
        } catch (Exception e) {
            LOG.error("Read jar manifest failed!");
            throw new ReadManifestException(e);
        } finally {
            IOUtils.closeQuietly(jis);
        }
    }

    public static String getMainClass(Resource jar) throws IOException {
        return getMainClass(jar.getFile());
    }

    public static String getMainClass(File jar) throws IOException {
        Manifest mf = getManifest(jar);
        if (mf != null) {
            String mainClass = mf.getMainAttributes().getValue("Main-Class");
            if (StringUtils.hasText(mainClass)) {
                return mainClass.replace("/", ".");
            }
        }
        return null;
    }

    public static void unjar(Resource jar, File baseDir) throws IOException {
        JarInputStream jis = new JarInputStream(jar.getInputStream());
        JarEntry entry = null;
        try {
            while ((entry = jis.getNextJarEntry()) != null) {
                if (!entry.isDirectory()) {
                    File file = new File(baseDir, entry.getName());
                    if (!file.getParentFile().mkdirs()) {
                        if (!file.getParentFile().isDirectory()) {
                            throw new IOException("Mkdirs failed to create " + file.getParentFile().toString());
                        }
                    }
                    OutputStream out = new FileOutputStream(file);
                    try {
                        byte[] buffer = new byte[8192];
                        int i;
                        while ((i = jis.read(buffer)) != -1) {
                            out.write(buffer, 0, i);
                        }
                    } finally {
                        IOUtils.closeQuietly(out);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(jis);
        }
    }

    public static File unzip(File src, File desDir) {
        JarFile jarFile = null;
        FileOutputStream out = null;
        try {
            File destFile = new File(desDir, src.getName().replaceAll(".jar",
                    ""));
            if (!destFile.exists() || !destFile.isDirectory()) {
                destFile.mkdirs();
            } else {
                throw new UnzipJarException(
                        "Target dir is already exising! Target: "
                                + destFile.getPath());
            }
            jarFile = new JarFile(src);
            Enumeration<JarEntry> jarEntrys = jarFile.entries();
            while (jarEntrys.hasMoreElements()) {
                ZipEntry ze = jarEntrys.nextElement();
                File dest = new File(destFile, ze.getName());
                if (ze.isDirectory()) {
                    if (!dest.exists() || !dest.isDirectory())
                        dest.mkdirs();
                } else {
                    File parent = dest.getParentFile();
                    if (!parent.exists() || !parent.isDirectory())
                        parent.mkdirs();
                    out = new FileOutputStream(dest);
                    FileUtils.copyStream(jarFile.getInputStream(ze), out);
                    out.flush();
                }
            }
            return destFile;
        } catch (Exception e) {
            LOG.error("Unzip jar failed!");
            throw new UnzipJarException(e);
        } finally {
            try {
                if (jarFile != null)
                    jarFile.close();
            } catch (IOException e) {
            }
            IOUtils.closeQuietly(out);
        }
    }


    public static InputStream createJarInputStream(Map<String, InputStream> inputs) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JarOutputStream out = new JarOutputStream(bos);
        for (String f : inputs.keySet()) {
            JarEntry jarAdd = new JarEntry(f);
            jarAdd.setTime(System.currentTimeMillis());
            out.putNextEntry(jarAdd);
            InputStream in = inputs.get(f);
            in.reset();
            FileUtils.copyStream(in, out);
            in.close();
        }
        out.close();
        return new ByteArrayInputStream(bos.toByteArray());
    }

    public static URL getClassURLinJar(File jar, String className) {
        try {
            String classPath = className.replace(".", "/");
            URL jarURL = jar.toURI().toURL();
            return  new URL(String.format("jar:%s!/%s.class",
                    jarURL.toString(), classPath));
        } catch (Exception e) {
            throw new RuntimeException("Error while getting class url in jar", e);
        }
    }

    public static URL getClassURLinJar(File jar, Class clazz) {
        return getClassURLinJar(jar, clazz.getCanonicalName());
    }
}

class UnzipJarException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnzipJarException(Exception e) {
        super(e);
    }

    public UnzipJarException(String msg) {
        super(msg);
    }

}

class ReadManifestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ReadManifestException(Exception e) {
        super(e);
    }

    public ReadManifestException(String msg) {
        super(msg);
    }

}
