package com.intel.cedar.service.client.view;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

public abstract class ComponentViewer extends LayoutContainer {

    public static enum TabID {
        CLOUD("Cloud"), IMAGE("Image"), INSTANCE("Instance"), TYPE("Type"), KEY(
                "Key"), LAUNCH("Launch");

        String id = null;

        TabID(String id) {
            this.id = id;
        }

        public String toString() {
            return id;
        }
    }

    public abstract void updateView();
}
