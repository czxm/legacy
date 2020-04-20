/**
 * 
 */
package com.intel.cedar.engine;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.intel.cedar.util.EntityWrapper;

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "history")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HistoryInfo implements Comparable<HistoryInfo> {

    @Id
    @Column(name = "id")
    private String id; // job id
    @Column(name = "userId")
    private Long userId; // user
    @Column(name = "submitTime")
    private Long submitTime; // submission time
    @Column(name = "endTime")
    private Long endTime; // finish time
    @Column(name = "featureId")
    private String featureId; // feature
    @Column(name = "status")
    private FeatureStatus status; // succeeded, killed or failed
    @Column(name = "desc")
    private String desc; // job desc
    @Column(name = "location")
    private String location; // job location

    /**
	 * 
	 */

    public HistoryInfo() {
    }

    @Override
    public int compareTo(HistoryInfo o) {
        return submitTime.compareTo(o.submitTime);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Long submitTime) {
        this.submitTime = submitTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public FeatureStatus getStatus() {
        return status;
    }

    public void setStatus(FeatureStatus status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public static List<HistoryInfo> listHistory() {
        EntityWrapper<HistoryInfo> db = new EntityWrapper<HistoryInfo>();
        try {
            return db.query(new HistoryInfo());
        } finally {
            db.rollback();
        }
    }
}
