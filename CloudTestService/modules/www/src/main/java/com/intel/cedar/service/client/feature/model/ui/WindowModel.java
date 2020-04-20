package com.intel.cedar.service.client.feature.model.ui;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;

public class WindowModel extends UIBaseNodes {
    private String title;

    private static final long serialVersionUID = UIUtils
            .getSerialVUID(UIUtils.UI_WINDOW);

    public WindowModel() {
        super();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    /**
     * get the first form if there exist, otherwise null
     */
    public FormModel getForm() {
        UIBaseNode node = getChildren().get(0);
        if (node == null || !(node instanceof FormModel)) {
            return null;
        }
        return (FormModel) node;
    }
}
