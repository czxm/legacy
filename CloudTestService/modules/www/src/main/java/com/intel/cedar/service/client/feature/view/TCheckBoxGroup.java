package com.intel.cedar.service.client.feature.view;

import java.util.ArrayList;

public class TCheckBoxGroup extends TBaseUI {
    private String label;

    private ArrayList<String> boxLabelList = new ArrayList<String>();

    @Override
    public void accept(UIBuilder builder) {
        // builder.visitCheckBoxGroup(this);
    }

    @Override
    public void addChild(TUI obj) {
        // TODO Auto-generated method stub

    }

    @Override
    public UIType getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isContainer() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void addBoxLabel(String boxLabel) {
        boxLabelList.add(boxLabel);
    }

    public ArrayList<String> getBoxLabelList() {
        return boxLabelList;
    }

}
