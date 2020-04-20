package com.intel.cedar.service.client;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;

public class LoginPage extends LayoutContainer {

    public static CloudRemoteServiceAsync _cloudRemoteService = GWT
            .create(CloudRemoteService.class);

    public void onRender(Element target, int index) {
        super.onRender(target, index);
        CenterLayout centerLayout = new CenterLayout();
        this.setLayout(centerLayout);
        int width = 400, height = 300;
        LayoutContainer innerContainer = new LayoutContainer();
        innerContainer.setWidth(width);
        innerContainer.setHeight(height);
        innerContainer.setStyleAttribute("background-color", "#e8e8ff");
        add(innerContainer);
    }
}
