package com.intel.cedar.feature.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.tasklet.TaskletInfo;
import com.intel.cedar.util.BaseDirectory;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;
import com.intel.cedar.util.SubDirectory;

public class FeatureUtil {

    public static String removeNull(String value) {
        return (value == null) ? "" : value;
    }

    public static List<FeatureInfo> listFeatures(List<String> featureIds) {
        EntityWrapper<FeatureInfo> db = EntityUtil.getFeatureEntityWrapper();
        List<FeatureInfo> res = new ArrayList<FeatureInfo>();
        FeatureInfo qInfo = new FeatureInfo();
        try {
            if (featureIds == null || featureIds.size() == 0) {
                res = db.query(qInfo);
            } else {
                for (String fid : featureIds) {
                    res.add(db.load(FeatureInfo.class, fid));
                }
            }
            return res;
        } finally {
            db.rollback();
        }
    }

    public static FeatureInfo getFeatureInfoById(String featureId) {
        EntityWrapper<FeatureInfo> db = new EntityWrapper<FeatureInfo>();
        FeatureInfo f = db.load(FeatureInfo.class, featureId);
        db.rollback();
        return f;
    }

    public static String getFeatureNameById(String featureId) {
        EntityWrapper<FeatureInfo> db = new EntityWrapper<FeatureInfo>();
        FeatureInfo f = db.load(FeatureInfo.class, featureId);
        db.rollback();
        if (f != null) {
            return f.getName();
        }
        return "";
    }

    public static FeatureInfo getFeatureInfo(String name, String version) {
        EntityWrapper<FeatureInfo> db = new EntityWrapper<FeatureInfo>();
        FeatureInfo f = new FeatureInfo();
        f.setName(name);
        f.setVersion(version);
        List<FeatureInfo> result = db.query(f);
        f = null;
        if (result.size() > 0) {
            f = result.get(0);
        }
        db.rollback();
        return f;
    }

    public static String getFeatureJarById(String featureId) {
        EntityWrapper<FeatureInfo> db = new EntityWrapper<FeatureInfo>();
        FeatureInfo f = db.load(FeatureInfo.class, featureId);
        db.rollback();
        return BaseDirectory.HOME + f.getContextPath() + f.getJar();
    }

    public static List<String> getFeatureDependsById(String featureId) {
        EntityWrapper<FeatureInfo> db = new EntityWrapper<FeatureInfo>();
        FeatureInfo f = db.load(FeatureInfo.class, featureId);
        db.rollback();
        List<String> result = new ArrayList<String>();
        for (String jar : f.getDependsLibs()) {
            result.add(BaseDirectory.HOME + f.getContextPath() + jar);
        }
        return result;
    }

    public static File getFile(String featureId, String relativePath) {
        File parent = computeTargetDir(featureId);
        File file = new File(parent, relativePath);
        return file;
    }

    public static File computeTargetDir(String featureId) {
        String parent = SubDirectory.FEATURES.getParent();
        File parentFile = new File(parent);

        String path = computeFeaturePath(featureId);
        File targetDir = new File(parentFile, path);
        FileUtils.ensureFolderExists(targetDir);

        return targetDir;
    }

    public static String computeJarName(String featureId) {
        return featureId + ".jar";
    }

    public static String computeFeaturePath(String featureId) {
        return SubDirectory.FEATURES.relative() + File.separator + featureId
                + File.separator;
    }

    public static byte[] getResource(String featureId, String resourceLocation) {
        try {
            File parent = computeTargetDir(featureId);
            File resourceFile = new File(parent, resourceLocation);
            FileInputStream fis = new FileInputStream(resourceFile);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();

            byte[] data = new byte[1024];
            int readIn = -1;
            while ((readIn = fis.read(data, 0, 1024)) != -1) {
                bo.write(data, 0, readIn);
            }

            return bo.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static boolean changeFileName(String featureId, String oldName,
            String newName) {
        try {
            File parent = computeTargetDir(featureId);
            File oldFile = new File(parent, oldName);
            File newFile = new File(parent, newName);
            oldFile.renameTo(newFile);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void logException(Logger logger, String err) throws Exception {
        Exception e = new Exception(err);
        logger.error("", e);
        throw e;
    }
}
