package com.intel.soak.utils;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Jar Resource, encapsulation the attributes and behaviors of a jar.
 * The class inherits {@link org.springframework.core.io.FileSystemResource}.
 * Note that it is not thread-safe.
 *
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 12/19/13 1:46 AM
 */
public class JarFileResource extends FileSystemResource {

    public JarFileResource(File file) {
        super(file);
        isJarValidation();
    }

    public JarFileResource(String path) {
        super(path);
        isJarValidation();
    }

    public JarFileResource(Resource resource) throws IOException {
        super(resource.getFile());
        isJarValidation();
    }

    private void isJarValidation() {
        Assert.isTrue(isJar(getFile()),
                "File is not a jar or not readable: "
                        + getFile().getName());
    }

    private boolean isJar(File file) {
        InputStream is = null;
        try {
            is = new FileSystemResource(file).getInputStream();
            JarInputStream ji = new JarInputStream(is);
            return true;
        } catch (IOException e) {
            IOUtils.closeQuietly(is);
            return false;
        }
    }

    public Manifest getManifest() throws IOException {
        JarInputStream ji = null;
        try {
            ji = new JarInputStream(this.getInputStream());
            return ji.getManifest();
        } finally {
            IOUtils.closeQuietly(ji);
        }
    }

    public JarEntry getJarEntry(String entryName) throws IOException {
        JarFile jf = new JarFile(getFile());
        return jf.getJarEntry(entryName);
    }

    public void unjar(File targetDir) throws IOException {
        JarInputStream jis = new JarInputStream(this.getInputStream());
        JarEntry entry = null;
        try {
            while ((entry = jis.getNextJarEntry()) != null) {
                if (!entry.isDirectory()) {
                    File file = new File(targetDir, entry.getName());
                    if (!file.getParentFile().mkdirs()) {
                        if (!file.getParentFile().isDirectory()) {
                            throw new IOException("Mkdirs failed to create "
                                    + file.getParentFile().toString());
                        }
                    }
                    OutputStream out = new FileOutputStream(file);
                    try {
                        byte[] buffer = new byte[1024];
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

}
