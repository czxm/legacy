package com.intel.cedar.service.client.feature.model.ui;

import java.io.Serializable;
import java.util.HashMap;

import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.service.client.feature.model.Variable.VarType;
import com.intel.cedar.service.client.feature.model.uitl.UIUtils;

public class VariableBuffer implements Serializable {
    private HashMap<String, Variable> variables = new HashMap<String, Variable>();

    private static final long serialVersionUID = UIUtils
            .getSerialVUID("VariableBuffer");

    public VariableBuffer() {

    }

    public void addVariable(Variable v) {
        String name = v.getName();
        Variable other = variables.get(name);
        if (other != null) {
            if (other.getType().equals(VarType.LOCAL_V)
                    && v.getType().equals(VarType.IMPORT_V)) {
                // do not update
                return;
            }
        }

        variables.put(name, v);
    }

    public Variable getVariable(String name) {
        if (name == null) {
            return null;
        }

        return variables.get(name);
    }
}
