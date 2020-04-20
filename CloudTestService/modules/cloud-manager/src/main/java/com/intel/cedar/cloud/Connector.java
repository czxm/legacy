package com.intel.cedar.cloud;

import java.net.URI;
import java.util.List;

import com.intel.cedar.core.entities.CloudNodeInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.KeyPairDescription;
import com.intel.cedar.core.entities.MachineInfo;
import com.intel.cedar.core.entities.MachineTypeInfo;
import com.intel.cedar.core.entities.VolumeInfo;

public interface Connector {
    public boolean testConnection();

    public boolean isLiveMigrationSupported();

    public boolean isElasticStorageSupported();

    public List<MachineInfo> getMachines(List<String> mid)
            throws CloudException;

    public List<MachineTypeInfo> getMachineTypes() throws CloudException;

    public List<InstanceInfo> getInstances() throws CloudException;

    public List<KeyPairDescription> getKeyPairs() throws CloudException;

    public List<InstanceInfo> runInstances(MachineInfo machine,
            MachineTypeInfo machineType, int count) throws CloudException;

    public String getInstanceAddress(InstanceInfo instance)
            throws CloudException;

    public String getInstanceHostname(InstanceInfo instance)
            throws CloudException;

    public String getInstancePublicAddress(InstanceInfo instance)
            throws CloudException;

    public String getInstanceKeyName(InstanceInfo instance)
            throws CloudException;

    public boolean instanceReady(InstanceInfo instance) throws CloudException;

    public boolean terminateInstances(List<InstanceInfo> instances)
            throws CloudException;

    public boolean rebootInstances(List<InstanceInfo> instances)
            throws CloudException;

    public String getConsoleOutput(InstanceInfo instance) throws CloudException;

    public List<VolumeInfo> getVolumes() throws CloudException;

    public VolumeInfo createVolume(int size) throws CloudException;

    public boolean attachVolume(InstanceInfo instance, VolumeInfo volume)
            throws CloudException;

    public boolean detachVolume(InstanceInfo instance, VolumeInfo volume)
            throws CloudException;

    public boolean deleteVolume(VolumeInfo volume) throws CloudException;

    public List<CloudNodeInfo> getCloudNodes() throws CloudException;

    public URI getInstanceDisplay(List<CloudNodeInfo> nodes,
            InstanceInfo instance);
    
    public String allocateAddress() throws CloudException;
    
    public boolean associateAddress(InstanceInfo instance, String addr) throws CloudException;
    
    public boolean disassociateAddress(String addr) throws CloudException;
    
    public boolean releaseAddress(String addr) throws CloudException;
}
