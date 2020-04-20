package com.intel.soak;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This data structure represents the actual Metrics for any Soak metrics agent.
 * <p>
 * A metric contains:
 * <p>
 * . Metric Category, such as "CPU" or "TransactionName"
 * <p>
 * . Metric Values for all required aggregator
 * <p>
 * . Aggregation Options
 * <p>
 * 
 * @author xzhan27
 * 
 */

public class MetricsData implements Serializable {
    private static int SAMPLE_SIZE = 1000;

    public static interface Functor {
        public Float aggregate(Float a, Float b);
    }

    public static enum Aggregator {
        /* value summing */
        SUM("_SUM", 1, new Functor() {
            @Override
            public Float aggregate(Float a, Float b) {
                return a == null ? b : (b != null ? (Float)(a + b) : a);
            }
        }), 
        MIN("_MIN", 2, new Functor() {
            @Override
            public Float aggregate(Float a, Float b) {
                return a == null ? b : (b != null ? (a > b ? b : a) : a);
            }
        }), 
        AVG("_AVG", 4, null), 
        /* medium value */
        MED("_MED", 8, null),
        MAX("_MAX", 16, new Functor() {
            @Override
            public Float aggregate(Float a, Float b) {
                return a == null ? b : (b != null ? (a < b ? b : a) : a);
            }
        }), 
        /* positive occurrence counting */
        CNT("_CNT", 32, new Functor() {
            @Override
            public Float aggregate(Float a, Float b) {
                return a == null ? (b != null ? 1 : 0) : (b != null ? (Float)(a + 1) : a);
            }
        }), 
        /* negative occurrence counting */
        NCNT("_NCNT", 64, new Functor() {
            @Override
            public Float aggregate(Float a, Float b) {
                return a == null ? (b == null ? 1 : 0) : (b == null ? (Float)(a + 1) : a);
            }
        }),
        /* Standard Deviation */
        STDDEV("_STDDEV", 128, null),
        /* 75th percentile */
        PCT75("_PCT75", 256, null),
        /* 95th percentile */
        PCT95("_PCT95", 512, null),
        /* 99th percentile */
        PCT99("_PCT99", 1024, null);

        private String postfix;
        private int mask;
        private Functor functor;

        Aggregator(String postfix, int mask, Functor functor) {
            this.postfix = postfix;
            this.mask = mask;
            this.functor = functor;
        }

        boolean matches(int aggregators) {
            return (this.mask & aggregators) > 0;
        }

        String with(String name) {
            return name + postfix;
        }

        boolean aggregate(MetricsData metrics, Float d) {
            return aggregate(metrics, d, functor);
        }
        
        boolean aggregate(MetricsData metrics, Float d, Functor functor) {
            Float[] values = metrics.getValues();
            if (functor != null) {
                values[ordinal()] = functor.aggregate(values[ordinal()], d);
                return true;
            }
            return false;
        }
    }

    private String category;
    private int aggregators;
    private Float[] values; /* the actual metric values */
    private Float[] dataList; /* the saved data points for temporal usage */

    private final int expandLength = 100;
    private int dataIndex = 0;

    private MetricsData() {
    }

    public MetricsData(String category, Aggregator[] list){
        int aggrs = 0;
        for(Aggregator aggr : list){
            aggrs |= aggr.mask;
            if(aggr.mask >= Aggregator.STDDEV.mask)
                aggrs |= Aggregator.MED.mask;
        }
        this.category = category;
        this.aggregators = aggrs;
        // AVG and MEG required CNT enabled
        if (Aggregator.AVG.matches(aggregators))
            this.aggregators |= Aggregator.CNT.mask | Aggregator.SUM.mask;
        // MED requires saving data points temporally
        if (Aggregator.MED.matches(aggregators))
            dataList = new Float[expandLength];
        this.values = new Float[Aggregator.values().length];
    }
    
    public MetricsData(String category) {
        this.category = category;
        this.aggregators = 0;
    }

    public MetricsData clone() {
        MetricsData t = new MetricsData();
        t.category = category;
        t.aggregators = aggregators;
        t.values = new Float[values.length];
        System.arraycopy(values, 0, t.values, 0, values.length);
        t.dataIndex = dataIndex;
        /* we just hold a shallow copy for the temporal data points */
        t.dataList = dataList;
        return t;
    }

    public int getColumns() {
        return values == null ? 0 : values.length;
    }

    public Float getByIndex(int index) {
        return values == null ? null : values[index];
    }

    public void setByIndex(int index, Float d) {
        if (values != null)
            values[index] = d;
    }

    public Aggregator[] getAggregators() {
        List<Aggregator> aggrs = new ArrayList<Aggregator>();
        for(Aggregator aggr : Aggregator.values()){
            if(aggr.matches(aggregators))
                aggrs.add(aggr);
        }
        return aggrs.toArray(new Aggregator[]{});
    }

    public void setAggregators(Aggregator[] aggrs) {
        this.aggregators = 0;
        for(Aggregator aggr : aggrs){
            this.aggregators |= aggr.mask;
            if(aggr.mask >= Aggregator.STDDEV.mask)
                this.aggregators |= Aggregator.MED.mask;
        }
        if(aggrs.length > 0){
            this.values = new Float[Aggregator.values().length];
            if (Aggregator.MED.matches(aggregators))
                dataList = new Float[expandLength];
        }
    }
    
    public void addAggregators(Aggregator[] aggrs) {
        for(Aggregator aggr : aggrs){
            this.aggregators |= aggr.mask;
            if(aggr.mask >= Aggregator.STDDEV.mask)
                this.aggregators |= Aggregator.MED.mask;
        }
        if(this.values == null)
            this.values = new Float[Aggregator.values().length];
        if(dataList == null && (Aggregator.MED.matches(aggregators)))
            dataList = new Float[expandLength];        
    }

    public Float[] getValues() {
        return values;
    }

    public void setValues(Float[] values) {
        this.values = values;
    }

    private void appendData(Float d) {
        if(dataList == null)
            dataList = new Float[expandLength];
        if (dataIndex == dataList.length && dataIndex > 0) {
            // temporal list full
            Float[] newList = new Float[dataList.length + expandLength];
            System.arraycopy(dataList, 0, newList, 0, dataList.length);
            dataList = newList;
        }
        dataList[dataIndex++] = d;
    }
    
    private void specialMerge(Aggregator aggr, Float d){
        if(Aggregator.MED.equals(aggr)) {
            // special handling for MED
            if(values[aggr.ordinal()] != null){
                appendData(values[aggr.ordinal()]);
                values[aggr.ordinal()] = null;
            }
            appendData(d);
        }
        else if(Aggregator.AVG.equals(aggr)){
            // if only AVG is enabled, internally use other 2 aggregators
            if(!Aggregator.CNT.matches(aggregators) &&
               !Aggregator.SUM.matches(aggregators)){
                Aggregator.CNT.aggregate(this, d);
                Aggregator.SUM.aggregate(this, d);
            }
        }
    }

    public MetricsData mergeValue(Float d) {
        synchronized (this) {
            for (Aggregator aggr : Aggregator.values()) {
                if (aggr.matches(aggregators)) {
                    if (!aggr.aggregate(this, d) && d != null){
                        specialMerge(aggr, d);
                    }
                }
            }
        }
        return this;
    }
    
    public MetricsData mergeMetrics(MetricsData otherMetrics) {
        synchronized (this) {
            for (Aggregator aggr : Aggregator.values()) {
                if (aggr.matches(aggregators) && 
                    aggr.matches(otherMetrics.aggregators)) {
                    Float d = otherMetrics.values[aggr.ordinal()];
                    /* when merging CNT/NCNT values for another metrics data 
                    /* we need this hack to reuse SUM 
                     */
                    if(aggr.equals(Aggregator.CNT) || aggr.equals(Aggregator.NCNT)){
                        aggr.aggregate(this, d, Aggregator.SUM.functor);
                    }
                    else if(!aggr.aggregate(this, d) && d != null){
                        specialMerge(aggr, d);
                    }
                }
            }
        }
        return this;
    }

    private Float[] sortDataList(){
        if (this.dataIndex > 0) {
            Float[] toSort = new Float[this.dataIndex];
            System.arraycopy(this.dataList, 0, toSort, 0, this.dataIndex);
            Arrays.sort(toSort);
            return toSort;
        }
        return null;
    }

    public MetricsData commit(){
        if (Aggregator.AVG.matches(this.aggregators)) {
            this.values[Aggregator.AVG.ordinal()] = (this.values[Aggregator.CNT
                    .ordinal()] != null && this.values[Aggregator.SUM.ordinal()] != null) ?
                    this.values[Aggregator.SUM.ordinal()] / this.values[Aggregator.CNT.ordinal()] : null;
        }
        Float[] sorted = null;
        if (Aggregator.MED.matches(this.aggregators)) {
            sorted = sortDataList();
            if(sorted != null) {
                Float med = null;
                if (sorted.length % 2 == 0) {
                    med = (sorted[sorted.length / 2 - 1] + sorted[sorted.length / 2]) / 2;
                } else {
                    med = sorted[sorted.length / 2];
                }
                this.values[Aggregator.MED.ordinal()] = med;
            }
        }
        if (Aggregator.STDDEV.matches(this.aggregators)) {
            if(sorted != null) {
                int i = 0;
                float avgValue = 0F;
                float accum = 0F;
                for (; i < SAMPLE_SIZE && i < dataList.length && dataList[i] != null; i++) {
                    avgValue += dataList[i];
                }
                avgValue = avgValue / i;
                for (i = 0; i < SAMPLE_SIZE && i < dataList.length && dataList[i] != null; i++) {
                    accum += Math.pow(Math.abs(dataList[i] - avgValue), 2);
                }
                int sampleSize = (i == SAMPLE_SIZE && SAMPLE_SIZE < dataList.length ? SAMPLE_SIZE - 1 : dataList.length);
                this.values[Aggregator.STDDEV.ordinal()] = (float) Math.sqrt(accum / sampleSize);
            }
        }
        if (Aggregator.PCT75.matches(this.aggregators)) {
            if(sorted != null) {
                int i = (int)Math.floor(sorted.length * 0.75);
                this.values[Aggregator.PCT75.ordinal()] = sorted[i];
            }
        }
        if (Aggregator.PCT95.matches(this.aggregators)) {
            if(sorted != null) {
                int i = (int)Math.floor(sorted.length * 0.95);
                this.values[Aggregator.PCT95.ordinal()] = sorted[i];
            }
        }
        if (Aggregator.PCT99.matches(this.aggregators)) {
            if(sorted != null) {
                int i = (int)Math.floor(sorted.length * 0.99);
                this.values[Aggregator.PCT99.ordinal()] = sorted[i];
            }
        }
        this.dataList = null;
        this.dataIndex = 0;
        return this;
    }
    
    public void average(int number){
        for(Aggregator aggr : Aggregator.values()){
            if(aggr.matches(aggregators) && 
              (aggr.equals(Aggregator.SUM) || aggr.equals(Aggregator.CNT) || aggr.equals(Aggregator.NCNT))){
                Float d = values[aggr.ordinal()];
                if(d != null){
                    values[aggr.ordinal()] = d / number;
                }
            }
        }
    }
    
    public MetricsData reset(){
        synchronized(this){
            if (Aggregator.MED.matches(aggregators))
                dataList = new Float[expandLength];
            dataIndex = 0;
            values = new Float[Aggregator.values().length];
        }
        return this;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getMetricsNames() {
        List<String> names = new ArrayList<String>();
        for (Aggregator aggr : Aggregator.values()) {
            if (aggr.matches(aggregators)) {
                names.add(aggr.with(category));
            }
        }
        return names;
    }

    public List<Float> getMetricsValues() {
        List<Float> ret = new ArrayList<Float>();
        for (Aggregator aggr : Aggregator.values()) {
            if (aggr.matches(aggregators)) {
                ret.add(values[aggr.ordinal()]);
            }
        }
        return ret;
    }
    
    public Float getMetricValue(Aggregator aggr){
        if(aggr.matches(aggregators)){
            return values[aggr.ordinal()];
        }
        return null;
    }
    
    public void setMetricValue(Aggregator aggr, Float d){
        if(aggr.matches(aggregators)){
            values[aggr.ordinal()] = d;
        }
    }
}
