package com.intel.cedar.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.commons.codec.net.URLCodec;

public class NetUtil {    
    private static InetAddress getFirstNonLoopbackAddress(boolean preferIpv4,
            boolean preferIPv6) throws Exception {
        Enumeration en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = (NetworkInterface) en.nextElement();
            if(i.getName().startsWith("vnet") || i.getName().startsWith("vir"))
                continue;
            for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements();) {
                InetAddress addr = (InetAddress) en2.nextElement();
                if (!addr.isLoopbackAddress()) {
                    if (addr instanceof Inet4Address) {
                        if (preferIPv6) {
                            continue;
                        }
                        return addr;
                    }
                    if (addr instanceof Inet6Address) {
                        if (preferIpv4) {
                            continue;
                        }
                        return addr;
                    }
                }
            }
        }
        return null;
    }

    private static InetAddress getAddressByInterface(String inf,
            boolean preferIpv4, boolean preferIPv6) throws Exception {
        Enumeration en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = (NetworkInterface) en.nextElement();
            if (!inf.equals(i.getName()))
                continue;
            for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements();) {
                InetAddress addr = (InetAddress) en2.nextElement();
                if (!addr.isLoopbackAddress()) {
                    if (addr instanceof Inet4Address) {
                        if (preferIPv6) {
                            continue;
                        }
                        return addr;
                    }
                    if (addr instanceof Inet6Address) {
                        if (preferIpv4) {
                            continue;
                        }
                        return addr;
                    }
                }
            }
        }
        return null;
    }

    public static String getHostAddress(String intf) {
        InetAddress inet = null;
        try {
            if (intf != null)
                inet = getAddressByInterface(intf, true, false);
            else
                inet = getFirstNonLoopbackAddress(true, false);
            return inet.getHostAddress();
        } catch (Exception e) {
            return "0.0.0.0";
        }
    }

    public static String getHostName(String intf) {
        InetAddress inet = null;
        try {
            if (intf != null)
                inet = getAddressByInterface(intf, true, false);
            else
                inet = getFirstNonLoopbackAddress(true, false);
            return inet.getCanonicalHostName();
        } catch (Exception e) {
            try {
                return inet.getHostName();
            } catch (Exception e1) {
                if (inet == null) {
                    return "0.0.0.0";
                } else {
                    return inet.getHostAddress();
                }
            }
        }
    }

    public static String getHostName() {
        return getHostName(CedarConfiguration.getInstance().getInterface());
    }
    
    public static void main(String[]  args) throws Exception{
        getFirstNonLoopbackAddress(true, false);
    }
}
