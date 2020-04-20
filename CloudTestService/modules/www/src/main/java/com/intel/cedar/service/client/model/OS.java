package com.intel.cedar.service.client.model;

public enum OS {
    as5, sles10, ubuntu, openSolaris, win2k3, winxp, win2k8, win7, centos, as6, sles11, unix;

    public String osName() {
        if (this.name().equals("unix")) {
            return "Generic Linux/Unix";
        } else if (this.name().equals("as5")) {
            return "RedHat Advanced Server 5";
        } else if (this.name().equals("as6")) {
            return "RedHat Advanced Server 6";
        } else if (this.name().equals("centos")) {
            return "CentOS";
        } else if (this.name().equals("sles10")) {
            return "SuSE Linux Enterprise Server 10";
        } else if (this.name().equals("sles11")) {
            return "SuSE Linux Enterprise Server 11";
        } else if (this.name().equals("ubuntu")) {
            return "Ubuntu Linux";
        } else if (this.name().equals("openSolaris")) {
            return "Oracle OpenSolaris";
        } else if (this.name().equals("win2k3")) {
            return "Microsoft Windows Server 2003";
        } else if (this.name().equals("winxp")) {
            return "Microsoft Windows XP";
        } else if (this.name().equals("win2k8")) {
            return "Microsoft Windows Server 2008";
        } else if (this.name().equals("win7")) {
            return "Microsoft Windows 7";
        }

        return "";
    }

    public boolean isWindows() {
        if (this.name().equals("win2k3") || this.name().equals("winxp")
                || this.name().equals("win2k8") || this.name().equals("win7"))
            return true;
        else
            return false;
    }

    public static OS fromString(String v) {
        try {
            if (v != null && !v.equals(""))
                return OS.valueOf(v);
        } catch (Exception e) {
            for (OS os : OS.values()) {
                if (os.osName().equals(v))
                    return os;
            }
            if (v.equals("windows"))
                return win2k3;
            else if (v.equals("linux"))
                return as5;
        }
        return null;
    }
}
