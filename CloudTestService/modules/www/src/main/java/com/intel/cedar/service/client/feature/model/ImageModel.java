package com.intel.cedar.service.client.feature.model;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BeanModelTag;

public class ImageModel implements BeanModelTag, Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private String name;

    private String path;

    public ImageModel() {

    }

    public ImageModel(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImageModel))
            return false;
        ImageModel rhs = (ImageModel) obj;
        if (rhs.getName().equals(name) && rhs.getPath().equals(path))
            return true;
        return false;
    }
}
