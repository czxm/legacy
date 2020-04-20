package com.intel.cedar.service.client.feature.model.ui;

import java.util.List;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;

public class RevModel extends UIBaseNode implements IBindModelProvider {
    private BindModel bind;

    private static final long serialVersionUID = UIUtils
            .getSerialVUID("RevModel");

    public RevModel() {
        super();
    }

    @Override
    public BindModel getBind() {
        return bind;
    }

    @Override
    public void setBind(BindModel bind) {
        this.bind = bind;
    }

    @Override
    public void setBind(String name) {
        bind = new BindModel(name);
    }

    @Override
    public List<String> getValues() {
        if (bind == null) {
            return BindModel.EMPTY;
        }
        return bind.getValues();
    }

    @Override
    public void init() {
        super.init();

        if (bind == null) {
            return;
        }

        bind.setFeature(getFeature());
    }
}
