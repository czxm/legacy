package com.intel.soak.gauge;

import java.io.InputStream;
import java.util.List;

import com.intel.soak.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.intel.soak.config.ConfigReader;
import com.intel.soak.gauge.measure.LoadMerger;
import com.intel.soak.gauge.storage.GaugeStorage;
import com.intel.soak.model.GaugeConfig;
import com.intel.soak.model.MergeConfig;

/**
 * @author xzhan27
 *
 */
public class LocalGaugeMaster implements GaugeMaster {
    private static final Log LOG = LogFactory.getLog(LocalGaugeMaster.class);
    
    private GaugeConfig config;
    private GaugeStorage storage;
    private SoakMetricService gangliaAgent;
    
    public void setGangliaAgent(SoakMetricService ganglia){
        this.gangliaAgent = ganglia;
    }
    
    public void setStorage(GaugeStorage storage){
        this.storage = storage;
        InputStream configFile = LocalGaugeMaster.class.getClassLoader().getResourceAsStream("localSoakGauge.xml");
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
            LoadMerger merger = new LoadMerger(gangliaAgent, storage);

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
            if(configList.size() > 0)
                report.setGangliaReport(merger.createGangliaReport(configList.get(0).getGangliaMetricsConfig(), start, end));
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return report;
    }

    @Override
    public void renderChart() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stopJob(String jobName) {
        storage.setStorageProperty(jobName, "endTime", Long.toString(System.currentTimeMillis()));
    }

}
