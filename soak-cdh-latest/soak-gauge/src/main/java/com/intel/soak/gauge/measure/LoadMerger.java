package com.intel.soak.gauge.measure;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.intel.soak.*;
import com.intel.soak.gauge.GangliaReport;
import com.intel.soak.gauge.GaugeChart;
import com.intel.soak.gauge.GaugeMetrics;
import com.intel.soak.model.GangliaMetricsConfigType;
import com.intel.soak.transaction.TransactionSummary;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.soak.gauge.magnify.ChartGenerator;
import com.intel.soak.gauge.storage.GaugeStorage;
import com.intel.soak.gauge.storage.MetricsSource;
import com.intel.soak.gauge.storage.impl.InMemSource;
import com.intel.soak.model.ChartConfigType;
import com.intel.soak.model.MergeConfig;
import com.intel.soak.model.YSerieType;

public class LoadMerger {	
    protected static Logger LOG = LoggerFactory.getLogger(LoadMerger.class);

	private GaugeStorage storage;
	private SoakMetricService ganglia;
	
	private Map<String, InputStream> results;
	
	public LoadMerger(SoakMetricService ganglia, GaugeStorage storage){
		this.storage = storage;
		this.ganglia = ganglia;
		this.results = new HashMap<String, InputStream>();
	}

    private DateTickUnit getTickUnit(String XAxisTickUnit, long totalTime, int width){
        if(XAxisTickUnit == null){
            // calculate the tickUnit according to totalTime
            XAxisTickUnit = (int)(totalTime / 1000 / (10 * ((float)width / 600))) + "s";
        }
        DateTickUnitType ut = DateTickUnitType.SECOND;
        int multiply = 15;
        try{
            char unit = XAxisTickUnit.charAt(XAxisTickUnit.length() - 1);
            if(unit > '9' || unit < '0'){
                if(unit == 'm' || unit == 'M'){
                    ut = DateTickUnitType.MINUTE;
                }
                else if(unit == 'h' || unit == 'H'){
                    ut = DateTickUnitType.HOUR;
                }
                multiply = Integer.parseInt(XAxisTickUnit.substring(0, XAxisTickUnit.length() - 1));
            }
            else{
                multiply = Integer.parseInt(XAxisTickUnit);
            }
        }
        catch(Exception e){
        }
        if(ut.equals(DateTickUnitType.SECOND) && multiply > 900){
            return new DateTickUnit(DateTickUnitType.MINUTE, multiply / 60);
        }
        return new DateTickUnit(ut, multiply);
    }
	
	public List<GaugeChart> generateCharts(MetricsStore metricsStore, List<ChartConfigType> chartConfigs, long totalTime) throws Exception {
        List<GaugeChart> charts = new ArrayList<GaugeChart>();
		// skip this chart if there's too few data points
		if(metricsStore.getMetricsList().size() > 1){
            for(ChartConfigType chartConfig : chartConfigs){
                GaugeChart chart = new GaugeChart();
                chart.setName(chartConfig.getName());

                int limit = chartConfig.getDatasetSize() == null ? Integer.MAX_VALUE : chartConfig.getDatasetSize();
                List<Object> indexList = metricsStore.getSubListByColumn(chartConfig.getXSerie(), limit);
                if(indexList.size() > 0){
                    String title = chartConfig.getTitle();
                    ChartGenerator gen = new ChartGenerator(title, chartConfig.getXAxisName(), chartConfig.getYAxisName(), chartConfig.getYSeries().getDecimal());
                    for(YSerieType yserie : chartConfig.getYSeries().getYSerie()){
                        List<Object> dataset = metricsStore.getSubListByColumn(yserie.getSerie(),limit);
                        if(dataset.size() > 0){
                            gen.addDataset(false, yserie.getLabel() == null ? yserie.getSerie() : yserie.getLabel(), indexList, dataset);
                        }
                    }
                    if(chartConfig.getSecondYSeries() != null && chartConfig.getSecondYAxisName() != null){
                        gen.setSecondaryAxis(chartConfig.getSecondYAxisName(), chartConfig.getSecondYSeries().getDecimal());
                        for(YSerieType yserie : chartConfig.getSecondYSeries().getYSerie()){
                            List<Object> dataset = metricsStore.getSubListByColumn(yserie.getSerie(),limit);
                            if(dataset.size() > 0){
                                gen.addDataset(true, yserie.getLabel() == null ? yserie.getSerie() : yserie.getLabel(), indexList, dataset);
                            }
                        }
                    }
                    int width = chartConfig.getWidth() == null ? 600 : chartConfig.getWidth();
                    int height = chartConfig.getHeight() == null ? 300 : chartConfig.getHeight();
                    int titleFontSize = chartConfig.getTitleFontSize() == null ? 15 : chartConfig.getTitleFontSize();
                    int labelFontSize = chartConfig.getLabelFontSize() == null ? 10 : chartConfig.getLabelFontSize();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    gen.generateChart(bos, width, height, titleFontSize, labelFontSize,
                            chartConfig.getXSerie().equals("Timestamp") ? getTickUnit(chartConfig.getTickUnit(), totalTime, width) : null);
                    chart.setContent(bos.toByteArray());

                    charts.add(chart);
                }
            }
        }
        return charts;
	}
	
	private long getTimeFromStorage(String jobId, String key){
	    try{
            String timeStr = storage.getStorageProperty(jobId, key);
            if(timeStr != null && timeStr.length() > 0)
                return Long.parseLong(timeStr);
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return -1;
	}

    public long getJobStartTime(String jobId, List<GaugeMetrics> metricsList){
        long start = getTimeFromStorage(jobId, "startTime");
        if(start < 0)
            start = metricsList.get(0).getTimestamp();
        return start;
    }

    public long getJobEndTime(String jobId, List<GaugeMetrics> metricsList){
        long end = getTimeFromStorage(jobId, "endTime");
        if(end < 0)
            end = metricsList.get(metricsList.size() - 1).getTimestamp();
        return end;
    }

    public long getJobDuration(String jobId, List<GaugeMetrics> metricsList){
        return getJobEndTime(jobId, metricsList) - getJobStartTime(jobId, metricsList);
    }

    public GangliaReport createGangliaReport(GangliaMetricsConfigType config, long start, long end) throws Exception {
        GangliaReport report = new GangliaReport();
        InMemSource gangliaSource = new InMemSource();
        String[] headers = config.getGangliaMetrics().split(",|;| ");
        GaugeMetrics[] metrics = ganglia.fetchMetrics(headers, start, end);
        if(metrics != null){
            for(GaugeMetrics m : metrics)
                if(m != null){
                    gangliaSource.append(m);
                }
        }
        MetricsStore gangliaStore = gangliaSource.load();
        LOG.info("{} Ganglia metrics collected", gangliaStore.getMetricsList().size());
        List<GaugeMetrics> metricsList = gangliaStore.getMetricsList();
        report.getGangliaMetrics().addAll(metricsList);

        List<GaugeChart> charts = generateCharts(gangliaStore, config.getChartConfig(), end - start);
        report.getCharts().addAll(charts);

        return report;
    }

    class MetricsRange{
        int activeUsers;
        long start;
        long end;
        List<GaugeMetrics> metricsList = new ArrayList<GaugeMetrics>();

        List<MetricsData> getMetricsByCategory(String category){
            List<MetricsData> result = new ArrayList<MetricsData>();
            for(GaugeMetrics m : metricsList){
                result.add(m.findMetricsByCategory(category));
            }
            return result;
        }

        Long[] getMetricsTimestamp(){
            Long[] result = new Long[metricsList.size()];
            for(int i = 0; i < metricsList.size(); i++){
                result[i] = metricsList.get(i).getTimestamp();
            }
            return result;
        }
    }

    private void setActiveUsers(GaugeMetrics metrics, int activeUsers) {
        MetricsData m = metrics.findMetricsByCategory("ActiveUsers");
        if(m == null){
            m = new MetricsData("ActiveUsers");
            m.setAggregators(new MetricsData.Aggregator[]{MetricsData.Aggregator.SUM});
            metrics.getMetrics().add(m);
        }
        m.setMetricValue(MetricsData.Aggregator.SUM, (float)activeUsers);
    }

    private int getActiveUsers(GaugeMetrics metrics){
        MetricsData d = metrics.findMetricsByCategory("ActiveUsers");
        if(d != null)
            return (int)(float)d.getMetricValue(MetricsData.Aggregator.SUM);
        return 0;
    }

    private List<MetricsRange> splitRangesByUsers(long start, long end, List<GaugeMetrics> metricsList){
        List<MetricsRange> ranges = new ArrayList<MetricsRange>();
        GaugeMetrics lastM = null;
        MetricsRange lastR = null;
        for(GaugeMetrics m : metricsList){
            if(ranges.size() == 0){
                MetricsRange r = new MetricsRange();
                r.activeUsers = getActiveUsers(m);
                r.start = start;
                r.metricsList.add(m);
                ranges.add(r);
                lastR = r;
            }
            else{
                if(getActiveUsers(m) == lastR.activeUsers){
                    lastR.metricsList.add(m);
                }
                else{
                    lastR.end = lastM.getTimestamp();
                    MetricsRange r = new MetricsRange();
                    r.activeUsers = getActiveUsers(m);
                    r.start = m.getTimestamp();
                    r.metricsList.add(m);
                    ranges.add(r);
                    lastR = r;
                }
            }
            lastM = m;
        }
        if(lastR.end == 0){
            lastR.end = end;
        }

        // filter those range with very little data
        List<MetricsRange> toRemove = new ArrayList<MetricsRange>();
        for(MetricsRange r : ranges){
            if(r.metricsList.size() < 3)
                toRemove.add(r);
        }
        for(MetricsRange r : toRemove)
            ranges.remove(r);
        return ranges;
    }

    public class SummaryMetrics extends GaugeMetrics {
        public int getDuration(){
            MetricsData d = findMetricsByCategory("Duration(s)");
            if(d != null)
                return (int)(float)d.getMetricValue(MetricsData.Aggregator.SUM);
            return 0;
        }

        public void setDuration(long duration) {
            MetricsData m = findMetricsByCategory("Duration(s)");
            if(m == null){
                m = new MetricsData("Duration(s)");
                m.setAggregators(new MetricsData.Aggregator[]{MetricsData.Aggregator.SUM});
                getMetrics().add(m);
            }
            m.setMetricValue(MetricsData.Aggregator.SUM, (float)duration);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(MetricsData t : getMetrics()){
                for(Float f : t.getMetricsValues()){
                    sb.append(f == null ? "," : String.format("%.2f,", f));
                }
            }
            return sb.toString();
        }

        public String toHTMLString() {
            StringBuilder sb = new StringBuilder();
            for(MetricsData t : getMetrics()){
                for(Float f : t.getMetricsValues()){
                    sb.append(f == null || f == Float.MIN_VALUE || f == Float.MAX_VALUE
                            ? "<TD>N/A</TD>" : String.format("<TD>%.2f</TD>", f));
                }
            }
            return sb.toString();
        }

        public String getHTMLHeaders(){
            StringBuilder sb = new StringBuilder();
            for(String t : this.getMetricsCategories()){
                sb.append(String.format("<TH>%s</TH>", t));
            }
            return sb.toString();
        }
    }

    private int getNrDowns(List<MetricsData> mdList){
        int c=0, upStart=-1;
        for(int i = 0; i < mdList.size(); i++){
            Float totalErr = mdList.get(i).getMetricValue(MetricsData.Aggregator.NCNT);
            if(totalErr == null || totalErr == 0){
                if(upStart < 0 ){
                    upStart = 1;
                }
            }
            else{
                if(upStart > 0){
                    c++;
                    upStart = -1;
                }
            }
        }
        return c;
    }

    private long getTotalUpTime(long start, long end, List<MetricsData> mdList, Long[] timestampList) {
        long t=0, upStart=start;
        for(int i = 0; i < mdList.size(); i++){
            Float totalErr = mdList.get(i).getMetricValue(MetricsData.Aggregator.NCNT);
            if(totalErr == null || totalErr == 0){
                if(upStart > 0){
                    t = t + timestampList[i] - upStart;
                }
                upStart = timestampList[i];
            }
            else{
                if(upStart > 0){
                    upStart = -1;
                }
            }
        }
        return t;
    }

    private long getTotalRepairTime(long start, long end, List<MetricsData> mdList, Long[] timestampList) {
        long t=0, failStart=-1;
        for(int i = 0; i < mdList.size(); i++){
            Float totalErr = mdList.get(i).getMetricValue(MetricsData.Aggregator.NCNT);
            if(totalErr != null && totalErr > 0){
                if(failStart < 0 ){
                    failStart = timestampList[i];
                }
            }
            else{
                if(failStart > 0){
                    t = t + timestampList[i] - failStart;
                }
                failStart = -1;
            }
        }
        return t;
    }

    private GaugeMetrics createSummaryMetrics(MetricsRange range, String category){
        List<MetricsData> mdList = range.getMetricsByCategory(category);
        SummaryMetrics metrics = new SummaryMetrics();
        setActiveUsers(metrics, range.activeUsers);
        long duration = range.end - range.start;
        metrics.setDuration(duration/1000);
        MetricsData md = mdList.get(0).clone();
        for(int i = 1; i < mdList.size(); i++){
            md.mergeMetrics(mdList.get(i));
        }
        md.commit();
        metrics.setMetricsValue("Minimum(ms)", md.getMetricValue(MetricsData.Aggregator.MIN));
        metrics.setMetricsValue("Mean(ms)", md.getMetricValue(MetricsData.Aggregator.AVG));
        metrics.setMetricsValue("Medium(ms)", md.getMetricValue(MetricsData.Aggregator.MED));
        metrics.setMetricsValue("Maximum(ms)", md.getMetricValue(MetricsData.Aggregator.MAX));
        metrics.setMetricsValue("STDDEV(ms)", md.getMetricValue(MetricsData.Aggregator.STDDEV));
        metrics.setMetricsValue("75th Percentile(ms)", md.getMetricValue(MetricsData.Aggregator.PCT75));
        metrics.setMetricsValue("95th Percentile(ms)", md.getMetricValue(MetricsData.Aggregator.PCT95));
        metrics.setMetricsValue("99th Percentile(ms)", md.getMetricValue(MetricsData.Aggregator.PCT99));
        Float total = md.getMetricValue(MetricsData.Aggregator.CNT);
        Float totalErr = md.getMetricValue(MetricsData.Aggregator.NCNT);
        metrics.setMetricsValue("Tps", total != null ? total / (duration/1000) : null);
        metrics.setMetricsValue("Errors", totalErr);
        metrics.setMetricsValue("ErrorRate(%)", total != null && totalErr != null ? totalErr * 100 / (total + totalErr) : null);
        int nrDown = getNrDowns(mdList);
        Long[] timestampList = range.getMetricsTimestamp();
        long totalUptime = getTotalUpTime(range.start, range.end, mdList, timestampList);
        long totalRepairTime = getTotalRepairTime(range.start, range.end, mdList, timestampList);
        if(nrDown == 0){
            metrics.setMetricsValue("MTBF(ms)", Float.MAX_VALUE);
            metrics.setMetricsValue("MTTR(ms)", Float.MIN_VALUE);
        }
        else{
            metrics.setMetricsValue("MTBF(ms)", (float)totalUptime / nrDown);
            metrics.setMetricsValue("MTTR(ms)", totalRepairTime != Long.MAX_VALUE ? ((float)totalRepairTime / nrDown) : Float.MAX_VALUE) ;
        }
        return metrics;
    }
	
	public JobReport createJobReport(MergeConfig config) throws Exception{
        JobReport report = new JobReport();
        report.setConfig(config);
        String jobId = config.getName();
        report.setName(jobId);

		List<MetricsSource> metricsSources = storage.listSource(jobId,
                config.getLoadMeasureConfig().getMergeSource());
		if(metricsSources.size() == 0){
			LOG.error("No metrics sources to merge!");
			return null;
		}

		MetricsStore mergeStore = metricsSources.remove(0).load();
		for(MetricsSource source : metricsSources){
			mergeStore.merge(source.load());
		}
		mergeStore.commitMerge();

        List<GaugeMetrics> metricsList = mergeStore.getMetricsList();
        LOG.info("{} metrics sources merged", metricsSources.size() + 1);
        LOG.info("{} metrics collected", metricsList.size());
		report.getDetailMetrics().addAll(metricsList);

        if(metricsList.size() < 1){
            throw new RuntimeException("Too few data points");
        }

        long totalTime = getJobDuration(jobId, metricsList);
        report.setDuration(totalTime);
        report.setStartTime(getJobStartTime(jobId, metricsList));
		List<GaugeChart> charts = generateCharts(mergeStore, config.getLoadMeasureConfig().getChartConfig(), totalTime);
        report.getCharts().addAll(charts);

        List<TransactionSummary> summaries = report.getSummaries();
        List<MetricsRange> ranges = splitRangesByUsers(report.getStartTime(),
                report.getStartTime() + report.getDuration(), metricsList);
        for(String category : metricsList.get(0).getMetricsCategories()){
            if(category.equals("ActiveUsers"))
                continue;
            List<GaugeMetrics> sumMetrics = new ArrayList<GaugeMetrics>();
            for(MetricsRange r : ranges){
                sumMetrics.add(createSummaryMetrics(r, category));
            }
            TransactionSummary summary = new TransactionSummary(category, sumMetrics);
            summaries.add(summary);
        }
        String description = storage.getStorageProperty(jobId, "description");
        if(StringUtils.isEmpty(description)){
            report.setDescription(summaries.get(0).getName());
        }
        else{
            report.setDescription(description);
        }

        return report;
	}
}
