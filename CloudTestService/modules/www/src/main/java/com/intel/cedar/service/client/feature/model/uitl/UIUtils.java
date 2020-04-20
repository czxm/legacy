package com.intel.cedar.service.client.feature.model.uitl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.service.client.feature.model.ui.IUINode;

public abstract class UIUtils implements Serializable {
    public static String UI_HBOX = "hBox";
    public static String UI_COMBO = "combo";
    public static String UI_COMBOITEM = "comboItem";
    public static String UI_CHECK = "check";
    public static String UI_WINDOW = "windows";
    public static String UI_LIST = "list";
    public static String UI_REV = "rev";
    public static String UI_URL = "url";
    public static String UI_VBOX = "vbox";
    public static String UI_SVN = "svn";
    public static String UI_FEATURE = "feature";
    public static String UI_FORM = "form";
    public static String UI_FIELDSET = "fieldset";
    public static String UI_CHECKBOXGROUP = "checkboxgroup";
    public static String UI_SELECT = "select";
    public static String UI_TEXTFIELD = "textfield";
    public static String UI_TEXTAREA = "textarea";
    public static String UI_FILEUPLOADFIELD = "fileuploadfield";
    public static String UI_COMPOSITE = "composite";

    public static String UI_FEATURE_ID = "feature_ID";

    public static List<?> EMPTY = new ArrayList<IUINode>();
    public static List<String> EMPTY_STR = new ArrayList<String>();

    private static final long serialVersionUID = UIUtils
            .getSerialVUID("UIUtils");

    public static long getSerialVUID(String name) {
        long sid = 0;
        int i = 0;
        for (i = 0; i < name.length() && i < 20; i++) {
            long v = (long) name.charAt(i);
            sid = (sid << 3) ^ v;
        }

        for (i = 0; i < 20; i++) {
            sid = sid ^ (sid << 3);
        }

        for (; i < name.length(); i++) {
            long v = (long) name.charAt(i);
            sid = sid ^ (v << (i % 60));
        }
        return sid;
    }
}
