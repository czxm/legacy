package com.intel.cedar.engine.model.feature;

import com.intel.cedar.engine.model.CollectionDataModel;
import com.intel.cedar.engine.model.IDataModelDocument;

public class LaunchSet extends CollectionDataModel<Launch> {
    private String name;
    private String desc;
    private boolean enabled;
    private Option option;

    public LaunchSet(IDataModelDocument document) {
        super(document);
        option = new Option(document);
        this.enabled = true;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
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
}
