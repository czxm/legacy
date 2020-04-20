package com.intel.soak.agent.gauge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.soak.logger.AbstractAgentLogger;
import com.intel.soak.logger.TransactionLogger;

public class AgentTransactionLogger extends AbstractAgentLogger implements TransactionLogger{
    private static Logger LOG = LoggerFactory.getLogger(AgentTransactionLogger.class);
    private String user;
    
    public AgentTransactionLogger() {
        super("Transaction");
    }

    @Override
    public void setUser(String user){
        this.user = user;
    }

    @Override
    public String getUser(){
        return this.user;
    }

    @Override
    public void info(String log) {
        LOG.info("{}({})[{}]: {}", getComponent(), getSource(), getUser(), log);
    }

    @Override
    public void error(String log) {
        LOG.error("{}({})[{}]: {}", getComponent(), getSource(), getUser(), log); 
    }

    @Override
    public void trace(String log) {
        LOG.trace("{}({})[{}]: {}", getComponent(), getSource(), getUser(), log);
    }

    @Override
    public void debug(String log) {
        LOG.debug("{}({})[{}]: {}", getComponent(), getSource(), getUser(), log);  
    }

    @Override
    public void warn(String log) {
        LOG.warn("{}({})[{}]: {}", getComponent(), getSource(), getUser(), log); 
    }

}
