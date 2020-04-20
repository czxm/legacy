package com.intel.cedar.tasklet.impl;

public class CommandTaskItem extends GenericTaskItem {
    private static final long serialVersionUID = -465648865147604336L;
    private String commandLine = null;

    public String getCommandLine() {
        if (null == this.commandLine) {
            return this.getProperty("cmdline");
        }
        return this.commandLine;
    }

    public void setCommandLine(String cmdline) {
        this.commandLine = cmdline;
    }

    public String getValue() {
        String value = getCommandLine();
        if (value.length() > 17) {
            value = value.substring(0, 17);
            value = value + "...";
        }
        return value;
    }
}