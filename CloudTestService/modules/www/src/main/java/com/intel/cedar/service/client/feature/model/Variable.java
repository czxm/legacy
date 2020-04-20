package com.intel.cedar.service.client.feature.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;

public class Variable implements Serializable {

    private String name;
    private VarType type;
    private List<VarValue> varValues = new ArrayList<VarValue>();

    private static final long serialVersionUID = UIUtils
            .getSerialVUID("UIVariable");

    public Variable() {
    }

    public Variable(String name, VarType type) {
        this.name = name;
        this.type = type;
    }

    public void clearValues() {
        varValues.clear();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void addValue(String value) {
        this.varValues.add(new VarValue(value));
    }

    public void addVarValue(VarValue value) {
        varValues.add(value);
    }

    public void addVarValues(List<VarValue> values) {
        varValues.addAll(values);
    }

    public List<VarValue> getVarValues(Params params) {
        List<VarValue> l = new ArrayList<VarValue>();
        for (VarValue v : varValues) {
            if (v.match(params)) {
                l.add(v);
            }
        }
        return l;
    }

    public List<VarValue> getVarValues(List<Variable> depends) {
        List<VarValue> l = new ArrayList<VarValue>();
        for (VarValue vv : varValues) {
            boolean match = true;
            for (Variable dv : depends) {
                for (VarValue ov : dv.getVarValues()) {
                    if (false == vv.compare(ov)) {
                        match = false;
                        break;
                    }
                }
            }
            if (match) {
                l.add(vv);
            }
        }
        return l;
    }

    public List<VarValue> getVarValues() {
        return varValues;
    }

    public List<String> getValues() {
        List<String> l = new ArrayList<String>();
        for (VarValue vm : varValues) {
            l.add(vm.getValue());
        }

        return l;
    }

    public void setType(VarType type) {
        this.type = type;
    }

    public VarType getType() {
        return this.type;
    }

    public String getValue() {
        if (varValues.size() > 0) {
            return varValues.get(0).getValue();
        }

        return null;
    }

    public void setValue(String val) {
        clearValues();
        addValue(val);
    }

    public void setValues(String[] val) {
        clearValues();
        for (String v : val) {
            VarValue nv = new VarValue();
            nv.setValue(v);
            addVarValue(nv);
        }
    }

    public enum VarType {
        IMPORT_V, LOCAL_V,
    };

    public Variable clone() {
        Variable v = new Variable(this.name, this.type);
        for (VarValue tv : this.getVarValues()) {
            VarValue vv = new VarValue();
            String value = tv.getValue();
            vv.setValue(value);
            for (String pn : tv.getParamNames()) {
                vv.addParam(pn, tv.getParamValue(pn));
            }
            v.addVarValue(vv);
        }
        return v;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" = [");
        boolean hasMoreValues = false;
        for (VarValue s : varValues) {
            if (hasMoreValues)
                sb.append(",");
            sb.append(s.getValue());
            hasMoreValues = true;
        }
        sb.append("]");
        ;
        return sb.toString();
    }
}
