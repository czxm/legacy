package com.intel.cedar.engine.model.feature;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;

public class Tasklet extends DataModel {

    public enum Sharable {
        none, queued, full;

        public static Sharable fromString(String v) {
            if (v == null || v.equals(""))
                return queued;
            try {
                return valueOf(v);
            } catch (Exception e) {
                return null;
            }
        }
    }

    private Sharable sharable;
    private boolean isPublic;
    private String id;
    private String desc;
    private String contributer;
    private String provider;

    public Tasklet(IDataModelDocument document) {
        super(document);
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean getIsPublic() {
        return this.isPublic;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getID() {
        return this.id;
    }

    public void setDescription(String desc) {
        this.desc = desc;
    }

    public String getDescription() {
        return this.desc;
    }

    public void setContributer(String contributer) {
        this.contributer = contributer;
    }

    public String getContributer() {
        return this.contributer;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return this.provider;
    }

    public Sharable getSharable() {
        return sharable;
    }

    public void setSharable(Sharable sharable) {
        this.sharable = sharable;
    }
}
