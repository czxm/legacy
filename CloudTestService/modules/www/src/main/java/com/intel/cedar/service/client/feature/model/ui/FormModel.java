package com.intel.cedar.service.client.feature.model.ui;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;
import com.intel.cedar.service.client.feature.view.UIBuilder;

public class FormModel extends UIBaseNodes {

    private static final long serialVersionUID = UIUtils
            .getSerialVUID(UIUtils.UI_FORM);

    public FormModel() {
        super();
    }

    @Override
    public void accept(UIBuilder builder) {
        if (builder == null) {
            return;
        }
        builder.visitForm(this);
    }

}
