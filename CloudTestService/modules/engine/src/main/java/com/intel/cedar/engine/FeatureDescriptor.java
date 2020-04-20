package com.intel.cedar.engine;

public class FeatureDescriptor {
    private String id; // auto generated feature id
    private String name; // feature name from MANIFEST
    private String contributer; // feature contributer from MANIFEST
    private String version; // feature version from MANIFEST
    private String hint; // feature hint from MANIFEST
    private boolean enabled; // feature is enabled?

    // TODO: add icon
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
}
