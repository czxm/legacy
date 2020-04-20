package com.intel.soak.gauge.log.merger;

import com.intel.soak.gauge.log.constant.LogConstants;
import com.intel.soak.gauge.log.utils.FileTools;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/22/13
 * Time: 10:01 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractLogMerger implements ILogMerger {

    protected Log LOG = LogFactory.getLog(this.getClass());

    protected Map<String, List<String>> aggregateLogDir(String taskId) {
        try {
            File rootDir = new File(LogConstants.LOG_ROOT_DIR);
            if (!rootDir.exists() || !rootDir.isDirectory()) {
                rootDir.mkdirs();
            }
            File caseDir = new File(rootDir, taskId);
            if (!rootDir.exists() || !rootDir.isDirectory()) {
                LOG.error("The log dir of your task is not found: " + caseDir.getPath());
                System.exit(-1);
            }
            Map<String, List<String>> result = aggregateLogDir(caseDir);
            printAggregatedDir(result);
            return result;
        } catch (Exception e) {
            LOG.error("Aggregate log dir failed: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<String, List<String>>();
        }

    }

    protected void printAggregatedDir(Map<String, List<String>> result) {
        System.out.println("Log classification result:");
        for (String key : result.keySet()) {
            System.out.println("  " + key + ":");
            for (String path : result.get(key)) {
                System.out.println("      " + path);
            }
            System.out.println("");
        }
    }


    protected Map<String, List<String>> aggregateLogDir(File caseDir) throws Exception {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        List<String> logFilePaths = FileTools.listFileDirs(caseDir);
        Map<String, String> pathMap = clearLogFilePaths(caseDir, logFilePaths);
        for (String dir : pathMap.keySet()) {
            String cleanedPath = pathMap.get(dir);
            String[] strs = cleanedPath.split(File.separator);
            StringBuilder keySB = new StringBuilder();
            for (int i = 1; i < strs.length - 1; i++) {
                try {
                    keySB.append(strs[i]).append(File.separator);
                } catch (IndexOutOfBoundsException e) {
                    LOG.error("It seems that the log files dir structure is incorrect");
                    System.exit(-1);
                }
            }
            String key = keySB.toString();
            if (result.get(key) == null) {
                result.put(key, new ArrayList<String>());
            }
            result.get(key).add(dir);
        }
        return result;
    }

    protected Map<String, String> clearLogFilePaths(File rootDir, List<String> dirs) {
        Map<String, String> result = new HashMap<String, String>();
        try {
            String rootPath = rootDir.getCanonicalPath();
            for (String dir : dirs) {
                result.put(dir, dir.replace(rootPath + File.separator, ""));
            }
        } catch (Exception e) {
            LOG.error("Clear log file paths failed:" + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
        return result;
    }

}
