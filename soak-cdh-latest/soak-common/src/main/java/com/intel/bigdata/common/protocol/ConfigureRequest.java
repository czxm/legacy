package com.intel.bigdata.common.protocol;

public class ConfigureRequest {


    private String target;

    private String content;

    public ConfigureRequest() {
        this(null, null);
    }

    public ConfigureRequest(String target, String content) {
        this.target = target;
        this.content = content;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ConfigureRequest [target=" + target + ", content=" + content + "]";
    }

}

