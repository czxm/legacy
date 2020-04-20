package com.intel.cedar.engine.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.intel.cedar.agent.runtime.CircularByteBuffer;
import com.intel.cedar.storage.IFile;

public class LogWriter extends Thread {
    private static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    private CircularByteBuffer buffer = new CircularByteBuffer();
    private InputStream input;
    private IFile logFile;
    private PrintWriter output;

    public LogWriter(IFile logFile) {
        this.logFile = logFile;
        this.output = new PrintWriter(buffer.getOutputStream());
        this.input = buffer.getInputStream();
    }

    public synchronized void append(String log) {
        this.output.print(sdf.format(new Date()));
        this.output.print(" ");
        this.output.println(log);
        this.output.flush();
    }

    public synchronized void append(Throwable t) {
        StringWriter buf = new StringWriter();
        PrintWriter writer = new PrintWriter(buf);
        t.printStackTrace(writer);
        this.output.print(sdf.format(new Date()));
        this.output.print(" ");
        this.output.println(buf.toString());
        this.output.flush();
    }

    public OutputStream getOutputStream() {
        return buffer.getOutputStream();
    }

    public void finish() {
        try {
            buffer.getOutputStream().close();
        } catch (Exception e) {
        }
    }

    public void run() {
        try {
            logFile.setContents(input);
        } catch (Exception e) {
        }
    }
}