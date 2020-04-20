package com.intel.cedar.engine.model.feature;

import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;
import com.intel.cedar.service.client.feature.model.Variable;

public class Variables extends DataModel {
    List<Variable> childs = new ArrayList<Variable>();
    private Variable.VarType type = Variable.VarType.LOCAL_V;

    public Variables(IDataModelDocument document) {
        super(document);
    }

    public void setType(Variable.VarType type) {
        this.type = type;
    }

    public void addVariable(Variable child) {
        child.setType(type);
        this.childs.add(child);
    }

    public List<Variable> getVariables() {
        return this.childs;
    }
}
