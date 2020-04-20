package com.intel.cedar.service.client.feature.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.intel.cedar.service.client.feature.model.uitl.UIUtils;

public class Params extends BaseModel {
    private HashMap<String, String> parameters = new HashMap<String, String>();

    private static final long serialVersionUID = UIUtils
            .getSerialVUID("Params");

    public Params() {
    }

    public void addParam(String name, String value) {
        parameters.put(name, value);
    }

    public List<String> getNames() {
        String[] params = parameters.keySet().toArray(new String[0]);
        List<String> l = new ArrayList<String>(params.length);
        for (String p : params) {
            l.add(p);
        }
        return l;
    }

    public String getValue(String param) {
        return parameters.get(param);
    }

    public void clear() {
        parameters.clear();
    }
}
