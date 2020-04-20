package com.intel.cedar.agent;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.xml.rss.util.CmdExecutionResult;
import com.intel.xml.rss.util.SimpleCmdExecutor;

public class GatewayManager {
    private static Logger LOG = LoggerFactory.getLogger(GatewayManager.class);
    private static GatewayManager singleton;

    public static GatewayManager getInstance() {
        if (singleton == null) {
            singleton = new GatewayManager();
        }
        return singleton;
    }

    private int startPort;
    private int endPort;
    private String intf;
    private int mappedPorts;

    private GatewayManager() {
    }

    protected String getInetAddr(String host) {
        if (host != null) {
            boolean isAddr = true;
            // very simple IP checking
            for (char a : host.toCharArray()) {
                if (a != '.' && (a > '9' || a < '0')) {
                    isAddr = false;
                    break;
                }
            }
            if (isAddr)
                return host;
            try {
                return InetAddress.getByName(host).getHostAddress();
            } catch (Exception e) {
            }
        }
        return host;
    }

    public List<NATEntry> getNATEntriesByHost(String host) {
        List<NATEntry> entries = new ArrayList<NATEntry>();
        host = getInetAddr(host);
        SimpleCmdExecutor executor = new SimpleCmdExecutor();
        try {
            CmdExecutionResult result = executor.execute(new String[] {
                    "iptables", "-t", "nat", "-L", "-n" });
            BufferedReader reader = new BufferedReader(new StringReader(
                    result.log));
            String line = "";
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("DNAT")) {
                    NATEntry entry = new NATEntry();
                    String[] params = line.split("\\s+");
                    String mappedPortString = params[6];
                    String portHostString = params[7];
                    params = portHostString.split(":");
                    entry.host = params[1];
                    entry.port = params[2];
                    if (host != null && !params[1].equals(host))
                        continue;
                    entry.mappedPort = mappedPortString.replace("dpt:", "");
                    entries.add(entry);
                }
            }

        } catch (Exception e) {
        }
        return entries;
    }

    private List<NATEntry> getNATEntries() {
        return getNATEntriesByHost(null);
    }

    private boolean isPortUsed(int port, List<NATEntry> entries) {
        for (NATEntry entry : entries) {
            if (Integer.parseInt(entry.mappedPort) == port)
                return true;
        }
        return false;
    }

    private int findAvailablePort(int start, int end) {
        List<NATEntry> entries = getNATEntries();
        for (int i = start; i <= end; i++) {
            if (!isPortUsed(i, entries))
                return i;
        }
        return -1;
    }

    private int findAvailablePort() {
        return findAvailablePort(startPort, endPort);
    }

    public synchronized int allocatePortMapping(String host, String port) {
        try {
            host = getInetAddr(host);
            for(NATEntry e : getNATEntriesByHost(host)){
                if(e.port.equals(port))
                    return Integer.parseInt(e.mappedPort);
            }
            int i = findAvailablePort();
            if (i > 0) {
                SimpleCmdExecutor executor = new SimpleCmdExecutor();
                CmdExecutionResult result = executor.execute(new String[] {
                        "iptables", "-t", "nat", "-A", "PREROUTING", "-p",
                        "tcp", "--dport", String.format("%d", i), "-i", intf,
                        "-j", "DNAT", "--to",
                        String.format("%s:%s", host, port) });
                LOG.info("{} to map {}:{} to {}", new Object[] {
                        result.exitValue == 0 ? "succeeded" : "failed", host,
                        port, i });
                if (result.exitValue == 0) {
                    mappedPorts++;
                    return i;
                }
            }
        } catch (Exception e) {
        }
        return -1;
    }

    public synchronized void releasePortMapping(String host, String port,
            String mappedPort) {
        try {
            host = getInetAddr(host);
            SimpleCmdExecutor executor = new SimpleCmdExecutor();
            CmdExecutionResult result = executor.execute(new String[] {
                    "iptables", "-t", "nat", "-D", "PREROUTING", "-p", "tcp",
                    "--dport", mappedPort, "-i", intf, "-j", "DNAT", "--to",
                    String.format("%s:%s", host, port) });
            LOG.info("{} to unmap {}:{} to {}", new Object[] {
                    result.exitValue == 0 ? "succeeded" : "failed", host, port,
                    mappedPort });
            if (result.exitValue == 0)
                mappedPorts--;
        } catch (Exception e) {
        }
    }

    public void clearPortMappings() {
        for (NATEntry e : getNATEntries()) {
            releasePortMapping(e.host, e.port, e.mappedPort);
        }
    }

    public int getTotalMappedPorts() {
        return mappedPorts;
    }

    public void initilalize(Properties props) {
        try {
            startPort = Integer.parseInt(props
                    .getProperty("startPort", "20000"));
        } catch (Exception e) {
            startPort = 20000;
        }
        try {
            endPort = Integer.parseInt(props.getProperty("endPort", "30000"));
        } catch (Exception e) {
            endPort = 30000;
        }
        intf = props.getProperty("interface", "eth1");

        mappedPorts = getNATEntries().size();
        startPort = findAvailablePort(startPort, 60000);
        endPort = findAvailablePort(endPort, 60000);
        if (startPort < 0 || endPort < 0 || startPort >= endPort) {
            LOG.info("Failed to initialize Gateway, exiting ...");
            System.exit(1);
        }
    }
}
