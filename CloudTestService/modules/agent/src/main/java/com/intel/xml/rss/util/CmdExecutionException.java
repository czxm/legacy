package com.intel.xml.rss.util;

/**
 * Author: hshen5 Created: 2004-12-24
 */
public class CmdExecutionException extends Exception {

    /**
   * 
   */
    private static final long serialVersionUID = 1L;
    private String cmdString;

    public CmdExecutionException(String cmdStringP, String message) {
        super(message);
        this.cmdString = cmdStringP;
    }

    public String getCmdString() {
        return cmdString;
    }

    public String toDetailString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Execution command: ").append(cmdString).append("\n");
        sb.append("Execption detail: ").append(getMessage());
        return new String(sb);
    }

}
