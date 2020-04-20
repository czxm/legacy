package com.intel.soak.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.intel.soak.MetricsData;
import com.intel.soak.MetricsData.Aggregator;

public class TestMetricsData {
    
    MetricsData data1;
    MetricsData data2;
    MetricsData data3;

    @Before
    public void initMetricsData(){
        data1 = new MetricsData("TestMetrics1", 
                new Aggregator[]{Aggregator.CNT, Aggregator.MIN, Aggregator.MAX, Aggregator.AVG});
        data2 = new MetricsData("TestMetrics2", 
                new Aggregator[]{Aggregator.SUM, Aggregator.NCNT, Aggregator.MAX, Aggregator.MED});
        data3 = new MetricsData("TestMetrics3");
    }
        
    @Test
    public void testMergeValues1(){
        for(float f = 1;  f <= 10f; f++){
            data1.mergeValue(f);
        }
        assertTrue(data1.getCategory().equals("TestMetrics1"));
        assertTrue(Math.abs(data1.getMetricValue(Aggregator.CNT) - 10f) < 1e-10);
        assertTrue(Math.abs(data1.getMetricValue(Aggregator.MIN) - 1f) < 1e-10);
        assertTrue(Math.abs(data1.getMetricValue(Aggregator.MAX) - 10f) < 1e-10);
        assertTrue(Math.abs(data1.getMetricValue(Aggregator.SUM) - 55f) < 1e-10);
        assertTrue(data1.getMetricValue(Aggregator.AVG) == null);
        assertTrue(data1.getMetricValue(Aggregator.MED) == null);
        assertTrue(data1.getMetricValue(Aggregator.NCNT) == null);
        data1.mergeValue(null);
        assertTrue(data1.getMetricValue(Aggregator.NCNT) == null);
        MetricsData data = data1.clone();
        data.commit();
        assertTrue(Math.abs(data.getMetricValue(Aggregator.AVG) - 5.5f) < 1e-10);
    }
    
    @Test
    public void testMergeValues2(){
        for(float f = 10f;  f >= 1f; f--){
            data2.mergeValue(f);
        }
        assertTrue(data2.getCategory().equals("TestMetrics2"));
        assertTrue(data2.getMetricValue(Aggregator.CNT) == null);
        assertTrue(data2.getMetricValue(Aggregator.MIN) == null);
        assertTrue(Math.abs(data2.getMetricValue(Aggregator.MAX) - 10f) < 1e-10);
        assertTrue(Math.abs(data2.getMetricValue(Aggregator.SUM) - 55f) < 1e-10);
        assertTrue(data2.getMetricValue(Aggregator.AVG) == null);
        assertTrue(data2.getMetricValue(Aggregator.MED) == null);
        assertTrue(data2.getMetricValue(Aggregator.NCNT) == 0);
        data2.mergeValue(null);
        assertTrue(Math.abs(data2.getMetricValue(Aggregator.NCNT) - 1f) < 1e-10);
        MetricsData data = data2.clone();
        data.commit();
        assertTrue(Math.abs(data.getMetricValue(Aggregator.MED) - 5.5f) < 1e-10);
    }
    
    @Test
    public void testMetricsNameValues(){
        String[] names = new String[]{"TestMetrics2_SUM","TestMetrics2_MED","TestMetrics2_MAX","TestMetrics2_NCNT"};
        Float[] values = new Float[]{null, null, null, null};
        int i = 0;
        for(String s : data2.getMetricsNames()){
            assertTrue(s.equals(names[i++]));
        }
        i = 0;
        for(Float f : data2.getMetricsValues()){
            assertTrue(f == values[i++]);
        }
        data2.mergeValue(null);
        values = new Float[]{null, null, null, 1f};
        i = 0;
        for(Float f : data2.getMetricsValues()){
            assertTrue(f == null ? f == values[i] : Math.abs(f - values[i]) < 1e-10);
            i++;
        }
        data2.mergeValue(100f);
        values = new Float[]{100f, null, 100f, 1f};
        i = 0;
        for(Float f : data2.getMetricsValues()){
            assertTrue(f == null ? f == values[i] : Math.abs(f - values[i]) < 1e-10);
            i++;
        }
        data2.commit();
        values = new Float[]{100f, 100f, 100f, 1f};
        i = 0;
        for(Float f : data2.getMetricsValues()){
            assertTrue(f == null ? f == values[i] : Math.abs(f - values[i]) < 1e-10);
            i++;
        }
        data2.reset();
        values = new Float[]{null, null, null, null};
        i = 0;
        for(Float f : data2.getMetricsValues()){
            assertTrue(f == null ? f == values[i] : Math.abs(f - values[i]) < 1e-10);
            i++;
        }
    }
    
    @Test
    public void testSetAggregators(){
        assertTrue(data3.getAggregators().length == 0);
        data3.setAggregators(new Aggregator[]{Aggregator.MED,Aggregator.AVG});
        assertTrue(data3.getAggregators().length == 2);
        data3.setAggregators(new Aggregator[]{Aggregator.CNT, Aggregator.NCNT});
        data3.mergeValue(null);
        assertTrue(Math.abs(data3.getMetricValue(Aggregator.CNT) - 0f) < 1e-10);
        assertTrue(Math.abs(data3.getMetricValue(Aggregator.NCNT) - 1f) < 1e-10);
        data3.setAggregators(new Aggregator[]{});
        data3.mergeValue(null);
        assertTrue(data3.getMetricValue(Aggregator.CNT) == null);
        assertTrue(data3.getMetricValue(Aggregator.NCNT) == null);
    }
    
    @Test
    public void testSetMetricValue(){
        data3.setAggregators(new Aggregator[]{Aggregator.AVG});
        assertTrue(data3.getMetricValue(Aggregator.AVG) == null);
        data3.setMetricValue(Aggregator.AVG, 100f);
        assertTrue(Math.abs(data3.getMetricValue(Aggregator.AVG) - 100f) < 1e-10);
    }
    
    @Test
    public void testCloneMetric(){
        MetricsData d1 = new MetricsData("temp1");
        d1.setAggregators(new Aggregator[]{Aggregator.MED});
        d1.mergeValue(3f);
        MetricsData d2 = d1.clone();
        d2.commit();
        assertTrue(d2.getCategory().equals("temp1"));
        assertTrue(Math.abs(d2.getMetricValue(Aggregator.MED) - 3f) < 1e-10);
        
        d1.setAggregators(new Aggregator[]{Aggregator.AVG});
        d1.mergeValue(3f);
        d2 = d1.clone();
        d2.commit();
        assertTrue(d2.getCategory().equals("temp1"));
        assertTrue(Math.abs(d2.getMetricValue(Aggregator.AVG) - 3f) < 1e-10);
        
        d1.setAggregators(new Aggregator[]{Aggregator.AVG});
        d1.mergeValue(3f);
        d1.setMetricValue(Aggregator.AVG, 4f);
        d2 = d1.clone();
        d2.commit();
        assertTrue(d2.getCategory().equals("temp1"));
        assertTrue(Math.abs(d2.getMetricValue(Aggregator.AVG) - 3f) < 1e-10);
    }
    
    @Test
    public void testMergeMetrics(){
        
    }
}
