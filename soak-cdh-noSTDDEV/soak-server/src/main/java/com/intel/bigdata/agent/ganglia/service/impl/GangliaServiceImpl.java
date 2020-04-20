package com.intel.bigdata.agent.ganglia.service.impl;

import com.intel.bigdata.agent.ganglia.data.*;
import com.intel.bigdata.agent.ganglia.service.GangliaService;
import com.intel.bigdata.common.protocol.MetricStatus;
import com.intel.bigdata.common.protocol.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

@Component
public class GangliaServiceImpl implements GangliaService {
	@Component
	public static class Config {
//		private String gangliaPort = "8651";
		private String gangliaPort = "8649";

		public Config() {}
		
		public void setGangliaPort(String port) {
			gangliaPort = port;
		}
	}
	private Config config;
	
	private class ProcessingContext {
		Map<String, Map<String, MetricStatus>> host2metric;
//		Map<String, AlertLevel> metric2alertLevel;
		ArrayList<MetricStatus> metricStatuses;
		
		public ProcessingContext(Map<String, Map<String, MetricStatus>> host2metric,/* Map<String, AlertLevel> metric2alertLevel, */ArrayList<MetricStatus> metricStatuses) {
			this.host2metric = host2metric;
//			this.metric2alertLevel = metric2alertLevel;
			this.metricStatuses = metricStatuses; 
		}
		
		public Map<String, Map<String, MetricStatus>> getHost2metric() {
			return host2metric;
		}
//		public Map<String, AlertLevel> getMetric2alertLevel() {
//			return metric2alertLevel;
//		}

		public ArrayList<MetricStatus> getMetricStatuses() {
			return metricStatuses;
		}
	}
	
	private static final int GANGLIA_SOCKET_TIMEOUT = 5000;

    protected static final Logger LOG = LoggerFactory.getLogger(GangliaServiceImpl.class);
    
	// It is possible that this reference will point to another object on the next read...
    private volatile Map<String, Map<String, MetricStatus>> host2metric;
    private volatile ArrayList<MetricStatus> metricStatuses = new ArrayList<MetricStatus>();

//RRD4J	private Rrd4Dao rrd4dao;
    
//	private ConfigDao configDao;

//    @Override
//    public String checkMetric(String host, String metric, double warning, double critical) {
//    	if (!StringUtils.isEmpty(host) && !StringUtils.isEmpty(metric)) {
//        	String val = getMetricValueFromXML(host, metric);
//
//        	if (val != null) {
//        		Status status = getMetricStatus(val, warning, critical);
//
//        		if (status != Status.UNKNOWN) {
//            		return "CHECKGANGLIA " + status.name() + ": " + metric + " is " + val;
//        		} else {
//        			return "CHECKGANGLIA UNKNOWN: Error while getting value";
//        		}
//        	}
//    	} else {
//    		LOG.error("Empty host or metric passed to checkMetric");
//    	}
//
//		return "CHECKGANGLIA UNKNOWN: Error while getting value";
//    }
    
    @Override
    public List<MetricStatus> getMetricStatuses() {
    	return metricStatuses;
    }
    
    private Status getMetricStatus(String value, double warning, double critical) {
		if (value != null) {
			try {
				Double val = Double.parseDouble(value);
		    	if (val != null) {
		    		if (critical > warning) {
		    		    if (val >= critical) {
		    		    	return Status.CRITICAL;
		    		    } else if (val >= warning) {
		    		    	return Status.WARNING;
		    		    } else {
		    		    	return Status.OK;
		    		    }
		    		} else {
		    		    if (critical >= val) {
		    		    	return Status.CRITICAL;
		    		    } else if (warning >= val) {
		    		    	return Status.WARNING;
		    		    } else {
		    		    	return Status.OK;
		    		    }
		    		}
		    	}    		
    		} catch (NumberFormatException e) {
    			LOG.error("Cannot parse a metric value: " + e);    			
    		}    		
		}

		return Status.UNKNOWN;
    }
    
	private String getMetricValueFromXML(String host, String metric) {
		Map<String, Map<String, MetricStatus>> h2m = host2metric; // Save the reference, could be modified by other thread later
    	
    	if (h2m != null) { // XML data was loaded from ganglia
    		Map<String, MetricStatus> m2v = h2m.get(host);
    		if (m2v != null) {
    			MetricStatus status = m2v.get(metric);
    			if (status != null) {
	    			return status.getValue();
    			}
    		}
    	}
    	
    	return null;
    }
    
	/**
	 * Returns true when Ganglia response has been processed successfully.
	 * Success means:
	 * a) no errors
	 * b) hosts count > 0
	 * @param is
	 * @return
	 */
    private boolean processGangliaResponse(InputStream is) {
        try {
	        JAXBContext c = JAXBContext.newInstance(GANGLIAXML.class.getPackage().getName());
	        GANGLIAXML gxml = (GANGLIAXML) c.createUnmarshaller().unmarshal(is);
	        
	        Map<String, Map<String, MetricStatus>> host2metric = new HashMap<String, Map<String, MetricStatus>>();
//	        Map<String, AlertLevel> metric2alertLevel = createAlertLevelMap();
	        ArrayList<MetricStatus> metricStatuses = new ArrayList<MetricStatus>();
	        processGangliaXml(gxml, new ProcessingContext(host2metric, /*metric2alertLevel, */metricStatuses));
	        
			this.host2metric = host2metric;
			this.metricStatuses = metricStatuses;
			
			return host2metric.keySet().size() > 0;
		} catch (Exception e) {
			LOG.error("Cannot parse ganglia XML: " + e);
			return false;
		} 
    }

    private void processObject(Object obj, ProcessingContext pc) {
		if (obj instanceof GRID) {
			GRID grid = (GRID) obj;
			processGrid(grid, pc);
		} else if (obj instanceof CLUSTER) {
			CLUSTER cluster = (CLUSTER) obj;
			processCluster(cluster, pc);
		} else if (obj instanceof HOST) {
			HOST host = (HOST) obj;
    		processHost(host, pc);
		} else if (obj instanceof List) {
			List<?> l = (List<?>) obj;
			for (Object o : l) {
				processObject(o, pc);
			}
		}    	
    }

	private void processGangliaXml(GANGLIAXML gxml, ProcessingContext pc) {
		Object obj = gxml.getGRIDOrCLUSTEROrHOST();
		processObject(obj, pc);
	}
    
    private void processGrid(GRID grid, ProcessingContext pc) {
    	Object obj = grid.getCLUSTEROrGRIDOrHOSTSOrMETRICS();
    	processObject(obj, pc);
	}

	private void processCluster(CLUSTER cluster, ProcessingContext pc) {
		Object obj = cluster.getHOSTOrHOSTSOrMETRICS();
		processObject(obj, pc);
	}

	private void processHost(HOST host, ProcessingContext pc) {
		HashMap<String, MetricStatus> metric2value = new HashMap<String, MetricStatus>();
		List<METRIC> metrics = host.getMETRIC();
		for (METRIC metric : metrics) {
			MetricStatus status = new MetricStatus(new Date(Long.valueOf(host.getREPORTED()) * 1000), metric.getNAME(), host.getNAME(), metric.getVAL());
//			AlertLevel al = pc.getMetric2alertLevel().get(metric.getNAME());
//			if (al != null) {
//				status.setStatus(getMetricStatus(metric.getVAL(), al.getWarningLevel(), al.getCriticalLevel()));				
//			}
			metric2value.put(metric.getNAME(), status);
			pc.getMetricStatuses().add(status);
		}
		
		pc.getHost2metric().put(host.getNAME(), metric2value);
	}

    //TODO impl later
    //@Scheduled(fixedDelay = 15000)
    public void updateGangliaXML() {        
    	Socket socket = null;
    	boolean restartRequired = true;
        try {
        	String gangliaHost;
        	Integer gangliaPort;
            try {
				gangliaPort = Integer.parseInt(config.gangliaPort);
			
//				if (gangliaPort == 8651) {
//					gangliaHost = "localhost";
//				} else {
//					SystemTopology systemTopology = configDao.getSystemTopology();
//					gangliaHost = systemTopology.getMasterAddress();
//				}
				gangliaHost = "localhost";

			} catch (Exception e) {
				// Fall back into the production mode
				LOG.error("Cannot obtain ganglia host or port: " + e);
	        	gangliaHost = "localhost";
	        	gangliaPort = 8649;				
			}
            LOG.debug("Ganglia host is " + gangliaHost + " port is " + gangliaPort);
            
            socket = new Socket(gangliaHost, gangliaPort);
            socket.setSoTimeout(GANGLIA_SOCKET_TIMEOUT);
            
            restartRequired = !processGangliaResponse(socket.getInputStream());
            
			socket.close();
        } catch (UnknownHostException e) {
        	LOG.error("Cannot open socket while updating ganglia XML: " + e);
        } catch (IOException e) {
        	LOG.error("Cannot update ganglia XML: " + e);
        }
        
        if (restartRequired) {
            LOG.info("Gmond is not healthy");
            try {
                restartService("gmond");
            } catch (Exception e) {
                LOG.error("Couldn't restart gmond ", e);
            }
        } else {
            LOG.info("Gmond is healthy");
        }
        
        // Update rrd database
//RRD4J        rrd4dao.saveMetrics(metricStatuses);
    }

//    private Map<String, AlertLevel> createAlertLevelMap() {
//    	AlertLevelConfig activeAlertLevelConfig = configDao.getActiveAlertLevelConfig();
//    	List<AlertLevel> alertLevels = activeAlertLevelConfig.getAlertLevels();
//    	Map<String, AlertLevel> alertLevelMap = new HashMap<String, AlertLevel>();
//    	
//    	for (AlertLevel alertLevel : alertLevels) {
//    		alertLevelMap.put(alertLevel.getMetricName(), alertLevel);
//		}
//    	
//    	return alertLevelMap;
//    }
    
    @Autowired
    @Required
    public void setConfig(Config config) {
        this.config = config;
    }

//RRD4J
//    @Autowired
//    @Required
//    public void setRrd4Dao(Rrd4Dao rrd4dao) {
//        this.rrd4dao = rrd4dao;
//    }
    
//    @Autowired
//    @Required
//    public void setConfigDao(ConfigDao configDao) {
//        this.configDao = configDao;
//    }
    
    private void restartService(final String service) {
        LOG.info("restart service {}", service);
        try {
            exec(String.format("/etc/init.d/%s restart", service));
        } catch (Exception e) {
            LOG.error("Couldn't restart service {}: {}", service, e);
        }
    }
    
    private void exec(String cmd) throws Exception {
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        if (p.exitValue() != 0) {
            LOG.warn("Failed to run " + cmd + " (code=" + p.exitValue() + ")");
        } else {
            LOG.info("Successfully executed " + cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                LOG.info(line);
            }
            br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = br.readLine()) != null) {
                LOG.info(line);
            }
            LOG.info("------------------------------");
        }
    }

}
