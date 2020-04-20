package com.intel.bigdata.agent;

import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;

public class ConsoleDaemonContext implements DaemonContext {
	
	private DaemonController daemonController;

    private String[] args;

    public ConsoleDaemonContext(DaemonController daemonController, String[] args) {
        this.daemonController = daemonController;
        this.args = args;
    }

    @Override
    public String[] getArguments() {
        return args;
    }

    @Override
    public DaemonController getController() {
        return daemonController;
    }

}
