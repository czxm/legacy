/**
 * 
 */
package com.intel.soak.transaction;

import com.intel.soak.MetricsData;
import com.intel.soak.MetricsData.Aggregator;
import com.intel.soak.gauge.GaugeMetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xzhan27
 *
 */
public class TransactionMetrics extends GaugeMetrics {
    private static final long serialVersionUID = -306297200481939035L;
   
    private MetricsData userCount;
    
    public TransactionMetrics(List<MetricsData> metrics){
        userCount = new MetricsData("ActiveUsers", new Aggregator[]{Aggregator.SUM});
        this.metrics = new ArrayList<MetricsData>();      
        for(MetricsData d : metrics){
            this.metrics.add(d.clone().reset());
        }
    }
    
    public void merge(List<MetricsData> nextMetrics){        
        for(int i = 0; i < metrics.size(); i++){
            MetricsData d1 = metrics.get(i);
            MetricsData d2 = nextMetrics.get(i).clone().commit();
            nextMetrics.get(i).reset();
            d1.mergeMetrics(d2);
        }
        userCount.mergeValue(1f);
    }
    
    public void commit(){
        for(MetricsData d : metrics){
            d.commit();
        }
        metrics.add(0, userCount);
    }
}
