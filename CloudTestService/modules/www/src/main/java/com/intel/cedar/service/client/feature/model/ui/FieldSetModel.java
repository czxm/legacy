package com.intel.cedar.service.client.feature.model.ui;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;
import com.intel.cedar.service.client.feature.view.UIBuilder;

public class FieldSetModel extends UIBaseNodes {

    private static final long serialVersionUID = UIUtils
            .getSerialVUID(UIUtils.UI_FIELDSET);

    public FieldSetModel() {
        super();
    }

    public void accept(UIBuilder builder) {
        builder.visitFieldSet(this);
    }
}
