package com.intel.soak.gauge.storage.impl;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.intel.soak.gauge.GaugeMetrics;
import com.intel.soak.gauge.measure.MetricsMemStore;
import com.intel.soak.gauge.measure.MetricsStore;
import com.intel.soak.gauge.storage.MetricsSource;

public class InMemSource implements MetricsSource {
    private static final Log LOG = LogFactory.getLog(InMemSource.class);
    
    private String name;
    private ByteArrayOutputStream bos;
    
    public InMemSource(){
        this.bos = new ByteArrayOutputStream();
    }
    
    @Override
    public String getName(){
        return this.name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    @Override
    public void append(GaugeMetrics metrics) throws Exception {
        boolean writeHeader = (bos.size() == 0);          
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(bos));           
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
        InputStream ins = new ByteArrayInputStream(bos.toByteArray());
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
        try{
            bos = new ByteArrayOutputStream();
            ((MetricsMemStore)store).save(bos);            
        }
        catch(Exception e){
            throw e;
        }
    }

    @Override
    public void clear() throws Exception {
        try{
            bos = new ByteArrayOutputStream();
        }
        catch(Exception e){
            throw e;
        }
    }
    
    public InputStream getInputStream(){
        return new ByteArrayInputStream(bos.toByteArray());
    }
}
