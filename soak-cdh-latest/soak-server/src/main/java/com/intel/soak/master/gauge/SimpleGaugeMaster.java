package com.intel.soak.master.gauge;

import java.io.InputStream;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intel.bigdata.common.protocol.Payload;
import com.intel.soak.*;
import com.intel.soak.gauge.GaugeMaster;
import com.intel.soak.gauge.GaugeMetrics;
import com.intel.soak.gauge.GaugeReport;
import com.intel.soak.master.AbstractAppMaster;
import com.intel.soak.protocol.GaugeRequest;

import com.intel.soak.config.ConfigReader;
import com.intel.soak.gauge.measure.LoadMerger;
import com.intel.soak.gauge.storage.GaugeStorage;
import com.intel.soak.model.GaugeConfig;
import com.intel.soak.model.MergeConfig;
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
@Component("simpleGaugeMaster")
@Scope("singleton")
@Lazy(true)
public class SimpleGaugeMaster extends AbstractAppMaster implements GaugeMaster {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleGaugeMaster.class);

    private GaugeConfig config;
    private GaugeStorage storage;

    @Override
    public Object onReceive(Payload payload) {
        try{
            if(payload instanceof GaugeRequest){
                GaugeRequest request = (GaugeRequest)payload;
                switch(request.getType()){
                    case SendMetrics:
                        String jobName = request.getItem(GaugeRequest.RequestKey.JobName);
                        GaugeMetrics[] metrics = request.getItem(GaugeRequest.RequestKey.Metrics);
                        receiveMetrics(jobName, metrics);
                        break;
                }
            }
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return new Boolean(false);
    }

    @Autowired
    @Qualifier(value = "localFileStorage")
    public void setStorage(GaugeStorage storage){
        this.storage = storage;
        InputStream configFile = SimpleGaugeMaster.class.getClassLoader().getResourceAsStream("simpleSoakGauge.xml");
        config = new ConfigReader<GaugeConfig>().load(configFile, GaugeConfig.class);
        storage.setParams(config.getParam());
    }

    @Override
    public void startJob(String jobName) {
        try{
            storage.createStorage(jobName);
            storage.setStorageProperty(jobName, "startTime", Long.toString(System.currentTimeMillis()));
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void setJobProperty(String jobName, String key, String value) {
        storage.setStorageProperty(jobName, key, value);
    }

    @Override
    public String getJobProperty(String jobName, String key) {
        return storage.getStorageProperty(jobName, key);
    }


    @Override
    public void receiveMetrics(String jobName, GaugeMetrics[] metrics) {
        try{
            if(metrics != null){
                for(GaugeMetrics metric : metrics){
                    storage.openSource(jobName, metric.getSource()).append(metric);
                }
                LOG.info(String.format("Received %d metrics", metrics.length));
            }
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public GaugeReport createReport(List<MergeConfig> configList) {
        GaugeReport report = new GaugeReport();
        try{
            LoadMerger merger = new LoadMerger(new SoakMetricService() {
                @Override
                public GaugeMetrics[] fetchMetrics(final String[] metricNames, final long from, final long to) {
                    List<String> nodes = getLiveNodes();
                    ImmutableMap<String, Object> result = sendRequest(ImmutableList.copyOf(nodes), new GaugeRequest()
                            .setType(GaugeRequest.RequestType.GetGangliaMetrics)
                            .setItem(GaugeRequest.RequestKey.GangliaRequest, new Object[]{metricNames, from, to}));
                    for(Object v : result.values()){
                        if(v instanceof GaugeMetrics[]){
                            return (GaugeMetrics[])v;
                        }
                    }
                    return null;
                }
            }, storage);

            long start = Long.MAX_VALUE, end = Long.MIN_VALUE;
            List<JobReport> jobReports = report.getJobReports();
            for(MergeConfig config : configList){
                JobReport r = merger.createJobReport(config);
                if(r != null){
                    jobReports.add(r);
                    long t = merger.getJobStartTime(config.getName(), r.getDetailMetrics());
                    if(t < start){
                        start = t;
                    }
                    t = merger.getJobEndTime(config.getName(), r.getDetailMetrics());
                    if(t > end){
                        end = t;
                    }
                }
            }
            report.setStartTime(start);
            report.setDuration(end - start);

            report.setGangliaReport(merger.createGangliaReport(configList.get(0).getGangliaMetricsConfig(), start, end));
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return report;
    }

    @Override
    public void renderChart() {

    }

    @Override
    public void stopJob(String jobName) {
        storage.setStorageProperty(jobName, "endTime", Long.toString(System.currentTimeMillis()));
    }
}
