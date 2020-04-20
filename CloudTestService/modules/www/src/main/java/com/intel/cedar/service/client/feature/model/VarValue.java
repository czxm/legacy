package com.intel.cedar.service.client.feature.model;

import java.io.Serializable;
import java.util.List;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;
import com.intel.cedar.service.client.model.CedarBaseModel;

public class VarValue extends CedarBaseModel implements Serializable {
    private String value;
    private Params params = new Params();

    private static final long serialVersionUID = UIUtils
            .getSerialVUID("VariableMap");

    public VarValue() {

    }

    public VarValue(String value, Params params) {
        this.value = value;
        this.params = params;
    }

    public VarValue(String value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public void refresh() {
        set("VariableValue", value);
        List<String> params = getParamNames();
        for (String p : params) {
            String value = getParamValue(p);
            if (value != null) {
                set(p, value);
            }
        }
    }

    public Params getParams() {
        return params;
    }

    public List<String> getParamNames() {
        if (params == null) {
            return UIUtils.EMPTY_STR;
        }

        return params.getNames();
    }

    public String getParamValue(String param) {
        if (param == null) {
            return null;
        }

        return params.getValue(param);
    }

    public void addParam(String name, String value) {
        params.addParam(name, value);
    }

    public boolean compare(VarValue other) {
        if (other == null) {
            return false;
        }

        return match(other.getParams());
    }

    public boolean match(Params otherParams) {
        for (String p : otherParams.getNames()) {
            String v1 = otherParams.getValue(p);
            String v2 = this.getParamValue(p);
            if (v1 == null || v2 == null || false == v1.equalsIgnoreCase(v2)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean match(String str){
        return this.value.contains(str);
    }
}
