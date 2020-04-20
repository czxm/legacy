package com.intel.soak.agent.service.metrix.ganglia;

import com.intel.soak.agent.service.metrix.dto.MetricDataDto;
import net.sourceforge.jrrd.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/24/13
 * Time: 8:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class LocalGangliaRrdDaoImpl implements GangliaRrdDao {

    private static final String GANGLIA_RRD_DIR = "/var/lib/ganglia/rrds";
    private static final String RRD_DEFAULT_DIR = "__SummaryInfo__";
    private static final ConsolidationFunctionType CF_TYPE = ConsolidationFunctionType.AVERAGE;

    private static final Log LOG = LogFactory.getLog(LocalGangliaRrdDaoImpl.class);

    private long[] calRealTimeRange(final RRDatabase db, final long from, final long to) {
        Iterator<Archive> archives = db.getArchives(CF_TYPE);
        long start = -1L, end = -1L;
        while (archives.hasNext()) {
            Archive archive = archives.next();
            start = start == -1L ? archive.getStart() : start;
            start = archive.getStart() < start ? archive.getStart() : start;
            end = end == -1L ? archive.getEnd() : end;
            end = archive.getEnd() > end ? archive.getEnd() : end;
        }
        if (from > end || to < start)
            return null;
        if (from > start) {
            start = from;
        }
        end = to > end ? end : to;
        return new long[] {start, end};
    }

    public MetricDataDto fetchMetric(String metricName, long from, long to) {
        LOG.info(String.format("Query metric [%s] from %tc to %tc", metricName, new Date(from), new Date(to)));
        final MetricDataDto result = new MetricDataDto();
        try {
            String rrd_dir = GANGLIA_RRD_DIR;
            int slashIndex = metricName.lastIndexOf("/");
            if(slashIndex > 0){
                String subdir = metricName.substring(0, slashIndex);
                metricName = metricName.substring(slashIndex + 1);
                if(subdir.length() > 0){
                    rrd_dir = rrd_dir + File.separator + subdir;
                }
            }
            if(rrd_dir.equals(GANGLIA_RRD_DIR)){
                rrd_dir = rrd_dir + File.separator + RRD_DEFAULT_DIR;                            
            }
            final File rrdDir = new File(rrd_dir);
            if (!rrdDir.exists() || !rrdDir.isDirectory()) {
                LOG.warn("Ganglia rrd summary dir does not exist.");
                return null;
            }
            final File rrdFile = new File(rrdDir, metricName + ".rrd");
            if (!rrdDir.exists() || !rrdDir.isDirectory()) {
                LOG.warn("Metric not found: " + metricName);
                return null;
            }
            final RRDatabase db = new RRDatabase(rrdFile.getCanonicalPath());
            long[] timeRange = calRealTimeRange(db, from / 1000, to / 1000);
            if (timeRange == null) {
                LOG.warn("No data matching the request!");
                return null;
            }
            final DataChunk dataChunk = db.getData(CF_TYPE, timeRange[0], timeRange[1], 1L);

            result.setMetric(metricName);
            result.setStart(new Date(timeRange[0] * 1000));
            result.setEnd(new Date(timeRange[1] * 1000));

            Map<Date, Double>[] maps = dataChunk.toArrayOfMap();
            if (maps.length < 1) return null;
            for (Map.Entry<Date, Double> entry : maps[0].entrySet()) {
                result.add(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            LOG.error("Fetch metrics failed: " + metricName);
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public static void main(String[] args) {
        LocalGangliaRrdDaoImpl i = new LocalGangliaRrdDaoImpl();
        long now = System.currentTimeMillis();
        i.fetchMetric("load_five", 0, now);
    }

}
