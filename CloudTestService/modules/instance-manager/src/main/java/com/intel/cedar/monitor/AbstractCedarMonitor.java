package com.intel.cedar.monitor;

import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.core.entities.AbstractHostInfo;
import com.intel.cedar.core.entities.CloudInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.PhysicalNodeInfo;
import com.intel.cedar.core.entities.VolumeInfo;
import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.mail.CedarMail;
import com.intel.cedar.mail.Messages;
import com.intel.cedar.scheduler.CedarTimerTask;
import com.intel.cedar.user.util.UserUtil;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.cedar.util.CloudUtil;
import com.intel.cedar.util.EntityUtil;

public abstract class AbstractCedarMonitor extends CedarTimerTask {
    private static Logger LOG = LoggerFactory.getLogger(AbstractCedarMonitor.class);

    private CedarMail mailer;
    private String template = "Please contact administrator of Cloud Test Service!";

    public AbstractCedarMonitor(String name) {
        super(name);
        mailer = new CedarMail();
        try {
            ByteArrayOutputStream ous = new ByteArrayOutputStream();
            FileUtils.copyStream(getClass().getClassLoader()
                    .getResourceAsStream("notifyfragment.tpl"), ous);
            template = ous.toString();
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
        }
    }

    private void syncDateTime(final AbstractHostInfo host) {
        CloudUtil.asyncExec(new Runnable() {
            @Override
            public void run() {
                host.syncDateTime();
            }
        });
    }

    public void monitorCloud(CloudInfo cloud){
        try {
            if (CloudUtil.testConnection(cloud)){
                CloudUtil.refreshCloud(cloud);
                cloud.setNotifyCount(0l);
                cloud.saveChanges();
            }
            else if(cloud.getNotifyCount() < 1) {
                String subject = Messages.getString(
                        "subject.cloud.not.available", cloud.getName());
                String body = Messages.getString(
                        "body.cloud.not.available", cloud.getProtocol(), cloud.getHost());
                mailer.setImportant(true);
                mailer.setUrgent(true);
                mailer.setUser(UserUtil.getAdmin());
                mailer.setSubject(subject);
                mailer.setBody(template.replace("$CONTENT$", body.toString()));
                mailer.sendMail();
                cloud.setNotifyCount(1l);
                cloud.saveChanges();
            }                
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
        }
    }
    
    public void monitorInstances() {
        for (InstanceInfo i : EntityUtil.listInstances(null)) {
            if (!i.getState().equals("running")
                    && !i.getState().equals("rebooting")
                    && !i.getState().equals("inactive"))
                continue;
            if (i.testConnection(true)) {
                if (i.getRetryCount() > 0) {
                    i.setRetryCount(0L);
                    i.setNotifyCount(0L);
                    i.setState("running");
                    i.saveChanges();
                    syncDateTime(i);
                }
            } else {
                Long c = i.getRetryCount();
                c = c + 1;
                if (c < 20) {
                    i.setRetryCount(c);
                    if (i.getState().equals("running")) {
                        i.setState("inactive");
                    }
                    i.saveChanges();
                } else {
                    if (i.getState().equals("inactive")) {
                        i.setRetryCount(1L);
                        i.setState("rebooting");
                    } else if (i.getState().equals("rebooting")) {
                        String subject = Messages.getString(
                                "subject.instance.not.available", i
                                        .getHost());
                        String body = Messages.getString(
                                "body.instance.not.available", i
                                        .getInstanceId(), i.getHost());
                        if (i.getNotifyCount() < 5) {
                            if (i.getPooled()) {
                                mailer.setImportant(true);
                                mailer.setUrgent(true);
                            }
                            if (i.getNotifyCount() == 4) {
                                body = Messages.getString(
                                        "last.body.instance.not.available",
                                        i.getInstanceId(), i.getHost());
                                mailer.setImportant(true);
                                mailer.setUrgent(true);
                            }
                            mailer.setUser(UserUtil.getUserById(i
                                    .getUserId()));
                            mailer.setBCCUser(UserUtil.getAdmin());
                            mailer.setSubject(subject);
                            mailer.setBody(template.replace("$CONTENT$",
                                    body.toString()));
                            mailer.sendMail();
                            i.setNotifyCount(i.getNotifyCount() + 1);
                        }
                        i.setRetryCount(1L);
                    }
                    i.saveChanges();
                    CloudUtil.rebootInstance(i);
                }
            }
        }
    }

    public void monitorNodes(){
        for (PhysicalNodeInfo n : EntityUtil.listPhysicalNodes()) {
            if (!n.getState().equals("running")
                    && !n.getState().equals("inactive"))
                continue;
            if (n.testConnection(true)) {
                if (n.getRetryCount() > 0) {
                    n.setRetryCount(0L);
                    n.setNotifyCount(0L);
                    n.setState("running");
                    n.saveChanges();
                    syncDateTime(n);
                }
            } else {
                Long c = n.getRetryCount();
                c = c + 1;
                if (c < 10) {
                    n.setRetryCount(c);
                    n.saveChanges();
                } else {
                    if (!n.getState().equals("inactive")) {
                        n.setState("inactive");
                    }
                    String subject = Messages.getString(
                            "subject.node.not.available", n.getHost());
                    String body = Messages.getString(
                            "body.node.not.available", n.getHost());
                    if (n.getNotifyCount() < 5) {
                        if (n.getPooled()) {
                            mailer.setImportant(true);
                            mailer.setUrgent(true);
                        }
                        if (n.getNotifyCount() == 4) {
                            mailer.setImportant(true);
                            mailer.setUrgent(true);
                            body = Messages.getString(
                                    "last.body.node.not.available", n
                                            .getHost());
                        }
                        mailer.setUser(UserUtil.getUserById(n.getUserId()));
                        mailer.setBCCUser(UserUtil.getAdmin());
                        mailer.setSubject(subject);
                        mailer.setBody(template.replace("$CONTENT$", body
                                .toString()));
                        mailer.sendMail();
                        n.setNotifyCount(n.getNotifyCount() + 1);
                    }
                    n.setRetryCount(1L);
                    n.saveChanges();
                }
            }
        }
    }
    
    public void monitorVolumes(){
        long time = System.currentTimeMillis();
        for (VolumeInfo v : EntityUtil.listVolumes(null)) {
            if (v.getAttachTime() != null
                    && (time - v.getAttachTime()) > CedarConfiguration
                            .getInstance().getVolumeExpire() * 1000) {
                if (v.getHeld()) {
                    if (v.isCloudVolume()) {
                        try {
                            if (v.getAttached() != null && v.getAttached() > 0) {
                                // just keep this held volume for more time
                                v.setAttachTime(System.currentTimeMillis());
                                v.saveChanges();
                            } else {
                                CloudUtil.deleteVolume(v);
                            }
                        } catch (Exception e) {
                            LOG.info("", e);
                        }
                    } else {
                        PhysicalNodeInfo n = PhysicalNodeInfo.load(v
                                .getAttached());
                        if (n != null) {
                            n.deleteVolume(v);
                            n.increaseDiskSize(v.getSize());
                        }
                    }
                } else if (!v.isCloudVolume()) { // local folder created by
                                                 // Engine
                    InstanceInfo n = InstanceInfo.load(v.getAttached());
                    if (n != null) {
                        n.deleteVolume(v);
                    }
                    else{
                        PhysicalNodeInfo p = PhysicalNodeInfo.load(v
                                .getAttached());
                        if (p != null) {
                            p.deleteVolume(v);
                            p.increaseDiskSize(v.getSize());
                        }
                    }
                }
            }
        }
    }
}
