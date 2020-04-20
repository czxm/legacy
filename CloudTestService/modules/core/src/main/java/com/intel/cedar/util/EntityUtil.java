package com.intel.cedar.util;

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.intel.cedar.core.entities.CloudInfo;
import com.intel.cedar.core.entities.CloudNodeInfo;
import com.intel.cedar.core.entities.GatewayInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.KeyPairDescription;
import com.intel.cedar.core.entities.MachineInfo;
import com.intel.cedar.core.entities.MachineTypeInfo;
import com.intel.cedar.core.entities.NATInfo;
import com.intel.cedar.core.entities.PhysicalNodeInfo;
import com.intel.cedar.core.entities.VolumeInfo;
import com.intel.cedar.engine.FeaturePropsInfo;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.tasklet.TaskletInfo;
import com.intel.cedar.user.SessionInfo;
import com.intel.cedar.user.UserInfo;

public class EntityUtil {
    public static List<InstanceInfo> listInstances(
            EntityWrapper<InstanceInfo> db, CloudInfo cloud) {
        InstanceInfo e = new InstanceInfo();
        if (cloud != null)
            e.setCloudId(cloud.getId());
        return db.query(e);
    }

    public static List<VolumeInfo> listVolumes(EntityWrapper<VolumeInfo> db,
            CloudInfo cloud) {
        VolumeInfo e = new VolumeInfo();
        if (cloud != null)
            e.setCloudId(cloud.getId());
        return db.query(e);
    }

    public static List<MachineInfo> listMachines(EntityWrapper<MachineInfo> db,
            CloudInfo cloud) {
        MachineInfo e = new MachineInfo();
        if (cloud != null)
            e.setCloudId(cloud.getId());
        return db.query(e);
    }

    public static List<MachineTypeInfo> listMachineTypes(
            EntityWrapper<MachineTypeInfo> db, CloudInfo cloud) {
        MachineTypeInfo e = new MachineTypeInfo();
        if (cloud != null)
            e.setCloudId(cloud.getId());
        return db.query(e);
    }

    public static List<KeyPairDescription> listKeyPairs(
            EntityWrapper<KeyPairDescription> db, CloudInfo cloud) {
        KeyPairDescription e = new KeyPairDescription();
        if (cloud != null)
            e.setCloudId(cloud.getId());
        return db.query(e);
    }

    public static List<CloudNodeInfo> listCloudNodes(
            EntityWrapper<CloudNodeInfo> db, CloudInfo cloud) {
        CloudNodeInfo e = new CloudNodeInfo();
        if (cloud != null)
            e.setCloudId(cloud.getId());
        return db.query(e);
    }

    public static List<CloudInfo> listClouds() {
        EntityWrapper<CloudInfo> db = new EntityWrapper<CloudInfo>();
        try {
            return db.query(new CloudInfo());
        } finally {
            db.rollback();
        }
    }

    public static List<InstanceInfo> listInstances(CloudInfo cloud) {
        EntityWrapper<InstanceInfo> db = new EntityWrapper<InstanceInfo>();
        try {
            return listInstances(db, cloud);
        } finally {
            db.rollback();
        }
    }

    public static List<VolumeInfo> listVolumes(CloudInfo cloud) {
        EntityWrapper<VolumeInfo> db = getVolumeEntityWrapper();
        try {
            return listVolumes(db, cloud);
        } finally {
            db.rollback();
        }
    }

    public static List<MachineInfo> listMachines(CloudInfo cloud) {
        EntityWrapper<MachineInfo> db = new EntityWrapper<MachineInfo>();
        try {
            return listMachines(db, cloud);
        } finally {
            db.rollback();
        }
    }

    public static List<MachineTypeInfo> listMachineTypes(CloudInfo cloud) {
        EntityWrapper<MachineTypeInfo> db = new EntityWrapper<MachineTypeInfo>();
        try {
            return listMachineTypes(db, cloud);
        } finally {
            db.rollback();
        }
    }

    public static List<KeyPairDescription> listKeyPairs(CloudInfo cloud) {
        EntityWrapper<KeyPairDescription> db = new EntityWrapper<KeyPairDescription>();
        try {
            return listKeyPairs(db, cloud);
        } finally {
            db.rollback();
        }
    }

    public static List<GatewayInfo> listGateways() {
        EntityWrapper<GatewayInfo> db = new EntityWrapper<GatewayInfo>();
        try {
            return db.query(new GatewayInfo());
        } finally {
            db.rollback();
        }
    }

    public static List<PhysicalNodeInfo> listPhysicalNodes() {
        EntityWrapper<PhysicalNodeInfo> db = new EntityWrapper<PhysicalNodeInfo>();
        try {
            return db.query(new PhysicalNodeInfo());
        } finally {
            db.rollback();
        }
    }

    public static List<CloudNodeInfo> listCloudNodes() {
        EntityWrapper<CloudNodeInfo> db = new EntityWrapper<CloudNodeInfo>();
        try {
            return db.query(new CloudNodeInfo());
        } finally {
            db.rollback();
        }
    }

    public static EntityWrapper<InstanceInfo> getInstanceEntityWrapper() {
        return new EntityWrapper<InstanceInfo>();
    }

    public static EntityWrapper<CloudInfo> getCloudEntityWrapper() {
        return new EntityWrapper<CloudInfo>();
    }

    public static EntityWrapper<MachineInfo> getMachineEntityWrapper() {
        return new EntityWrapper<MachineInfo>();
    }

    public static EntityWrapper<PhysicalNodeInfo> getPhysicalNodeEntityWrapper() {
        return new EntityWrapper<PhysicalNodeInfo>();
    }

    public static EntityWrapper<CloudNodeInfo> getCloudNodeEntityWrapper() {
        return new EntityWrapper<CloudNodeInfo>();
    }

    public static EntityWrapper<GatewayInfo> getGatewayEntityWrapper() {
        return new EntityWrapper<GatewayInfo>();
    }

    public static EntityWrapper<NATInfo> getNATEntityWrapper() {
        return new EntityWrapper<NATInfo>();
    }

    public static EntityWrapper<MachineTypeInfo> getMachineTypeEntityWrapper() {
        return new EntityWrapper<MachineTypeInfo>();
    }

    public static EntityWrapper<KeyPairDescription> getKeyPairEntityWrapper() {
        return new EntityWrapper<KeyPairDescription>();
    }

    public static EntityWrapper<VolumeInfo> getVolumeEntityWrapper() {
        return new EntityWrapper<VolumeInfo>();
    }

    public static EntityWrapper<UserInfo> getUserEntityWrapper() {
        return new EntityWrapper<UserInfo>();
    }

    public static EntityWrapper<SessionInfo> getSessionEntityWrapper() {
        return new EntityWrapper<SessionInfo>();
    }

    public static EntityWrapper<FeatureInfo> getFeatureEntityWrapper() {
        return new EntityWrapper<FeatureInfo>();
    }

    public static EntityWrapper<TaskletInfo> getTaskletEntityWrapper() {
        return new EntityWrapper<TaskletInfo>();
    }

    public static EntityWrapper<FeaturePropsInfo> getFeaturePropsEntityWrapper() {
        return new EntityWrapper<FeaturePropsInfo>();
    }

    public static void executeSQL(EntityWrapper<?> entity, String sql) {
        Session ses = entity.getSession();
        Transaction trans = ses.beginTransaction();
        trans.begin();
        SQLQuery query = ses.createSQLQuery(sql);
        query.executeUpdate();
        trans.commit();
    }
}
