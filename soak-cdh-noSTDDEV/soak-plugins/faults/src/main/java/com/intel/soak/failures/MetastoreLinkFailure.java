package com.intel.soak.failures;

import org.apache.bigtop.itest.failures.NetworkShutdownFailure;

import java.util.List;


public class MetastoreLinkFailure extends NetworkShutdownFailure {
    private static final String DROP_INPUT_CONNECTIONS = "sudo iptables -A INPUT -p tcp --sport 9083 -s %s -j DROP";
    private static final String RESTORE_INPUT_CONNECTIONS = "sudo iptables -D INPUT -p tcp --sport 9083 -s %s -j DROP";
    private static final String DROP_OUTPUT_CONNECTIONS = "sudo iptables -A OUTPUT -p tcp --dport 9083 -d %s -j DROP";
    private static final String RESTORE_OUTPUT_CONNECTIONS = "sudo iptables -D OUTPUT -p tcp --dport 9083 -d %s -j DROP";

    /**
     * Creates list of network disruptions between specified hosts.
     *
     * @param srcHost host whose connections will but cut
     * @param dstHosts destination hosts connections to which from srcHost will be shut down.
     */
    MetastoreLinkFailure(String srcHost, List<String> dstHosts) {
        super(srcHost, dstHosts);
        populateCommandsList(srcHost, dstHosts);
    }

    /**
     * Creates list of network disruptions between specified hosts,
     * allows to set all additional params.
     *
     * @param srcHost host whose connections will but cut
     * @param dstHosts destination hosts connections to which from srcHost will be shut down
     * @param startDelay time in milliseconds) the failures will wait before start
     */
    MetastoreLinkFailure(String srcHost, List<String> dstHosts, long startDelay) {
        super(srcHost, dstHosts, startDelay);
        populateCommandsList(srcHost, dstHosts);
    }

    /*
 * Populate commands list, making choice between local execution and remote one.
 */
    private void populateCommandsList(String host, List<String> dstHosts){
        failCommands.clear();
        restoreCommands.clear();
        if ("localhost".equalsIgnoreCase(host)) {
            for(String dstHost : dstHosts){
                failCommands.add(String.format(DROP_INPUT_CONNECTIONS, dstHost));
                failCommands.add(String.format(DROP_OUTPUT_CONNECTIONS, dstHost));
                restoreCommands.add(String.format(RESTORE_INPUT_CONNECTIONS, dstHost));
                restoreCommands.add(String.format(RESTORE_OUTPUT_CONNECTIONS, dstHost));
            }
        } else {
            for(String dstHost : dstHosts){
                failCommands.add(getSshWrappedCommand(String.format(DROP_INPUT_CONNECTIONS, dstHost), host));
                failCommands.add(getSshWrappedCommand(String.format(DROP_OUTPUT_CONNECTIONS, dstHost), host));
                restoreCommands.add(getSshWrappedCommand(String.format(RESTORE_INPUT_CONNECTIONS, dstHost), host));
                restoreCommands.add(getSshWrappedCommand(String.format(RESTORE_OUTPUT_CONNECTIONS, dstHost), host));
            }
        }
    }
}

