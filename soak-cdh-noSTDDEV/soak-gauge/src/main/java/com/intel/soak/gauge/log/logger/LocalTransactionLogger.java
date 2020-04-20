package com.intel.soak.gauge.log.logger;


import com.intel.soak.logger.TransactionLogger;
import com.intel.soak.logger.AbstractLocalLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * The Transaction Logger is used to log anything in the transaction <p>
 * All the log content will be sent to SLF4J
 * @author xzhan27
 *
 */

public class LocalTransactionLogger extends AbstractLocalLogger implements TransactionLogger{
    private static Logger LOG = LoggerFactory.getLogger(LocalTransactionLogger.class);

    protected String user;

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public void info(String log) {
        LOG.info("[{}]: {}", getUser(), log);
    }

    @Override
    public void error(String log) {
        LOG.error("[{}]: {}", getUser(), log);
    }

    @Override
    public void trace(String log) {
        LOG.trace("[{}]: {}", getUser(), log);
    }

    @Override
    public void debug(String log) {
        LOG.debug("[{}]: {}", getUser(), log);
    }

    @Override
    public void warn(String log) {
        LOG.warn("[{}]: {}", getUser(), log);
    }
}