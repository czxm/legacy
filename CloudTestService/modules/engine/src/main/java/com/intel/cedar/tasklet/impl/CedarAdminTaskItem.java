package com.intel.cedar.tasklet.impl;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.google.common.collect.Lists;
import com.intel.cedar.core.entities.VolumeInfo;
import com.intel.cedar.tasklet.ITaskItem;

public class CedarAdminTaskItem extends GenericTaskItem implements ITaskItem {
    public enum AdminTaskType {
        CreateVolume("CreateVolume"), AttachVolume("AttachVolume"), DetachVolume(
                "DetachVolume"), CreateDir("CreateDir"), DeleteDir("DeleteDir"), SetAdminPassword(
                "SetAdminPassword"), SyncDateTime("SyncDateTime"), RestartCedar(
                "RestartCedar"), StopCedar("StopCedar"), Reboot("Reboot"), UpdateDNS(
                "UpdateDNS");

        String type;
        String desc;
        String[] params;
        boolean exposed;

        AdminTaskType(String type, String desc, String[] params, boolean exposed) {
            this.type = type;
            this.desc = desc;
            this.params = params;
            this.exposed = exposed;
        }

        AdminTaskType(String type) {
            this.type = type;
            this.exposed = false;
        }

        public String getType() {
            return this.type;
        }

        public String getDescription() {
            return this.desc;
        }

        public String[] getParams() {
            return this.params;
        }

        public List<AdminTaskType> getExposedTasks() {
            List<AdminTaskType> result = Lists.newArrayList();
            for (AdminTaskType t : AdminTaskType.values()) {
                if (t.exposed)
                    result.add(t);
            }
            return result;
        }
    }

    private static final long serialVersionUID = -3171820306384315768L;
    private AdminTaskType taskType;

    protected void setCreateDirTaskItem(Object[] params) {
        if (params.length != 2 || !(params[0] instanceof Boolean)
                || !(params[1] instanceof String))
            throw new RuntimeException("invalid parameters");
        boolean isWindows = (Boolean) params[0];
        String path = (String) params[1];
        String command = String.format("mkdir %s %s", isWindows ? "" : "-m 777 -p",
                path);
        this.setValue(command);
    }

    protected void setDeleteDirTaskItem(Object[] params) {
        if (params.length != 2 || !(params[0] instanceof Boolean)
                || !(params[1] instanceof String))
            throw new RuntimeException("invalid parameters");
        boolean isWindows = (Boolean) params[0];
        String path = (String) params[1];
        String command = String.format("%s %s %s", isWindows ? "rd" : "rm",
                isWindows ? "/S /Q" : "-rf", path);
        this.setValue(command);
    }

    protected void setCreateVolumeTaskItem(Object[] params) {
        if (params.length != 2 || !(params[0] instanceof Boolean)
                || !(params[1] instanceof VolumeInfo))
            throw new RuntimeException("invalid parameters");
        boolean isWindows = (Boolean) params[0];
        VolumeInfo v = (VolumeInfo) params[1];
        String command = String.format("createVolume.%s %s %s",
                isWindows ? "vbs" : "sh", isWindows ? v.getDeviceIndex() + 1
                        : v.getDeviceName(), v.getPath());
        this.setValue(command);
        this.setProperty("script", String.format("createVolume.%s",
                isWindows ? "vbs" : "sh"));
    }

    protected void setAttachVolumeTaskItem(Object[] params) {
        if (params.length != 2 || !(params[0] instanceof Boolean)
                || !(params[1] instanceof VolumeInfo))
            throw new RuntimeException("invalid parameters");
        boolean isWindows = (Boolean) params[0];
        VolumeInfo v = (VolumeInfo) params[1];
        String command = String.format("attachVolume.%s %s %s",
                isWindows ? "vbs" : "sh", isWindows ? v.getDeviceIndex() + 1
                        : v.getDeviceName(), v.getPath());
        this.setValue(command);
        this.setProperty("script", String.format("attachVolume.%s",
                isWindows ? "vbs" : "sh"));
    }

    protected void setDetachVolumeTaskItem(Object[] params) {
        if (params.length != 2 || !(params[0] instanceof Boolean)
                || !(params[1] instanceof VolumeInfo))
            throw new RuntimeException("invalid parameters");
        boolean isWindows = (Boolean) params[0];
        VolumeInfo v = (VolumeInfo) params[1];
        String command = String.format("detachVolume.%s %s %s",
                isWindows ? "vbs" : "sh", isWindows ? v.getDeviceIndex() + 1
                        : v.getDeviceName(), v.getPath());
        this.setValue(command);
        this.setProperty("script", String.format("detachVolume.%s",
                isWindows ? "vbs" : "sh"));
    }

    protected void setSetAdminPasswordTaskItem(Object[] params) {
        if (params.length != 2 || !(params[0] instanceof Boolean)
                || !(params[1] instanceof String))
            throw new RuntimeException("invalid parameters");
        boolean isWindows = (Boolean) params[0];
        String passwd = (String) params[1];
        String command = String.format("%s %s",
                isWindows ? "net user administrator" : "set-passwd.sh", passwd);
        this.setValue(command);
        this.setProperty("script", "set-passwd.sh");
    }

    protected void setSyncDateTimeTaskItem(Object[] params) {
        if (params.length != 1 || !(params[0] instanceof Boolean))
            throw new RuntimeException("invalid parameters");
        boolean isWindows = (Boolean) params[0];
        TimeZone timeZone = TimeZone.getDefault();
        String timeZoneId = timeZone.getID(); // for *nix
        String timeZoneName = timeZone.getDisplayName(Locale.US); // for Windoz
        Date now = new Date();
        String time = new SimpleDateFormat("HH:mm:ss").format(now);
        String date = new SimpleDateFormat("MM/dd/yyyy").format(now);
        String command = String.format("setDateTime.%s \"%s\" %s %s",
                isWindows ? "vbs" : "sh",
                isWindows ? timeZoneName : timeZoneId, date, time);
        this.setValue(command);
        this.setProperty("script", String.format("setDateTime.%s",
                isWindows ? "vbs" : "sh"));
    }

    protected void setRestartCedarTaskItem(Object[] params) {
        if (params.length != 1 || !(params[0] instanceof Boolean))
            throw new RuntimeException("invalid parameters");
        boolean isWindows = (Boolean) params[0];
        String command = String.format("restartCedar.%s", isWindows ? "vbs"
                : "sh");
        this.setValue(command);
        this.setProperty("script", command);
    }

    protected void setStopCedarTaskItem(Object[] params) {
        if (params.length != 1 || !(params[0] instanceof Boolean))
            throw new RuntimeException("invalid parameters");
        boolean isWindows = (Boolean) params[0];
        String command = String
                .format("stopCedar.%s", isWindows ? "vbs" : "sh");
        this.setValue(command);
        this.setProperty("script", command);
    }

    protected void setRebootTaskItem(Object[] params) {
        if (params.length != 1 || !(params[0] instanceof Boolean))
            throw new RuntimeException("invalid parameters");
        boolean isWindows = (Boolean) params[0];
        String command = String.format("%s", isWindows ? "reboot.vbs"
                : "reboot");
        this.setValue(command);
        this.setProperty("script", "reboot.vbs");
    }

    protected void setUpdateDNSTaskItem(Object[] params) {
        if (params.length != 1 || !(params[0] instanceof String))
            throw new RuntimeException("invalid parameters");
        String command = "updateDNS.sh " + (String) params[0];
        this.setValue(command);
        this.setProperty("script", "updateDNS.sh");
    }

    public CedarAdminTaskItem(AdminTaskType type) {
        this.taskType = type;
    }

    public AdminTaskType getTaskType() {
        return this.taskType;
    }

    public void setTaskParameters(Object[] params) {
        String methodName = "set" + taskType.name() + "TaskItem";
        try {
            Method m = getClass().getDeclaredMethod(methodName, Object[].class);
            m.invoke(this, new Object[] { params });
        } catch (Throwable e) {
            this.setValue("failed to set task parameters");
        }
    }
}
