package com.intel.cedar.service.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
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

@RemoteServiceRelativePath("cts")
public interface CloudRemoteService extends RemoteService {
    public BaseListLoadResult<MachineInfoBean> retrieveImageList(
            ArrayList<String> imageIds) throws Exception;

    public BaseListLoadResult<KeyPairBean> retrieveKeyPairList(
            ArrayList<String> keyIds) throws Exception;

    public BaseListLoadResult<MachineTypeInfoBean> retrieveMachineTypeList(
            ArrayList<String> typeIds, Boolean flag) throws Exception;

    public PagingLoadResult<InstanceInfoBean> retrieveInstanceList(
            PagingLoadConfig config) throws Exception;

    public BaseListLoadResult<InstanceInfoBean> retrieveInstanceList(
            ArrayList<String> instanceIds) throws Exception;

    public BaseListLoadResult<PhysicalNodeInfoBean> retrievePhysicalNodeList(
            ArrayList<String> nodes) throws Exception;

    public PagingLoadResult<KeyPairBean> retrieveKeyPairList(
            PagingLoadConfig config) throws Exception;

    public PagingLoadResult<MachineInfoBean> retrieveImageList(
            PagingLoadConfig config) throws Exception;

    public PagingLoadResult<MachineTypeInfoBean> retrieveMachineTypeList(
            PagingLoadConfig config) throws Exception;

    public BaseListLoadResult<MachineInfoBean> retrieveMachineList(
            ArrayList<CloudInfoBean> clouds) throws Exception;

    public BaseListLoadResult<MachineTypeInfoBean> retrieveMachineTypeList(
            ArrayList<CloudInfoBean> clouds) throws Exception;

    public BaseListLoadResult<CloudInfoBean> retrieveCloudList(
            ArrayList<String> clouds) throws Exception;

    public BaseListLoadResult<UserInfoBean> retrieveUserList(
            ArrayList<String> users) throws Exception;

    public Boolean applyInstance(CloudInfoBean cloud, MachineInfoBean machine,
            MachineTypeInfoBean type, KeyPairBean key, int num,
            List<NATInfoBean> nats) throws Exception;

    public List<InstanceInfoBean> terminateInstance(
            List<InstanceInfoBean> instances) throws Exception;

    public Boolean rebootInstance(InstanceInfoBean instance) throws Exception;

    public List<CloudInfoBean> deregisterCloud(List<CloudInfoBean> clouds)
            throws Exception;

    public List<UserInfoBean> deleteUser(List<UserInfoBean> users)
            throws Exception;

    public Boolean registerCloud(CloudInfoBean cloud,
            ArrayList<GatewayInfoBean> gateways) throws Exception;

    public BaseListLoadResult<GatewayInfoBean> retrieveGatewayList(
            CloudInfoBean cloud) throws Exception;

    public FeatureModel loadFeatureUI(String featureId) throws Exception;

    public ArrayList<String> loadCaseSet(MachineFeature machineFeature,
            TestFeature testFeature) throws Exception;

    public BaseListLoadResult<VarValue> loadCaseSet(
            MachineFeature machineFeature, TestFeature testFeature, boolean flag)
            throws Exception;

    public BaseListLoadResult<CedarSCMLogEntry> loadSVNEntries(String svnurl,
            CedarScmLoadConfig config) throws Exception;

    String getSingleSVNLogMessage(String svnurl, String rev,
            CedarScmLoadConfig config) throws Exception;

    public String getActualTime() throws Exception;

    public ServerTime getSystemTime() throws Exception;

    public Boolean registerUser(UserInfoBean user) throws Exception;

    public UserInfoBean loginUser(UserInfoBean user) throws Exception;

    public UserInfoBean checkCredentialCookie() throws Exception;

    public Boolean deployFeature(Boolean forceUpgrade) throws Exception;

    public BaseListLoadResult<FeatureInfoBean> retrieveFeatureList(
            List<String> featureIds) throws Exception;

    public BaseListLoadResult<FeatureBean> retrieveFeatureBeanList(
            List<String> featureIds) throws Exception;

    public PagingLoadResult<FeatureInfoBean> retrieveFeatureList(
            PagingLoadConfig config) throws Exception;
    
    public FeatureInfoBean disableFeatures(List<String> featureIds)
            throws Exception;

    public List<FeatureInfoBean> enableFeatures(List<String> featureIds)
            throws Exception;

    public List<FeatureBean> deleteFeatures(List<FeatureBean> features)
            throws Exception;

    public Boolean enablePortMappings(InstanceInfoBean bean) throws Exception;

    public String submitTask(FeatureJobRequestBean requestBean)
            throws Exception;

    public CloudInfoBean saveCloud(CloudInfoBean bean,
            List<GatewayInfoBean> gList) throws Exception;

    public BaseListLoadResult<VolumeInfoBean> retrieveVolumeList()
            throws Exception;

    public VolumeInfoBean createVolume(CloudInfoBean bean, Integer size,
            String tag) throws Exception;

    public List<VolumeInfoBean> deleteVolumes(List<VolumeInfoBean> list)
            throws Exception;

    public VolumeInfoBean attachVolume(boolean format, VolumeInfoBean v,
            InstanceInfoBean ins) throws Exception;

    public String showConsoleOutput(InstanceInfoBean ins) throws Exception;

    public String showInstanceInfo(InstanceInfoBean ins) throws Exception;

    public BaseListLoadResult<VolumeInfoBean> retrieveAvailableVolumes(
            InstanceInfoBean ins) throws Exception;

    public BaseListLoadResult<VolumeInfoBean> retrieveAttachedVolumes(
            InstanceInfoBean ins) throws Exception;

    public List<VolumeInfoBean> detachVolumes(List<VolumeInfoBean> list)
            throws Exception;

    public PagingLoadResult<HistoryInfoBean> retrieveHistory(
            PagingLoadConfig config) throws Exception;

    public List<ProgressInfoBean> retrieveJobList(ProgressInfoBean bean)
            throws Exception;

    public Boolean changePassword(InstanceInfoBean ins, String auth)
            throws Exception;

    public Boolean syncDateTime(InstanceInfoBean ins);

    public BaseListLoadResult<MachineInfoBean> retrieveMachineListInCloud(
            CloudInfoBean cloud);

    public Boolean importCloudImage(MachineInfoBean image);

    public Boolean discardCloudImage(MachineInfoBean image);

    public Boolean registerPhysicalNode(PhysicalNodeInfoBean bean)
            throws Exception;

    public Boolean discardPhysicalNode(PhysicalNodeInfoBean bean);

    public Boolean changePassword(PhysicalNodeInfoBean ins, String auth);

    public Boolean syncDateTime(PhysicalNodeInfoBean ins);

    boolean registerMachineType(MachineTypeInfoBean nodeInfoBean)
            throws Exception;

    boolean discardMachineType(MachineTypeInfoBean bean);

    boolean changeUserSettings(UserInfoBean bean);

    boolean updateMachineType(MachineTypeInfoBean nodeInfoBean)
            throws Exception;

	public BaseListLoadResult<PropertyPair> retrieveImageProperties(Long imageId) throws Exception;

	public Boolean addProperties(List<PropertyPair> properPairs, Long machineId) throws Exception;

	public List<String> retrieveCapabilities(Long imageId) throws Exception;
	
	public Boolean addCapabilities(List<FeatureBean> Capa, Long machineId)throws Exception;

    BaseListLoadResult<PropertyPair> retrievePhysicalNodeProperties(Long nodeId) throws Exception;

    boolean addPhysicalNodeCapabilities(List<FeatureBean> Capa, Long nodeId) throws Exception;

    boolean addPhysicalNodeProperties(List<PropertyPair> properPairs,
            Long nodeId) throws Exception;

    public List<String> retrievePhysicalNodeCapabilities(Long nodeId) throws Exception;

    public boolean killJob(String jobId);
    
    public String getUploadedFile() throws Exception;

    public List<String> getGitBranches(String url, CedarScmLoadConfig config) throws Exception;

    int addPortMapping(InstanceInfoBean instance, NATInfoBean nat);
}
