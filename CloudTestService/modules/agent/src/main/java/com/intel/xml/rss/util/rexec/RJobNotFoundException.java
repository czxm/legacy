package com.intel.xml.rss.util.rexec;

/**
 * This code should compilable under java source version 1.4.
 * 
 * @author han
 * 
 */
public class RJobNotFoundException extends RException {

    /**
   * 
   */
    private static final long serialVersionUID = 6899263854572225396L;

    private String jobId;

    public RJobNotFoundException(String rserver, String jobId) {
        super(rserver, "Job '" + jobId + "' not found");
        this.jobId = jobId;
    }

    public String getDescription() {
        return "No job found with id: " + jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

}
