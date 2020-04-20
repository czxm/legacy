package com.intel.cedar.service.client.feature.view;

import java.util.ArrayList;

public class TComboBox extends TBaseUI {
    private String label;

    private ArrayList<String> values = new ArrayList<String>();

    @Override
    public void accept(UIBuilder builder) {
        // builder.visitComboBox(this);
    }

    @Override
    public void addChild(TUI obj) {
        return;
    }

    @Override
    public UIType getType() {
        return UIType.COMBOBOX;
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void addValue(String value) {
        values.add(value);
    }

    public ArrayList<String> getValues() {
        return values;
    }
}
