package com.intel.cedar.engine.model.feature.flow;

import com.intel.cedar.engine.xml.StandardNames;

public class MachineParameter {
    protected int fingerprint;
    protected int min;
    protected int max;
    protected String value;

    public static int MINDEFAULT = 0;
    public static int MAXDEFAULT = 10;

    public MachineParameter(int fingerprint) {
        this(fingerprint, MINDEFAULT, MAXDEFAULT);
    }

    public MachineParameter(int fingerprint, int min, int max) {
        this.fingerprint = fingerprint;
        this.min = min;
        this.max = max;
    }

    public int getFingerPrint() {
        return this.fingerprint;
    }

    public String getName() {
        return StandardNames.getLocalName(fingerprint);
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMin() {
        return this.min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMax() {
        return this.max;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
