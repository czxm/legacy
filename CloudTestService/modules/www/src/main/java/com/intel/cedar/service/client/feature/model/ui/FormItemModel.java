package com.intel.cedar.service.client.feature.model.ui;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;
import com.intel.cedar.service.client.feature.view.UIBuilder;

public class FormItemModel extends UIBaseNode {

    private static final long serialVersionUID = UIUtils
            .getSerialVUID("FormItemModel");

    public FormItemModel() {
        super();
    }

    public void accept(UIBuilder builder) {
        if (builder == null) {
            return;
        }

        builder.visitFormItem(this);
    }
}
