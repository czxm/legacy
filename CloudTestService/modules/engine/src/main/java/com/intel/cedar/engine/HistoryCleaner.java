package com.intel.cedar.engine;

import com.intel.cedar.scheduler.CedarTimerTask;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.StorageFactory;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.cedar.util.EntityWrapper;

public class HistoryCleaner extends CedarTimerTask {

    protected HistoryCleaner() {
        super("History Cleaner");
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        long expire = CedarConfiguration.getInstance().getHistoryExpire();
        EntityWrapper<HistoryInfo> db = new EntityWrapper<HistoryInfo>();
        try {
            for (HistoryInfo h : db.query(new HistoryInfo())) {
                if (time - h.getEndTime() > expire * 1000) {
                    IFolder root = StorageFactory.getInstance().getStorage()
                            .getRoot();
                    IFolder jobFolder = root.getFolder(h.getId());
                    jobFolder.delete();
                    db.delete(h);
                }
            }
        } finally {
            db.commit();
        }
    }
}
