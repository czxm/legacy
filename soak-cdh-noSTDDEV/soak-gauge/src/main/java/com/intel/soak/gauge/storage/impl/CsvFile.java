package com.intel.soak.gauge.storage.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.intel.soak.gauge.GaugeMetrics;
import com.intel.soak.gauge.measure.MetricsMemStore;
import com.intel.soak.gauge.measure.MetricsStore;
import com.intel.soak.gauge.storage.MetricsSource;

public class CsvFile implements MetricsSource {
    private static final Log LOG = LogFactory.getLog(CsvFile.class);
    
    private File backfile;
    
    public CsvFile(File backfile){
        this.backfile = backfile;
    }
    
    @Override
    public String getName(){
        return backfile.getName();
    }
    
    @Override
    public void append(GaugeMetrics metrics) throws Exception {
        boolean writeHeader = !backfile.exists();
        FileOutputStream fos = new FileOutputStream(backfile, true);           
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));           
        if(writeHeader){
            writer.write("Timestamp");
            for(String name : metrics.getMetricsNames()){
                writer.write(",");
                writer.write(name);
            }
            writer.write("\n");
        }
        writer.write(metrics.toString());
        writer.write("\n");
        writer.close();
    }                

    @Override
    public MetricsStore load() throws Exception {
        InputStream ins = new FileInputStream(backfile);
        try{
            MetricsMemStore store = new MetricsMemStore();
            store.load(ins);
            return store;
        }
        catch(Exception e){
            throw e;
        }
        finally{
            ins.close();
        }
    }

    @Override
    public void save(MetricsStore store) throws Exception {
        OutputStream fos = new FileOutputStream(backfile);
        try{
            ((MetricsMemStore)store).save(fos);            
        }
        catch(Exception e){
            throw e;
        }
        finally{
            fos.close();
        } 
    }

    @Override
    public void clear() throws Exception {
        try{
            backfile.delete();
        }
        catch(Exception e){
            throw e;
        }
    }
}
