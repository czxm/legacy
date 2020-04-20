package com.intel.cedar.service.client.feature.model.ui;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;
import com.intel.cedar.service.client.feature.view.UIBuilder;

public class SubmitModel extends UIBaseNodes {

    private static final long serialVersionUID = UIUtils
            .getSerialVUID("SubmitModel");

    public SubmitModel() {
        super();
    }

    public void accept(UIBuilder builder) {
        if (builder == null) {
            return;
        }

        builder.visitSubmit(this);
    }
}
