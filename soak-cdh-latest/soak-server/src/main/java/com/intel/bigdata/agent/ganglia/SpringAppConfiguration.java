package com.intel.bigdata.agent.ganglia;

import com.intel.bigdata.agent.ganglia.data.CLUSTER;
import com.intel.bigdata.agent.ganglia.service.impl.GangliaServiceImpl;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created with IntelliJ IDEA.
 * User: holm
 * Date: 10/25/13
 * Time: 4:14 PM
 * To change this template use File | Settings | File Templates.
 */
@Configurable
@EnableScheduling
public class SpringAppConfiguration {

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(CLUSTER.class.getPackage().getName());
        return marshaller;
    }

    @Bean
    public GangliaServiceImpl.Config config() {
        GangliaServiceImpl.Config config = new GangliaServiceImpl.Config();
        config.setGangliaPort(System.getProperty("ganglia.port", "8649"));
        return config;
    }

}
