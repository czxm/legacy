package com.intel.cedar.feature;

public class TaskSummaryItem {
    private String name;
    private String value;
    private boolean isHyperLink;
    private String url;
    private String style;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isHyperLink() {
        return isHyperLink;
    }

    public void setHyperLink(boolean isHyperLink) {
        this.isHyperLink = isHyperLink;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
}
