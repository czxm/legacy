package com.intel.bigdata.agent.ganglia.dao.impl;

import com.intel.bigdata.agent.ganglia.dao.Rrd4Dao;
import com.intel.bigdata.common.protocol.MetricStatus;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class Rrd4DaoImpl implements Rrd4Dao {
    protected static Logger LOG = LoggerFactory.getLogger(Rrd4DaoImpl.class);
	
	private static final String RRD_DB_DIR = "./rrds";

	private static final RrdDefTemplate rrdDefTemplate;
	
//	private static final HashMap<String, RrdDb> metric2rrdDb = new HashMap<String, RrdDb>();
	
	static {
		RrdDefTemplate rdt;
		try {
			Resource resource = new ClassPathResource("rrdDef.xml");			
			rdt = new RrdDefTemplate(resource.getFile());
		} catch (IOException e) {
			LOG.error("Cannot read rrd template xml. " + e);
			rdt = null;
		}
		rrdDefTemplate = rdt;
	}
	
	private ConsolFun getConsolidationFunction(String metricName) {
		// TODO - implement mapping if needed
		return ConsolFun.AVERAGE;
	}
	
	/**
	 * Rrd db should be closed after using it
	 * @param metricName
	 * @return
	 */
	private RrdDb getRrdDb(String metricName) {
//		RrdDb rrdDb = metric2rrdDb.get(metricName);
//		if (rrdDb != null) {
//			return rrdDb;
//		} else {
		RrdDb rrdDb;
		String rrdFileName = getRrdFileName(metricName);
		if (Util.fileExists(rrdFileName)) {
			try {
				rrdDb = new RrdDb(rrdFileName);
			} catch (IOException e) {
				LOG.error("Cannot read an existing rrd database: " + rrdFileName + ". " + e);
				// TODO - Recreate the rrd db here?
				rrdDb = null;
			}
		} else {
			rrdDb = createRrdDb(metricName);
		}
			
//			metric2rrdDb.put(metricName, rrdDb);
		return rrdDb;
//		}		
	}

	private RrdDb createRrdDb(String metricName) {
		rrdDefTemplate.setVariable("path", getRrdFileName(metricName));
		rrdDefTemplate.setVariable("CF", getConsolidationFunction(metricName).name());
		RrdDef rrdDef = rrdDefTemplate.getRrdDef();
		try {
			File parentDir = new File(rrdDef.getPath()).getParentFile();
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			} 
			return new RrdDb(rrdDef);
		} catch (IOException e) {
			LOG.error("Cannot create an rrd database: " + rrdDef.getPath() + ". " + e);
		}
		return null;
	}

	private String getRrdFileName(String metricName) {
		return RRD_DB_DIR + "/" + metricName + ".rrd";
	}

	@Override
	public void saveMetricValue(MetricStatus ms) {
		RrdDb rrdDb = getRrdDb(ms.getName());
		if (rrdDb != null) {
			try {
				Sample sample = rrdDb.createSample(ms.getLastCheckTime().getTime()/1000);
				sample.setValues(Double.parseDouble(ms.getValue()));
				sample.update();
			} catch (IOException e) {
				LOG.error("Cannot store sample in an rrd database: " + ms.getName() + ". " + e);
			} catch (NumberFormatException e) {
//				LOG.info("Cannot store sample in an rrd database: " + ms.getName() + ". " + e);
				// Ignore
			} finally {
				try {
					rrdDb.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	@Override
	public void saveMetrics(List<MetricStatus> metricStatuses) {
		for (MetricStatus metricStatus : metricStatuses) {
			saveMetricValue(metricStatus);
		}
	}
}
