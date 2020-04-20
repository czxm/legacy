package com.intel.cedar.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.core.entities.CloudInfo;
import com.intel.cedar.scheduler.CedarTimer;
import com.intel.cedar.util.CloudUtil;

public class CedarMonitor {
    private static Logger LOG = LoggerFactory.getLogger(CedarMonitor.class);
    private static CedarMonitor singleton;

    public static synchronized CedarMonitor getInstance() {
        if (singleton == null)
            singleton = new CedarMonitor();
        return singleton;
    }

    private CedarMonitor() {        
    }

    public void start() {
        CedarTimer.getInstance().scheduleTask(30, new AbstractCedarMonitor("Physical Node Monitor"){
            @Override
            public void run() {
                try{
                    monitorNodes();
                }
                catch(Exception e){
                    LOG.error(e.getMessage(), e);
                }
            } 
        });
        
        CedarTimer.getInstance().scheduleTask(30, new AbstractCedarMonitor("Instance Monitor"){
            @Override
            public void run() {
                try{
                    monitorInstances();
                }
                catch(Exception e){
                    LOG.error(e.getMessage(), e);
                }
            } 
        });
        
        CedarTimer.getInstance().scheduleTask(300, new AbstractCedarMonitor("Volume Monitor"){
            @Override
            public void run() {
                try{
                    monitorVolumes();
                }
                catch(Exception e){
                    LOG.error(e.getMessage(), e);
                }
            } 
        });
        
        CedarTimer.getInstance().scheduleTask(15, new AbstractCedarMonitor("Cloud Monitor"){
            @Override
            public void run() {
                for (final CloudInfo cloud : CloudUtil.getClouds()) {
                    monitorCloud(cloud);
                }
            } 
        });
    }

    public void stop() {

    }
}
