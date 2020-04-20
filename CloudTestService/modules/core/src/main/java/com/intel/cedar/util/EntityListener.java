package com.intel.cedar.util;

public interface EntityListener<TYPE> {
    public void entityAdded(TYPE e);

    public void entityDeleted(TYPE e);

    public void entityUpdated(TYPE e);
}
