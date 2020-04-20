package com.intel.soak.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * The abstract Local Logger structure <p>
 * @author xzhan27
 *
 */

public abstract class AbstractLocalLogger implements SoakLogger{
    private static Logger LOG = LoggerFactory.getLogger(AbstractLocalLogger.class);    

    public void info(String log){
        LOG.info(log);
    }
    public void error(String log){
        LOG.error(log);
    }
    public void trace(String log){
        LOG.trace(log);
    }
    public void debug(String log){
        LOG.debug(log);
    }
    public void warn(String log){
        LOG.warn(log);
    }
}