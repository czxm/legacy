package com.intel.cedar.storage;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.core.Bootstrapper;
import com.intel.cedar.scheduler.CedarTimer;
import com.intel.cedar.scheduler.CedarTimerTask;
import com.intel.cedar.storage.impl.LocalFile;
import com.intel.cedar.util.CedarConfiguration;

public class StorageBootstrapper implements Bootstrapper {
    private static Logger LOG = LoggerFactory
            .getLogger(StorageBootstrapper.class);
    private ExecutorService exec;

    public StorageBootstrapper() {
    }

    @Override
    public void start() {
        LOG.info("starting cedar storage");
        exec = Executors.newFixedThreadPool(1);
        exec.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    IStorage storage = StorageFactory.getInstance()
                            .getStorage();
                    storage.create();
                } catch (Exception e) {
                    System.exit(1);
                }
            }
        });
        
        CedarTimer.getInstance().scheduleTask(30,
                new CedarTimerTask("Storage Cleaner") {
                    @Override
                    public void run() {
                        IFolder root = StorageFactory.getInstance().getStorage().getRoot();
                        IFolder uploadRoot = root.getFolder("upload");
                        if(!uploadRoot.exist())
                            return;
                        long time = System.currentTimeMillis();
                        long expire = CedarConfiguration.getInstance().getHistoryExpire();
                        try {
                            for (IStorage f : uploadRoot.list()) {
                                if(f instanceof LocalFile){
                                    File lf = ((LocalFile)f).toFile();                                    
                                    if (time - lf.lastModified() > expire * 1000) {
                                        f.delete();
                                    }   
                                }
                            }
                        } finally {
                        }
                    }
            });        
    }

    @Override
    public void stop() {
    }
}
