package com.intel.cedar.mail;

import java.io.StringReader;
import java.util.List;

import com.intel.cedar.mail.Mailer.RecipientType;
import com.intel.cedar.user.UserInfo;
import com.intel.cedar.user.util.UserUtil;
import com.intel.cedar.util.CedarConfiguration;

public class CedarMail {
    private boolean important;
    private boolean urgent;
    private String subject;
    private String body;
    private UserInfo user;
    private UserInfo ccUser;
    private UserInfo bccUser;
    private List<String> ccEmails;
    private String fromName;
    private String fromAddr;

    public CedarMail() {
        this("Mail from Cloud Test Service", "");
    }

    public CedarMail(String subject, String body) {
        this(UserUtil.getAdmin(), subject, false, false, body);
    }

    public CedarMail(UserInfo user, String subject, String body) {
        this(user, subject, false, false, body);
    }

    public CedarMail(UserInfo user, String subject, boolean important,
            boolean urgent, String body) {
        this(null, null, user, subject, important, urgent, body);
    }
    
    public CedarMail(String fromName, String fromAddr, UserInfo user, String subject, boolean important,
            boolean urgent, String body) {
        this.subject = subject;
        this.important = important;
        this.urgent = urgent;
        this.body = body;
        this.user = user;
        this.fromName = fromName;
        if(this.fromName == null || this.fromName.length() == 0){
            this.fromName = Messages.getString("from.name");
        }
        this.fromAddr = fromAddr;
        if(this.fromAddr == null || this.fromAddr.length() == 0){
            this.fromAddr = Messages.getString("from.email");
        }
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public void setCCUser(UserInfo user) {
        this.ccUser = user;
    }

    public void setBCCUser(UserInfo user) {
        this.bccUser = user;
    }

    public void setEmails(List<String> emails) {
        this.ccEmails = emails;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public void sendMail() {
        if (user == null
                && (this.ccEmails == null || this.ccEmails.size() == 0))
            return;
        Mailer mailer = helpCreateMailer();
        helpSetMailHeader(mailer);
        helpSendMail(mailer, body);
    }

    protected void helpSetMailHeader(Mailer mailer) {
        mailer.setFrom(fromName, fromAddr);
        if (user != null) {
            String emails = user.getEmail();
            for (String email : emails.split(",| |;")) {
                mailer.addRecipient(user.getUser(), email);
            }
        } else {
            if (ccEmails != null) {
                mailer.addRecipient(ccEmails.get(0));
                for (int i = 1; i < ccEmails.size(); i++) {
                    mailer.addRecipient(ccEmails.get(i));
                }
            }
        }

        if (this.ccUser != null) {
            String emails = ccUser.getEmail();
            for (String email : emails.split(",| |;")) {
                mailer.addRecipient(RecipientType.CC, ccUser.getUser(), email);
            }
        }
        if (this.bccUser != null) {
            String emails = bccUser.getEmail();
            for (String email : emails.split(",| |;")) {
                mailer
                        .addRecipient(RecipientType.BCC, bccUser.getUser(),
                                email);
            }
        }

        if (important)
            mailer.addHeader("Importance", "high");
        if (urgent)
            mailer.addHeader("Priority", "Urgent");
        mailer.setSubject(subject);
    }

    protected void helpSendMail(Mailer mailer, String content) {
        try {
            mailer.sendEmail(new StringReader(content.toString()));
        } catch (MailerException e) {
        }
    }

    protected Mailer helpCreateMailer() {
        return new Mailer(CedarConfiguration.getInstance().getSMTPServer());
    }
}
