package com.intel.soak.plugin.mapred.dataload;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/12/13
 * Time: 9:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataLoaderConfig {

    private String dataType;
    private long records;
    private String target;
    private String coreSitePath;
    private String mrSitePath;

    public DataLoaderConfig(String dataType, long records, String target,
                            String coreSitePath, String mrSitePath) {
        this.dataType = dataType;
        this.records = records;
        this.target = target;
        this.coreSitePath = coreSitePath;
        this.mrSitePath = mrSitePath;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public long getRecords() {
        return records;
    }

    public void setRecords(long records) {
        this.records = records;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getCoreSitePath() {
        return coreSitePath;
    }

    public void setCoreSitePath(String coreSitePath) {
        this.coreSitePath = coreSitePath;
    }

    public String getMrSitePath() {
        return mrSitePath;
    }

    public void setMrSitePath(String mrSitePath) {
        this.mrSitePath = mrSitePath;
    }

}
