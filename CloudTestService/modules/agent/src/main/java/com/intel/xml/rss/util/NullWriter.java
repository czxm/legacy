/**
 * 
 */
package com.intel.xml.rss.util;

import java.io.Writer;

/**
 * @author hshen5
 * 
 */
public class NullWriter extends Writer {

    public NullWriter() {
    }

    @Override
    public Writer append(char c) {
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) {
        return this;
    }

    @Override
    public Writer append(CharSequence csq) {
        return this;
    }

    @Override
    public void close() {

    }

    @Override
    public void flush() {

    }

    @Override
    public void write(char[] cbuf, int off, int len) {
    }

    @Override
    public void write(char[] cbuf) {
    }

    @Override
    public void write(int c) {
    }

    @Override
    public void write(String str, int off, int len) {
    }

    @Override
    public void write(String str) {
    }

}
