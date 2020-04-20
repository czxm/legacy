package com.intel.soak.plugin;

import com.intel.soak.Bootable;
import com.intel.soak.web.jetty.JettyBootstrap;

import java.net.URL;

public class WebContainerBootstrap extends JettyBootstrap implements Bootable {

    @Override
    protected URL getJettyConfig() {
        return getClass().getClassLoader().getResource("service-jetty.xml");
    }

}
