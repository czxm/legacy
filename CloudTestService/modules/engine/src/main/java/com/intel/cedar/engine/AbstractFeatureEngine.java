package com.intel.cedar.engine;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.intel.cedar.core.CedarException;
import com.intel.cedar.engine.model.DataModelException;
import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.feature.impl.FeatureDeploy;
import com.intel.cedar.feature.impl.FeaturePool;
import com.intel.cedar.feature.impl.FeatureUnDeploy;
import com.intel.cedar.feature.util.FeatureUtil;
import com.intel.cedar.scheduler.CedarScheduler;
import com.intel.cedar.util.BaseDirectory;
import com.intel.cedar.util.EntityWrapper;

public abstract class AbstractFeatureEngine implements IEngine {
    private static Logger LOG = LoggerFactory
            .getLogger(AbstractFeatureEngine.class);

    protected FeaturePool pool = FeaturePool.getInstance();

    public AbstractFeatureEngine() {
        for (FeatureInfo f : FeatureUtil.listFeatures(null)) {
            File featureDir = new File(BaseDirectory.HOME.toString()
                    + f.getContextPath());
            boolean isValid = true;
            if (!featureDir.isDirectory()) {
                isValid = false;
            } else {
                File featureXML = new File(featureDir, f.getDescriptor());
                if (!featureXML.isFile() || !featureXML.canRead()) {
                    isValid = false;
                }
            }
            if (!isValid) {
                try {
                    LOG
                            .info(
                                    "undeploying feature {} version {} due to invalid descriptor",
                                    new Object[] { f.getName(), f.getVersion() });
                    new FeatureUnDeploy().unDeploy(f.getId(), false);
                } catch (Exception e) {
                    LOG.info("failed to undeploy feature {} version {}: {}",
                            new Object[] { f.getName(), f.getVersion(),
                                    e.getMessage() });
                }
            }
        }
    }

    @Override
    public void deployFeature(String path) throws CedarException {
        FeatureDeploy featureDeploy = new FeatureDeploy();
        try {
            featureDeploy.deploy(path);
            CedarScheduler.getInstance().scheduleFeature(
                    featureDeploy.getManiFest().getFeatureId());
        } catch (Exception e) {
            throw new CedarException(e.getMessage(), e);
        }
    }

    @Override
    public List<FeatureDescriptor> listFeatures(List<String> features) {
        List<FeatureDescriptor> result = Lists.newArrayList();
        EntityWrapper<FeatureInfo> db = new EntityWrapper<FeatureInfo>();
        if (features == null || features.size() == 0) {
            for (FeatureInfo f : db.query(new FeatureInfo())) {
                FeatureDescriptor desc = new FeatureDescriptor();
                desc.setContributer(f.getContributer());
                desc.setHint(f.getHint());
                desc.setId(f.getId());
                desc.setName(f.getName());
                desc.setVersion(f.getVersion());
                desc.setEnabled(f.isEnabled());
                result.add(desc);
            }
        } else {
            for (FeatureInfo f : db.query(new FeatureInfo())) {
                for (String id : features) {
                    if (id.equals(f.getId())) {
                        FeatureDescriptor desc = new FeatureDescriptor();
                        desc.setContributer(f.getContributer());
                        desc.setHint(f.getHint());
                        desc.setId(f.getId());
                        desc.setName(f.getName());
                        desc.setVersion(f.getVersion());
                        desc.setEnabled(f.isEnabled());
                        result.add(desc);
                    }
                }
            }
        }
        db.rollback();
        return result;
    }

    @Override
    public Feature loadFeature(String featureId) throws CedarException {
        try {
            return pool.getFeature(featureId);
        } catch (DataModelException e) {
            throw new CedarException(e.getMessage());
        }
    }

    @Override
    public void removeFeature(String featureId) throws CedarException {
        pool.unRegister(featureId);
        FeatureUnDeploy featureUnDeploy = new FeatureUnDeploy();
        try {
            featureUnDeploy.unDeploy(featureId);
            CedarScheduler.getInstance().stopScheduledFeature(featureId);
        } catch (Exception e) {
            throw new CedarException(e.getMessage(), e);
        }
    }

    @Override
    public void updateFeature(Feature model) throws CedarException {
        // TODO Auto-generated method stub

    }
}
