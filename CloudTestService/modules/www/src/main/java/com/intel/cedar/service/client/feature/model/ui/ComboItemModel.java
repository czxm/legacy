package com.intel.cedar.service.client.feature.model.ui;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;

public class ComboItemModel extends UIBaseNode {
    private String valueAttr;

    private static final long serialVersionUID = UIUtils
            .getSerialVUID(UIUtils.UI_COMBOITEM);

    public ComboItemModel() {
        super();
    }

    public void setValue(String value) {
        this.valueAttr = value;
    }

    public String getValue() {
        return this.valueAttr;
    }
}
