package com.intel.cedar.tasklet.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.tasklet.AbstractTaskRunner;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;

public class ShellProxyRunner extends AbstractTaskRunner {
    private static final long serialVersionUID = -6757703992971128315L;
    static final String knownHostPathPat = "%s/.ssh/known_hosts";
    static final String idDSAPathPat = "%s/.ssh/id_dsa";
    static final String idRSAPathPat = "%s/.ssh/id_rsa";

    @Override
    public ResultID run(ITaskItem ti, Writer output, Environment env) {
        GenericTaskItem item = (GenericTaskItem) ti;
        String host = item.getProperty("host");
        String username = item.getProperty("user", "root");
        String password = item.getProperty("passwd", "");
        String cmd = item.getProperty("cmd", "");
        String lastError = null;
        if (host == null || username == null) {
            return ResultID.Failed;
        }
        try {
            String knownHostPath = knownHostPathPat;
            String idDSAPath = idDSAPathPat;
            String idRSAPath = idRSAPathPat;
            if(username.equals("root")){
               knownHostPath = String.format(knownHostPathPat, "/" + username);
               idDSAPath = String.format(idDSAPathPat, "/" + username);
               idRSAPath = String.format(idRSAPathPat, "/" + username);
            }
            else{
               knownHostPath = String.format(knownHostPathPat, "/home/" + username);
               idDSAPath = String.format(idDSAPathPat, "/home/" + username);
               idRSAPath = String.format(idRSAPathPat, "/home/" + username);
            }
            Connection conn = new Connection(host);
            conn.connect();
            if (conn.isAuthMethodAvailable(username, "publickey")) {
                File key = new File(idDSAPath);
                if (key.exists()) {
                    if (!conn
                            .authenticateWithPublicKey(username, key, password))
                        lastError = "DSA authentication failed.";
                } else {
                    key = new File(idRSAPath);
                    if (key.exists()) {
                        if (!conn.authenticateWithPublicKey(username, key,
                                password))
                            lastError = "RSA authentication failed.";
                    }
                }
            }
            if (!conn.isAuthenticationComplete()
                    && conn.isAuthMethodAvailable(username, "password")) {
                if (!conn.authenticateWithPassword(username, password))
                    lastError = "Password authentication failed.";
            }
            if (conn.isAuthenticationComplete()) {
                if (cmd.length() > 0) {
                    Session sess = conn.openSession();
                    InputStream is = sess.getStdout();
                    sess.execCommand(cmd);
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int n = -1;
                    while ((n = is.read(buf)) != -1) {
                        os.write(buf, 0, n);
                    }
                    is.close();
                    is = sess.getStderr();
                    while ((n = is.read(buf)) != -1) {
                        os.write(buf, 0, n);
                    }
                    output.write(os.toString());
                    sess.close();
                } else {
                    output.write("Login successfully!");
                }
                conn.close();
                output.flush();
                return ResultID.Passed;
            }
        } catch (Exception e) {
            lastError = e.getMessage();
        }
        if (lastError != null) {
            System.err.print(lastError);
        }
        return ResultID.Failed;
    }

    public static void main(String[] args) {
        ShellProxyRunner runner = new ShellProxyRunner();
        GenericTaskItem item = new GenericTaskItem();
        item.setProperty("host", "node-2.sh.intel.com");
        item.setProperty("passwd", "123456");
        item.setProperty("cmd", "ps -ef");
        runner.run(item, new OutputStreamWriter(System.out), null);
    }
}
