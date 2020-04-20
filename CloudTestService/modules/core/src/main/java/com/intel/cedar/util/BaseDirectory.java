package com.intel.cedar.util;

import java.io.File;

public enum BaseDirectory {
    HOME("cedar.home");
    private String key;

    BaseDirectory(final String key) {
        this.key = key;
    }

    public boolean check() {
        if (System.getProperty(this.key) == null) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return System.getProperty(this.key) + File.separator;
    }

    public void create() {
        final File dir = new File(this.toString());
        if (dir.exists()) {
            return;
        }
        dir.mkdirs();
    }
}
