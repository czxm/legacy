package com.intel.cedar.jws.viewer;

import java.lang.reflect.Method;

import vncviewer.CConn;
import vncviewer.VNCViewer;

public class NoPromptCCon extends CConn {
    private String user = "";
    private String passwd = "";

    public NoPromptCCon(VNCViewer viewer, String passwd) {
        super(viewer);
        this.passwd = passwd;
    }

    public boolean getUserPasswd(StringBuffer user, StringBuffer passwd) {
        if (user != null)
            user.append(this.user);
        if (passwd != null)
            passwd.append(this.passwd);
        return true;
    }

    void removeWindow() {
        Class<?> connClz = CConn.class;
        try {
            Method m = connClz.getDeclaredMethod("removeWindow", null);
            m.setAccessible(true);
            m.invoke(this, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}