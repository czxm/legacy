package com.intel.cedar.service.client.feature.model.ui;

import java.io.Serializable;

public interface IBindModelProvider extends Serializable {
    /**
     * set the bind with the bind name
     */
    public void setBind(String name);

    /**
     * set the bind
     */
    public void setBind(BindModel bind);

    /**
     * get the bind
     */
    public IBindModel getBind();

}
