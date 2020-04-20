package com.intel.cedar.core;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.core.entities.MachineInfo;
import com.intel.cedar.core.entities.MachineMappingInfo;
import com.intel.cedar.user.UserInfo;
import com.intel.cedar.user.util.UserUtil;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.cedar.util.DatabaseUtil;
import com.intel.cedar.util.EntityWrapper;
import com.intel.cedar.util.Hashes;
import com.intel.cedar.util.LocalDatabaseConfig;
import com.intel.cedar.util.SubDirectory;

public class LocalDatabaseBootstrapper extends DatabaseBootstrapper implements
        Runnable {
    private static Logger LOG = LoggerFactory
            .getLogger(LocalDatabaseBootstrapper.class);
    private Server db;

    private static String config = "CREATE SCHEMA PUBLIC AUTHORIZATION DBA\n"
            + "CREATE USER SA PASSWORD \"" + Hashes.getHexSignature() + "\"\n"
            + "GRANT DBA TO SA\n" + "SET WRITE_DELAY 100 MILLIS\n"
            + "SET SCHEMA PUBLIC\n";

    protected void preStart() throws Exception {
        for (String context : LocalDatabaseConfig.getContexts()) {
            String context_script = SubDirectory.DB.toString() + File.separator
                    + context + ".script";
            File script = new File(context_script);
            if (!script.exists()) {
                FileWriter writer = new FileWriter(script);
                writer.write(config);
                writer.close();
            }
        }
    }

    protected void postStart() throws Exception {
        DatabaseUtil.enableAccess();
    }

    protected boolean existsMachineMapping(List<MachineMappingInfo> list,
            String pat) {
        for (MachineMappingInfo i : list) {
            if (i.getPattern().equals(pat))
                return true;
        }
        return false;
    }

    protected void initialize() throws Exception {
        try {
            EntityWrapper<MachineMappingInfo> mtdb = new EntityWrapper<MachineMappingInfo>();
            MachineMappingInfo mmi = new MachineMappingInfo();
            List<MachineMappingInfo> mmiList = mtdb.query(mmi);
            if (!existsMachineMapping(mmiList, ".*rhel5.*-x86.*"))
                mtdb.add(new MachineMappingInfo(".*rhel5.*-x86.*",
                        MachineInfo.OS.as5, MachineInfo.ARCH.x86));

            if (!existsMachineMapping(mmiList, ".*rhel5.*-x64.*"))
                mtdb.add(new MachineMappingInfo(".*rhel5.*-x64.*",
                        MachineInfo.OS.as5, MachineInfo.ARCH.x86_64));

            if (!existsMachineMapping(mmiList, ".*rhel6.*-x86.*"))
                mtdb.add(new MachineMappingInfo(".*rhel6.*-x86.*",
                        MachineInfo.OS.as6, MachineInfo.ARCH.x86));

            if (!existsMachineMapping(mmiList, ".*rhel6.*-x64.*"))
                mtdb.add(new MachineMappingInfo(".*rhel6.*-x64.*",
                        MachineInfo.OS.as6, MachineInfo.ARCH.x86_64));

            if (!existsMachineMapping(mmiList, ".*cent.*-x86.*"))
                mtdb.add(new MachineMappingInfo(".*cent.*-x86.*",
                        MachineInfo.OS.centos, MachineInfo.ARCH.x86));

            if (!existsMachineMapping(mmiList, ".*cent.*-x64.*"))
                mtdb.add(new MachineMappingInfo(".*cent.*-x64.*",
                        MachineInfo.OS.centos, MachineInfo.ARCH.x86_64));

            if (!existsMachineMapping(mmiList, ".*sles10.*-x86.*"))
                mtdb.add(new MachineMappingInfo(".*sles10.*-x86.*",
                        MachineInfo.OS.sles10, MachineInfo.ARCH.x86));

            if (!existsMachineMapping(mmiList, ".*sles10.*-x64.*"))
                mtdb.add(new MachineMappingInfo(".*sles10.*-x64.*",
                        MachineInfo.OS.sles10, MachineInfo.ARCH.x86_64));

            if (!existsMachineMapping(mmiList, ".*sles11.*-x86.*"))
                mtdb.add(new MachineMappingInfo(".*sles11.*-x86.*",
                        MachineInfo.OS.sles11, MachineInfo.ARCH.x86));

            if (!existsMachineMapping(mmiList, ".*sles11.*-x64.*"))
                mtdb.add(new MachineMappingInfo(".*sles11.*-x64.*",
                        MachineInfo.OS.sles11, MachineInfo.ARCH.x86_64));

            if (!existsMachineMapping(mmiList, ".*win2k3.*-x86.*"))
                mtdb.add(new MachineMappingInfo(".*win2k3.*-x86.*",
                        MachineInfo.OS.win2k3, MachineInfo.ARCH.x86));

            if (!existsMachineMapping(mmiList, ".*win2k3.*-x64.*"))
                mtdb.add(new MachineMappingInfo(".*win2k3.*-x64.*",
                        MachineInfo.OS.win2k3, MachineInfo.ARCH.x86_64));

            if (!existsMachineMapping(mmiList, ".*win2k8.*-x86.*"))
                mtdb.add(new MachineMappingInfo(".*win2k8.*-x86.*",
                        MachineInfo.OS.win2k8, MachineInfo.ARCH.x86));

            if (!existsMachineMapping(mmiList, ".*win2k8.*-x64.*"))
                mtdb.add(new MachineMappingInfo(".*win2k8.*-x64.*",
                        MachineInfo.OS.win2k8, MachineInfo.ARCH.x86_64));

            if (!existsMachineMapping(mmiList, ".*win7.*-x86.*"))
                mtdb.add(new MachineMappingInfo(".*win7.*-x86.*",
                        MachineInfo.OS.win7, MachineInfo.ARCH.x86));

            if (!existsMachineMapping(mmiList, ".*win7.*-x64.*"))
                mtdb.add(new MachineMappingInfo(".*win7.*-x64.*",
                        MachineInfo.OS.win7, MachineInfo.ARCH.x86_64));

            if (!existsMachineMapping(mmiList, ".*winxp.*-x86.*"))
                mtdb.add(new MachineMappingInfo(".*winxp.*-x86.*",
                        MachineInfo.OS.winxp, MachineInfo.ARCH.x86));

            if (!existsMachineMapping(mmiList, ".*winxp.*-x64.*"))
                mtdb.add(new MachineMappingInfo(".*winxp.*-x64.*",
                        MachineInfo.OS.winxp, MachineInfo.ARCH.x86_64));

            if (!existsMachineMapping(mmiList, ".*linux32.*"))
                mtdb.add(new MachineMappingInfo(".*linux32.*",
                        MachineInfo.OS.as5, MachineInfo.ARCH.x86));

            if (!existsMachineMapping(mmiList, ".*linux64.*"))
                mtdb.add(new MachineMappingInfo(".*linux64.*",
                        MachineInfo.OS.as5, MachineInfo.ARCH.x86_64));

            if (!existsMachineMapping(mmiList, ".*win32.*"))
                mtdb.add(new MachineMappingInfo(".*win32.*",
                        MachineInfo.OS.win2k3, MachineInfo.ARCH.x86));

            if (!existsMachineMapping(mmiList, ".*win64.*"))
                mtdb.add(new MachineMappingInfo(".*win64.*",
                        MachineInfo.OS.win2k3, MachineInfo.ARCH.x86_64));
            mtdb.commit();

            if (UserUtil.listUsers().size() == 0) {
                UserInfo user = new UserInfo();
                user.setUser("admin");
                user.setPassword("passwd");
                user.setAdmin(true);
                user.setEmail("ChangeIt");
                UserUtil.registerUser(user);
            }

            // TODO: add internal features/tasklets
        } catch (Exception e) {
        }
    }

    @Override
    public void start() {
        this.createDatabase();
        this.waitForDatabase();
        try {
            initialize();
        } catch (Exception e) {
            LOG.error("database initialization error", e);
            System.exit(-1);
        }
    }

    protected void createDatabase() {
        try {
            preStart();
        } catch (Exception e) {
            LOG.error("Failed to initialize the database layer.", e);
            System.exit(-1);
        }
        this.db = new Server();
        this.db.setProperties(new HsqlProperties(LocalDatabaseConfig
                .getProperties()));
        SystemBootstrapper.makeSystemThread(this).start();
        try {
            postStart();
        } catch (Exception e) {
            LOG.error("Failed to initialize the database layer.", e);
            System.exit(-1);
        }
    }

    protected void waitForDatabase() {
        while (this.db.getState() != 1) {
            Throwable t = this.db.getServerError();
            if (t != null) {
                LOG.error("Dabase error", t);
                throw new RuntimeException(t);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            LOG.info("Waiting for database to start...");
        }
    }

    @Override
    public void run() {
        this.db.start();
    }

    @Override
    public void stop() {
        if (db != null)
            db.stop();
    }
}
