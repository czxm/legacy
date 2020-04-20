package com.intel.bigdata.common.util.platform;

import com.intel.bigdata.common.util.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This class arranges to perform package related operations.
 */
@Component
public class YumPackageManagerImpl implements PackageManager {

    protected static Logger LOG = LoggerFactory.getLogger(YumPackageManagerImpl.class);

    @Override
    public void install(String name) throws IOException {
        yum("install", name);
    }

    @Override
    public void uninstall(String name) throws IOException {
        yum("uninstall", name);
    }

    @Override
    public void upgrade(String name) throws IOException {
        yum("upgrade", name);
    }

    private void yum(String operation, String name) throws IOException {
        try {
            String command = String.format("yum %s -y %s", operation, name);
            Command.execute(command);
        }
        catch (Exception e) {
            LOG.error("failed to %s %s: %s", operation, name, e.getMessage());
            throw new IOException(e);
        }
        LOG.info("succeed to %s %s", operation, name);
    }

}
