package com.intel.cedar.util.protocal;

import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.impl.Result;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ConversionProtocal {

    private XStream xstream = null;

    public ConversionProtocal() {
        xstream = new XStream(new DomDriver());
    }

    public String serializeTaskItem(ITaskItem taskItem) {
        // System.out.println(xstream.toXML(taskItem));
        return xstream.toXML(taskItem);
    }

    public ITaskItem generateTaskItem(String content) {
        // System.out.println(content);
        return (ITaskItem) xstream.fromXML(content);
    }

    public String serializeResult(IResult result) {
        return xstream.toXML(result);
    }

    public Result generateResult(String content) throws Exception{
        try{
            return (Result) xstream.fromXML(content);
        }
        catch(Exception e){
            System.out.println("Content=" + content);
            throw e;
        }
    }
}