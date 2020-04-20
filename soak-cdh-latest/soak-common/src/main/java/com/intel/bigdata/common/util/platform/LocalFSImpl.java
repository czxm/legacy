package com.intel.bigdata.common.util.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * This class arranges to perform local file system related operations.
 */
@Component
public class LocalFSImpl implements LocalFS {

    protected static Logger LOG = LoggerFactory.getLogger(LocalFSImpl.class);

    @Override
    public boolean makeDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        } else {
            return removeDirRecursively(file);
        }
    }

    private boolean removeDirRecursively(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    removeDirRecursively(file);
                } else {
                    file.delete();
                }
            }
        }
        return path.delete();
    }

    @Override
    public boolean makeFile(String path, String content) {
        return makeFile(path, content, false);
    }

    @Override
    public boolean makeFile(String path, String content, boolean override) {
        boolean success = false;
        File file = new File(path);
        BufferedWriter bw = null;
        try {
            if (file.exists()) {
                if (override) {
                    file.delete();
                    file.createNewFile();
                } else {
                    return false;
                }
            } else {
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
            }

            bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
            bw.write(content);
            success = true;
        }
        catch (IOException e) {
            success = false;
        }
        finally {
            if (bw != null) {
                try {
                    bw.close();
                }
                catch (IOException e) {
                    success = false;
                }
            }
        }
        return success;
    }

    @Override
    public String readFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return "";
        }

        BufferedReader br = null;
        StringBuffer content = new StringBuffer();
        try {
            br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }

        }
        catch (IOException e) {
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                }
                catch (IOException e) {
                }
            }
        }
        return content.toString();
    }

}