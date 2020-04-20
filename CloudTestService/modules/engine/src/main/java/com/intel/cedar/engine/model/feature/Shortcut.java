package com.intel.cedar.engine.model.feature;

import com.intel.cedar.engine.model.CollectionDataModel;
import com.intel.cedar.engine.model.IDataModelDocument;
import com.intel.cedar.service.client.feature.model.ui.WindowModel;

public class Shortcut extends CollectionDataModel<Launch> {
    private String name;
    private String desc;
    private boolean enabled;
    private WindowModel window;
    private Variables variables;

    public Shortcut(IDataModelDocument document) {
        super(document);
        window = new WindowModel();
        variables = new Variables(document);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public WindowModel getWindow() {
        return window;
    }

    public void setWindow(WindowModel window) {
        this.window = window;
    }

    public Variables getVariables() {
        return variables;
    }

    public void setVariables(Variables variables) {
        this.variables = variables;
    }
}
