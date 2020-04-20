package com.intel.soak.gauge.log.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/22/13
 * Time: 10:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileTools {

    public static synchronized List<String> listFileDirs(File rootDir) throws Exception {
        return parseFile2Path(listFiles(rootDir));
    }

    public static List<String> parseFile2Path(List<File> files) throws Exception {
        List<String> paths = new ArrayList<String>();
        for (File file : files) {
            paths.add(file.getCanonicalPath());
        }
        return paths;
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

}
