package com.intel.cedar.service.client.util;

import java.util.ArrayList;
import java.util.Arrays;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ComponentPlugin;
import com.intel.cedar.service.client.model.Arch;
import com.intel.cedar.service.client.model.OS;

public class Util {
    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isDigit(char ch) {
        if (ch >= '0' && ch <= '9')
            return true;
        return false;
    }

    public static boolean isZero(char ch) {
        if (ch == '0')
            return true;
        return false;
    }

    public static String diffData(long rhs, long lhs) {
        // guarantee rhs is greater than or equal to lhs
        long deltaInSec = (rhs - lhs) / 1000;
        long hours = deltaInSec / 3600;
        long mins = (deltaInSec / 60) % 60;
        long secs = deltaInSec % 60;

        StringBuffer sb = new StringBuffer();
        if (hours != 0) {
            sb.append(hours + (hours > 1 ? " hours " : " hour "));
        }
        if (mins != 0) {
            sb.append(mins + (mins > 1 ? " mins " : " min "));
        }
        if (secs != 0) {
            sb.append(secs + (secs > 1 ? " secs " : " sec "));
        }
        return sb.toString();
    }

    public static ComponentPlugin createComponentPlugin() {
        return new ComponentPlugin() {

            @Override
            public void init(Component component) {
                component.addListener(Events.Render,
                        new Listener<ComponentEvent>() {

                            @Override
                            public void handleEvent(ComponentEvent be) {
                                El elem = be.getComponent().el().findParent(
                                        ".x-form-element", 3);
                                elem
                                        .appendChild(XDOM
                                                .create("<div style='font-size: 11px; color: #615f5f; padding: 1 0 2 0px;'>"
                                                        + be
                                                                .getComponent()
                                                                .getData("text")
                                                        + "</div>"));
                            }
                        });
            }
        };
    }

    public static ArrayList<String> getSupportedOS() {
        ArrayList<String> list = new ArrayList<String>();
        for (OS os : OS.values()) {
            list.add(os.osName());
        }

        return list;
    }

    public static ArrayList<String> getSupportedARCH() {
        ArrayList<String> list = new ArrayList<String>();
        for (Arch arch : Arch.values()) {
            list.add(arch.name());
        }

        return list;
    }
}
