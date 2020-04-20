package com.intel.cedar.service;

import java.net.URL;

import com.intel.cedar.core.Bootstrapper;
import com.intel.cedar.core.JettyBootstrapper;

public class WIBootstrapper extends JettyBootstrapper implements Bootstrapper {
    @Override
    protected URL getJettyConfig() {
        return getClass().getClassLoader().getResource("service-jetty.xml");
    }
}
