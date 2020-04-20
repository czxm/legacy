package com.intel.cedar.service.client.feature.model.ui;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;
import com.intel.cedar.service.client.feature.view.UIBuilder;

public class TextAreaModel extends UIBaseNode {
    private static final long serialVersionUID = UIUtils
            .getSerialVUID(UIUtils.UI_TEXTAREA);

    public TextAreaModel() {
        super();
    }

    @Override
    public void accept(UIBuilder builder) {
        if (builder == null) {
            return;
        }
        builder.visitTextArea(this);
    }
}
