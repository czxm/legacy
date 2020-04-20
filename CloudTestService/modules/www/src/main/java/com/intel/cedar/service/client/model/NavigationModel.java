package com.intel.cedar.service.client.model;

public class NavigationModel extends CedarBaseModel {

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    private String name;

    public NavigationModel() {

    }

    public NavigationModel(String name) {
        setName(name);
    }

    public void setName(String name) {
        this.name = name;
        set("name", name);
    }

    public String getName() {
        return name;
    }

}
