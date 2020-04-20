package com.intel.cedar.service.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.intel.cedar.service.client.feature.model.CedarScmLoadConfig;
import com.intel.cedar.service.client.feature.model.CedarSCMLogEntry;
import com.intel.cedar.service.client.feature.model.FeatureBean;
import com.intel.cedar.service.client.feature.model.FeatureInfoBean;
import com.intel.cedar.service.client.feature.model.FeatureJobRequestBean;
import com.intel.cedar.service.client.feature.model.HistoryInfoBean;
import com.intel.cedar.service.client.feature.model.MachineFeature;
import com.intel.cedar.service.client.feature.model.ProgressInfoBean;
import com.intel.cedar.service.client.feature.model.TestFeature;
import com.intel.cedar.service.client.feature.model.VarValue;
import com.intel.cedar.service.client.feature.model.ui.FeatureModel;
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

public interface CloudRemoteServiceAsync {
    public void retrieveImageList(ArrayList<String> imageIds,
            AsyncCallback<BaseListLoadResult<MachineInfoBean>> callback);
    
    public void retrieveImageProperties(Long imageId, 
    		AsyncCallback<BaseListLoadResult<PropertyPair>> callback); 
    
    public void addProperties(List<PropertyPair> properPairs, Long machineId,
    		AsyncCallback<Boolean> callback);
    
    public void addCapabilities(List<FeatureBean> Capa, Long machineId,
    		AsyncCallback<Boolean> callback);

    public void retrievePhysicalNodeProperties(Long nodeId, 
            AsyncCallback<BaseListLoadResult<PropertyPair>> callback); 
    
    public void addPhysicalNodeProperties(List<PropertyPair> properPairs, Long nodeId,
            AsyncCallback<Boolean> callback);
    
    public void addPhysicalNodeCapabilities(List<FeatureBean> Capa, Long nodeId,
            AsyncCallback<Boolean> callback);

    
    public void retrieveCloudList(ArrayList<String> clouds,
            AsyncCallback<BaseListLoadResult<CloudInfoBean>> callback);

    public void retrieveUserList(ArrayList<String> users,
            AsyncCallback<BaseListLoadResult<UserInfoBean>> callback);

    public void retrieveMachineTypeList(ArrayList<CloudInfoBean> clouds,
            AsyncCallback<BaseListLoadResult<MachineTypeInfoBean>> callback);

    public void retrieveMachineTypeList(ArrayList<String> typeIds,
            Boolean flag,
            AsyncCallback<BaseListLoadResult<MachineTypeInfoBean>> callback);

    public void retrieveMachineList(ArrayList<CloudInfoBean> clouds,
            AsyncCallback<BaseListLoadResult<MachineInfoBean>> callback);

    public void retrieveMachineListInCloud(CloudInfoBean cloud,
            AsyncCallback<BaseListLoadResult<MachineInfoBean>> callback);

    public void retrieveKeyPairList(ArrayList<String> keyIds,
            AsyncCallback<BaseListLoadResult<KeyPairBean>> callback);

    public void retrieveImageList(PagingLoadConfig config,
            AsyncCallback<PagingLoadResult<MachineInfoBean>> callback);

    public void retrieveInstanceList(PagingLoadConfig config,
            AsyncCallback<PagingLoadResult<InstanceInfoBean>> callback);

    public void retrieveInstanceList(ArrayList<String> instanceIds,
            AsyncCallback<BaseListLoadResult<InstanceInfoBean>> callback);

    public void retrievePhysicalNodeList(ArrayList<String> nodes,
            AsyncCallback<BaseListLoadResult<PhysicalNodeInfoBean>> callback);

    public void retrieveKeyPairList(PagingLoadConfig config,
            AsyncCallback<PagingLoadResult<KeyPairBean>> callback);

    public void retrieveMachineTypeList(PagingLoadConfig config,
            AsyncCallback<PagingLoadResult<MachineTypeInfoBean>> callback);

    public void applyInstance(CloudInfoBean cloud, MachineInfoBean machine,
            MachineTypeInfoBean type, KeyPairBean key, int num,
            List<NATInfoBean> nats, AsyncCallback<Boolean> callback);

    public void terminateInstance(List<InstanceInfoBean> instances,
            AsyncCallback<List<InstanceInfoBean>> callback);

    public void rebootInstance(InstanceInfoBean instance,
            AsyncCallback<Boolean> callback);

    public void registerCloud(CloudInfoBean cloud,
            ArrayList<GatewayInfoBean> gateways, AsyncCallback<Boolean> callback);

    public void deregisterCloud(List<CloudInfoBean> clouds,
            AsyncCallback<List<CloudInfoBean>> callback);

    public void importCloudImage(MachineInfoBean image,
            AsyncCallback<Boolean> callback);

    public void discardCloudImage(MachineInfoBean image,
            AsyncCallback<Boolean> callback);

    public void deleteUser(List<UserInfoBean> users,
            AsyncCallback<List<UserInfoBean>> callback);

    public void retrieveGatewayList(CloudInfoBean cloud,
            AsyncCallback<BaseListLoadResult<GatewayInfoBean>> callback);

    public static class Util {
        private static CloudRemoteServiceAsync _singleton = null;

        public static CloudRemoteServiceAsync getInstance() {
            synchronized (CloudRemoteServiceAsync.class) {
                if (_singleton == null) {
                    _singleton = GWT.create(CloudRemoteService.class);
                }

                return _singleton;
            }
        }
    }

    public void loadFeatureUI(String featureId,
            AsyncCallback<FeatureModel> callback);

    public void loadCaseSet(MachineFeature machineFeature,
            TestFeature testFeature, AsyncCallback<ArrayList<String>> callback);

    public void loadCaseSet(MachineFeature machineFeature,
            TestFeature testFeature, boolean flag,
            AsyncCallback<BaseListLoadResult<VarValue>> callback);

    public void loadSVNEntries(String svnurl, CedarScmLoadConfig config,
            AsyncCallback<BaseListLoadResult<CedarSCMLogEntry>> callback);

    public void getSingleSVNLogMessage(String svnurl, String rev, CedarScmLoadConfig config, 
            AsyncCallback<String> callback);
    
    public void getGitBranches(String url, CedarScmLoadConfig config, 
            AsyncCallback<List<String>> callback);
    
    public void getActualTime(AsyncCallback<String> callback);

    public void getSystemTime(AsyncCallback<ServerTime> callback);

    public void registerUser(UserInfoBean user, AsyncCallback<Boolean> callback);

    public void loginUser(UserInfoBean user,
            AsyncCallback<UserInfoBean> callback);

    public void checkCredentialCookie(AsyncCallback<UserInfoBean> callback);

    public void deployFeature(Boolean forceUpgrade,
            AsyncCallback<Boolean> callback);

    void deleteFeatures(List<FeatureBean> features,
            AsyncCallback<List<FeatureBean>> callback);

    void disableFeatures(List<String> featureIds,
            AsyncCallback<FeatureInfoBean> callback);

    void enableFeatures(List<String> featureIds,
            AsyncCallback<List<FeatureInfoBean>> callback);

    void retrieveFeatureList(List<String> featureIds,
            AsyncCallback<BaseListLoadResult<FeatureInfoBean>> callback);
    
    void retrieveFeatureList(PagingLoadConfig config,
            AsyncCallback<PagingLoadResult<FeatureInfoBean>> callback);
    
    void retrieveFeatureBeanList(List<String> featureIds,
            AsyncCallback<BaseListLoadResult<FeatureBean>> callback);

    void enablePortMappings(InstanceInfoBean bean,
            AsyncCallback<Boolean> callback);

    void submitTask(FeatureJobRequestBean requestBean,
            AsyncCallback<String> callback);

    void saveCloud(CloudInfoBean bean, List<GatewayInfoBean> gList,
            AsyncCallback<CloudInfoBean> callback);

    void retrieveVolumeList(
            AsyncCallback<BaseListLoadResult<VolumeInfoBean>> callback);

    void createVolume(CloudInfoBean bean, Integer size, String tag,
            AsyncCallback<VolumeInfoBean> callback);

    public void deleteVolumes(List<VolumeInfoBean> list,
            AsyncCallback<List<VolumeInfoBean>> callback);

    void attachVolume(boolean format, VolumeInfoBean v, InstanceInfoBean ins,
            AsyncCallback<VolumeInfoBean> callback);

    void showConsoleOutput(InstanceInfoBean ins, AsyncCallback<String> callback);

    void showInstanceInfo(InstanceInfoBean ins, AsyncCallback<String> callback);

    public void retrieveAvailableVolumes(InstanceInfoBean ins,
            AsyncCallback<BaseListLoadResult<VolumeInfoBean>> callback);

    public void retrieveAttachedVolumes(InstanceInfoBean ins,
            AsyncCallback<BaseListLoadResult<VolumeInfoBean>> callback);

    public void detachVolumes(List<VolumeInfoBean> list,
            AsyncCallback<List<VolumeInfoBean>> callback);

    public void retrieveHistory(PagingLoadConfig config,
            AsyncCallback<PagingLoadResult<HistoryInfoBean>> callback);

    public void retrieveJobList(ProgressInfoBean bean,
            AsyncCallback<List<ProgressInfoBean>> callback);

    public void changePassword(InstanceInfoBean ins, String auth,
            AsyncCallback<Boolean> callback);

    public void syncDateTime(InstanceInfoBean ins,
            AsyncCallback<Boolean> callback);

    public void changePassword(PhysicalNodeInfoBean ins, String auth,
            AsyncCallback<Boolean> callback);

    public void syncDateTime(PhysicalNodeInfoBean ins,
            AsyncCallback<Boolean> callback);

    public void registerPhysicalNode(PhysicalNodeInfoBean nodeInfoBean,
            AsyncCallback<Boolean> callback);

    public void discardPhysicalNode(PhysicalNodeInfoBean bean,
            AsyncCallback<Boolean> callback);

    public void registerMachineType(MachineTypeInfoBean nodeInfoBean,
            AsyncCallback<Boolean> callback);

    public void updateMachineType(MachineTypeInfoBean nodeInfoBean,
            AsyncCallback<Boolean> callback);

    public void discardMachineType(MachineTypeInfoBean bean,
            AsyncCallback<Boolean> callback);

    public void changeUserSettings(UserInfoBean bean,
            AsyncCallback<Boolean> callback);

	public void retrieveCapabilities(Long imageId,
			AsyncCallback<List<String>> callback);

    public void retrievePhysicalNodeCapabilities(Long nodeId,
            AsyncCallback<List<String>> callback);
    
    public void killJob(String jobId, AsyncCallback<Boolean> callback);
    
    public void getUploadedFile(AsyncCallback<String> callback);
    
    public void addPortMapping(InstanceInfoBean instance, NATInfoBean nat, AsyncCallback<Integer> callback);
}