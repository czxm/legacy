package com.intel.cedar.engine.impl;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.intel.cedar.service.client.feature.model.Variable;

public class VariableManager {
    private HashMap<String, Variable> variables = new HashMap<String, Variable>();

    public VariableManager(List<Variable> vars) {
        for (Variable var : vars) {
            variables.put(var.getName(), var);
        }
    }

    public List<Variable> getVariables() {
        return Lists.newArrayList(variables.values());
    }

    public String getValue(String name) {
        Variable var = variables.get(name);
        if (var != null) {
            return var.getValue();
        }
        return "";
    }

    public List<String> getValues(String name) {
        Variable var = variables.get(name);
        if (var != null) {
            return var.getValues();
        }
        return Lists.newArrayList();
    }

    public synchronized void putValue(String name, Variable newVar) {
        Variable var = variables.get(name);
        if (var != null) {
            var.clearValues();
            var.addVarValues(newVar.getVarValues());
        }
    }

    public Variable getVariable(String name) {
        return variables.get(name);
    }

    public synchronized void putVariable(Variable var) {
        variables.put(var.getName(), var);
    }
}
