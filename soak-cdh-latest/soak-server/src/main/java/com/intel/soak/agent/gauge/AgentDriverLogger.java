package com.intel.soak.agent.gauge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.soak.logger.AbstractAgentLogger;
import com.intel.soak.logger.DriverLogger;

public class AgentDriverLogger extends AbstractAgentLogger implements DriverLogger{
    private static Logger LOG = LoggerFactory.getLogger(AgentDriverLogger.class);
    
    public AgentDriverLogger() {
        super("Driver");
    }

    @Override
    public void info(String log) {
        LOG.info("{}({}): {}", getComponent(), getSource(), log);
    }

    @Override
    public void error(String log) {
        LOG.error("{}({}): {}", getComponent(), getSource(), log); 
    }

    @Override
    public void trace(String log) {
        LOG.trace("{}({}): {}", getComponent(), getSource(), log);
    }

    @Override
    public void debug(String log) {
        LOG.debug("{}({}): {}", getComponent(), getSource(), log);  
    }

    @Override
    public void warn(String log) {
        LOG.warn("{}({}): {}", getComponent(), getSource(), log); 
    }

}
