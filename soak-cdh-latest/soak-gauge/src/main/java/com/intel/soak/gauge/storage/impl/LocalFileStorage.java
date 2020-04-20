package com.intel.soak.gauge.storage.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.soak.config.SoakConfig;
import com.intel.soak.gauge.storage.GaugeStorage;
import com.intel.soak.gauge.storage.MetricsSource;
import com.intel.soak.model.ParamType;
import com.intel.soak.utils.FileUtils;

public class LocalFileStorage implements GaugeStorage {
    private static final Logger LOG = LoggerFactory.getLogger(LocalFileStorage.class);
    
    private File outputFolder;
    
    @Override
    public void setParams(List<ParamType> params){
        String output = null;
        for(ParamType param : params){
            if(param.getName().equals("output")){
                output = param.getValue();
            }
        }
        if(output == null){
            output = "result";
        }
        outputFolder = new File(SoakConfig.BaseDir.HOME.toString() + File.separator + output);
        LOG.info("Using output folder: " + outputFolder.getAbsolutePath());
        if(!outputFolder.exists())
            outputFolder.mkdir();
    }
    
    @Override
    public String getStorageProperty(String storage, String key){
        Properties props = new Properties();
        try{
            File storageFolder = new File(outputFolder, storage);
            props.load(new FileInputStream(new File(storageFolder, "storage.ini")));
            return props.getProperty(key);
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
    
    @Override
    public void setStorageProperty(String storage, String key, String value){
        Properties props = new Properties();
        FileOutputStream fos = null;
        try{
            File storageFolder = new File(outputFolder, storage);
            File iniFile = new File(storageFolder, "storage.ini");
            if(iniFile.exists())
                props.load(new FileInputStream(iniFile));
            props.setProperty(key, value);
            fos = new FileOutputStream(new File(storageFolder, "storage.ini"));
            props.store(fos, "DON'T EDIT THIS FILE MANUALLY");
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        finally{
            if(fos != null){
                try{
                    fos.close();
                }
                catch(Exception e){
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public boolean createStorage(String storage) throws Exception {
        File storageFolder = new File(outputFolder, storage);
        if (storageFolder.exists()) {
            File newfolder = new File(FileUtils.getNextFileName(storageFolder.getAbsolutePath()));
            LOG.info(storageFolder.getName()
                    + " is renamed to "
                    + newfolder.getName()
                    + (storageFolder.renameTo(newfolder) ? " successfully"
                            : " failed"));
        }
        if(!storageFolder.mkdir())
            return false;
        File metricsFolder = new File(storageFolder, "metrics");
        if(!metricsFolder.exists() && !metricsFolder.mkdir())
            return false;
        File logsFolder = new File(storageFolder, "logs");
        if(!logsFolder.exists() && !logsFolder.mkdir())
            return false;
        return true;
    }

    @Override
    public boolean deleleStorage(String storage) {
        File folder = new File(outputFolder, storage);
        if (folder.exists()) {
            FileUtils.deleteFolderAndContents(folder);
        }
        LOG.info(folder.getName() + " is deleted " + (folder.exists() ? "failed" : "successfully"));
        return !folder.exists();
    }

    private File checkExists(String storage, String source) throws Exception{
        File storageFolder = new File(outputFolder, storage);
        if(!storageFolder.exists())
            throw new IOException("Storage does not exist!");
        File metricsFolder = new File(storageFolder, "metrics");
        if(!metricsFolder.exists())
            throw new IOException("Metrics folder does not exist!");
        return metricsFolder;
    }
    
    @Override
    public MetricsSource openSource(String storage, String source) throws Exception {
        File metricsFile = new File(checkExists(storage, source), source + ".csv");
        return new CsvFile(metricsFile);
    }

    @Override
    public List<MetricsSource> listSource(String storage, String pattern) {
        List<MetricsSource> sources = new ArrayList<MetricsSource>();
        File metricDir = new File(outputFolder, storage);
        List<File> metricsFiles = FileUtils.listFiles(metricDir, pattern);
        for(File f : metricsFiles){
            sources.add(new CsvFile(f));
        }
        return sources;
    }

}
