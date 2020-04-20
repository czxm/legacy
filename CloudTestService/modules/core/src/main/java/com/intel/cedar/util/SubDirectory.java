package com.intel.cedar.util;

import java.io.File;

public enum SubDirectory {
    DB(BaseDirectory.HOME, "db"), FEATURES(BaseDirectory.HOME, "features"), LIBS(
            BaseDirectory.HOME, "lib"), CONFIG(BaseDirectory.HOME, "conf"), WEBAPPS(
            BaseDirectory.HOME, "webapps");

    BaseDirectory parent;
    String dir;

    SubDirectory(final BaseDirectory parent, final String dir) {
        this.parent = parent;
        this.dir = dir;
    }

    public String relative() {
        return dir;
    }

    public String getParent() {
        return parent.toString();
    }

    @Override
    public String toString() {
        return this.parent.toString() + File.separator + this.dir
                + File.separator;
    }

    public void create() {
        final File dir = new File(this.toString());
        if (dir.exists()) {
            return;
        }
        dir.mkdirs();
    }
}
