package com.intel.soak.utils;

import com.intel.soak.config.ConfigUtils;
import com.intel.soak.config.SoakConfig;
import com.intel.soak.gauge.GaugeMaster;
import com.intel.soak.gauge.GaugeReport;
import com.intel.soak.gauge.magnify.ReportGenerator;
import com.intel.soak.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.io.PrintStream;

public enum SoakServerUtils {

    INSTANCE;

    private static Logger LOG = LoggerFactory.getLogger(SoakServerUtils.class);

    public static boolean parseArguments(String[] args, String helpMsg, PrintStream out) {
        if (args.length < 1) {
            out.println(helpMsg);
            return false;
        }
        List<Object> list = ConfigUtils.parseParams(args);
        if (list.size() == 0) {
            out.println("No valid config found!");
            return false;
        }
        return true;
    }

    public static boolean generateReport(List<MergeConfig> configList) {
        ApplicationContext systemAppCxt = SpringBeanFactoryManager.getSystemAppCxt();
        SoakConfig soakConfig = systemAppCxt.getBean(SoakConfig.CONFIG, SoakConfig.class);
        GaugeMaster gauge = systemAppCxt.getBean(
                soakConfig.getConfig(SoakConfig.ConfigKey.GaugeMaster),
                GaugeMaster.class);
        return generateReport(gauge, configList, null);
    }

    public static boolean generateReport(GaugeMaster gauge, List<MergeConfig> configList, String file){
        try{
            if(configList.size() > 0){
                GaugeReport report = gauge.createReport(configList);
                ReportGenerator gen = new ReportGenerator();
                InputStream ins = gen.createArchive(report);
                FileOutputStream fos = new FileOutputStream(file == null ?
                        "soak-" + report.getStartTime() + ".zip" :  file);
                FileUtils.copyStream(ins, fos);
                fos.close();
                ins.close();
                return true;
            }
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return false;
    }
}
