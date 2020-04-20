package com.intel.cedar.cloud.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.intel.cedar.cloud.CloudException;
import com.intel.cedar.cloud.Connector;
import com.intel.cedar.cloud.InvalidInstanceException;
import com.intel.cedar.cloud.UnsupportedCloudException;
import com.intel.cedar.cloud.VolumeCreationException;
import com.intel.cedar.core.CedarException;
import com.intel.cedar.core.entities.CloudInfo;
import com.intel.cedar.core.entities.CloudNodeInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.KeyPairDescription;
import com.intel.cedar.core.entities.MachineInfo;
import com.intel.cedar.core.entities.MachineTypeInfo;
import com.intel.cedar.core.entities.VolumeInfo;
import com.intel.cedar.util.EC2Util;
import com.xerox.amazonws.ec2.AttachmentInfo;
import com.xerox.amazonws.ec2.AvailabilityZone;
import com.xerox.amazonws.ec2.EC2Exception;
import com.xerox.amazonws.ec2.GroupDescription;
import com.xerox.amazonws.ec2.ImageDescription;
import com.xerox.amazonws.ec2.InstanceType;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.ReservationDescription;

public class EC2Connector implements Connector {
    private static Logger LOG = LoggerFactory.getLogger(EC2Connector.class);
    protected int WAIT_COUNT = 10;
    protected Jec2 ec2;
    protected CloudInfo cloud;

    public EC2Connector(CloudInfo cloud) {
        this.cloud = cloud;
        boolean secured = cloud.getSecured();
        String accessKey = cloud.getParam1();
        String secretKey = cloud.getParam2();
        ec2 = new Jec2(accessKey, secretKey, secured, cloud.getHost(), cloud
                .getPort());
        ec2.setSoTimeout(10000);
        ec2.setConnectionTimeout(5000);
        ec2.setResourcePrefix(cloud.getService());
        if (cloud.getProxyHost() != null && cloud.getProxyPort() != null) {
            if (cloud.getProxyAuth() == null)
                ec2.setProxyValues(cloud.getProxyHost(), Integer.parseInt(cloud
                        .getProxyPort()));
            else
                ec2.setProxyValues(cloud.getProxyHost(), Integer.parseInt(cloud
                        .getProxyPort()), cloud.getProxyAuth(), cloud
                        .getProxyPasswd());
        }
    }

    protected void handleException(Exception e) throws CloudException {
        LOG.info(e.getMessage());
        throw new CloudException(e.getMessage(), e);
    }

    protected String getOwnerId() throws CloudException {
        if (ec2 != null) {
            try {
                List<GroupDescription> groups = ec2
                        .describeSecurityGroups(new ArrayList<String>());
                if (groups != null && groups.size() > 0) {
                    return groups.get(0).getOwner();
                }
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean testConnection() {
        if (ec2 != null) {
            try {
                ec2.describeAvailabilityZones(new ArrayList<String>());
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean attachVolume(InstanceInfo instance, VolumeInfo volume)
            throws CloudException {
        try {
            ec2.attachVolume(volume.getImageId(), instance.getInstanceId(),
                    volume.getDeviceName());
            checkAvailability(volume, "in-use");
            return true;
        } catch (EC2Exception e) {
            handleException(e);
        } catch (CloudException e) {
            throw e;
        }
        return false;
    }

    @Override
    public boolean detachVolume(InstanceInfo instance, VolumeInfo volume)
            throws CloudException {
        try {
            if (instance == null) {
                ec2.detachVolume(volume.getImageId(), null, null, true);
            } else {
                ec2.detachVolume(volume.getImageId(), instance.getInstanceId(),
                        null, true);
            }
            checkAvailability(volume, "available");
            return true;
        } catch (EC2Exception e) {
            handleException(e);
        } catch (CloudException e) {
            throw e;
        }
        return false;
    }

    @Override
    public boolean deleteVolume(VolumeInfo volume) throws CloudException {
        try {
            ec2.deleteVolume(volume.getImageId());
            return true;
        } catch (EC2Exception e) {
            handleException(e);
        }
        return false;
    }

    @Override
    public List<InstanceInfo> getInstances() throws CloudException {
        List<InstanceInfo> result = Lists.newArrayList();
        if (ec2 != null) {
            try {
                List<ReservationDescription> descs = ec2
                        .describeInstances(new ArrayList<String>());
                for (ReservationDescription desc : descs) {
                    for (ReservationDescription.Instance instance : desc
                            .getInstances()) {
                        try {
                            InstanceInfo i = new InstanceInfo();
                            i.setCloudId(cloud.getId());
                            i.setInstanceId(instance.getInstanceId());
                            i.setHost(instance.getDnsName());
                            i.setPrivateIp(instance.getPrivateIpAddress());
                            i.setMachineId(cloud.findMachineId(instance
                                    .getImageId()));
                            i.setTypeId(cloud.findTypeId(EC2Util
                                    .getInstanceType(instance)));
                            i.setKeyName(instance.getKeyName());
                            i.setState(instance.getState());
                            result.add(i);
                        } catch (CedarException e1) {
                        }
                    }
                }
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return result;
    }

    @Override
    public List<MachineInfo> getMachines(List<String> mid)
            throws CloudException {
        List<String> idList = Lists.newArrayList();
        if (mid != null && mid.size() > 0) {
            for (String i : mid) {
                idList.add(i);
            }
        }
        List<MachineInfo> result = Lists.newArrayList();
        if (ec2 != null) {
            try {
                List<ImageDescription> descs;
                if (idList.size() > 0) {
                    descs = ec2.describeImages(idList);
                } else {
                    String owner = getOwnerId();
                    if (owner != null)
                        descs = ec2.describeImagesByOwner(Lists
                                .newArrayList(owner));
                    else
                        descs = ec2.describeImages(idList);
                }
                for (ImageDescription desc : descs) {
                    if ("machine".equals(desc.getImageType())) {
                        MachineInfo i = new MachineInfo();
                        i.setCloudId(cloud.getId());
                        i.setImageId(desc.getImageId());
                        i.setImageName(desc.getImageLocation());
                        i.setEnabled(false);
                        result.add(i);
                    }
                }
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return result;
    }

    @Override
    public List<MachineTypeInfo> getMachineTypes() throws CloudException {
        return Lists.newArrayList();
    }

    @Override
    public List<KeyPairDescription> getKeyPairs() throws CloudException {
        // TODO Auto-generated method stub
        List<KeyPairDescription> result = new ArrayList<KeyPairDescription>();
        if (ec2 != null) {
            try {
                List<KeyPairInfo> infos = ec2
                        .describeKeyPairs(new ArrayList<String>());
                for (KeyPairInfo info : infos) {
                    KeyPairDescription des = new KeyPairDescription();
                    des.setCloudId(cloud.getId());
                    des.setKeyName(info.getKeyName());
                    des.setKeyFingerPrint(info.getKeyFingerprint());
                    result.add(des);
                }
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return result;
    }

    @Override
    public String getInstanceAddress(InstanceInfo instance)
            throws CloudException {
        if (ec2 != null) {
            try {
                List<ReservationDescription> descs = ec2
                        .describeInstances(Lists.newArrayList(instance
                                .getInstanceId()));
                for (ReservationDescription desc : descs) {
                    for (ReservationDescription.Instance i : desc
                            .getInstances()) {
                        return i.getPrivateIpAddress();
                    }
                }
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return "";
    }

    @Override
    public String getInstanceHostname(InstanceInfo instance)
            throws CloudException {
        if (ec2 != null) {
            try {
                List<ReservationDescription> descs = ec2
                        .describeInstances(Lists.newArrayList(instance
                                .getInstanceId()));
                for (ReservationDescription desc : descs) {
                    for (ReservationDescription.Instance i : desc
                            .getInstances()) {
                        return i.getPrivateDnsName();
                    }
                }
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return "";
    }

    @Override
    public String getInstancePublicAddress(InstanceInfo instance)
            throws CloudException {
        if (ec2 != null) {
            try {
                List<ReservationDescription> descs = ec2
                        .describeInstances(Lists.newArrayList(instance
                                .getInstanceId()));
                for (ReservationDescription desc : descs) {
                    for (ReservationDescription.Instance i : desc
                            .getInstances()) {
                        return i.getDnsName();
                    }
                }
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return "";
    }

    @Override
    public String getInstanceKeyName(InstanceInfo instance)
            throws CloudException {
        if (ec2 != null) {
            try {
                List<ReservationDescription> descs = ec2
                        .describeInstances(Lists.newArrayList(instance
                                .getInstanceId()));
                for (ReservationDescription desc : descs) {
                    for (ReservationDescription.Instance i : desc
                            .getInstances()) {
                        return i.getKeyName();
                    }
                }
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return "";
    }

    protected String getInstanceStatus(String instanceId) throws CloudException {
        if (ec2 != null) {
            try {
                List<ReservationDescription> descs = ec2
                        .describeInstances(Lists.newArrayList(instanceId));
                for (ReservationDescription desc : descs) {
                    for (ReservationDescription.Instance instance : desc
                            .getInstances()) {
                        if (instance.getState().equals("terminated")
                                || instance.getState().equals("shutting-down"))
                            throw new InvalidInstanceException(
                                    "instance is terminated");
                        return instance.getState();
                    }
                }
                throw new InvalidInstanceException(
                        "instance not exists, maybe terminated");
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return "";
    }

    @Override
    public boolean instanceReady(InstanceInfo instance) throws CloudException {
        String instanceId = instance.getInstanceId();

        try {
            if (ec2 != null && getInstanceStatus(instanceId).equals("running")) {
                return true;
            }
        } catch (CloudException e) {
            handleException(e);
        }
        return false;
    }

    @Override
    public List<InstanceInfo> runInstances(MachineInfo machine,
            MachineTypeInfo machineType, int count) throws CloudException {
        List result = new ArrayList<InstanceInfo>();
        if (ec2 != null) {
            try {
                String imageId = machine.getImageId();
                InstanceType type = InstanceType.getTypeFromString(machineType
                        .getType());
                ReservationDescription desc = ec2.runInstances(imageId, count,
                        count, new ArrayList<String>(), null, null, type);
                for (ReservationDescription.Instance instance : desc
                        .getInstances()) {
                    InstanceInfo i = new InstanceInfo();
                    i.setCloudId(cloud.getId());
                    i.setInstanceId(instance.getInstanceId());
                    i.setHost(instance.getDnsName());
                    i.setPrivateIp(instance.getPrivateIpAddress());
                    i.setMachineId(machine.getId());
                    i.setTypeId(machineType.getId());
                    i.setState(instance.getState());
                    i.setKeyName(instance.getKeyName());
                    i.setCreationTime(System.currentTimeMillis());
                    result.add(i);
                }
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return result;
    }

    @Override
    public boolean terminateInstances(List<InstanceInfo> instances)
            throws CloudException {
        if (ec2 != null) {
            try {
                List<String> instanceIds = new ArrayList<String>();
                for (InstanceInfo instance : instances) {
                    instanceIds.add(instance.getInstanceId());
                }
                // ec2.setServerTimeZone(TimeZone.getDefault());
                ec2.terminateInstances(instanceIds);
                return true;
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return false;
    }

    @Override
    public boolean rebootInstances(List<InstanceInfo> instances)
            throws CloudException {
        if (ec2 != null) {
            try {
                List<String> instanceIds = new ArrayList<String>();
                for (InstanceInfo instance : instances) {
                    instanceIds.add(instance.getInstanceId());
                }
                ec2.rebootInstances(instanceIds);
                return true;
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return false;
    }

    protected String getZoneName() throws CloudException {
        if (ec2 != null) {
            try {
                List<AvailabilityZone> descs = ec2
                        .describeAvailabilityZones(Lists
                                .newArrayList("verbose"));
                for (AvailabilityZone desc : descs) {
                	String name = desc.getName();
                	if(!name.startsWith("|") && !name.equals("internal"))
                		return  name;
                }
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return null;
    }

    protected boolean checkAvailability(VolumeInfo volume, String checkMsg)
            throws CloudException {
        try {
            int wait = 0;
            while (true) {
                List<com.xerox.amazonws.ec2.VolumeInfo> states = ec2
                        .describeVolumes(new String[] { volume.getImageId() });
                for (com.xerox.amazonws.ec2.VolumeInfo info : states) {
                    if (info.getVolumeId().equals(volume.getImageId())) {
                    	String status = info.getStatus();
                        if (status.startsWith(checkMsg)) {
                            if (checkMsg.startsWith("available")) // detach
                                return true;
                            else if (checkMsg.startsWith("in-use")) { // attach
                                for (AttachmentInfo i : info
                                        .getAttachmentInfo()) {
                                    if ("attached".startsWith(i.getStatus())) {
                                        return true;
                                    }
                                }
                            }
                        }
                        else if(status.startsWith("error")){
                            throw new CloudException(
                                    "Cloud did not respond successfully");
                        }
                    }
                }
                if (wait < WAIT_COUNT)
                    TimeUnit.SECONDS.sleep(5);
                else
                    throw new CloudException(
                            "Cloud did not respond successfully");
                wait++;
            }
        } catch (Exception e) {
            handleException(e);
        }
        return false;
    }

    @Override
    public VolumeInfo createVolume(int size) throws CloudException {
        String zoneName = getZoneName();
        if (zoneName == null || ec2 == null) {
            throw new VolumeCreationException();
        }
        try {
            com.xerox.amazonws.ec2.VolumeInfo volume = ec2.createVolume(String
                    .format("%d", size), null, zoneName);
            String volumeId = volume.getVolumeId();
            Integer volumeSize = Integer.parseInt(volume.getSize());
            VolumeInfo volumeInfo = new VolumeInfo();
            volumeInfo.setCloudId(cloud.getId());
            volumeInfo.setImageId(volumeId);
            volumeInfo.setSize(volumeSize);
            volumeInfo.setCreationTime(System.currentTimeMillis());
            checkAvailability(volumeInfo, "available");
            return volumeInfo;
        } catch (EC2Exception e) {
            handleException(e);
        }
        throw new VolumeCreationException();
    }

    @Override
    public String getConsoleOutput(InstanceInfo instance) throws CloudException {
        if (ec2 == null) {
            return "";
        } else {
            try {
                return ec2.getConsoleOutput(instance.getInstanceId())
                        .getOutput();
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return "";
    }

    @Override
    public List<VolumeInfo> getVolumes() throws CloudException {
        List<VolumeInfo> volumes = new ArrayList<VolumeInfo>();
        try {
            List<com.xerox.amazonws.ec2.VolumeInfo> states = ec2
                    .describeVolumes(new String[] {});
            for (com.xerox.amazonws.ec2.VolumeInfo info : states) {
                VolumeInfo v = new VolumeInfo();
                v.setCloudId(cloud.getId());
                v.setImageId(info.getVolumeId());
                v.setSize(Integer.parseInt(info.getSize()));
                v.setAttachedCount(0);
                v.setDeviceIndex(-1);
                v.setPath("");
                if (info.getStatus().equals("available")) {
                    v.setAttached(null);
                } else {
                    for (AttachmentInfo i : info.getAttachmentInfo()) {
                        if ("attached".equals(i.getStatus())) {
                            v.setAttachedInstanceId(i.getInstanceId());
                            break;
                        }
                    }
                }
                volumes.add(v);
            }
        } catch (EC2Exception e) {
            handleException(e);
        }
        return volumes;
    }
    
    @Override
    public String allocateAddress() throws CloudException{
        if (ec2 != null) {
            try {
                return ec2.allocateAddress();
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return null;    
    }

    @Override
    public boolean releaseAddress(String addr) throws CloudException{
        if (ec2 != null) {
            try {
                ec2.releaseAddress(addr);
                return true;
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return false;    
    }
    
    @Override
    public boolean associateAddress(InstanceInfo instance, String addr) throws CloudException{
        if (ec2 != null) {
            try {
                ec2.associateAddress(instance.getInstanceId(), addr);
                return true;
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return false;    
    }

    
    @Override
    public boolean disassociateAddress(String addr) throws CloudException{
        if (ec2 != null) {
            try {
                ec2.disassociateAddress(addr);
                return true;
            } catch (EC2Exception e) {
                handleException(e);
            }
        }
        return false;    
    }
    
    public static void main(String[] args) {
        String ACCESS_KEY_UEC_SERVER = "ed5a15a43c184798a00ac518643c7e10";
        String SECRET_KEY_UEC_SERVER = "0f0bc901269743809804d378ba8d18e0";

        CloudInfo cloud = new CloudInfo();
        cloud.setSeperated(false);
        cloud.setSecured(false);
        cloud.setEnabled(true);
        cloud.setName("UEC-CLOUD");
        cloud.setProtocol("OpenStack");
        cloud.setService("/services/Cloud");
        cloud.setHost("cts-gateway.sh.intel.com");
        cloud.setPort(19998);
        cloud.setParam1(ACCESS_KEY_UEC_SERVER);
        cloud.setParam2(SECRET_KEY_UEC_SERVER);
        InstanceInfo instance = new InstanceInfo();
        instance.setInstanceId("i-00000019");
        try {
            EC2Connector conn = new OpenStackConnector(cloud);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public List<CloudNodeInfo> getCloudNodes() throws CloudException {
        throw new UnsupportedCloudException();
    }

    @Override
    public URI getInstanceDisplay(List<CloudNodeInfo> nodes,
            InstanceInfo instance) {
        return URI.create("unknown://unreachable");
    }

    @Override
    public boolean isElasticStorageSupported() {
        return true;
    }

    @Override
    public boolean isLiveMigrationSupported() {
        return false;
    }
}
