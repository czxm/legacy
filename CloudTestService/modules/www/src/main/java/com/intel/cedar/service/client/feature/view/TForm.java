package com.intel.cedar.service.client.feature.view;

import java.util.ArrayList;

public class TForm extends TBaseUI {
    private ArrayList<TUI> children = new ArrayList<TUI>();

    private int labelWidth = 150;

    public TForm() {

    }

    public ArrayList<TUI> getChildren() {
        return children;
    }

    @Override
    public void addChild(TUI obj) {
        // TODO Auto-generated method stub
        children.add(obj);
    }

    @Override
    public boolean isContainer() {
        // TODO Auto-generated method stub
        return true;
    }

    public UIType getType() {
        return UIType.FORM;
    }

    @Override
    public void accept(UIBuilder builder) {
        // builder.visitForm(this);
    }

    public void setLabelWidth(int labelWidth) {
        this.labelWidth = labelWidth;
    }

    public int getLabelWidth() {
        return labelWidth;
    }
}
