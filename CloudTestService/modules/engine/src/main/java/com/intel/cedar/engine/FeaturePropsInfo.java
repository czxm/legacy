/**
 * 
 */
package com.intel.cedar.engine;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "feature_props")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FeaturePropsInfo implements Comparable<FeaturePropsInfo> {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id = -1l;
    @Column(name = "name")
    private String featureName; // feature's name
    @Column(name = "version")
    private String featureVersion; // feature's version
    @Column(name = "key")
    private String key; // property key
    @Column(name = "value")
    private String value; // property value

    public FeaturePropsInfo() {
    }

    @Override
    public int compareTo(FeaturePropsInfo o) {
        return this.getId().compareTo(o.getId());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureVersion() {
        return featureVersion;
    }

    public void setFeatureVersion(String featureVersion) {
        this.featureVersion = featureVersion;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
