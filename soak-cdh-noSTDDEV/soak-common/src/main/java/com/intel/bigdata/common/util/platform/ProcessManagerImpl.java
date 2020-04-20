package com.intel.bigdata.common.util.platform;

import com.intel.bigdata.common.util.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class arranges to perform service related operations.
 */
@Component
public class ProcessManagerImpl implements ProcessManager {

    protected static Logger LOG = LoggerFactory.getLogger(ProcessManagerImpl.class);

    @Override
    public void restart(String name) throws IOException {
        stop(name);
        start(name);
    }

    @Override
    public void start(String name) throws IOException {
        service(name, "start");
    }

    @Override
    public void stop(String name) throws IOException {
        service(name, "stop");
    }

    @Override
    public String status(String name) throws IOException {
        List<String> output = service(name, "status");
        StringBuilder sb = new StringBuilder();
        for(String line : output)
            sb.append(line);
        return sb.toString();
    }

    private List<String> service(String name, String operation) throws IOException {
        try {
            List<String> output = new ArrayList<String>();
            int val = Command.executeWithOutput(output, 60000, "service", name, operation);
            if(val!=0)
                throw new IOException("Fail to execute service command. Exit code is "+val);
            return output;
        } catch (Exception e) {
            LOG.error("failed to %s service %s: %s", operation, name, e.getMessage());
            throw new IOException(e);
        }
    }

}