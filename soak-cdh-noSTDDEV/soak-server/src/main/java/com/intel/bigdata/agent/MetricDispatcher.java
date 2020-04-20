package com.intel.bigdata.agent;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.google.common.collect.ImmutableList;
import com.intel.bigdata.agent.events.MetricStatusList;
import com.intel.bigdata.agent.events.RegisterListener;
import com.intel.bigdata.agent.events.Tick;
import com.intel.bigdata.agent.events.UnregisterListener;
import com.intel.bigdata.agent.ganglia.service.GangliaService;
import com.intel.bigdata.common.protocol.MetricStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: holm
 * Date: 10/23/13
 * Time: 9:46 PM
 * To change this template use File | Settings | File Templates.
 */
@Component("MetricDispatcher")
@Scope("prototype")
public class MetricDispatcher extends UntypedActor {

    protected final Logger LOG = LoggerFactory.getLogger(MetricDispatcher.class);

    private List<ActorRef> listeners = new ArrayList<ActorRef>();

    private GangliaService gangliaService;

    @Autowired
    @Required
    public void setGangliaService(GangliaService gangliaService) {
        this.gangliaService = gangliaService;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

        getContext().system().scheduler().schedule(
                Duration.Zero(),
                Duration.create(15, TimeUnit.SECONDS),
                getSelf(), new Tick(),
                getContext().system().dispatcher(), getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Tick) {
            MetricStatusList metricsStatus = getMetricStatus();
            for (ActorRef listener : listeners) {
                listener.tell(metricsStatus, getSelf());
            }
        } else if (message instanceof RegisterListener) {
            listeners.add(((RegisterListener) message).getListener());
        } else if (message instanceof UnregisterListener) {
            listeners.remove(((UnregisterListener) message).getListener());
        } else {
            unhandled(message);
        }
    }

    private MetricStatusList getMetricStatus() {
        List<MetricStatus> metricStatusList = gangliaService.getMetricStatuses();
        LOG.debug("Updated metric status table, retrieved {} metrics", metricStatusList.size());
        return new MetricStatusList(ImmutableList.copyOf(metricStatusList));
    }

}
