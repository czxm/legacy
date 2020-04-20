package com.intel.soak.agent.gauge;

import com.intel.bigdata.common.protocol.Payload;
import com.intel.soak.SoakMetricService;
import com.intel.soak.agent.AbstractAppSlave;
import com.intel.soak.protocol.GaugeRequest;

import com.intel.soak.gauge.GaugeMetrics;
import com.intel.soak.gauge.GaugeSlave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author xzhan27
 *
 */
@Component("simpleGaugeSlave")
@Scope("singleton")
@Lazy(true)
public class SimpleGaugeSlave extends AbstractAppSlave implements GaugeSlave {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleGaugeSlave.class);

    @Autowired
    @Qualifier("localGangliaAgent")
    private SoakMetricService gangliaAgent;

    @Override
    public void sendMetrics(String jobName, GaugeMetrics[] metrics) {
        GaugeRequest request = new GaugeRequest()
                .setType(GaugeRequest.RequestType.SendMetrics)
                .setItem(GaugeRequest.RequestKey.JobName, jobName)
                .setItem(GaugeRequest.RequestKey.Metrics, metrics);
        postRequest(request);
    }

    @Override
    public Object onReceive(Payload payload) {
        try{
            if(payload instanceof GaugeRequest){
                GaugeRequest request = (GaugeRequest)payload;
                switch(request.getType()){
                    case Initialize:
                        break;
                    case GetGangliaMetrics:
                        Object[] params = request.getItem(GaugeRequest.RequestKey.GangliaRequest);
                        Object result = gangliaAgent.fetchMetrics((String[])params[0], (Long)params[1], (Long)params[2]);
                        if(result != null)
                            return result;
                        break;
                    case Finalize:
                        break;
                }
            }
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return new Boolean(false);
    }
}

