package com.intel.bigdata.agent;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.intel.bigdata.common.util.SpringExtension;
import com.intel.soak.agent.bootstrap.AgentBootstrap;
import com.intel.soak.utils.SpringBeanFactoryManager;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

@Component
public class Agent implements Daemon {

    protected static Logger LOG = LoggerFactory.getLogger(Agent.class);


    private ActorSystem system;

    public Agent() {
    }

    public void setActorSystem(ActorSystem system) {
        this.system = system;
    }

    @Override
    public void destroy() {
        this.system.shutdown();
        this.system.awaitTermination(Duration.create(5, TimeUnit.SECONDS));
        AgentBootstrap.stop();
    }

    @Override
    public void init(DaemonContext daemonContext) throws Exception {
        LOG.info("Initializing agent...");
        LOG.info("Agent config: " + ConfigFactory.load().getConfig("agent"));
        AgentBootstrap.start();
        ApplicationContext ctx = SpringBeanFactoryManager.getSystemAppCxt();
        ctx.getAutowireCapableBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
        startDispatcher();
    }

    private void startDispatcher() throws Exception {
        ActorRef agentDispatcher = system.actorOf(
                SpringExtension.SpringExtProvider.get(system).props("AgentDispatcher"), "agentDispatcher");
    }

    @Override
    public void start() throws Exception {
        LOG.info("Starting agent");
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Stopping agent");
    }

}
