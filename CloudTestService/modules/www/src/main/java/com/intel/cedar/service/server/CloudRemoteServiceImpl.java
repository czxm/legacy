package com.intel.cedar.service.server;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.intel.cedar.cloud.CloudEucaException;
import com.intel.cedar.cloud.CloudException;
import com.intel.cedar.core.CedarException;
import com.intel.cedar.core.entities.CloudInfo;
import com.intel.cedar.core.entities.GatewayInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.KeyPairDescription;
import com.intel.cedar.core.entities.MachineInfo;
import com.intel.cedar.core.entities.MachineTypeInfo;
import com.intel.cedar.core.entities.NATInfo;
import com.intel.cedar.core.entities.PhysicalNodeInfo;
import com.intel.cedar.core.entities.VolumeInfo;
import com.intel.cedar.engine.EngineFactory;
import com.intel.cedar.engine.FeatureJobInfo;
import com.intel.cedar.engine.FeatureJobRequest;
import com.intel.cedar.engine.HistoryInfo;
import com.intel.cedar.engine.IEngine;
import com.intel.cedar.engine.TaskRunnerInfo;
import com.intel.cedar.engine.TaskRunnerInfo.AgentInfo;
import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.feature.impl.FeatureLoader;
import com.intel.cedar.feature.util.FeatureUtil;
import com.intel.cedar.feature.util.GitClient;
import com.intel.cedar.feature.util.SCMChangeItem;
import com.intel.cedar.feature.util.SCMChangeSet;
import com.intel.cedar.feature.util.SVNHistory;
import com.intel.cedar.service.client.CloudRemoteService;
import com.intel.cedar.service.client.Constants;
import com.intel.cedar.service.client.exception.CedarUIException;
import com.intel.cedar.service.client.exception.ExceptionSeverity;
import com.intel.cedar.service.client.feature.model.CedarSCMLogEntry;
import com.intel.cedar.service.client.feature.model.CedarScmLoadConfig;
import com.intel.cedar.service.client.feature.model.CedarScmType;
import com.intel.cedar.service.client.feature.model.FeatureBean;
import com.intel.cedar.service.client.feature.model.FeatureInfoBean;
import com.intel.cedar.service.client.feature.model.FeatureJobInfoBean;
import com.intel.cedar.service.client.feature.model.FeatureJobRequestBean;
import com.intel.cedar.service.client.feature.model.HistoryInfoBean;
import com.intel.cedar.service.client.feature.model.MachineFeature;
import com.intel.cedar.service.client.feature.model.ProgressInfoBean;
import com.intel.cedar.service.client.feature.model.TaskletInfoBean;
import com.intel.cedar.service.client.feature.model.TestFeature;
import com.intel.cedar.service.client.feature.model.VarValue;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.service.client.feature.model.ui.FeatureModel;
import com.intel.cedar.service.client.model.CedarOperation;
import com.intel.cedar.service.client.model.CloudInfoBean;
import com.intel.cedar.service.client.model.GatewayInfoBean;
import com.intel.cedar.service.client.model.InstanceInfoBean;
import com.intel.cedar.service.client.model.KeyPairBean;
import com.intel.cedar.service.client.model.MachineInfoBean;
import com.intel.cedar.service.client.model.MachineTypeInfoBean;
import com.intel.cedar.service.client.model.NATInfoBean;
import com.intel.cedar.service.client.model.PhysicalNodeInfoBean;
import com.intel.cedar.service.client.model.ServerTime;
import com.intel.cedar.service.client.model.UserInfoBean;
import com.intel.cedar.service.client.model.VolumeInfoBean;
import com.intel.cedar.service.client.view.PropertyPair;
import com.intel.cedar.service.util.Util;
import com.intel.cedar.user.SessionInfo;
import com.intel.cedar.user.UserInfo;
import com.intel.cedar.user.util.UserUtil;
import com.intel.cedar.util.BaseDirectory;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.cedar.util.CloudUtil;
import com.intel.cedar.util.DatabaseUtil;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;
import com.intel.cedar.util.Hashes;
import com.intel.cedar.util.ResultCode;
import com.intel.cedar.util.Utils;

public class CloudRemoteServiceImpl extends RemoteServiceServlet implements
        CloudRemoteService {
    public final static Logger LOG = LoggerFactory
            .getLogger(CloudRemoteServiceImpl.class);

    /**
	 * 
	 */
    private static final long serialVersionUID = -4408388732281119014L;

    private static Logger logger = LoggerFactory
            .getLogger(CloudRemoteServiceImpl.class);

    public BaseListLoadResult<MachineInfoBean> retrieveImageList(
            ArrayList<String> imageIds) throws CloudEucaException {
        // TODO Auto-generated method stub
        ArrayList<MachineInfoBean> res = new ArrayList<MachineInfoBean>();
        List<MachineInfo> infos = EntityUtil.listMachines(null);
        Collections.sort(infos);
        boolean enable = CedarConfiguration.getInstance().getEnableCloudService();
        for (MachineInfo info : infos) {
            if (imageIds != null && imageIds.size() != 0 && !imageIds.contains(info.getImageId())) {
                continue;
            }
            MachineInfoBean bean = Util.convertMachineInfo(info);
            bean.setEnabled(enable && info.getEnabled() && info.getCloudInfo().getEnabled());
            res.add(bean);
        }

        return new BaseListLoadResult<MachineInfoBean>(res);
    }
    
    public BaseListLoadResult<PropertyPair> retrieveImageProperties(Long machineId) throws Exception{
		MachineInfo machine = MachineInfo.load(machineId);
    	Properties properties = machine.getProperties();
    	List<PropertyPair> pairs = new ArrayList<PropertyPair>();
    	Set<String> properKeys = properties.stringPropertyNames();
    	for(String key: properKeys){
    		pairs.add(new PropertyPair(key, properties.get(key)));
    	}
    	return new BaseListLoadResult<PropertyPair>(pairs);
    }
    
    public Boolean addProperties(List<PropertyPair> properPairs, Long machineId) throws Exception{
    	try{
	    	EntityWrapper<MachineInfo> db = EntityUtil.getMachineEntityWrapper();
			MachineInfo machineInfo = db.load(MachineInfo.class, machineId);
			Properties properties = new Properties();
		  	  for(PropertyPair pair : properPairs){
		  			  String key = pair.getKey(); 
		  			  String value = pair.getValue();
		  			  properties.put(key, value);
		  	  }
			machineInfo.setProperties(properties); 
			db.merge(machineInfo);
			db.commit();
			return true;
    	}
    	catch(Exception e){
    		throw new CedarUIException(e.getMessage(), "No trace");
		}
	}
    
    public List<String> retrieveCapabilities(Long machineId){
    	MachineInfo machineInfo = MachineInfo.load(machineId);
    	List<String> capas = machineInfo.getCapabilities();
    	return capas;
    }
    
    public Boolean addCapabilities(List<FeatureBean> featureBeans, Long machineId) throws Exception{
    	try{
	    	EntityWrapper<MachineInfo> db = EntityUtil.getMachineEntityWrapper();
			MachineInfo machineInfo = db.load(MachineInfo.class, machineId);
			List<String> capa = new ArrayList<String>();
			for(FeatureBean featureBean : featureBeans){
				capa.add(featureBean.getName());
			}
			machineInfo.setCapabilities(capa);
			db.merge(machineInfo);
			db.commit();
			return true;
    	}
    	catch(Exception e){
    		throw new CedarUIException(e.getMessage(), "No trace");
		}
    }

    public List<String> retrievePhysicalNodeCapabilities(Long nodeId){
        PhysicalNodeInfo machineInfo = PhysicalNodeInfo.load(nodeId);
        List<String> capas = machineInfo.getCapabilities();
        return capas;
    }

    @Override
    public BaseListLoadResult<PropertyPair> retrievePhysicalNodeProperties(
            Long nodeId) {
        PhysicalNodeInfo machine = PhysicalNodeInfo.load(nodeId);
        Properties properties = machine.getProperties();
        List<PropertyPair> pairs = new ArrayList<PropertyPair>();
        Set<String> properKeys = properties.stringPropertyNames();
        for(String key: properKeys){
            pairs.add(new PropertyPair(key, properties.get(key)));
        }
        return new BaseListLoadResult<PropertyPair>(pairs);
    }

    @Override
    public boolean addPhysicalNodeCapabilities(List<FeatureBean> featureBeans,
            Long nodeId) throws Exception {
        try{
            EntityWrapper<PhysicalNodeInfo> db = EntityUtil.getPhysicalNodeEntityWrapper();
            PhysicalNodeInfo machineInfo = db.load(PhysicalNodeInfo.class, nodeId);
            List<String> capa = new ArrayList<String>();
            for(FeatureBean featureBean : featureBeans){
                capa.add(featureBean.getName());
            }
            machineInfo.setCapabilities(capa);
            db.merge(machineInfo);
            db.commit();
            return true;
        }
        catch(Exception e){
            throw new CedarUIException(e.getMessage(), "No trace");
        }
    }

    @Override
    public boolean addPhysicalNodeProperties(List<PropertyPair> properPairs,
            Long nodeId) throws Exception{
        try{
            EntityWrapper<PhysicalNodeInfo> db = EntityUtil.getPhysicalNodeEntityWrapper();
            PhysicalNodeInfo nodeInfo = db.load(PhysicalNodeInfo.class, nodeId);
            Properties properties = new Properties();
              for(PropertyPair pair : properPairs){
                      String key = pair.getKey(); 
                      String value = pair.getValue();
                      properties.put(key, value);
              }
            nodeInfo.setProperties(properties); 
            db.merge(nodeInfo);
            db.commit();
            return true;
        }
        catch(Exception e){
            throw new CedarUIException(e.getMessage(), "No trace");
        }
    }
    
    public BaseListLoadResult<MachineInfoBean> retrieveMachineList(
            ArrayList<CloudInfoBean> clouds) throws CloudEucaException {
        ArrayList<MachineInfoBean> res = new ArrayList<MachineInfoBean>();

        List<MachineInfo> machines = new ArrayList<MachineInfo>();
        if (clouds == null || clouds.size() == 0) {
            machines = EntityUtil.listMachines(null);
        } else {
            for (CloudInfoBean cloud : clouds) {
                CloudInfo info = new CloudInfo();
                info.setId(cloud.getId());
                machines.addAll(info.getMachines());
            }
        }
        boolean enable = CedarConfiguration.getInstance().getEnableCloudService();
        for (MachineInfo info : machines) {
            MachineInfoBean bean = Util.convertMachineInfo(info);
            bean.setEnabled(enable && info.getEnabled() && info.getCloudInfo().getEnabled());
            res.add(bean);
        }
        return new BaseListLoadResult<MachineInfoBean>(res);
    }

    public BaseListLoadResult<MachineTypeInfoBean> retrieveMachineTypeList(
            ArrayList<CloudInfoBean> clouds) throws CloudEucaException {
        ArrayList<MachineTypeInfoBean> res = new ArrayList<MachineTypeInfoBean>();

        List<MachineTypeInfo> types = new ArrayList<MachineTypeInfo>();
        if (clouds == null || clouds.size() == 0) {
            types = EntityUtil.listMachineTypes(null);
            Collections.sort(types);
        } else {
            for (CloudInfoBean info : clouds) {
                CloudInfo cloud = new CloudInfo();
                cloud.setId(info.getId());
                types.addAll(cloud.getMachineTypes(true));
            }
        }

        for (MachineTypeInfo type : types) {
            res.add(Util.convertMachineTypeInfo(type));
        }
        return new BaseListLoadResult<MachineTypeInfoBean>(res);
    }

    public BaseListLoadResult<CloudInfoBean> retrieveCloudList(
            ArrayList<String> clouds) throws CloudEucaException {
        ArrayList<CloudInfoBean> res = new ArrayList<CloudInfoBean>();
        for (CloudInfo info : EntityUtil.listClouds()) {
            res.add(Util.convertCloudInfoEC2(info));
        }

        return new BaseListLoadResult<CloudInfoBean>(res);
    }

    public BaseListLoadResult<UserInfoBean> retrieveUserList(
            ArrayList<String> users) throws CloudEucaException {
        List<UserInfo> userList = UserUtil.listUsers();
        Collections.sort(userList);
        ArrayList<UserInfoBean> res = new ArrayList<UserInfoBean>();
        for (UserInfo info : userList) {
            res.add(Util.convertUserInfo(info));
        }

        return new BaseListLoadResult<UserInfoBean>(res);
    }

    public BaseListLoadResult<KeyPairBean> retrieveKeyPairList(
            ArrayList<String> keyIds) throws CloudEucaException {
        ArrayList<KeyPairBean> res = new ArrayList<KeyPairBean>();
        for (KeyPairDescription des : EntityUtil.listKeyPairs(null)) {
            if (keyIds != null && keyIds.size() != 0
                    && !keyIds.contains(des.getKeyName()))
                continue;
            res.add(Util.convertKeyPairDescription(des));
        }

        return new BaseListLoadResult<KeyPairBean>(res);
    }

    public PagingLoadResult<InstanceInfoBean> retrieveInstanceList(
            final PagingLoadConfig config) throws CloudEucaException {
        ArrayList<InstanceInfoBean> instances = new ArrayList<InstanceInfoBean>();
        for (InstanceInfo instance : EntityUtil.listInstances(null)) {
            instances.add(Util.convertInstanceInfo(instance));
        }

        if (config.getSortInfo().getSortField() != null) {
            final String sortField = config.getSortInfo().getSortField();
            if (sortField != null) {
                Collections.sort(instances, config.getSortInfo().getSortDir()
                        .comparator(new Comparator<InstanceInfoBean>() {
                            public int compare(InstanceInfoBean ins1,
                                    InstanceInfoBean ins2) {
                                if (sortField.equals("Instance")) {
                                    return ins1.getInstanceId().compareTo(
                                            ins2.getInstanceId());
                                }

                                return 0;
                            }
                        }));
            }
        }

        ArrayList<InstanceInfoBean> sublist = new ArrayList<InstanceInfoBean>();
        int start = config.getOffset();
        int limit = instances.size();
        if (config.getLimit() > 0) {
            limit = Math.min(start + config.getLimit(), limit);
        }
        for (int i = config.getOffset(); i < limit; i++) {
            sublist.add(instances.get(i));
        }
        return new BasePagingLoadResult<InstanceInfoBean>(sublist, config
                .getOffset(), instances.size());
    }

    public BaseListLoadResult<InstanceInfoBean> retrieveInstanceList(
            ArrayList<String> instanceIds) throws Exception {
        UserInfo userInfo = Util.retrieveUserSession(getThreadLocalRequest());
        ArrayList<InstanceInfoBean> res = new ArrayList<InstanceInfoBean>();
        List<InstanceInfo> infos = EntityUtil.listInstances(null);
        Collections.sort(infos);
        for (InstanceInfo ins : infos) {
            if (userInfo.getId().equals(ins.getUserId()) || userInfo.getAdmin())
                res.add(Util.convertInstanceInfo(ins));
        }

        return new BaseListLoadResult<InstanceInfoBean>(res);
    }

    public BaseListLoadResult<PhysicalNodeInfoBean> retrievePhysicalNodeList(
            ArrayList<String> nodes) throws Exception {
        ArrayList<PhysicalNodeInfoBean> res = new ArrayList<PhysicalNodeInfoBean>();
        List<PhysicalNodeInfo> infos = EntityUtil.listPhysicalNodes();
        Collections.sort(infos);
        for (PhysicalNodeInfo info : infos) {
            res.add(Util.convertPhysicalNodeInfo(info));
        }
        return new BaseListLoadResult<PhysicalNodeInfoBean>(res);
    }

    public PagingLoadResult<KeyPairBean> retrieveKeyPairList(
            final PagingLoadConfig config) throws CloudEucaException {
        ArrayList<KeyPairBean> res = new ArrayList<KeyPairBean>();
        for (KeyPairDescription des : EntityUtil.listKeyPairs(null)) {
            res.add(Util.convertKeyPairDescription(des));
        }

        if (config.getSortInfo().getSortField() != null) {
            final String sortField = config.getSortInfo().getSortField();
            if (sortField != null) {
                Collections.sort(res, config.getSortInfo().getSortDir()
                        .comparator(new Comparator<KeyPairBean>() {
                            public int compare(KeyPairBean key1,
                                    KeyPairBean key2) {
                                if (sortField.equals("KeyName")) {
                                    return key1.getKeyName().compareTo(
                                            key2.getKeyName());
                                }

                                return 0;
                            }
                        }));
            }
        }

        ArrayList<KeyPairBean> sublist = new ArrayList<KeyPairBean>();
        int start = config.getOffset();
        int limit = res.size();
        if (config.getLimit() > 0) {
            limit = Math.min(start + config.getLimit(), limit);
        }
        for (int i = config.getOffset(); i < limit; i++) {
            sublist.add(res.get(i));
        }
        return new BasePagingLoadResult<KeyPairBean>(sublist, config
                .getOffset(), res.size());
    }

    public PagingLoadResult<MachineInfoBean> retrieveImageList(
            final PagingLoadConfig config) throws CloudEucaException {
        ArrayList<MachineInfoBean> res = new ArrayList<MachineInfoBean>();
        for (MachineInfo machineInfo : EntityUtil.listMachines(null)) {
            res.add(Util.convertMachineInfo(machineInfo));
        }

        if (config.getSortInfo().getSortField() != null) {
            final String sortField = config.getSortInfo().getSortField();
            if (sortField != null) {
                Collections.sort(res, config.getSortInfo().getSortDir()
                        .comparator(new Comparator<MachineInfoBean>() {
                            public int compare(MachineInfoBean img1,
                                    MachineInfoBean img2) {
                                if (sortField.equals("ImageId")) {
                                    return img1.getImageId().compareTo(
                                            img2.getImageId());
                                }

                                return 0;
                            }
                        }));
            }
        }

        ArrayList<MachineInfoBean> sublist = new ArrayList<MachineInfoBean>();
        int start = config.getOffset();
        int limit = res.size();
        if (config.getLimit() > 0) {
            limit = Math.min(start + config.getLimit(), limit);
        }
        for (int i = config.getOffset(); i < limit; i++) {
            sublist.add(res.get(i));
        }
        return new BasePagingLoadResult<MachineInfoBean>(sublist, config
                .getOffset(), res.size());
    }

    @Override
    public PagingLoadResult<MachineTypeInfoBean> retrieveMachineTypeList(
            PagingLoadConfig config) throws Exception {
        // TODO Auto-generated method stub
        ArrayList<MachineTypeInfoBean> res = new ArrayList<MachineTypeInfoBean>();
        for (MachineTypeInfo machineTypeInfo : EntityUtil
                .listMachineTypes(null)) {
            res.add(Util.convertMachineTypeInfo(machineTypeInfo));
        }

        if (config.getSortInfo().getSortField() != null) {
            final String sortField = config.getSortInfo().getSortField();
            if (sortField != null) {
                Collections.sort(res, config.getSortInfo().getSortDir()
                        .comparator(new Comparator<MachineTypeInfoBean>() {
                            public int compare(MachineTypeInfoBean type1,
                                    MachineTypeInfoBean type2) {
                                if (sortField.equals("Id")) {
                                    return type1.getId().compareTo(
                                            type2.getId());
                                }

                                return 0;
                            }
                        }));
            }
        }

        ArrayList<MachineTypeInfoBean> sublist = new ArrayList<MachineTypeInfoBean>();
        int start = config.getOffset();
        int limit = res.size();
        if (config.getLimit() > 0) {
            limit = Math.min(start + config.getLimit(), limit);
        }
        for (int i = config.getOffset(); i < limit; i++) {
            sublist.add(res.get(i));
        }
        return new BasePagingLoadResult<MachineTypeInfoBean>(sublist, config
                .getOffset(), res.size());
    }

    public BaseListLoadResult<MachineTypeInfoBean> retrieveMachineTypeList(
            ArrayList<String> typeIds, Boolean flag) throws Exception {
        ArrayList<MachineTypeInfoBean> res = new ArrayList<MachineTypeInfoBean>();
        List<MachineTypeInfo> types = EntityUtil.listMachineTypes(null);
        Collections.sort(types);
        for (MachineTypeInfo machineTypeInfo : types) {
            if (machineTypeInfo != null && typeIds.size() != 0
                    && !typeIds.contains(machineTypeInfo.getType()))
                continue;

            res.add(Util.convertMachineTypeInfo(machineTypeInfo));
        }

        return new BaseListLoadResult<MachineTypeInfoBean>(res);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            // TODO: only required for GWT dev mod now
            DatabaseUtil.enableAccess();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public Boolean applyInstance(CloudInfoBean cloud, MachineInfoBean machine,
            MachineTypeInfoBean type, KeyPairBean key, int num,
            List<NATInfoBean> nats) throws Exception {
        // consider that the cloud is de-registered while conducting remote
        // invocation
        // cloudinfo, machineInfo and machineType info are not available from db
        // any more
        // all will return null if query from DB;
        UserInfo userInfo = Util.retrieveUserSession(getThreadLocalRequest());
        CloudInfo cinfo = Util.convertCloudInfoBean(cloud);
        MachineInfo minfo = Util.convertMachineInfoBean(machine);
        MachineTypeInfo tinfo = Util.convertMachineTypeInfoBean(type);
        ArrayList<NATInfo> natInfos = new ArrayList<NATInfo>();
        for (NATInfoBean bean : nats) {
            natInfos.add(Util.convertNATInfoBean(bean));
        }
        try {
            return CloudUtil.startInstance(cinfo, minfo, tinfo, num, natInfos,
                    userInfo.getId());
        } catch (CedarException e) {
            throw new CedarUIException(CedarOperation.LAUNCH_INSTANCE,
                    ExceptionSeverity.ERROR, e.getMessage(),
                    e.getCause() != null ? Util.getTraces(e.getCause()) : null);
        }
    }

    @Override
    public Boolean rebootInstance(InstanceInfoBean instance) throws Exception {
        CloudUtil.rebootInstance(Util.convertInstanceInfoBean(instance));
        return true;
    }

    @Override
    public List<InstanceInfoBean> terminateInstance(
            List<InstanceInfoBean> instances) throws Exception {
        for (InstanceInfoBean bean : instances) {
            CloudUtil.terminateInstance(Util.convertInstanceInfoBean(bean));
        }
        return instances;
    }

    @Override
    public List<CloudInfoBean> deregisterCloud(List<CloudInfoBean> clouds)
            throws Exception {
        List<CloudInfoBean> success = new ArrayList<CloudInfoBean>();
        for (CloudInfoBean bean : clouds) {
            CloudInfo cloud = Util.convertCloudInfoBean(bean);
            ResultCode result = CloudUtil.deregisterCloud(cloud);
            if (result == ResultCode.IN_USE) {
                LOG
                        .info(
                                "Cloud {} is in use. Please confirm not any instance running on it",
                                bean.getCloudName());
                throw new CedarUIException(
                        CedarOperation.DEREGISTER_CLOUD,
                        ExceptionSeverity.INFO,
                        "Cloud "
                                + bean.getCloudName()
                                + " is in use. Please confirm not any instance running.",
                        "No Trace");
            }
            if (result != ResultCode.SUCCESS) {
                throw new CedarUIException(CedarOperation.DEREGISTER_CLOUD,
                        ExceptionSeverity.ERROR, "Failed to degrgister cloud: "
                                + bean.getCloudName(), "No Trace");
            }
            success.add(bean);
        }
        return success;
    }

    public List<UserInfoBean> deleteUser(List<UserInfoBean> users)
            throws Exception {
        List<UserInfoBean> success = new ArrayList<UserInfoBean>();
        for (UserInfoBean bean : users) {
            UserInfo info = Util.convertUserInfoBean(bean);
            if (!UserUtil.deleteUser(info))
                return null;
            success.add(bean);
        }
        return success;
    }

    @Override
    public Boolean registerCloud(CloudInfoBean cloudBean,
            ArrayList<GatewayInfoBean> gateways) throws Exception {
        CloudInfo info = Util.convertCloudInfoBean(cloudBean);
        ArrayList<GatewayInfo> list = new ArrayList<GatewayInfo>();
        for (GatewayInfoBean bean : gateways) {
            list.add(Util.convertGatewayInfoBean(bean));
        }
        try {
            CloudUtil.registerCloud(info, list);
        } catch (CedarException e) {
            throw new CedarUIException(CedarOperation.REGISTER_CLOUD,
                    ExceptionSeverity.ERROR, e.getMessage(), Util.getTraces(e));
        } catch (Exception e) {

        }
        return null;
    }

    public BaseListLoadResult<GatewayInfoBean> retrieveGatewayList(
            CloudInfoBean bean) throws Exception {
        CloudInfo cloud = Util.convertCloudInfoBean(bean);
        ArrayList<GatewayInfoBean> res = new ArrayList<GatewayInfoBean>();
        for (GatewayInfo gateway : CloudUtil.getGateways(cloud)) {
            res.add(Util.convertGatewayInfo(gateway));
        }

        return new BaseListLoadResult<GatewayInfoBean>(res);
    }

    public FeatureModel loadFeatureUI(String featureId) throws Exception {
        try {
            return EngineFactory.getInstance().getEngine().loadFeature(
                    featureId).getFeatureModel();
        } catch (Exception e) {
            CedarUIException ce = new CedarUIException(e.getMessage(), Util
                    .getTraces(e.getCause()));
            ce.setOp(CedarOperation.LOAD_INSTANCE);
            ce.setSeverity(ExceptionSeverity.FATALERROR);
            throw ce;
        }
    }

    public ArrayList<String> loadCaseSet(MachineFeature machineFeature,
            TestFeature testFeature) {
        return new ArrayList<String>();
    }

    public BaseListLoadResult<VarValue> loadCaseSet(
            MachineFeature machineFeature, TestFeature testFeature, boolean flag) {
        ArrayList<VarValue> res = new ArrayList<VarValue>();

        return new BaseListLoadResult<VarValue>(res);
    }
    
    public Feature getFeature(String featureId) throws Exception{
        FeatureInfo feature = FeatureInfo.load(featureId);
        Feature f = new FeatureLoader().loadFeature(feature);
        return f;
    }
    
    public GitClient getGitClient(String url, String featureId) throws Exception{
        Feature f = getFeature(featureId);
        String username = null;
        String password = null;
        String privatekey = null;
        String proxy = null;
        int port = 0;
        for(Variable v : f.getVariables()){
            if(v.getName().equals("git_username")){
                username = v.getValue();
            }
            else if(v.getName().equals("git_password")){
                password = v.getValue();
            }
            else if(v.getName().equals("git_privatekey")){
                privatekey = v.getValue();
            }
            else if(v.getName().equals("git_proxyhost")){
                proxy = v.getValue();
            }
            else if(v.getName().equals("git_proxyport")){
                port = Integer.parseInt(v.getValue());
            }
        }
        FeatureInfo fi = FeatureInfo.load(featureId);
        String basedir = url.substring(url.lastIndexOf("/") + 1);
        String localrepo = BaseDirectory.HOME.toString() + File.separator + fi.getContextPath() + File.separator + basedir;
        GitClient git = null;
        if(password != null){
            git = new GitClient(url, localrepo, username, password);
        }
        else if(privatekey != null){
            git = new GitClient(url, localrepo, username, Utils.decodeBase64(privatekey));
        }
        if(git != null){
            git.setProxy(proxy, port);
        }
        return git;
    }
    
    public BaseListLoadResult<CedarSCMLogEntry> loadSVNEntries(String url,
            CedarScmLoadConfig config) throws Exception {
        List<SCMChangeSet> items = null;
        ArrayList<CedarSCMLogEntry> res = new ArrayList<CedarSCMLogEntry>();
        if (url == null || url.length() == 0)
            return new BaseListLoadResult<CedarSCMLogEntry>(res);
        Feature f = getFeature(config.getFeatureId());
        if(config.getType().equals(CedarScmType.SVN)){
            String username = null;
            String password = null;
            for(Variable v : f.getVariables()){
                if(v.getName().equals("svn_username")){
                    username = v.getValue();
                }
                else if(v.getName().equals("svn_password")){
                    password = v.getValue();
                }
            }
            SVNHistory history = new SVNHistory(url, username, password, url.startsWith("svn"));
            items = history.getLatestRevisions(config.getNumOfRev(), -1);
            if(items != null && items.size() == 0){
                history = new SVNHistory(url.substring(0, url.lastIndexOf("/")));
                items = history.getLatestRevisions(config.getNumOfRev(), -1);
            }
        }
        else{
            GitClient git = null;
            try{
                git = getGitClient(url, config.getFeatureId());
                if(git != null && git.openRepository()){
                    git.checkout(config.getBranch());
                    items = new ArrayList<SCMChangeSet>();
                    for(RevCommit c : git.getCommitHistory(config.getNumOfRev())){
                        items.add(git.getChangeSet(c));
                    }
                }
            }finally{
                if(git != null)
                    git.closeRepository();
            }
        }
        if(items != null){
            for(SCMChangeSet item : items){
                res.add(Util.convertSCMLogItem(item));
            }
        }
        return new BaseListLoadResult<CedarSCMLogEntry>(res);
    }

    public String getSingleSVNLogMessage(String url, String rev, CedarScmLoadConfig config) throws Exception{
        String res = "Not available!";
        if(config.getType().equals(CedarScmType.SVN)){
            Feature f = getFeature(config.getFeatureId());
            String username = null;
            String password = null;
            for(Variable v : f.getVariables()){
                if(v.getName().equals("svn_username")){
                    username = v.getValue();
                }
                else if(v.getName().equals("svn_password")){
                    password = v.getValue();
                }
            }
            SVNHistory history = new SVNHistory(url, username, password, url.startsWith("svn"));
            res = history.getRevisionLog(rev);
        }
        else{
            GitClient git = null;
            try{
                git = getGitClient(url, config.getFeatureId());
                if(git != null && git.openRepository()){
                    git.update();
                    git.checkout(config.getBranch());
                    RevCommit c = git.getCommitByName(rev);
                    SCMChangeSet cs = git.getChangeSet(c);
                    StringBuilder sb = new StringBuilder();
                    sb.append(cs.getLogMsg());
                    sb.append("\n\n");
                    for(SCMChangeItem i : cs.getChangeItems()){
                        sb.append(i.getAction());
                        sb.append("    ");
                        sb.append(i.getPath());
                        sb.append("\n\n");
                    }
                    res = sb.toString();
                }
            }finally{
                if(git != null)
                    git.closeRepository();
            }
            
        }
        return res;
    }
    

    @Override
    public List<String> getGitBranches(String url, CedarScmLoadConfig config)
            throws Exception {
        if(config.getType().equals(CedarScmType.GIT)){
            GitClient git = null;
            try{
                git = getGitClient(url, config.getFeatureId());
                if(git != null && git.openRepository()){
                    git.update();
                    return git.listBranches();
                }
            }finally{
                if(git != null)
                    git.closeRepository();
            }
        }
        return null;
    }

    public String getActualTime() throws Exception {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(date);
    }

    public ServerTime getSystemTime() throws Exception {
        Calendar calendar = Calendar.getInstance();
        long offset = calendar.get(Calendar.ZONE_OFFSET)
                + calendar.get(Calendar.DST_OFFSET);
        long now = System.currentTimeMillis();
        // returning UTC time to set correct time zone offset on the client side
        return new ServerTime(now, offset);
    }

    public Boolean registerUser(UserInfoBean user) throws Exception {
        UserInfo info = Util.convertUserInfoBean(user);
        return UserUtil.registerUser(info);
    }

    public UserInfoBean loginUser(UserInfoBean user) throws Exception {
        UserInfo info = Util.convertUserInfoBean(user);

        UserInfo result = UserUtil.loginUser(info);
        if (result == null)
            return null;
        // TODO: add session management functionality
        UserInfo fromCredential = Util
                .retrieveUserSession(getThreadLocalRequest());
        if (fromCredential == null
                || (fromCredential != null && fromCredential.getId() != result
                        .getId())) {
            // set new credential cookies
            String credentialCookie = Util.getCredentialCookie();
            SessionInfo sessionInfo = new SessionInfo();
            sessionInfo.setSessionid(credentialCookie);
            sessionInfo.setUserid(result.getId());
            sessionInfo.setTimestamp(System.currentTimeMillis());
            UserUtil.saveSession(sessionInfo);
            Cookie cookie = new Cookie(Constants.CTS_CREDENTIAL_COOKIE,
                    credentialCookie);
            cookie.setMaxAge(3 * 24 * 3600);
            getThreadLocalResponse().addCookie(cookie);
        }

        return Util.convertUserInfo(result);
    }

    public UserInfoBean checkCredentialCookie() throws Exception {
        HttpServletRequest httpRequest = getThreadLocalRequest();

        UserInfo userInfo = Util.retrieveUserSession(httpRequest);
        return Util.convertUserInfo(userInfo);
    }

    public Boolean deployFeature(Boolean forceUpgrade) throws Exception {
        HttpSession session = this.getThreadLocalRequest().getSession();
        String tmpFilePath = UploadServlet
                .getUploadedFeaturePath(session);
        try {
            EngineFactory.getInstance().getEngine().deployFeature(tmpFilePath);
        } catch (Exception e) {
            throw new CedarUIException(CedarOperation.DeployFeature,
                    ExceptionSeverity.ERROR, e.getMessage(),
                    e.getCause() != null ? Util.getTraces(e.getCause()) : Util
                            .getTraces(e));
        }
        return Boolean.TRUE;
    }

    @Override
    public List<FeatureBean> deleteFeatures(List<FeatureBean> features)
            throws Exception {
        List<FeatureBean> suc = new ArrayList<FeatureBean>();
        for (FeatureBean f : features) {
            try {
                EngineFactory.getInstance().getEngine()
                        .removeFeature(f.getId());
                suc.add(f);
            } catch (Exception e) {
                throw new CedarUIException(CedarOperation.UndeployFeature,
                        ExceptionSeverity.ERROR, e.getMessage(),
                        e.getCause() != null ? Util.getTraces(e.getCause())
                                : Util.getTraces(e));
            }
        }
        return suc;
    }

    @Override
    public FeatureInfoBean disableFeatures(List<String> featureIds)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<FeatureInfoBean> enableFeatures(List<String> featureIds)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BaseListLoadResult<FeatureInfoBean> retrieveFeatureList(
            List<String> featureIds) throws Exception {
        List<FeatureInfo> features = FeatureUtil.listFeatures(featureIds);
        Collections.sort(features);
        ArrayList<FeatureInfoBean> res = new ArrayList<FeatureInfoBean>();
        for (FeatureInfo feature : features) {
            res.add(Util.convertFeatureInfo(feature));
        }
        return new BaseListLoadResult<FeatureInfoBean>(res);
    }
    
    @Override
    public PagingLoadResult<FeatureInfoBean> retrieveFeatureList(
            PagingLoadConfig config) throws Exception {
        List<FeatureInfo> features = FeatureUtil.listFeatures(new ArrayList<String>());
        Collections.sort(features);
        List<FeatureInfoBean> subList = new ArrayList<FeatureInfoBean>();
        int start = config.getOffset();
        int totalLength = features.size();
        int offset = 0, limit = 0;
        if (totalLength <= start) {
            int ps = totalLength / config.getLimit();
            offset = ps * config.getLimit();
            limit = totalLength;
        } else {
            offset = start;
            limit = Math.min(start + config.getLimit(), totalLength);
        }
        for (int i = offset; i < limit; i++) {
            subList.add(Util.convertFeatureInfo(features.get(i)));
        }
        boolean enabled = CedarConfiguration.getInstance().getEnableTaskService();
        for(FeatureInfoBean b : subList){
            b.setEnabled(enabled && b.getEnabled());
        }
        return new BasePagingLoadResult<FeatureInfoBean>(subList, offset,
                totalLength);        
    }

    @Override
    public BaseListLoadResult<FeatureBean> retrieveFeatureBeanList(
            List<String> featureIds) throws Exception {
        List<FeatureInfo> features = FeatureUtil.listFeatures(featureIds);
        Collections.sort(features);
        ArrayList<FeatureBean> res = new ArrayList<FeatureBean>();
        for (FeatureInfo feature : features) {
            res.add(Util.convertFeatureInfoRe(feature));
        }
        return new BaseListLoadResult<FeatureBean>(res);
    }

    @Override
    public Boolean enablePortMappings(InstanceInfoBean bean) throws Exception {
        InstanceInfo info = CloudUtil.getInstanceInfoById(bean.getId());
        if (info == null) {
            LOG.error("Instance {} has terminated just now", bean
                    .getInstanceId());
            throw new CedarUIException(CedarOperation.ENABLE_PORTMAPPING,
                    ExceptionSeverity.ERROR, "Instance " + bean.getInstanceId()
                            + " has terminated just now", "");
        }
        return CloudUtil.enablePortMappings(info);
    }

    @Override
    public String submitTask(FeatureJobRequestBean requestBean)
            throws Exception {
        FeatureJobRequest request = Util
                .convertFeatureJobRequestBean(requestBean);
        UserInfo userInfo = Util.retrieveUserSession(getThreadLocalRequest());
        request.userId = userInfo.getId();
        return EngineFactory.getInstance().getEngine().submit(request);
    }

    @Override
    public CloudInfoBean saveCloud(CloudInfoBean bean,
            List<GatewayInfoBean> gList) throws Exception {
        CloudInfo info = Util.convertCloudInfoBean(bean);
        ArrayList<GatewayInfo> list = new ArrayList<GatewayInfo>();
        for (GatewayInfoBean gateway : gList) {
            GatewayInfo gw;
            if (gateway.getId() >= 0) {
                // already registered!
                gw = new GatewayInfo();
                gw.setId(gateway.getId());
            } else {
                gw = Util.convertGatewayInfoBean(gateway);
            }
            list.add(gw);
        }
        try {
            ResultCode rc = CloudUtil.editCloud(info, list);
            if (rc == ResultCode.IN_USE) {
                LOG
                        .info(
                                "Cloud {} is on serving. You can only edit the cloud when it's not serving",
                                info.getName());
                throw new CedarUIException(
                        CedarOperation.EDIT_CLOUD,
                        ExceptionSeverity.INFO,
                        "Cloud "
                                + info.getName()
                                + " is on serving. You can only edit the cloud when it's not serving",
                        "No Trace");
            } else {
                return bean;
            }
        } catch (CedarException e) {
            throw new CedarUIException(CedarOperation.EDIT_CLOUD,
                    ExceptionSeverity.ERROR, e.getMessage(),
                    e.getCause() != null ? Util.getTraces(e.getCause()) : Util
                            .getTraces(e));
        }
    }

    @Override
    public VolumeInfoBean attachVolume(boolean format, VolumeInfoBean v,
            InstanceInfoBean ins) throws Exception {
        VolumeInfo volumeInfo = VolumeInfo.load(v.getId());
        InstanceInfo instanceInfo = InstanceInfo.load(ins.getId());
        try {
            if (volumeInfo != null && instanceInfo != null) {
                if (format) {
                    if (CloudUtil.formatVolume(instanceInfo, volumeInfo))
                        return Util.convertVolumeInfo(volumeInfo);
                } else {
                    if (CloudUtil.attachVolume(instanceInfo, volumeInfo))
                        return Util.convertVolumeInfo(volumeInfo);
                }
            }
        } catch (CloudException e) {
            throw new CedarUIException(CedarOperation.ATTACH_VOLUME,
                    ExceptionSeverity.ERROR, e.getMessage(),
                    e.getCause() != null ? Util.getTraces(e.getCause()) : Util
                            .getTraces(e));
        }
        return null;
    }

    @Override
    public VolumeInfoBean createVolume(CloudInfoBean bean, Integer size,
            String tag) throws Exception {
        CloudInfo info = CloudUtil.getCloudById(bean.getId());
        UserInfo userInfo = Util.retrieveUserSession(getThreadLocalRequest());
        try {
            VolumeInfo vinfo = CloudUtil.createVolume(info, Hashes.generateId(
                    "vol", "V"), tag, size, userInfo.getId());
            return Util.convertVolumeInfo(vinfo);
        } catch (CloudException e) {
            throw new CedarUIException(CedarOperation.CREATE_VOLUME,
                    ExceptionSeverity.ERROR, e.getMessage(),
                    e.getCause() != null ? Util.getTraces(e.getCause()) : Util
                            .getTraces(e));
        }
    }

    @Override
    public BaseListLoadResult<VolumeInfoBean> retrieveVolumeList()
            throws Exception {
        UserInfo userInfo = Util.retrieveUserSession(getThreadLocalRequest());
        ArrayList<VolumeInfo> infos = new ArrayList<VolumeInfo>();
        for (VolumeInfo info : CloudUtil.getVolumes()) {
            if (info.isCloudVolume()
                    && (info.getUserId().equals(userInfo.getId()) || userInfo
                            .getAdmin()))
                infos.add(info);
        }
        ArrayList<VolumeInfoBean> res = new ArrayList<VolumeInfoBean>();
        Collections.sort(infos);
        for (VolumeInfo info : infos) {
            res.add(Util.convertVolumeInfo(info));
        }
        return new BaseListLoadResult<VolumeInfoBean>(res);
    }

    public String showConsoleOutput(InstanceInfoBean ins) throws Exception {
        InstanceInfo info = Util.convertInstanceInfoBean(ins);
        try {
            return CloudUtil.getConsoleOuput(info);
        } catch (CloudException e) {
            throw new CedarUIException(CedarOperation.GET_CONSOLEOUTPUT,
                    ExceptionSeverity.ERROR, e.getMessage(), Util.getTraces(e));
        }
    }

    @Override
    public List<VolumeInfoBean> detachVolumes(List<VolumeInfoBean> list)
            throws Exception {
        ArrayList<VolumeInfoBean> res = new ArrayList<VolumeInfoBean>();
        for (VolumeInfoBean v : list) {
            VolumeInfo volumeInfo = VolumeInfo.load(v.getId());
            if (volumeInfo != null && CloudUtil.detachVolume(volumeInfo)) {
                res.add(Util.convertVolumeInfo(volumeInfo));
            }
        }
        return res;
    }

    @Override
    public BaseListLoadResult<VolumeInfoBean> retrieveAttachedVolumes(
            InstanceInfoBean ins) throws Exception {
        UserInfo userInfo = Util.retrieveUserSession(getThreadLocalRequest());
        ArrayList<VolumeInfoBean> res = new ArrayList<VolumeInfoBean>();
        if (ins != null) {
            for (VolumeInfo info : CloudUtil.getVolumes()) {
                if (info.getCloudId().equals(ins.getCloudId())
                        && ins.getId().equals(info.getAttached())
                        && (userInfo.getAdmin() ? true : info.getUserId().equals(userInfo.getId()))) {
                    res.add(Util.convertVolumeInfo(info));
                }
            }
        }
        return new BaseListLoadResult<VolumeInfoBean>(res);
    }

    @Override
    public BaseListLoadResult<VolumeInfoBean> retrieveAvailableVolumes(
            InstanceInfoBean ins) throws Exception {
        UserInfo userInfo = Util.retrieveUserSession(getThreadLocalRequest());
        ArrayList<VolumeInfoBean> res = new ArrayList<VolumeInfoBean>();
        List<VolumeInfo> list = CloudUtil.getVolumes();
        if (ins != null) {
            for (VolumeInfo info : list) {
                if (info.getCloudId().equals(ins.getCloudId())
                        && info.getAttached() == null && !info.getPooled()
                        && info.isCloudVolume()
                        && (userInfo.getAdmin() ? true : info.getUserId().equals(userInfo.getId()))) {
                    // available in current cloud
                    res.add(Util.convertVolumeInfo(info));
                }
            }
        }
        return new BaseListLoadResult<VolumeInfoBean>(res);
    }

    @Override
    public String showInstanceInfo(InstanceInfoBean ins) throws Exception {
        try {
            InstanceInfo info = CloudUtil.getInstanceInfoById(ins.getId());
            List<NATInfo> nats = info.getPortMappings();
            StringBuffer sb = new StringBuffer();
            sb.append("hostname:\n");
            sb.append(info.getHostName());
            sb.append("\n");
            if(nats.size() > 0){
                sb.append("Available Port Mappings:\n");
                for (NATInfo nat : nats) {
                    if (nat.getMappedPort() == null)
                        continue;
                    GatewayInfo gw = CloudUtil.getGatewayById(nat.getGatewayId());
                    if (gw != null) {
                        sb.append(nat.getName() + ": " + gw.getHost() + ":"
                                + nat.getMappedPort() + "\n");
                    } else {
                        LOG.info("gateway is not found according to natinfo");
                    }
                }
            }
            return sb.toString();
        } catch (CedarException e) {
            throw new CedarUIException(CedarOperation.SHOW_LOGIN_INFO,
                    ExceptionSeverity.ERROR, e.getMessage(), Util.getTraces(e));
        }
    }

    @Override
    public List<VolumeInfoBean> deleteVolumes(List<VolumeInfoBean> list)
            throws Exception {
        List<VolumeInfoBean> res = new ArrayList<VolumeInfoBean>();
        try {
            for (VolumeInfoBean bean : list) {
                VolumeInfo v = CloudUtil.getVolumeById(bean.getId());
                CloudUtil.deleteVolume(v);
                res.add(Util.convertVolumeInfo(v));
            }
        } catch (CloudException e) {
            throw new CedarUIException(CedarOperation.DELETE_VOLUME,
                    ExceptionSeverity.ERROR, e.getMessage(), Util.getTraces(e));
        }
        return res;
    }

    private static Random random = new Random();
    private static int TOTAL_LENGTH = 320;
    private int ITERMS_PER_PAGE = 32;

    @Override
    public PagingLoadResult<HistoryInfoBean> retrieveHistory(
            PagingLoadConfig config) throws Exception {
        UserInfo userInfo = Util.retrieveUserSession(getThreadLocalRequest());
        EntityWrapper<HistoryInfo> db = new EntityWrapper<HistoryInfo>();
        List<HistoryInfo> infos = db.query(new HistoryInfo());
        Collections.sort(infos, new Comparator<HistoryInfo>(){
            @Override
            public int compare(HistoryInfo o1, HistoryInfo o2) {            
                return o2.getSubmitTime().compareTo(o1.getSubmitTime());
            }            
        });
        List<HistoryInfoBean> list = new ArrayList<HistoryInfoBean>();
        for(HistoryInfo h : infos){
            if(userInfo.getAdmin() ? true : userInfo.getId().equals(h.getUserId()))
                list.add(Util.convertHistoryInfo(h));
        }
        db.rollback();
        /*
        double res;
        List<HistoryInfoBean> list = new ArrayList<HistoryInfoBean>();
        while ((res = random.nextDouble()) < 0.5)
            ;
        for (Long i = 0L; i < res * TOTAL_LENGTH; i++) {
            HistoryInfoBean bean = new HistoryInfoBean();
            bean.setJobId("" + i);
            bean.refresh();
            list.add(bean);
        }
        */
        List<HistoryInfoBean> subList = new ArrayList<HistoryInfoBean>();
        int start = config.getOffset();
        int totalLength = list.size();
        int offset = 0, limit = 0;
        if (totalLength <= start) {
            int ps = totalLength / ITERMS_PER_PAGE;
            offset = ps * ITERMS_PER_PAGE;
            limit = totalLength;
        } else {
            offset = start;
            limit = Math.min(start + ITERMS_PER_PAGE, totalLength);
        }
        for (int i = offset; i < limit; i++) {
            subList.add(list.get(i));
        }
        return new BasePagingLoadResult<HistoryInfoBean>(subList, offset,
                totalLength);
    }

    @Override
    public List<ProgressInfoBean> retrieveJobList(ProgressInfoBean bean)
            throws Exception {
        /*
        List<FeatureJobInfo> jobs = new ArrayList<FeatureJobInfo>();
        FeatureJobInfo j = new FeatureJobInfo();
        j.setFeatureId("feature1");
        j.setDesc("desc");
        j.setSubmitTime(System.currentTimeMillis());
        j.setUserId(1L);
        j.setPercent(98);
        j.setId("j1");
        TaskRunnerInfo ri = new TaskRunnerInfo();
        ri.setTaskName("task1");
        ri.setProgress(53);
        ri.addAgentInfo("a1", "host1", "ping1", "started");
        ri.addAgentInfo("a2", "host1", "ping2", "started");
        j.addTaskRunnerInfo(ri);
        jobs.add(j);
        
        j = new FeatureJobInfo();
        j.setFeatureId("feature1");
        j.setDesc("desc2");
        j.setSubmitTime(System.currentTimeMillis());
        j.setUserId(2L);
        j.setId("j2");
        j.setPercent(37);
        ri = new TaskRunnerInfo();
        ri.setTaskName("task1");
        ri.setProgress(82);
        ri.addAgentInfo("a3", "host1", "ping3", "started");
        ri.addAgentInfo("a4", "host1", "ping4", "started");
        j.addTaskRunnerInfo(ri);
        jobs.add(j);
        */
        UserInfo userInfo = Util.retrieveUserSession(getThreadLocalRequest());
        List<ProgressInfoBean> res = new ArrayList<ProgressInfoBean>();
        List<FeatureJobInfo> jobs = EngineFactory.getInstance().getEngine().listFeatureJob(new ArrayList<String>());
        Collections.sort(jobs);
        if (bean == null) {
            for (FeatureJobInfo job : jobs) {
                if(!job.getStatus().isStopped() && (userInfo.getAdmin() ? true : userInfo.getId().equals(job.getUserId())))
                    res.add(Util.convertFeatureJobInfo(job));
            }
        }
        else if (bean instanceof FeatureJobInfoBean) {
            for (FeatureJobInfo job : jobs) {
                if (job.getId().equals(((FeatureJobInfoBean) bean).getJobId())) {
                    List<TaskRunnerInfo> taskletInfos = job.getTaskRunnerInfo();
                    for (TaskRunnerInfo t : taskletInfos) {
                        TaskletInfoBean tiBean = Util.convertTaskRunnerInfo(t, job.getId());
                        res.add(tiBean);
                    }
                }
            }
        }
        else if (bean instanceof TaskletInfoBean) {
            for (FeatureJobInfo job : jobs) {
                if (job.getId().equals(((TaskletInfoBean) bean).getTaskletId())) {
                    List<TaskRunnerInfo> taskletInfos = job.getTaskRunnerInfo();
                    try {
                        for (TaskRunnerInfo t : taskletInfos) {
                            if (t.getTaskName().equals(bean.getName())) {
                                for (AgentInfo agent : t.getAgents()) {
                                    res.add(Util.convertAgentInfo(agent));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CedarUIException(
                                CedarOperation.SHOW_JOB_STATUS,
                                ExceptionSeverity.ERROR, e.getMessage(), Util
                                        .getTraces(e));
                    }
                }
            }
        }

        return res;
    }

    @Override
    public Boolean changePassword(InstanceInfoBean ins, String auth)
            throws Exception {
        return Util.convertInstanceInfoBean(ins).setAdminPasswd(auth);
    }

    @Override
    public Boolean syncDateTime(InstanceInfoBean ins) {
        return Util.convertInstanceInfoBean(ins).syncDateTime(true);
    }

    @Override
    public BaseListLoadResult<MachineInfoBean> retrieveMachineListInCloud(
            CloudInfoBean cloud) {
        ArrayList<MachineInfoBean> res = new ArrayList<MachineInfoBean>();
        if (cloud != null) {
            try {
                List<MachineInfo> machines = new ArrayList<MachineInfo>();
                CloudInfo info = Util.convertCloudInfoBean(cloud);
                machines.addAll(CloudUtil.getCurrentMachines(info));

                for (MachineInfo machine : machines) {
                    String id = machine.getImageId();
                    // Matcher m = cloudImagePattern.matcher(id);
                    // if(m.find()){
                    res.add(Util.convertMachineInfo(machine));
                    // }
                }
            } catch (Exception e) {
                LOG.info("", e);
            }
        }
        return new BaseListLoadResult<MachineInfoBean>(res);
    }

    @Override
    public Boolean importCloudImage(MachineInfoBean image) {
        try {
            MachineInfo m = Util.convertMachineInfoBean(image);
            EntityWrapper<MachineInfo> db = EntityUtil
                    .getMachineEntityWrapper();
            db.add(m);
            db.commit();
            return true;
        } catch (Exception e) {
            LOG.info("", e);
        }
        return false;
    }

    @Override
    public Boolean discardCloudImage(MachineInfoBean image) {
        try {
            EntityWrapper<MachineInfo> db = EntityUtil
                    .getMachineEntityWrapper();
            MachineInfo m = db.load(MachineInfo.class, image.getId());
            if (m != null) {
                // check there's no running instance created from this machine
                // image
                Long cloudId = image.getCloudId();
                CloudInfo cloud = CloudInfo.load(cloudId);
                if (cloud != null) {
                    for (InstanceInfo instance : EntityUtil
                            .listInstances(cloud)) {
                        if (instance.getMachineId().equals(image.getId()))
                            return false;
                    }
                }
                db.delete(m);
            }
            db.commit();
            return true;
        } catch (Exception e) {
            LOG.info("", e);
        }
        return false;
    }

    public Boolean registerPhysicalNode(PhysicalNodeInfoBean nodeInfoBean)
            throws Exception {
        PhysicalNodeInfo info = Util.convertPhysicalNodeInfoBean(nodeInfoBean);
        CloudInfo cloud = info.getCloudId() != null ? CloudInfo.load(info
                .getCloudId()) : null;
        ArrayList<NATInfo> nats = new ArrayList<NATInfo>();
        if (cloud != null && cloud.getSeperated()) {
            GatewayInfo gw = cloud.findGateway();
            if (gw != null)
                info.setGatewayId(gw.getId());
            for (NATInfo n : NATInfo.getDefaultPort(info.getOSName())) {
                nats.add(n);
            }
        } else {
            info.setCloudId(-1L);
            info.setGatewayId(-1L);
        }
        if (info.getPooled()) {
            info.setManaged(true);
        }
        info.setNodeName(info.getHost());
        return CloudUtil.registerPhysicalNode(info, nats);
    }

    @Override
    public Boolean discardPhysicalNode(PhysicalNodeInfoBean bean) {
        try {
            EntityWrapper<PhysicalNodeInfo> db = EntityUtil
                    .getPhysicalNodeEntityWrapper();
            PhysicalNodeInfo m = db.load(PhysicalNodeInfo.class, bean.getId());
            db.rollback();
            CloudUtil.deregisterPhysicalNode(m);
            return true;
        } catch (Exception e) {
            LOG.info("", e);
        }
        return false;
    }

    @Override
    public Boolean changePassword(PhysicalNodeInfoBean ins, String auth) {
        return Util.convertPhysicalNodeInfoBean(ins).setAdminPasswd(auth);
    }

    @Override
    public Boolean syncDateTime(PhysicalNodeInfoBean ins) {
        return Util.convertPhysicalNodeInfoBean(ins).syncDateTime();
    }

    @Override
    public boolean discardMachineType(MachineTypeInfoBean bean) {
        try {
            EntityWrapper<MachineTypeInfo> db = EntityUtil
                    .getMachineTypeEntityWrapper();
            MachineTypeInfo m = new MachineTypeInfo();
            m.setId(bean.getId());
            db.delete(db.getUnique(m));
            db.commit();
            return true;
        } catch (Exception e) {
            LOG.info("", e);
        }
        return false;
    }

    @Override
    public boolean registerMachineType(MachineTypeInfoBean bean)
            throws Exception {
        EntityWrapper<MachineTypeInfo> db = EntityUtil
                .getMachineTypeEntityWrapper();
        MachineTypeInfo m = new MachineTypeInfo();
        m.setType(bean.getType());
        m.setCloudId(bean.getCloudId());
        if (db.query(m).size() > 0) {
            throw new Exception("Machine type is already registered!");
        }
        m.setMax(bean.getMax());
        m.setFree(bean.getFree());
        m.setCpu(bean.getCpu());
        m.setMemory(bean.getMemory());
        m.setDisk(bean.getDisk());
        m.setSecondDisk(bean.getSecondDisk());
        db.add(m);
        db.commit();
        return true;
    }

    @Override
    public boolean updateMachineType(MachineTypeInfoBean bean) throws Exception {
        EntityWrapper<MachineTypeInfo> db = EntityUtil
                .getMachineTypeEntityWrapper();
        MachineTypeInfo m = new MachineTypeInfo();
        m.setType(bean.getType());
        m.setCloudId(bean.getCloudId());
        List<MachineTypeInfo> mtList = db.query(m);
        if (mtList.size() == 0) {
            throw new Exception("Machine type is not registered!");
        }
        m = mtList.get(0);
        m.setMax(bean.getMax());
        m.setFree(bean.getFree());
        m.setCpu(bean.getCpu());
        m.setMemory(bean.getMemory());
        m.setDisk(bean.getDisk());
        m.setSecondDisk(bean.getSecondDisk());
        db.merge(m);
        db.commit();
        return true;
    }

    @Override
    public boolean changeUserSettings(UserInfoBean bean) {
        EntityWrapper<UserInfo> db = EntityUtil.getUserEntityWrapper();
        UserInfo e = new UserInfo();
        e.setUser(bean.getUserName());
        List<UserInfo> list = db.query(e);
        if (list.size() == 1) {
            UserInfo u = list.get(0);
            u.setEmail(bean.getEmail());
            u.setPassword(bean.getPassword());
            db.merge(u);
            db.commit();
            return true;
        }
        db.rollback();
        return false;
    }

    @Override
    public boolean killJob(String jobId) {
        IEngine engine = EngineFactory.getInstance().getEngine();
        if(engine.queryFeatureJob(jobId) == null){
            return false;
        }
        engine.kill(jobId);
        return true;
    }
        
    @Override
    public String getUploadedFile() {
        HttpSession session = this.getThreadLocalRequest().getSession();
        return UploadServlet.getUploadedFile(session);
    }

    @Override
    public int addPortMapping(InstanceInfoBean bean, NATInfoBean nat) {
        InstanceInfo instance = Util.convertInstanceInfoBean(bean);
        NATInfo n = Util.convertNATInfoBean(nat);
        for(NATInfo m : instance.getPortMappings()){
            if(m.getPort().equals(n.getPort()) || m.getName().equals(n.getName())){
                return -1;
            }
        }
        instance.assignPortMapping(n);
        instance.enablePortMapping(n);
        return n.getMappedPort();
    }
}
