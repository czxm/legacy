package com.intel.cedar.feature.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.engine.model.feature.Tasklet;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.feature.util.FeatureUtil;
import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.tasklet.TaskletInfo;
import com.intel.cedar.util.EntityWrapper;

public class FeatureDeploy {
    private String inputjarPath;
    private File targetDir;
    private FeatureManifest manifest;
    private String outputJar;

    private static Logger LOG = LoggerFactory.getLogger(FeatureDeploy.class);

    protected static final String ICON = ".ico";
    protected static final String JIF = ".jif";
    protected static final String JPG = ".jpg";
    protected static final String PNG = ".png";

    protected static List<String> POSTFIXS = new ArrayList<String>();
    static {
        POSTFIXS.add(ICON);
        POSTFIXS.add(JIF);
        POSTFIXS.add(JPG);
        POSTFIXS.add(PNG);
    }

    public static void main(String args[]) {
        try {
            String path = "C:\\workspace\\cloud\\Eucalyptus\\CloudTestService\\examples\\features\\cpp.conformance.jar";
            FeatureDeploy deploy = new FeatureDeploy();
            deploy.deploy(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FeatureDeploy() {
    }

    public void deploy(String jarPath) throws Exception {
        deploy(jarPath, true, true);
    }

    public void deploy(String jarPath, boolean check, boolean changeDB)
            throws Exception {
        LOG.info("Deploy feature begin...");
        if (jarPath == null) {
            throw new Exception("The input jar path is empty.");
        }

        initDeploy(jarPath);

        if (check && false == verifyFeature()) {
            throw new Exception("The feature is invalid.");
        }

        if (check && false == isNewFeature()) {
            throw new Exception("This feature is duplicated.");
        }

        copyJar();

        extract();

        if (changeDB)
            commit();

        LOG.info("Deploy feature successful.");
    }

    protected void initDeploy(String jarPath) {
        inputjarPath = jarPath;
        targetDir = null;
        manifest = null;
        outputJar = null;
    }

    protected void extract() throws Exception {
        LOG.info("extract feature jar...");
        File file = new File(getInputJarPath());

        FileInputStream fi = new FileInputStream(file);

        File target = getTargetDir();
        FileUtils.ensureFolderExists(target);

        FileUtils.unzip(fi, target);

        fi.close();
    }

    protected void commit() {
        LOG.info("Commit feature...");
        FeatureInfo feature = getFeatureInfo();

        commitFeature(feature);

        commitTaskLet(feature);
    }

    protected void commitTaskLet(FeatureInfo feature) {
        try {
            EntityWrapper<TaskletInfo> tdb = new EntityWrapper<TaskletInfo>();
            FeatureLoader loader = new FeatureLoader();
            Feature featureModel = loader.loadFeature(feature);
            for (Tasklet t : featureModel.getTasklets().getModelChildren()) {
                TaskletInfo tasklet = new TaskletInfo();
                tasklet.setId(t.getID(), feature.getId());
                tasklet.setProvider(t.getProvider());
                tasklet.setContributer(t.getContributer());
                tasklet.setDesc(t.getDescription());
                tasklet.setPublic(t.getIsPublic());
                tasklet.setSharable(t.getSharable());
                tdb.add(tasklet);
            }
            tdb.commit();
        } catch (Exception e) {
            LOG.error("", e);
            e.printStackTrace();
        }
    }

    protected void commitFeature(FeatureInfo feature) {
        EntityWrapper<FeatureInfo> fdb = new EntityWrapper<FeatureInfo>();
        fdb.add(feature);
        fdb.commit();
    }

    protected boolean verifyFeature() throws Exception {
        // verify manifest
        FeatureManifest fmaniFest = getManiFest();
        if (false == fmaniFest.verify()) {
            throw new Exception(fmaniFest.getErrMessage());
        }

        // feature.xml
        String jarpath = getInputJarPath();
        FeatureJar featureJar = new FeatureJar(new String[] { jarpath });
        if (null == featureJar.getResourceStream(fmaniFest.getDescriptor())) {
            throw new Exception(
                    "There is no the Descriptor file in the input jar.");
        }

        // resource
        List<String> icons = fmaniFest.getIcons();
        for (String icon : icons) {
            if (null == featureJar.getResourceStream(icon)) {
                throw new Exception("There is no resource file: " + icon
                        + " in the input jar.");
            }
        }

        return true;
    }

    protected boolean isNewFeature() {
        FeatureManifest fmaniFest = getManiFest();
        String name = fmaniFest.getName();
        String version = fmaniFest.getVersion();
        FeatureInfo f = FeatureUtil.getFeatureInfo(name, version);
        return f == null ? true : false;
    }

    protected void copyJar() throws Exception {
        LOG.info("copy feature jar...");

        File target = getTargetDir();
        File jarFile = new File(target, getOutputJarName());
        FileUtils.ensureFolderExists(jarFile.getParentFile());

        File inputFile = new File(getInputJarPath());

        InputStream input = new FileInputStream(inputFile);
        FileOutputStream outputStream = new FileOutputStream(jarFile);

        FileUtils.copyStream(input, outputStream);

        input.close();
        outputStream.close();
    }

    private FeatureInfo getFeatureInfo() {

        FeatureInfo feature = new FeatureInfo();

        FeatureManifest fManifest = getManiFest();

        String name = FeatureUtil.removeNull(fManifest.getName());
        feature.setName(name);

        String featureId = fManifest.getFeatureId();

        feature.setId(featureId);

        feature.setShortName(FeatureUtil.removeNull(fManifest.getShortName()));

        feature.setVersion(FeatureUtil.removeNull(fManifest.getVersion()));

        feature.setContributer(FeatureUtil.removeNull(fManifest
                .getContributer()));

        feature
                .setDescriptor(FeatureUtil
                        .removeNull(fManifest.getDescriptor()));

        feature.setJar(getOutputJarName());

        feature.setHint(FeatureUtil.removeNull(fManifest.getHint()));

        feature
                .setDependsLib(FeatureUtil
                        .removeNull(fManifest.getDependsLib()));

        feature.setContextPath(FeatureUtil.computeFeaturePath(featureId));

        feature.setEnabled(true);

        File descriptor = FeatureUtil.getFile(featureId, feature
                .getDescriptor());
        feature.setLastModified(descriptor.lastModified());

        fillResource(fManifest, feature);

        /*
         * try{ File file = new File(getInputJarPath());
         * 
         * FileInputStream fi = new FileInputStream(file); ByteArrayOutputStream
         * bo = new ByteArrayOutputStream(); byte [] data = new byte[1024]; int
         * readIn = -1; while((readIn =fi.read(data, 0, 1024))!=-1){
         * bo.write(data, 0, readIn); }
         * 
         * feature.setImage(bo.toByteArray()); }catch(Exception e){
         * LOG.error("", e); e.printStackTrace(); }
         */

        return feature;
    }

    protected void fillResource(FeatureManifest manifest, FeatureInfo feature) {
        List<String> icons = manifest.getIcons();
        if (icons.size() > 0) {
            String path = appendCEDAR(feature.getId(), icons.get(0));
            feature.setEnIcon(path);
        }

        if (icons.size() > 1) {
            String path = appendCEDAR(feature.getId(), icons.get(1));
            feature.setDisIcon(path);
        }
    }

    public FeatureManifest getManiFest() {
        if (manifest == null) {
            manifest = new FeatureManifest(getInputJarPath());
        }
        return manifest;
    }

    protected File getTargetDir() {
        if (targetDir == null) {
            targetDir = FeatureUtil.computeTargetDir(getManiFest()
                    .getFeatureId());
        }

        return targetDir;
    }

    protected String getOutputJarName() {
        if (outputJar == null) {
            outputJar = FeatureUtil
                    .computeJarName(getManiFest().getFeatureId());
        }
        return outputJar;
    }

    protected String getInputJarPath() {
        return inputjarPath;
    }

    protected String appendCEDAR(String featureId, String filepath) {
        // FIXME if there is better solution
        for (String postfix : POSTFIXS) {
            if (false == filepath.endsWith(postfix)) {
                continue;
            }
            String v = filepath.substring(0, filepath.length()
                    - postfix.length());
            String newName = v + ".CEDAR" + postfix;
            if (FeatureUtil.changeFileName(featureId, filepath, newName)) {
                return newName;
            } else {
                return filepath;
            }
        }
        return filepath;
    }
}
