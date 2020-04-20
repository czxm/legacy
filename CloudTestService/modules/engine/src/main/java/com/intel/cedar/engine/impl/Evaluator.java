package com.intel.cedar.engine.impl;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.jexl2.Expression;

import com.intel.cedar.service.client.feature.model.Variable;

public class Evaluator {
    private JexlEngine jexl;
    private JexlContext jc;
    private VariableManager vars;

    public Evaluator(VariableManager vars) {
        this.vars = vars;
        jexl = new JexlEngine();
        jc = new MapContext();
        for (Variable var : this.vars.getVariables()) {
            jc.set("$" + var.getName(), var.getValue());
            jc.set("$" + var.getName() + "_length", var.getValues().size());
            for(int i = 0; i < var.getValues().size(); i++){
                jc.set("$" + var.getName() + "_" + i, var.getValues().get(i));
            }
        }
        jexl.setSilent(true);
    }

    public Integer evalAsInteger(String expr) {
        if (expr == null)
            return 0;
        Expression e = jexl.createExpression(expr);
        try {
            Object value = e.evaluate(jc);
            if (value instanceof Integer)
                return (Integer) value;
            else if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
        } catch (Exception ex) {
        }
        return 0;
    }

    public String eval(String expr) {
        if (expr == null)
            return "";
        // must contain variable reference
        if (expr.indexOf("$") < 0)
            return expr;
        Expression e = jexl.createExpression(expr);
        try {
            Object value = e.evaluate(jc);
            if (value instanceof String)
                return (String) value;
        } catch (Exception ex) {
        }
        return expr;
    }
}
