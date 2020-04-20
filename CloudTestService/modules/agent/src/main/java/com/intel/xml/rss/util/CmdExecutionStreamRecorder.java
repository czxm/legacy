package com.intel.xml.rss.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.concurrent.Callable;

/**
 * Author: hshen5
 */
public class CmdExecutionStreamRecorder implements Callable<String> {

    private InputStream cmdExecutionStream = null;

    private char[] buffer = new char[2048];

    private Writer writer = null;

    public CmdExecutionStreamRecorder(InputStream ins) {
        this.cmdExecutionStream = ins;
        this.writer = null;
    }

    public CmdExecutionStreamRecorder(InputStream ins, Writer writerP) {
        this.cmdExecutionStream = ins;
        this.writer = writerP;
    }

    public String call() throws Exception {
        StringBuilder sb = new StringBuilder();
        try {
            int n;
            InputStreamReader reader = new InputStreamReader(cmdExecutionStream);
            if (writer == null) {
                while ((n = reader.read(buffer)) != -1) {
                    sb.append(buffer, 0, n);
                }
                return new String(sb);
            }

            // writer != null;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
                writer.flush();
            }
            return null;
        } catch (Exception ioe) {
            sb
                    .append("Exception occurred while reading execution output stream, detailed info listed below:\n");
            sb.append(Routine.getExceptionLogInfo(ioe));
            return new String(sb);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }

            }
        }
    }

}
