package com.intel.cedar.tasklet;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.intel.cedar.engine.model.feature.Tasklet;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;

@Embeddable
class TaskletID implements Serializable, Comparable<TaskletID> {
    private static final long serialVersionUID = 1620796542433099258L;
    String id;
    String featureId;

    public TaskletID(String id, String featureId) {
        this.id = id;
        this.featureId = featureId;
    }

    public TaskletID() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TaskletID) {
            TaskletID other = (TaskletID) obj;
            return id.equals(other.id) && featureId.equals(other.featureId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (id + featureId).hashCode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    @Override
    public int compareTo(TaskletID o) {
        int c = this.featureId.compareTo(o.featureId);
        if (c != 0) {
            return c;
        }
        return this.id.compareTo(o.id);
    }
}

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "tasklets")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TaskletInfo implements Comparable<TaskletInfo> {
    @EmbeddedId
    private TaskletID id; // user specified tasklet id
    @Column(name = "desc")
    private String desc; // tasklet's description
    @Column(name = "contributer")
    private String contributer; // tasklet contributer
    @Column(name = "provider")
    private String provider; // java class for this tasklet
    @Column(name = "public")
    private Boolean isPublic; // public for access by other features
    @Column(name = "sharable")
    private Tasklet.Sharable sharable; // denotes that this tasklet could
                                       // execute together with others

    /**
	 * 
	 */

    public TaskletInfo() {
    }

    public static TaskletInfo load(String id, String featureId) {
        EntityWrapper<TaskletInfo> db = EntityUtil.getTaskletEntityWrapper();
        try {
            return db.load(TaskletInfo.class, new TaskletID(id, featureId));
        } finally {
            db.rollback();
        }
    }

    @Override
    public int compareTo(TaskletInfo o) {
        return this.getId().compareTo(o.getId());
    }

    public String getId() {
        return id.id;
    }

    public void setId(String id, String featureId) {
        this.id = new TaskletID(id, featureId);
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getContributer() {
        return contributer;
    }

    public void setContributer(String contributer) {
        this.contributer = contributer;
    }

    public String getFeatureId() {
        return id.featureId;
    }

    public void setFeatureId(String featureId) {
        this.id.featureId = featureId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getIsPublic() {
        if (isPublic == null)
            return false;
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Tasklet.Sharable getSharable() {
        return sharable;
    }

    public void setSharable(Tasklet.Sharable sharable) {
        this.sharable = sharable;
    }

}
