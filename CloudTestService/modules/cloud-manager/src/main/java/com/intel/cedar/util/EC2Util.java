package com.intel.cedar.util;

import com.intel.cedar.core.entities.MachineTypeInfo;
import com.xerox.amazonws.ec2.AvailabilityZone;
import com.xerox.amazonws.ec2.ReservationDescription;

public class EC2Util {
    public static String getInstanceType(ReservationDescription.Instance i) {
        if (i.getInstanceType() != null)
            return i.getInstanceType().getTypeId();
        else
            return null;
    }

    public static String findNovaNode(AvailabilityZone zone) {
        String name = zone.getName();
        if (name != null && name.contains("|-") && !name.contains("| |-")) {
            return name.replace("|- ", "");
        } else {
            return null;
        }
    }

    public static boolean containTypeInfo(AvailabilityZone zone) {
        String name = zone.getName();
        if (name != null && name.contains("|-") && !name.equals("|- vm types")) {
            return true;
        } else {
            return false;
        }
    }

    public static void setTypeInfo(AvailabilityZone zone, MachineTypeInfo info) {
        String state = zone.getState();
        if (state != null) {
            String[] s = state.split(" +");
            info.setFree(Integer.parseInt(s[0]));
            info.setMax(Integer.parseInt(s[2]));
            info.setCpu(Integer.parseInt(s[3]));
            info.setMemory(Integer.parseInt(s[4]));
            info.setDisk(Integer.parseInt(s[5]));
            info.setSecondDisk(0);
            info.setType(zone.getName().replace("|- ", ""));
        }
    }
}
