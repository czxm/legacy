package com.intel.cedar.cloud;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import com.intel.cedar.cloud.impl.EC2Connector;
import com.intel.cedar.cloud.impl.EucaConnector;
import com.intel.cedar.cloud.impl.OpenStackConnector;
import com.intel.cedar.core.entities.CloudInfo;

public class ConnectorFactory {
    private static HashMap<String, Class<?>> supportedConnectors;
    private static ConnectorFactory singleton;

    public static synchronized ConnectorFactory getInstance() {
        if (singleton == null) {
            singleton = new ConnectorFactory();
        }
        return singleton;
    }

    private ConnectorFactory() {
        supportedConnectors = new HashMap<String, Class<?>>();
        supportedConnectors.put("EC2", EC2Connector.class);
        supportedConnectors.put("Eucalyptus", EucaConnector.class);
        supportedConnectors.put("OpenStack", OpenStackConnector.class);
    }

    public boolean supportedCloud(CloudInfo cloud) {
        return supportedConnectors.containsKey(cloud.getProtocol());
    }

    public Connector createConnector(CloudInfo cloud)
            throws UnsupportedCloudException {
        Class<?> clz = (Class<?>) supportedConnectors.get(cloud.getProtocol());
        if (clz != null) {
            try {
                Constructor ctr = clz.getConstructor(cloud.getClass());
                return (Connector) ctr.newInstance(cloud);
            } catch (Exception e) {
                throw new UnsupportedCloudException();
            }
        } else
            throw new UnsupportedCloudException();
    }
}
