package com.intel.cedar.service.client.feature.view;

import java.util.ArrayList;
import java.util.HashMap;

import com.intel.cedar.service.client.feature.model.MachineFeature;

public class TListField extends TBaseUI {
    private HashMap<MachineFeature, ArrayList<String>> cases;

    private ArrayList<TUI> depends = new ArrayList<TUI>();

    public TListField() {

    }

    public ArrayList<String> getCaseSet(MachineFeature machine) {
        return cases.get(machine);
    }

    public void setCaseSet(MachineFeature machine, ArrayList<String> caseSet) {
        cases.put(machine, caseSet);
    }

    public void addDepend(TUI obj) {
        depends.add(obj);
    }
}
