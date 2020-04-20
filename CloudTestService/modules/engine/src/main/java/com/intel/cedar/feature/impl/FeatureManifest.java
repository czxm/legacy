package com.intel.cedar.feature.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

import com.intel.cedar.feature.util.FeatureUtil;
import com.intel.cedar.feature.util.JarUtility;
import com.intel.cedar.feature.util.RandomUtility;

public class FeatureManifest {
    private Manifest manifest;
    private String errMsg;

    public static final String MniefestFile = "META-INF/MANIFEST.MF";

    public static final String NAME = "Feature-Name";
    public static final String SHORT_NAME = "Feature-ShortName";
    public static final String HINT = "Feature-Hint";
    public static final String VERSION = "Feature-Version";
    public static final String CONTRIBUTER = "Feature-Contributer";
    public static final String ICONS = "Feature-Icons";
    public static final String DESCRIPTOER = "Feature-Descriptor";
    public static final String DEPENDSLIB = "Feature-Dependences";

    private static List<String> KEYS = new ArrayList<String>();
    private static List<String> CHECKFIELDS = new ArrayList<String>();

    static {
        CHECKFIELDS.add(NAME);
        CHECKFIELDS.add(HINT);
        CHECKFIELDS.add(VERSION);
        CHECKFIELDS.add(CONTRIBUTER);
        CHECKFIELDS.add(ICONS);
        CHECKFIELDS.add(DESCRIPTOER);
        CHECKFIELDS.add(SHORT_NAME);

        KEYS.addAll(CHECKFIELDS);
        KEYS.add(DEPENDSLIB);
    }

    public static void main(String args[]) {
        String file = "examples\\features\\cpp.conformance\\META-INF\\MANIFEST.MF";
        try {
            FeatureManifest loader = new FeatureManifest(file);

            System.out.println(NAME + ": " + loader.getValue(NAME));
            System.out.println(HINT + ": " + loader.getValue(HINT));
            System.out.println(VERSION + ": " + loader.getValue(VERSION));
            System.out.println(CONTRIBUTER + ": "
                    + loader.getValue(CONTRIBUTER));
            System.out.println(ICONS + ": " + loader.getValue(ICONS));
            System.out.println(DESCRIPTOER + ": "
                    + loader.getValue(DESCRIPTOER));
            System.out.println(DEPENDSLIB + ": " + loader.getValue(DEPENDSLIB));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FeatureManifest(String jarFile) {
        reset(jarFile);
    }

    protected FeatureManifest() {

    }

    public static String computeFeatureID(String featureName) {
        String id = Long.toString(RandomUtility.getRandom(featureName));
        return id;
    }

    public void reset(String jarFile) {
        manifest = JarUtility.getMainfest(jarFile);
        errMsg = null;
    }

    public boolean verify() {
        errMsg = null;
        if (manifest == null) {
            errMsg = "There is no " + MniefestFile + " in the jar";
            return false;
        }

        String fileds = "";
        int count = 0;
        for (String key : CHECKFIELDS) {
            if (null == getValue(key)) {
                fileds += key + "";
                count++;
            }
        }

        fileds = fileds.trim();
        if (count == 0) {
            return true;
        } else if (count == 1) {
            errMsg = "This field: " + fileds + "is empty.";
        } else {
            errMsg = "These fields: " + fileds + "are empty.";
        }
        return false;
    }

    public String getErrMessage() {
        return errMsg;
    }

    public String getName() {
        String v = manifest.getMainAttributes().getValue(NAME);
        return (v == null) ? null : v.trim();
    }

    public String getShortName() {
        String v = manifest.getMainAttributes().getValue(SHORT_NAME);
        return (v == null) ? null : v.trim();
    }

    public String getHint() {
        String v = manifest.getMainAttributes().getValue(HINT);
        return (v == null) ? null : v.trim();
    }

    public String getVersion() {
        String v = manifest.getMainAttributes().getValue(VERSION);
        return (v == null) ? null : v.trim();
    }

    public String getContributer() {
        String v = manifest.getMainAttributes().getValue(CONTRIBUTER);
        return (v == null) ? null : v.trim();
    }

    public List<String> getIcons() {
        List<String> icons = new ArrayList<String>();
        String values = manifest.getMainAttributes().getValue(ICONS);
        if (values == null) {
            return icons;
        }

        for (String icon : values.trim().split("\\s")) {
            icons.add(icon);
        }
        return icons;
    }

    public String getDescriptor() {
        String v = manifest.getMainAttributes().getValue(DESCRIPTOER);
        return (v == null) ? null : v.trim();
    }

    public String getDependsLib() {
        String v = manifest.getMainAttributes().getValue(DEPENDSLIB);
        return (v == null) ? null : v.trim();
    }

    public List<String> getDependsLibs() {
        List<String> libs = new ArrayList<String>();
        String depend = getDependsLib();
        if (depend == null) {
            return libs;
        }

        for (String e : depend.split("\\s")) {
            libs.add(e);
        }

        return libs;
    }

    public String getValue(String key) {
        String v = manifest.getMainAttributes().getValue(key);
        return (v == null) ? null : v.trim();
    }

    public String getFeatureId() {
        // this is to generate different ID for different versions
        String name = FeatureUtil.removeNull(getName() + "_" + getVersion());
        return getName().replaceAll("/|\\|>|<|=| |,", "") + "_" + computeFeatureID(name);
    }
}
