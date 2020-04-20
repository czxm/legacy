package com.intel.cedar.service.client.feature.view;

import java.util.ArrayList;

public class TFieldSet extends TBaseUI {
    private int labelWidth;
    private String label;
    public ArrayList<TUI> children = new ArrayList<TUI>();

    public UIType getType() {
        return UIType.FIELDSET;
    }

    public boolean isContainer() {
        return true;
    }

    public void addChild(TUI obj) {
        children.add(obj);
    }

    public void accept(UIBuilder builder) {
        // builder.visitFieldSet(this);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public ArrayList<TUI> getChildren() {
        return children;
    }

    public void setLabelWidth(int labelWidth) {
        this.labelWidth = labelWidth;
    }

    public int getLabelWidth() {
        return labelWidth;
    }
}
