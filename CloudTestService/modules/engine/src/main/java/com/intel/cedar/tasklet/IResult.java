/**
 * 
 */
package com.intel.cedar.tasklet;

/**
 * @author xhao1
 * 
 */
public interface IResult extends java.io.Serializable {
    public ResultID getID();

    public String getLog();

    public String getFailureMessage();
}
