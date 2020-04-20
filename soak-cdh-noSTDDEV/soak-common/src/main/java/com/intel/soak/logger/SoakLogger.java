/**
 * 
 */
package com.intel.soak.logger;

/**
 * @author xzhan27
 *
 */
public interface SoakLogger {
    public void info(String log);
    public void error(String log);
    public void trace(String log);
    public void debug(String log);
    public void warn(String log);
}
