package com.intel.soak.gauge.measure;

import java.util.List;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import com.intel.soak.gauge.GaugeMetrics;


public class Evaluator {
    private JexlEngine jexl;
    private JexlContext jc;

    public Evaluator(GaugeMetrics metrics) {
        jexl = new JexlEngine();
        jexl.setStrict(true);
        jc = new MapContext();
        jc.set("Timestamp", metrics.getTimestamp());
        List<String> names = metrics.getMetricsNames();
        List<Float> values = metrics.getMetricsValues();
        for(int i = 0; i < names.size(); i++){
            jc.set(names.get(i), values.get(i));
        }
    }

    public Float eval(String expr) {
        if (expr == null || expr.length() == 0)
            return null;
        Expression e = jexl.createExpression(expr);
        try {
            Object value = e.evaluate(jc);
            if (value instanceof Double)
                return ((Double)value).floatValue();
        } catch (Exception ex) {
        }
        return null;
    }
}
