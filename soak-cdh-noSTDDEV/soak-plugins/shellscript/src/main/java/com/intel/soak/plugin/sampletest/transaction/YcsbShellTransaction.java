package com.intel.soak.plugin.sampletest.transaction;

import com.intel.soak.MetricsData;
import com.intel.soak.annotation.Metrics;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zb161
 * Date: 13-11-5
 * Time: 上午10:47
 * To change this template use File | Settings | File Templates.
 */
@Plugin(desc="ycsb shell Transaction", type = PLUGIN_TYPE.TRANSACTION)
public class YcsbShellTransaction extends ShellScriptTransaction {
    private Map<String, String> metrics = new HashMap<String, String>();

    private final String Key_Runtime = "Runtime";
    private final String Key_Throughput = "Throughput";
    private final String Key_UpdateAvgLatency = "UpdateAvgLatency";
    private final String Key_UpdateMinLatency = "UpdateMinLatency";
    private final String Key_UpdateMaxLatency = "UpdateMaxLatency";
    private final String Key_ReadAvgLatency = "ReadAvgLatency";
    private final String Key_ReadMinLatency = "ReadMinLatency";
    private final String Key_ReadMaxLatency = "ReadMaxLatency";

    @Override
    protected void processLine(String line) {
        if (line.startsWith("[OVERALL]")) {
            String[] tmp = line.split(",");
            if (tmp[1].trim().equals("RunTime(ms)")) {
                metrics.put(Key_Runtime, tmp[2].trim());
            } else if (tmp[1].trim().equals("Throughput(ops/sec)")) {
                metrics.put(Key_Throughput, tmp[2].trim());
            }
        } else if (line.startsWith("[UPDATE]")) {
            String[] tmp = line.split(",");
            if (tmp[1].trim().equals("AverageLatency(us)")) {
                metrics.put(Key_UpdateAvgLatency, tmp[2].trim());
            } else if (tmp[1].trim().equals("MinLatency(us)")) {
                metrics.put(Key_UpdateMinLatency, tmp[2].trim());
            } else if (tmp[1].trim().equals("MaxLatency(us)")) {
                metrics.put(Key_UpdateMaxLatency, tmp[2].trim());
            }
        } else if (line.startsWith("[READ]")) {
            String[] tmp = line.split(",");
            if (tmp[1].trim().equals("AverageLatency(us)")) {
                metrics.put(Key_ReadAvgLatency, tmp[2].trim());
            } else if (tmp[1].trim().equals("MinLatency(us)")) {
                metrics.put(Key_ReadMinLatency, tmp[2].trim());
            } else if (tmp[1].trim().equals("MaxLatency(us)")) {
                metrics.put(Key_ReadMaxLatency, tmp[2].trim());
            }
        }
    }

    @Metrics(name="Runtime", aggregators={MetricsData.Aggregator.MIN, MetricsData.Aggregator.MED, MetricsData.Aggregator.MAX})
    public Float getRuntime(){
        if (metrics.containsKey("Runtime")) {
            return Float.valueOf(metrics.get("Runtime"));
        }

        return null;
    }

    @Metrics(name="Throughput", aggregators={MetricsData.Aggregator.MIN, MetricsData.Aggregator.AVG, MetricsData.Aggregator.MAX})
    public Float getThroughput(){
        if (metrics.containsKey("Throughput")) {
            return Float.valueOf(metrics.get("Throughput"));
        }

        return null;
    }

    @Metrics(name="UpdateAvgLatency", aggregators={MetricsData.Aggregator.AVG})
    public Float getUpdateAvgLatency(){
        if (metrics.containsKey(Key_UpdateAvgLatency)) {
            return Float.valueOf(metrics.get(Key_UpdateAvgLatency));
        }

        return null;
    }

    @Metrics(name="UpdateMinLatency", aggregators={MetricsData.Aggregator.MIN})
    public Float getUpdateMinLatency(){
        if (metrics.containsKey(Key_UpdateMinLatency)) {
            return Float.valueOf(metrics.get(Key_UpdateMinLatency));
        }

        return null;
    }

    @Metrics(name="UpdateMaxLatency", aggregators={MetricsData.Aggregator.MAX})
    public Float getUpdateMaxLatency(){
        if (metrics.containsKey(Key_UpdateMaxLatency)) {
            return Float.valueOf(metrics.get(Key_UpdateMaxLatency));
        }

        return null;
    }

    @Metrics(name="ReadAvgLatency", aggregators={MetricsData.Aggregator.AVG})
    public Float getReadAvgLatency(){
        if (metrics.containsKey(Key_ReadAvgLatency)) {
            return Float.valueOf(metrics.get(Key_ReadAvgLatency));
        }

        return null;
    }

    @Metrics(name="ReadMinLatency", aggregators={MetricsData.Aggregator.MIN})
    public Float getReadMinLatency(){
        if (metrics.containsKey(Key_ReadMinLatency)) {
            return Float.valueOf(metrics.get(Key_ReadMinLatency));
        }

        return null;
    }

    @Metrics(name="ReadMaxLatency", aggregators={MetricsData.Aggregator.MAX})
    public Float getReadMaxLatency(){
        if (metrics.containsKey(Key_ReadMaxLatency)) {
            return Float.valueOf(metrics.get(Key_ReadMaxLatency));
        }

        return null;
    }
}
