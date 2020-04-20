package com.intel.cedar.mail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Mailer {

    private String smtpServer = null;

    private LinkedList<Attachment> attachments = new LinkedList<Attachment>();

    private InternetAddress from = null;

    private LinkedList<Recipient> recipients = new LinkedList<Recipient>();

    private LinkedList<Header> headers = new LinkedList<Header>();

    private String subject;

    public static enum RecipientType {
        TO, CC, BCC
    }

    class Attachment {
        File file;

        String fileName;

        Attachment(File file, String name) {
            this.file = file;
            fileName = name;
        }

    };

    class Recipient {
        RecipientType type;

        InternetAddress email;

        Recipient(RecipientType type, InternetAddress email) {
            this.type = type;
            this.email = email;
        }

    };

    class Header {
        Header(String n, String v) {
            this.name = n;
            this.value = v;
        }

        String name;
        String value;
    }

    public Mailer() {
    }

    public Mailer(String smtpServerP) {
        this.smtpServer = smtpServerP;
    }

    public void sendEmail(InputStream contentInput) throws MailerException {
        sendEmail(new InputStreamReader(contentInput));
    }

    public void sendEmail(Reader contentReader) throws MailerException {
        try {
            doSendMail(contentReader);
        } catch (MessagingException e) {
            throw new MailerException(e.getMessage());
        } catch (IOException e) {
            throw new MailerException(e.getMessage());
        }
    }

    private void doSendMail(Reader contentReader) throws MessagingException,
            IOException {

        // create session
        Properties props = System.getProperties();
        props.put("mail.smtp.host", smtpServer);
        Session session = Session.getDefaultInstance(props, null);

        // create message
        Message message = new MimeMessage(session);

        // set header
        for (Header header : headers) {
            message.setHeader(header.name, header.value);
        }

        // settting from
        message.setFrom(from);

        // settting recipient
        for (Recipient rec : recipients) {
            switch (rec.type) {
            case TO:
                message.addRecipient(Message.RecipientType.TO, rec.email);
                break;
            case CC:
                message.addRecipient(Message.RecipientType.CC, rec.email);
                break;
            case BCC:
                message.addRecipient(Message.RecipientType.BCC, rec.email);
                break;
            }
        }

        // set subject
        message.setSubject(subject);

        // getting message body
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[10240];
        int rd;
        while ((rd = contentReader.read(buffer)) != -1) {
            sb.append(buffer, 0, rd);
        }
        String content = sb.toString();

        // getting content type
        String contentType = null;
        if (content != null && content.length() >= 13
                && content.substring(0, 7).toLowerCase().startsWith("<html>")) {
            contentType = "text/html";
        } else {
            contentType = "text/plain";
        }

        // settting content and contenttype
        BodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(content, contentType);

        //
        Multipart allParts = new MimeMultipart();

        // add body part
        allParts.addBodyPart(bodyPart);

        // creating attachment part
        for (Attachment atta : attachments) {
            DataSource source = new FileDataSource(atta.file);
            DataHandler handler = new DataHandler(source);

            BodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setDataHandler(handler);
            attachmentPart.setFileName(atta.fileName);

            allParts.addBodyPart(attachmentPart);
        }

        message.setContent(allParts);

        Transport.send(message);

    }

    public void setFrom(String email) {

        try {
            from = new InternetAddress(email);
        } catch (AddressException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void setFrom(String name, String email) {
        try {
            from = new InternetAddress(email, name);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void clearRecipient() {
        recipients.clear();
    }

    public void addRecipient(String email) {
        addRecipient(RecipientType.TO, null, email);
    }

    public void addRecipient(RecipientType type, String email) {
        addRecipient(type, null, email);
    }

    public void addRecipient(String name, String email) {
        addRecipient(RecipientType.TO, name, email);
    }

    public void addRecipient(RecipientType type, String name, String email) {
        try {
            InternetAddress emailAddress = null;
            if (name == null) {
                emailAddress = new InternetAddress(email);
            } else {
                emailAddress = new InternetAddress(email, name);
            }
            recipients.addLast(new Recipient(type, emailAddress));
        } catch (MessagingException e) {
            // TODO: handle exception
            // to be refined
        } catch (UnsupportedEncodingException e1) {

        }

    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    public void addAttachment(File file) {
        addAttachment(file, file.getName());
    }

    public void addAttachment(File file, String name) {
        attachments.add(new Attachment(file, name));
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public InternetAddress getFrom() {
        return from;
    }

    public int getRecipientCount() {
        return recipients.size();
    }

    public void addHeader(String name, String value) {
        headers.add(new Header(name, value));
    }

}
