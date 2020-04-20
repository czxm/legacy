/**
 * 
 */
package com.intel.cedar.feature;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "features")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FeatureInfo implements Comparable<FeatureInfo> {

    @Id
    @Column(name = "id")
    private String id; // auto generated feature id
    @Column(name = "name")
    private String name; // feature name from MANIFEST
    @Column(name = "short_name")
    private String short_name; // feature short-name
    @Column(name = "context_path")
    private String contextPath; // feature context path to locate relative
                                // resources
    @Column(name = "contributer")
    private String contributer; // feature contributer from MANIFEST
    @Column(name = "version")
    private String version; // feature version from MANIFEST
    @Column(name = "hint")
    private String hint; // feature hint from MANIFEST
    @Column(name = "enabled_icon")
    private String enIcon; // first icon in MANIFEST
    @Column(name = "disabled_icon")
    private String disIcon; // second icon in MANIFEST
    @Column(name = "jar")
    private String jar; // feature jar
    @Column(name = "descriptor")
    private String descriptor; // feature XML descriptor
    @Column(name = "enabled")
    private Boolean enabled; // disabled feature?
    @Column(name = "dependsLib")
    private String dependsLib;
    @Column(name = "lastModified")
    private Long lastModified; // last modified time of the feature.xml

    public static FeatureInfo load(String id) {
        EntityWrapper<FeatureInfo> db = EntityUtil.getFeatureEntityWrapper();
        try {
            return db.load(FeatureInfo.class, id);
        } finally {
            db.rollback();
        }
    }

    public FeatureInfo() {
    }

    @Override
    public int compareTo(FeatureInfo o) {
        return this.getId().compareTo(o.getId());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShortName(String short_name) {
        this.short_name = short_name;
    }

    public String getShortName() {
        return this.short_name;
    }

    public String getContributer() {
        return contributer;
    }

    public void setContributer(String contributer) {
        this.contributer = contributer;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEnIcon() {
        return enIcon;
    }

    public void setEnIcon(String enIcon) {
        this.enIcon = enIcon;
    }

    public String getDisIcon() {
        return disIcon;
    }

    public void setDisIcon(String disIcon) {
        this.disIcon = disIcon;
    }

    public String getJar() {
        return jar;
    }

    public void setJar(String jar) {
        this.jar = jar;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setDependsLib(String dependsLib) {
        this.dependsLib = dependsLib;
    }

    public List<String> getDependsLibs() {
        List<String> libs = new ArrayList<String>();
        if (dependsLib == null) {
            return libs;
        }

        for (String lib : dependsLib.split("\\s")) {
            libs.add(lib);
        }

        return libs;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public synchronized void saveChanges() {
        EntityWrapper<FeatureInfo> db = EntityUtil.getFeatureEntityWrapper();
        try {
            FeatureInfo change = db.load(FeatureInfo.class, id);
            if (change == null)
                return;
            change.setContextPath(getContextPath());
            change.setContributer(getContributer());
            change.setDependsLib(this.dependsLib);
            change.setDescriptor(descriptor);
            change.setDisIcon(disIcon);
            change.setEnabled(enabled);
            change.setEnIcon(enIcon);
            change.setHint(hint);
            change.setJar(jar);
            change.setLastModified(lastModified);
            change.setName(name);
            change.setShortName(short_name);
            change.setVersion(version);
            db.merge(change);
            db.commit();
        } catch (Exception e) {
            db.rollback();
        }
    }
}
