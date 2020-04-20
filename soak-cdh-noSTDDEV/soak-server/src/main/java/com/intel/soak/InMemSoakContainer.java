package com.intel.soak;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.intel.soak.master.LocalSoakMaster;
import com.intel.soak.utils.LoadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import akka.actor.ActorRef;

import com.intel.soak.config.SoakConfig;
import com.intel.soak.master.AbstractAppMaster;
import com.intel.soak.master.SimpleSoakMaster;
import com.intel.soak.model.LoadConfig;
import com.intel.soak.utils.SpringBeanFactoryManager;

/**
 * Created with IntelliJ IDEA.
 * User: xzhan27
 * Date: 12/4/13
 * Time: 1:12 PM
 */
public class InMemSoakContainer implements SoakContainer {
    private static Logger LOG = LoggerFactory.getLogger(InMemSoakContainer.class);
    
    private ConcurrentHashMap<String, Object> soakList;
    private ExecutorService executor;
    private SoakConfig soakConfig;
    private ActorRef appMaster;

    public void setAppMaster(ActorRef appMaster){
        this.appMaster = appMaster;
    }

    public InMemSoakContainer(){
        soakList = new ConcurrentHashMap<String, Object>();
        executor = Executors.newCachedThreadPool();
    }

    public void setSoakConfig(SoakConfig config){
        this.soakConfig = config;
    }

    @Override
    public String submit(final LoadConfig config) throws SoakException {
        if(!LoadUtils.validateLoadConfig(config)){
            throw new SoakException("Invalid load config");
        }
        String name = config.getName();
        try{
            String masterBean = soakConfig.getConfig(SoakConfig.ConfigKey.SoakMaster);
            ApplicationContext appCxt = SpringBeanFactoryManager.getSystemAppCxt();
            final Object bean = appCxt.getBean(masterBean, soakConfig.isLocalMode() ?
                    LocalSoakMaster.class : SimpleSoakMaster.class);

            if(!soakConfig.isLocalMode()){
                ((AbstractAppMaster)bean).setTarget(appMaster);
            }

            soakList.put(name, bean);
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    ((SoakMaster)bean).run(config);
                    soakList.remove(config.getName());
                }
            });
        }
        catch(Exception e){
            throw new SoakException(e.getMessage());
        }
        LOG.info("Soak Job Submitted: " + name);
        return name;
    }

    @Override
    public Set<String> list() {
        return soakList.keySet();
    }

    @Override
    public <T>T retrieve(String id) {
        return (T)soakList.get(id);
    }

    @Override
    public void remove(String id) {
        soakList.remove(id);
    }
}
