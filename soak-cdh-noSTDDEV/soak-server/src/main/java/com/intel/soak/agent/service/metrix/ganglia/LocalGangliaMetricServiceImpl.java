package com.intel.soak.agent.service.metrix.ganglia;

import com.intel.soak.gauge.GaugeMetrics;
import com.intel.soak.SoakMetricService;
import com.intel.soak.agent.service.metrix.dto.MetricDataDto;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/24/13
 * Time: 8:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class LocalGangliaMetricServiceImpl implements SoakMetricService {

    private GangliaRrdDao dao = new LocalGangliaRrdDaoImpl();

    @Override
    public GaugeMetrics[] fetchMetrics(String[] metricNames, long from, long to) {
        GaugeMetrics[] metrics = null;
        for(String metricName : metricNames){
            MetricDataDto data = dao.fetchMetric(metricName, from, to);
            if(data != null){
                if(metrics == null)
                    metrics = new GaugeMetrics[(int)data.getSize()];
                for(int i = 0; i < data.getSize() && i < metrics.length; i++){
                    if(metrics[i] == null){
                        metrics[i] = new GaugeMetrics();
                        metrics[i].setSource("LocalGanglia");
                        metrics[i].setTimestamp(data.getTime().get(i).getTime());
                    }
                    metrics[i].setMetricsValue(metricName, data.getData().get(i).floatValue());
                }
            }
        }
        return metrics;
    }

}
