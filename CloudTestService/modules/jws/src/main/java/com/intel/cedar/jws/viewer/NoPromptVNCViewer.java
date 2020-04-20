package com.intel.cedar.jws.viewer;

import rfb.Configuration;
import vncviewer.VNCViewer;

public class NoPromptVNCViewer extends VNCViewer {
    private static final long serialVersionUID = -3457614957689163161L;

    public NoPromptVNCViewer(String[] strings) {
        super(strings);
    }

    public void run() {
        NoPromptCCon cc = null;
        try {
            cc = new NoPromptCCon(this, "123456");
            if (cc.init(null, Configuration.getParam("Server").getValueStr(),
                    Boolean.parseBoolean(Configuration.getParam(
                            "AlwaysShowServerDialog").getValueStr())))
                do
                    cc.processMsg();
                while (true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cc.removeWindow();
    }
}
