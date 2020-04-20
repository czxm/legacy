package com.intel.cedar.engine.model.feature;

import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;
import com.intel.cedar.service.client.feature.model.Variable;

public class Import extends DataModel {
    private List<Variable> variables = new ArrayList<Variable>();
    private List<Tasklet> tasklets = new ArrayList<Tasklet>();

    public Import(IDataModelDocument document) {
        super(document);
    }

    public List<Variable> getVariables() {
        return this.variables;
    }

    public List<Tasklet> getTasklets() {
        return this.tasklets;
    }

    public void addVariable(String name) {
        Variable v = new Variable(name, Variable.VarType.IMPORT_V);
        addVariable(v);
    }

    public void addVariable(Variable v) {
        v.setType(Variable.VarType.IMPORT_V);
        variables.add(v);
    }

    public void addTasklet(String id) {
        Tasklet t = new Tasklet(this.getDocument());
        t.setID(id);
        addTasklet(t);
    }

    public void addTasklet(Tasklet t) {
        tasklets.add(t);
    }

    public void removeVariable(String name) {
        for (int i = 0; i < variables.size(); i++) {
            Variable v = variables.get(i);
            if (name == v.getName()) {
                variables.remove(i);
                break;
            }
        }
    }

    public void removeVariable(Variable v) {
        variables.remove(v);
    }

    public void removeTasklet(String id) {
        for (int i = 0; i < tasklets.size(); i++) {
            Tasklet t = tasklets.get(i);
            if (t.getID() == id) {
                tasklets.remove(i);
                break;
            }
        }
    }

    public void removeTasklet(Tasklet t) {
        tasklets.remove(t);
    }
}
