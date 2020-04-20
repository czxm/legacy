package com.intel.cedar.util.protocal;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ModelStream<TYPE> {

    private XStream xstream = null;

    public ModelStream() {
        xstream = new XStream(new DomDriver());
    }

    public String serialize(TYPE example) {
        return xstream.toXML(example);
    }

    @SuppressWarnings("unchecked")
    public TYPE generate(String content) {
        return (TYPE) xstream.fromXML(content);
    }
}