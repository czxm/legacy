package com.intel.cedar.service.client.feature.model;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BeanModelTag;

public class TestType implements BeanModelTag, Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 9128031048284107286L;

    private String name;

    private String path;

    public static TestType[] types = new TestType[] {
            new TestType("CPP.Conf",
                    "resources/images/cloudtestservice/cpp.conformance.shadow.png"),
            new TestType("JAVA.Nightly",
                    "resources/images/cloudtestservice/java.nightly.shadow.png"),
            new TestType("JAVA.PV",
                    "resources/images/cloudtestservice/java.pv.shadow.png"),
            new TestType("SplitPoint",
                    "resources/images/cloudtestservice/splitpoint.shadow.png"), };

    public TestType() {

    }

    public TestType(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
