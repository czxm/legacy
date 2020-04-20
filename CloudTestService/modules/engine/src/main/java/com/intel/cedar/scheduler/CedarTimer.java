package com.intel.cedar.scheduler;

import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *  simple scheduler for internal bookkeeping tasks
 */

public class CedarTimer extends Timer {
    private static Logger LOG = LoggerFactory.getLogger(CedarTimer.class);
    private static CedarTimer singleton;

    public static CedarTimer getInstance() {
        if (singleton == null)
            singleton = new CedarTimer();
        return singleton;
    }

    private CedarTimer() {
        super("Cedar Internal Timer");
    }

    public void scheduleTask(int interval, CedarTimerTask task) {
        this.scheduleAtFixedRate(task, 5 * 1000, interval * 1000);
        LOG.info("Scheduled " + task.getName() + " task every " + interval
                + "(s)");
    }
}
