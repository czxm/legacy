package com.intel.cedar.jws.viewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import vncviewer.VNCViewer;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.LocalPortForwarder;

public class Viewer {
    private static void launch(String[] cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(cmd);
            Process p = pb.start();
            InputStream is = p.getInputStream();
            byte[] buf = new byte[1024];
            int n = -1;
            while ((n = is.read(buf)) != -1) {
                System.out.write(buf, 0, n);
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static void launchRDP(String destHost, String destPort) {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                launch(new String[] { "mstsc",
                        "/v:" + destHost + ":" + destPort });
            } else {
                InputStream viewerExec = Viewer.class.getClassLoader()
                        .getResourceAsStream("rdesktop");
                if (viewerExec != null) {
                    File tmp = File.createTempFile("rdesktop", "");
                    FileOutputStream fos = new FileOutputStream(tmp);
                    byte[] buf = new byte[1024];
                    int n = -1;
                    while ((n = viewerExec.read(buf)) != -1) {
                        fos.write(buf, 0, n);
                    }
                    fos.close();
                    tmp.setExecutable(true);
                    launch(new String[] { tmp.getAbsolutePath(),
                            destHost + ":" + destPort });
                    tmp.deleteOnExit();
                }
                else{
                    launch(new String[] { "rdesktop",
                            destHost + ":" + destPort });
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static void launchVNC(String destHost, String destPort) {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                InputStream viewerExec = Viewer.class.getClassLoader()
                        .getResourceAsStream("vncviewer.exe");
                if (viewerExec != null) {
                    File tmp = File.createTempFile("vncviewer", ".exe");
                    FileOutputStream fos = new FileOutputStream(tmp);
                    byte[] buf = new byte[1024];
                    int n = -1;
                    while ((n = viewerExec.read(buf)) != -1) {
                        fos.write(buf, 0, n);
                    }
                    fos.close();
                    tmp.setExecutable(true);
                    launch(new String[] { tmp.getAbsolutePath(),
                            destHost + ":" + destPort });
                    tmp.deleteOnExit();
                    return;
                }
            }
            else{
                InputStream viewerExec = Viewer.class.getClassLoader()
                        .getResourceAsStream("vncviewer");
                if (viewerExec != null) {
                    File tmp = File.createTempFile("vncviewer", "");
                    FileOutputStream fos = new FileOutputStream(tmp);
                    byte[] buf = new byte[1024];
                    int n = -1;
                    while ((n = viewerExec.read(buf)) != -1) {
                        fos.write(buf, 0, n);
                    }
                    fos.close();
                    tmp.setExecutable(true);
                    launch(new String[] { tmp.getAbsolutePath(),
                            destHost + ":" + destPort });
                    tmp.deleteOnExit();
                }
                else{
                    launch(new String[] { "vncviewer",
                            destHost + ":" + destPort });
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {
        String service = args[0];
        String destHost = args[1];
        String destPort = args[2];
        String forwarder = args[3];

        try {
            if (!forwarder.equals("N/A")) {
                /* Create a connection instance */
                Connection conn = new Connection(forwarder);
                /* Now connect */
                conn.connect();
                /* Authenticate */
                boolean isAuthenticated = conn.authenticateWithPassword(
                        "cedar", "secret");
                if (isAuthenticated == false)
                    throw new Exception("Authentication failed.");

                int freePort = SocketUtil.findFreePort();
                LocalPortForwarder lpf = conn.createLocalPortForwarder(
                        freePort, destHost, Integer.parseInt(destPort));
                try {
                    if (service.equals("RDP")) {
                        launchRDP("127.0.0.1", Integer.toString(freePort));
                    } else {
                        launchVNC("127.0.0.1", Integer.toString(freePort));
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
                lpf.close();
                /* Close the connection */
                conn.close();
            } else {
                try {
                    if (service.equals("RDP")) {
                        launchRDP(destHost, destPort);
                    } else {
                        launchVNC(destHost, destPort);
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        System.exit(0);
    }
}