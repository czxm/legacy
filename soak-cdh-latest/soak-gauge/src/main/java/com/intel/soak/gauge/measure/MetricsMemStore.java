package com.intel.soak.gauge.measure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.intel.soak.gauge.GaugeMetrics;
import com.intel.soak.MetricsData;
import com.intel.soak.MetricsData.Aggregator;

public class MetricsMemStore implements MetricsStore{
    String[] header;
    List<GaugeMetrics> dataList;
    Map<Long, GaugeMetrics> dataIndex;
    
    public MetricsMemStore(){
        dataIndex = new HashMap<Long, GaugeMetrics>();
        dataList = new ArrayList<GaugeMetrics>();
    }
    
    public void setHeaders(String[] header){
        this.header = header;
    }
    
    public String getHeader() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(header[0]);
            for (int i = 1; i < header.length; i++) {
                sb.append(",");
                sb.append(header[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    };

    @Override
    public String[] getHeaders() {
        return this.header;
    }

    private void setValueByIndex(GaugeMetrics metrics, Float value, int index){
        String name = getHeaders()[index];
        int underscore = name.lastIndexOf("_");
        String aggregator = name.substring(underscore + 1);
        Aggregator aggr = null;
        try{
            aggr = Aggregator.valueOf(aggregator);
        }
        catch(Exception e){                
        }
        if(aggr == null){
            //try to use _AVG as default aggregator
            aggr = Aggregator.AVG;
        }
        String category = name.substring(0, underscore);
        MetricsData md = metrics.findMetricsByCategory(category);
        if(md != null){
            md.setMetricValue(aggr, value);
        }
    }
    
    private Float getValueByIndex(GaugeMetrics metrics, int index){
        String name = getHeaders()[index];
        int underscore = name.lastIndexOf("_");
        String aggregator = name.substring(underscore + 1);
        Aggregator aggr = null;
        try{
            aggr = Aggregator.valueOf(aggregator);
        }
        catch(Exception e){                
        }
        if(aggr == null){
            //try to use _AVG as default aggregator
            aggr = Aggregator.AVG;
        }
        String category = name.substring(0, underscore);
        MetricsData md = metrics.findMetricsByCategory(category);
        if(md != null){
            return md.getMetricValue(aggr);
        }
        return null;
    }
    
    public void load(InputStream fis) throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line = reader.readLine();
        if(line == null)
            return;
        this.header = line.split(",");
        while((line = reader.readLine()) != null){
            if(line.length() > 0){
                GaugeMetrics metrics = GaugeMetrics.fromMetricsNames(this.header);
                String[] str = line.split(",");
                metrics.setTimestamp(Long.parseLong(str[0]));
                for(int i = 1; i < str.length; i++){
                    setValueByIndex(metrics, str[i].equals("") ? null : Float.parseFloat(str[i]), i);
                }
                dataList.add(metrics);
                dataIndex.put(metrics.getTimestamp(), metrics);
            }
        }
    }
    
    public void save(OutputStream fos) throws Exception{
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
        writer.write(header[0]);
        for(int i = 1; i < header.length; i++)
            writer.write("," + header[i]);
        writer.newLine();            
        for(GaugeMetrics d : dataList){
            writer.write(d.toString());
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }
    
    @Override
    public List<Object> getSubListByColumn(String column, int max) {
        int index = 0;
        List<Object> result = new ArrayList<Object>();
        try {
            for (String h : header) {
                if (h.equals(column)) {
                    break;
                }
                index++;
            }
            if (index >= header.length) {
                index = 0;
                // try the default AVG aggregator
                String newColume = column + "_AVG";
                for (String h : header) {
                    if (h.equals(newColume)) {
                        break;
                    }
                    index++;
                }
            }
            if (index < header.length) {
                int count = 0;
                for (GaugeMetrics data : dataList) {
                    if (count < max) {
                        Object o = null;
                        if(index == 0)
                            o = data.getTimestamp();
                        else
                            o = getValueByIndex(data, index);
                        result.add(o);
                    }
                    count++;
                }
            }
            else{
                // try EL evaluation
                int count = 0;
                for (GaugeMetrics data : dataList) {
                    if (count < max) {
                        Object o = new Evaluator(data).eval(column);
                        result.add(o);
                    }
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public int getDataListSize() {
        return this.dataList.size();
    }
    
    public void addGaugeMetrics(GaugeMetrics data){
        dataIndex.put(data.getTimestamp(), data);
        for(int i = 0; i < dataList.size(); i++){
            if(data.getTimestamp() < dataList.get(i).getTimestamp()){
                dataList.add(i, data);
                return;
            }
        }
        dataList.add(data);
    }
    
    @Override
    public void merge(MetricsStore other) throws Exception{
        for(GaugeMetrics od : ((MetricsMemStore)other).dataList){
            GaugeMetrics md = dataIndex.get(od.getTimestamp());
            if(md != null){
                md.merge(od);
            }
            else{
                addGaugeMetrics(od);
            }
        }
    }
    
    @Override
    public void commitMerge(){
        for(GaugeMetrics md : dataList){
            md.commit();
        }
    }
    
    @Override
    public List<GaugeMetrics> getMetricsList(){
        return dataList;
    }
}