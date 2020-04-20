package com.intel.cedar.service.client.feature.model.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;

public enum DependsAction implements Serializable {
    UNKOWN(-1, "unkonw"), GET_VALUE(0, "getValue"), GET_VALUES(1, "getValues"), GET_SELECTED(
            2, "getSelect"), GET_CHECK(3, "getCheck"), GET_SELECTIONS(4,
            "getSelections");

    private int action;
    private String name;
    public static List<DependsAction> ACTIONS;

    private static final long serialVersionUID = UIUtils
            .getSerialVUID("DependsAction");

    private DependsAction(int action, String name) {
        this.action = action;
        this.name = name;
    }

    public int getAction() {
        return action;
    }

    public String getName() {
        return name;
    }

    public static List<DependsAction> getActions() {
        if (ACTIONS == null) {
            computeActions();
        }
        return ACTIONS;
    }

    public static DependsAction getDependsAction(int id) {
        List<DependsAction> actions = getActions();
        for (int i = 0; i < actions.size(); i++) {
            DependsAction action = actions.get(i);
            if (action.getAction() == id) {
                return action;
            }
        }
        return UNKOWN;
    }

    public static DependsAction getDependsAction(String name) {
        if (name == null) {
            return UNKOWN;
        }

        List<DependsAction> actions = getActions();
        for (int i = 0; i < actions.size(); i++) {
            DependsAction action = actions.get(i);
            if (name.equalsIgnoreCase(action.getName())) {
                return action;
            }
        }
        return UNKOWN;
    }

    protected static void computeActions() {
        ACTIONS = new ArrayList<DependsAction>();
        ACTIONS.add(GET_VALUE);
        ACTIONS.add(GET_VALUES);
        ACTIONS.add(GET_SELECTED);
        ACTIONS.add(GET_CHECK);
        ACTIONS.add(GET_SELECTIONS);
        ACTIONS.add(UNKOWN);
    }
}
