package com.intel.cedar.service.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.intel.cedar.core.entities.CloudInfo;
import com.intel.cedar.core.entities.GatewayInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.KeyPairDescription;
import com.intel.cedar.core.entities.MachineInfo;
import com.intel.cedar.core.entities.MachineInfo.ARCH;
import com.intel.cedar.core.entities.MachineInfo.OS;
import com.intel.cedar.core.entities.MachineTypeInfo;
import com.intel.cedar.core.entities.NATInfo;
import com.intel.cedar.core.entities.PhysicalNodeInfo;
import com.intel.cedar.core.entities.VolumeInfo;
import com.intel.cedar.engine.FeatureJobInfo;
import com.intel.cedar.engine.FeatureJobRequest;
import com.intel.cedar.engine.FeatureStatus;
import com.intel.cedar.engine.HistoryInfo;
import com.intel.cedar.engine.TaskRunnerInfo;
import com.intel.cedar.engine.TaskRunnerInfo.AgentInfo;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.feature.util.FeatureUtil;
import com.intel.cedar.feature.util.SCMChangeItem;
import com.intel.cedar.feature.util.SCMChangeSet;
import com.intel.cedar.service.client.Constants;
import com.intel.cedar.service.client.feature.model.AgentInfoBean;
import com.intel.cedar.service.client.feature.model.CedarSCMLogEntry;
import com.intel.cedar.service.client.feature.model.FeatureBean;
import com.intel.cedar.service.client.feature.model.FeatureInfoBean;
import com.intel.cedar.service.client.feature.model.FeatureJobInfoBean;
import com.intel.cedar.service.client.feature.model.FeatureJobRequestBean;
import com.intel.cedar.service.client.feature.model.HistoryInfoBean;
import com.intel.cedar.service.client.feature.model.TaskletInfoBean;
import com.intel.cedar.service.client.model.BlockDeviceMappingBean;
import com.intel.cedar.service.client.model.CloudInfoBean;
import com.intel.cedar.service.client.model.FeatureStatusBean;
import com.intel.cedar.service.client.model.GatewayInfoBean;
import com.intel.cedar.service.client.model.ImageDescriptionBean;
import com.intel.cedar.service.client.model.InstanceBean;
import com.intel.cedar.service.client.model.InstanceBean.InstanceTypeBean;
import com.intel.cedar.service.client.model.InstanceInfoBean;
import com.intel.cedar.service.client.model.KeyPairBean;
import com.intel.cedar.service.client.model.MachineInfoBean;
import com.intel.cedar.service.client.model.MachineTypeInfoBean;
import com.intel.cedar.service.client.model.NATInfoBean;
import com.intel.cedar.service.client.model.PhysicalNodeInfoBean;
import com.intel.cedar.service.client.model.ReservationDescriptionBean;
import com.intel.cedar.service.client.model.UserInfoBean;
import com.intel.cedar.service.client.model.VolumeInfoBean;
import com.intel.cedar.user.UserInfo;
import com.intel.cedar.user.util.UserUtil;
import com.intel.cedar.util.CloudUtil;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import com.xerox.amazonws.ec2.BlockDeviceMapping;
import com.xerox.amazonws.ec2.ImageDescription;
import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.ReservationDescription;

public class Util {

    public static InstanceInfoBean convertInstanceInfo(InstanceInfo instance) {
        InstanceInfoBean w = new InstanceInfoBean();
        w.setCloudId(instance.getCloudId());
        w.setGatewayId(instance.getGatewayId());
        w.setId(instance.getId());
        w.setInstanceId(instance.getInstanceId());
        w.setIp(instance.getHost());
        w.setPrivateIp(instance.getPrivateIp());
        w.setPrivateDns(instance.getPrivateDns());
        w.setMachineId(instance.getMachineId());
        w.setState(instance.getState());
        w.setTypeId(instance.getTypeId());
        w.setManaged(instance.getMachineInfo().getManaged());
        w.setPooled(instance.getPooled());
        w.setUser(convertUserInfo(UserUtil.getUserById(instance.getUserId())));
        w.setRemoteDisplay(instance.getRemoteDisplay());
        if (instance.getCloudInfo() != null) {
            w.setCloudName(instance.getCloudInfo().getName());
        } else
            w.setCloudName("N/A");

        MachineInfo m = instance.getMachineInfo();
        if (m != null)
            w.setOs(m.getOSName());
        else
            w.setOs("N/A");

        if ((m != null) && (m.getArch() != null)) {
            w.setArch(m.getArch().name());
        } else
            w.setArch("N/A");

        String comment = "";
        if ((m != null) && (m.getComment() != null)) {
            comment = m.getComment();
        }
        if (instance.getPooled())
            comment += " (Pooled)";
        w.setComment(comment);

        if (instance.getMachineTypeInfo() != null) {
            w.setTypeName(instance.getMachineTypeInfo().getType());
        } else
            w.setTypeName("N/A");

        w.refresh();
        return w;
    }

    public static InstanceInfo convertInstanceInfoBean(InstanceInfoBean bean) {
        EntityWrapper<InstanceInfo> db = EntityUtil.getInstanceEntityWrapper();
        try {
            return db.load(InstanceInfo.class, bean.getId());
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            db.rollback();
        }
        return null;
    }

    public static PhysicalNodeInfo convertPhysicalNodeInfoBean(
            PhysicalNodeInfoBean nodeInfoBean) {
        PhysicalNodeInfo info = new PhysicalNodeInfo();
        info.setHost(nodeInfoBean.getHost());
        info.setOs(OS.fromString(nodeInfoBean.getOsName()));
        info.setArch(ARCH.fromString(nodeInfoBean.getArchName()));
        info.setCpu(nodeInfoBean.getCpu());
        info.setMemory(nodeInfoBean.getMem());
        info.setRootPath(nodeInfoBean.getRootPath());
        info.setDisk(nodeInfoBean.getDisk());
        info.setUserId(nodeInfoBean.getUserId());
        info.setCloudId(nodeInfoBean.getCloudId());
        info.setShared(nodeInfoBean.getShared());
        info.setPooled(nodeInfoBean.getPooled());
        info.setState(nodeInfoBean.getState());
        info.setComment(nodeInfoBean.getComment());
        info.setManaged(nodeInfoBean.getManaged());
        return info;
    }

    public static PhysicalNodeInfoBean convertPhysicalNodeInfo(
            PhysicalNodeInfo info) {
        PhysicalNodeInfoBean nodeInfoBean = new PhysicalNodeInfoBean();
        nodeInfoBean.setId(info.getId());
        nodeInfoBean.setHost(info.getHost());
        nodeInfoBean.setOsName(info.getOs().getOSName());
        nodeInfoBean.setArchName(info.getArch().name());
        nodeInfoBean.setCpu(info.getCpu());
        nodeInfoBean.setMem(info.getMemory());
        nodeInfoBean.setDisk(info.getDisk());
        nodeInfoBean.setRootPath(info.getRootPath());
        nodeInfoBean.setUserId(info.getUserId());
        nodeInfoBean.setCloudId(info.getCloudId());
        nodeInfoBean.setPooled(info.getPooled());
        nodeInfoBean.setShared(info.getShared());
        nodeInfoBean.setManaged(info.getManaged());
        nodeInfoBean.setState(info.getState());
        nodeInfoBean.setUser(UserUtil.getUserById(info.getUserId()).getUser());
        CloudInfo cloud = CloudInfo.load(info.getCloudId());
        if (cloud != null) {
            nodeInfoBean.setCloudName(cloud.getName());
        } else {
            nodeInfoBean.setCloudName("Global");
        }
        nodeInfoBean.setComment(info.getComment());
        nodeInfoBean.refresh();
        return nodeInfoBean;
    }

    public static MachineInfoBean convertMachineInfo(MachineInfo imageInfo) {
        MachineInfoBean image = new MachineInfoBean();
        if (imageInfo.getCloudId() > 0) {
            image.setCloudId(imageInfo.getCloudId());
            image.setCloudName(imageInfo.getCloudInfo().getName());
        }
        image.setId(imageInfo.getId());
        image.setImageId(imageInfo.getImageId());
        image.setImageName(imageInfo.getImageName());
        image.setArch(imageInfo.getArchitecture());
        image.setOs(imageInfo.getOSName());
        image.setComment(imageInfo.getComment());
        image.setManaged(imageInfo.getManaged());
        image.setEnabled(imageInfo.getEnabled());
        image.refresh();
        return image;

    }

    public static CloudInfoBean convertCloudInfoEC2(CloudInfo cloudInfo) {
        CloudInfoBean cloud = new CloudInfoBean();
        cloud.setId(cloudInfo.getId());
        cloud.setCloudName(cloudInfo.getName());
        cloud.setProtocol(cloudInfo.getProtocol());
        cloud.setHost(cloudInfo.getHost());
        cloud.setPort(cloudInfo.getPort());
        cloud.setEnabled(cloudInfo.getEnabled());
        cloud.setSeparated(cloudInfo.getSeperated());
        cloud.setResourcePrefix(cloudInfo.getService());
        if (cloudInfo.getProtocol().equalsIgnoreCase("EC2")
                || cloudInfo.getProtocol().equalsIgnoreCase("Eucalyptus")
                || cloudInfo.getProtocol().equalsIgnoreCase("OpenStack")) {
            cloud.setAccessKey(cloudInfo.getParam1());
            cloud.setSecretKey(cloudInfo.getParam2());
        }
        cloud.setSecured(cloudInfo.getSecured());
        cloud.setProxyHost(cloudInfo.getProxyHost());
        cloud.setProxyPort(cloudInfo.getProxyPort());
        cloud.setProxyAuth(cloudInfo.getProxyAuth());
        cloud.setProxyPass(cloudInfo.getProxyPasswd());
        cloud.refresh();
        return cloud;
    }

    public static CloudInfo convertCloudInfoBean(CloudInfoBean cloudBean) {
        CloudInfo cloud = new CloudInfo();
        cloud.setId(cloudBean.getId());
        cloud.setName(cloudBean.getCloudName());
        cloud.setProtocol(cloudBean.getProtocol());
        cloud.setHost(cloudBean.getHost());
        cloud.setPort(cloudBean.getPort());
        cloud.setEnabled(cloudBean.isEnabled());
        cloud.setSeperated(cloudBean.isSeparated());
        if (cloudBean.getProtocol() != null
                && (cloudBean.getProtocol().equalsIgnoreCase("EC2")
                        || cloudBean.getProtocol().equalsIgnoreCase(
                                "Eucalyptus") || cloudBean.getProtocol()
                        .equalsIgnoreCase("OpenStack"))) {
            cloud.setParam1(cloudBean.getAccessKey());
            cloud.setParam2(cloudBean.getSecretKey());
        }
        cloud.setService(cloudBean.getResourcePrefix());
        cloud.setSecured(cloudBean.isSecured());
        cloud.setProxyHost(cloudBean.getProxyHost());
        cloud.setProxyPort(cloudBean.getProxyPort());
        cloud.setProxyAuth(cloudBean.getProxyAuth());
        cloud.setProxyPasswd(cloudBean.getProxyPass());
        return cloud;
    }

    public static MachineTypeInfoBean convertMachineTypeInfo(
            MachineTypeInfo info) {
        MachineTypeInfoBean type = new MachineTypeInfoBean();
        type.setId(info.getId());
        type.setCloudId(info.getCloudId());
        type.setCloudName(info.getCloudInfo().getName());
        type.setType(info.getType());
        type.setCpu(info.getCpu());
        type.setMemory(info.getMemory());
        type.setDisk(info.getDisk());
        type.setSecondDisk(info.getSecondDisk());
        type.setFree(info.getFree());
        type.setMax(info.getMax());
        type.refresh();
        return type;
    }

    public static KeyPairBean convertKeyPairDescription(KeyPairDescription des) {
        KeyPairBean key = new KeyPairBean();
        key.setId(des.getId());
        key.setCloudId(des.getCloudId());
        key.setKeyName(des.getKeyName());
        key.setKeyFingerPrint(des.getKeyFingerPrint());

        if (des.getCloudInfo() != null)
            key.setCloudName(des.getCloudInfo().getName());
        else
            key.setCloudName("N/A");

        key.refresh();
        return key;
    }

    public static void copyImageDescription(List<ImageDescription> src,
            ArrayList<ImageDescriptionBean> dest) {
        if (src == null || src.size() == 0) {
            return;
        }

        for (ImageDescription description : src) {
            ArrayList<String> productCodes = new ArrayList<String>();
            for (String str : description.getProductCodes()) {
                productCodes.add(str);
            }

            ArrayList<BlockDeviceMappingBean> blockDeviceMappingBeans = new ArrayList<BlockDeviceMappingBean>();
            for (BlockDeviceMapping bdm : description.getBlockDeviceMapping()) {
                blockDeviceMappingBeans.add(new BlockDeviceMappingBean(bdm
                        .getDeviceName(), bdm.getSnapshotId(), bdm
                        .getVolumeSize(), bdm.isDeleteOnTerminate()));
            }

            ImageDescriptionBean cp = new ImageDescriptionBean(description
                    .getImageId(), description.getImageLocation(), description
                    .getImageOwnerId(), description.getImageState(),
                    description.isPublic(), productCodes, description
                            .getArchitecture(), description.getImageType(),
                    description.getKernelId(), description.getRamdiskId(),
                    description.getPlatform(), description.getReason(),
                    description.getImageOwnerAlias(), description.getName(),
                    description.getDescription(), description
                            .getRootDeviceType(), description
                            .getRootDeviceName(), blockDeviceMappingBeans,
                    description.getVirtualizationType());
            dest.add(cp);
        }
    }

    public static void copyReservationDescription(
            List<ReservationDescription> src,
            ArrayList<ReservationDescriptionBean> dest) {
        if (src == null || src.size() == 0) {
            return;
        }

        for (ReservationDescription description : src) {
            ReservationDescriptionBean cp = new ReservationDescriptionBean(
                    description.getRequestId(), description.getOwner(),
                    description.getReservationId(), description
                            .getRequesterId());

            List<ReservationDescription.Instance> instances = description
                    .getInstances();
            for (ReservationDescription.Instance ins : instances) {
                InstanceTypeBean type = InstanceBean.InstanceTypeBean
                        .getTypeFromString(ins.getInstanceType().getTypeId());
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm")
                        .format(ins.getLaunchTime().getTime());
                InstanceBean ins_cp = new InstanceBean(cp.getRequesterId(), cp
                        .getOwner(), cp.getReservationId(), cp.getRequestId(),
                        ins.getImageId(), ins.getInstanceId(), ins
                                .getPrivateDnsName(), ins.getDnsName(), ins
                                .getKeyName(), ins.getState(), type, time, ins
                                .getAvailabilityZone(), ins.getKernelId(), ins
                                .getRamdiskId(), ins.getPlatform());
                cp.addInstance(ins_cp);
            }

            dest.add(cp);
        }
    }

    public static void copyKeyPair(List<KeyPairInfo> src,
            ArrayList<KeyPairBean> dest) {
        if (src == null || src.size() == 0) {
            return;
        }

        for (KeyPairInfo key : src) {
            KeyPairBean cp = new KeyPairBean();
            cp.setKeyName(key.getKeyName());
            cp.setKeyFingerPrint(key.getKeyFingerprint());
            dest.add(cp);
        }
    }

    public static String getTraces(Throwable t) {
        if (t == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        t.printStackTrace(pw);

        return sw.toString();
    }

    public static MachineInfo convertMachineInfoBean(MachineInfoBean bean) {
        MachineInfo info = new MachineInfo();
        info.setCloudId(bean.getCloudId());
        info.setId(bean.getId());
        info.setImageId(bean.getImageId());
        info.setOs(MachineInfo.OS.fromString(bean.getOs()));
        info.setArch(MachineInfo.ARCH.fromString(bean.getArch()));
        info.setComment(bean.getComment());
        info.setEnabled(bean.getEnabled());
        info.setImageName(bean.getImageName());
        info.setManaged(bean.getManaged());
        return info;
    }

    public static MachineTypeInfo convertMachineTypeInfoBean(
            MachineTypeInfoBean bean) {
        MachineTypeInfo info = new MachineTypeInfo();
        info.setCloudId(bean.getCloudId());
        info.setType(bean.getType());
        info.setId(bean.getId());
        info.setCpu(bean.getCpu());
        info.setMemory(bean.getMemory());
        info.setDisk(bean.getDisk());
        info.setSecondDisk(bean.getSecondDisk());
        info.setFree(bean.getFree());
        info.setMax(bean.getMax());
        return info;
    }

    public static GatewayInfoBean convertGatewayInfo(GatewayInfo gateway) {
        GatewayInfoBean bean = new GatewayInfoBean();
        bean.setId(gateway.getId());
        bean.setHost(gateway.getHost());
        bean.setMappedPorts(gateway.getMappedPorts());
        bean.refresh();
        return bean;
    }

    public static GatewayInfo convertGatewayInfoBean(GatewayInfoBean bean) {
        GatewayInfo node = new GatewayInfo();
        node.setHost(bean.getHost());
        node.setId(bean.getId());
        node.setMappedPorts(bean.getMappedPorts());
        return node;
    }

    public static NATInfo convertNATInfoBean(NATInfoBean bean) {
        NATInfo nat = new NATInfo();
        nat.setGatewayId(bean.getGatewayId());
        nat.setName(bean.getName());
        nat.setPort(bean.getPort());
        nat.setMappedPort(bean.getMappedPort());

        return nat;
    }

    public static NATInfoBean convertNATInfo(NATInfo nat) {
        NATInfoBean bean = new NATInfoBean();
        bean.setId(nat.getId());
        bean.setGatewayId(nat.getGatewayId());
        bean.setInstanceId(nat.getInstanceId());
        bean.setName(bean.getName());
        bean.setPort(nat.getPort());
        bean.setMappedPort(nat.getMappedPort());

        bean.refresh();
        return bean;
    }

    public static UserInfo convertUserInfoBean(UserInfoBean bean) {
        UserInfo info = new UserInfo();
        info.setUser(bean.getUserName());
        info.setAdmin(bean.getAdmin());
        info.setEmail(bean.getEmail());
        info.setPassword(bean.getPassword());
        return info;
    }

    public static UserInfoBean convertUserInfo(UserInfo info) {
        UserInfoBean bean = new UserInfoBean();
        if (info == null) {
            bean.setId(0L);
            bean.setUserName("N/A");
            bean.setEmail("N/A");
            bean.setAdmin(false);
            bean.setPassword("fuzzy");
        }
        else{
            bean.setId(info.getId());
            bean.setUserName(info.getUser());
            bean.setEmail(info.getEmail());
            bean.setAdmin(info.getAdmin());
            bean.setPassword(info.getPassword());
        }
        bean.refresh();
        return bean;
    }

    public static FeatureInfoBean convertFeatureInfo(FeatureInfo info) {
        FeatureInfoBean bean = new FeatureInfoBean();
        bean.setId(info.getId());
        bean.setName(info.getName());
        bean.setShortName(info.getShortName());
        bean.setContextPath(info.getContextPath());
        bean.setContributor(info.getContributer());
        bean.setVersion(info.getVersion());
        bean.setDescription(info.getDescriptor());
        bean.setEnIcon(info.getEnIcon());
        bean.setDisIcon(info.getDisIcon());
        bean.setEnabled(info.isEnabled());

        // bean.refresh();
        return bean;
    }

    public static FeatureBean convertFeatureInfoRe(FeatureInfo info) {
        FeatureBean bean = new FeatureBean();
        bean.setId(info.getId());
        bean.setName(info.getName());
        bean.setShortName(info.getShortName());
        bean.setContextPath(info.getContextPath());
        bean.setContributor(info.getContributer());
        bean.setVersion(info.getVersion());
        bean.setDescription(info.getDescriptor());
        bean.setEnIcon(info.getEnIcon());
        bean.setDisIcon(info.getDisIcon());
        bean.setEnabled(info.isEnabled());

        bean.refresh();
        return bean;
    }

    public static FeatureInfo convertFeatureInfoBean(FeatureInfoBean bean) {
        FeatureInfo info = new FeatureInfo();

        return info;
    }

    public static VolumeInfoBean convertVolumeInfo(VolumeInfo info) {
        VolumeInfoBean bean = new VolumeInfoBean();
        bean.setId(info.getId());
        bean.setName(info.getName());
        bean.setPath(info.getPath());
        bean.setSize(info.getSize());
        bean.setVolumeId(info.getImageId());
        bean.setInstanceId(info.getAttached());
        bean.setCloudId(info.getCloudId());
        bean.setDeviceIndex(info.getDeviceIndex());
        bean.setAttachedCount(info.getAttachedCount());
        bean.setPooled(info.getPooled());
        String comment = "";
        if ((info != null) && (info.getComment() != null)) {
            comment = info.getComment();
        }
        if (info.getPooled())
            comment += " (Pooled)";
        if (info.getHeld()) {
            comment += " (Held)";
        }
        bean.setComment(comment);
        bean.setUser(convertUserInfo(UserUtil.getUserById(info.getUserId())));
        CloudInfo cinfo = CloudUtil.getCloudById(info.getCloudId());
        if (cinfo != null) {
            bean.setCloudName(cinfo.getName());
        } else {
            bean.setCloudName("N/A");
        }

        bean.refresh();
        return bean;
    }

    public static EnumMap<FeatureStatus, FeatureStatusBean> statusMap = new EnumMap<FeatureStatus, FeatureStatusBean>(
            FeatureStatus.class);
    static {
        statusMap.put(FeatureStatus.Cancelled, FeatureStatusBean.Cancelled);
        statusMap.put(FeatureStatus.Failed, FeatureStatusBean.Failed);
        statusMap.put(FeatureStatus.Finished, FeatureStatusBean.Finished);
        statusMap.put(FeatureStatus.Started, FeatureStatusBean.Started);
        statusMap.put(FeatureStatus.Submitted, FeatureStatusBean.Submitted);
    }

    public static HistoryInfoBean convertHistoryInfo(HistoryInfo info) {
        HistoryInfoBean bean = new HistoryInfoBean();
        bean.setJobId(info.getId());
        bean.setUser(convertUserInfo(UserUtil.getUserById(info.getUserId())));
        bean.setSubmitTime(info.getSubmitTime());
        bean.setEndTime(info.getEndTime());
        String f = FeatureUtil.getFeatureNameById(info.getFeatureId());
        if(f != null && f.length() > 0)
            bean.setFeature(f);
        else
            bean.setFeature(info.getFeatureId());
        bean.setDes(info.getDesc());
        bean.setStatus(statusMap.get(info.getStatus()));
        bean.setLogLocation(info.getLocation());
        bean.refresh();
        return bean;
    }

    public static FeatureJobInfoBean convertFeatureJobInfo(FeatureJobInfo info) {
        FeatureJobInfoBean bean = new FeatureJobInfoBean();
        // TODO temporarally added, will remove "id" when kill action is
        // supported
        bean.setName(FeatureUtil.getFeatureNameById(info.getFeatureId()) + " (" + info.getId() + ")");
        //bean.setName(info.getFeatureId()); //should be featureName
        bean.setJobId(info.getId());
        bean.setStartTime(info.getSubmitTime());
        bean.setEndTime(info.getEndTime());
        bean.setProgress(info.getPercent() / 100.0);
        bean.setUser(convertUserInfo(UserUtil.getUserById(info.getUserId())));
        bean.setDes(info.getDesc());
        bean.setLogLocation(info.getLocation());
        bean.refresh();
        return bean;
    }

    public static TaskletInfoBean convertTaskRunnerInfo(TaskRunnerInfo info, String jobId) {
        TaskletInfoBean bean = new TaskletInfoBean();
        bean.setName(info.getTaskName());
        // bean.setTaskletId
        // bean.setStartTime(info.getSubmitTime());
        // bean.setEndTime(info.getEndTIme());
        bean.setProgress(info.getProgress() / 100.0);
        bean.setTaskletId(jobId);
        bean.refresh();
        return bean;
    }

    public static AgentInfoBean convertAgentInfo(AgentInfo info) {
        AgentInfoBean bean = new AgentInfoBean();
        bean.setName(info.getHost() + " (" + info.getAgentID() + ")");
        bean.setProgressLine(info.getProgress());
        bean.refresh();
        return bean;
    }

    public static FeatureJobRequest convertFeatureJobRequestBean(
            FeatureJobRequestBean requestBean) {
        FeatureJobRequest request = new FeatureJobRequest();
        request.userId = requestBean.getUserId();
        request.featureId = requestBean.getFeatureId();
        request.variables = requestBean.getVariables();
        request.reproducable = requestBean.isReproducable();
        request.description = requestBean.getDescription();
        request.receivers = requestBean.getReceivers();
        if (request.receivers == null)
            request.receivers = new ArrayList<String>();
        return request;
    }

    public static UserInfo retrieveUserSession(
            HttpServletRequest httpServletRequest) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName() != null
                        && cookie.getName().equals(
                                Constants.CTS_CREDENTIAL_COOKIE)) {
                    String secret = cookie.getValue();
                    if (secret != null) {
                        return UserUtil.getSessionBindUser(secret);
                    }
                }
            }
        }
        return null;
    }

    public static String getCredentialCookie() throws Exception {
        Base64Encoder encoder = new Base64Encoder();
        MessageDigest algorithm = MessageDigest.getInstance("MD5");
        algorithm.reset();
        String plainString = Long.toString(System.currentTimeMillis())
                + Thread.currentThread().getId();
        algorithm.update(plainString.getBytes());
        return encoder.encode(algorithm.digest());
    }
    
    public static CedarSCMLogEntry convertSCMLogItem(SCMChangeSet item){
        CedarSCMLogEntry e = new CedarSCMLogEntry();
        e.setDateTime(item.getDateTime());
        e.setLogMsg(item.getLogMsg());
        e.setRev(item.getRev());
        e.setUser(item.getUser());
        e.refresh();
        return e;
    }
}
