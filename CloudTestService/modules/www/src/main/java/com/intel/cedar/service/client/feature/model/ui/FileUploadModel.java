package com.intel.cedar.service.client.feature.model.ui;

import java.util.List;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;
import com.intel.cedar.service.client.feature.view.UIBuilder;

public class FileUploadModel extends UIBaseNode implements IBindModelProvider {
    private BindModel bind;
    private static final long serialVersionUID = UIUtils
            .getSerialVUID(UIUtils.UI_FILEUPLOADFIELD);

    public FileUploadModel() {
        super();
    }
    
    @Override
    public void accept(UIBuilder builder) {
        if (builder == null) {
            return;
        }
        builder.visitFileUploadField(this);
    }

    @Override
    public List<String> getValues() {
        if (bind == null) {
            return BindModel.EMPTY;
        }
        return bind.getValues();
    }

    /*
     * public void setDefault(String defaultValue){ this.defaultValue =
     * defaultValue; }
     * 
     * public String getDefault(){ return this.defaultValue; }
     */

    @Override
    public BindModel getBind() {
        return bind;
    }

    @Override
    public void setBind(String name) {
        bind = new BindModel(name);
    }

    @Override
    public void setBind(BindModel bind) {
        this.bind = bind;
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
