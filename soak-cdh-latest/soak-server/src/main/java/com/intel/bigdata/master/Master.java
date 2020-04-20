package com.intel.bigdata.master;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.cluster.Cluster;
import akka.contrib.pattern.ClusterSingletonManager;
import com.intel.bigdata.common.util.SpringExtension;
import com.intel.soak.bootstrap.Bootstrap;
import com.intel.soak.utils.SpringBeanFactoryManager;
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
public class Master implements Daemon {

    protected static Logger LOG = LoggerFactory.getLogger(Master.class);

    private ActorSystem system;

    public Master() {
    }

    public void setActorSystem(ActorSystem system) {
        this.system = system;
    }

    @Override
    public void destroy() {
        this.system.shutdown();
        this.system.awaitTermination(Duration.create(5, TimeUnit.SECONDS));
        Bootstrap.stop();
    }

    @Override
    public void init(DaemonContext daemonContext) throws Exception {
        Bootstrap.setLocalMode(false);
        Bootstrap.start();
        ApplicationContext ctx = SpringBeanFactoryManager.getSystemAppCxt();
        ctx.getAutowireCapableBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);

        startMasterServer();
    }

    private void startMasterServer(){
        Cluster cluster = Cluster.get(system);
        cluster.join(cluster.selfAddress());

        system.actorOf(
                ClusterSingletonManager.defaultProps(
                        SpringExtension.SpringExtProvider.get(system).props("MasterServerActor"),
                        "master",
                        PoisonPill.getInstance(),
                        null),
                "masterServer"
        );
    }

    @Override
    public void start() throws Exception {
        LOG.info("Starting Soak Master");
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Stopping Soak Master");
    }

}
