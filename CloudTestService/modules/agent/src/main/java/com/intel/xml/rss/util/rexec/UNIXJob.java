/*
 * Copyright 1995-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.intel.xml.rss.util.rexec;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/* java.lang.Process subclass in the UNIX environment.
 *
 * @author Mario Wolczko and Ross Knippel.
 * @author Konstantin Kladko (ported to Linux)
 */

final class UNIXJob extends Job {
    private static final JavaIOFileDescriptorAccess fdAccess = SharedSecrets
            .getJavaIOFileDescriptorAccess();

    private int pid;
    private int exitcode;
    private boolean hasExited;

    private OutputStream stdin_stream;
    private InputStream stdout_stream;
    private InputStream stderr_stream;

    /* this is for the reaping thread */
    private native int waitForProcessExit(int pid);

    /**
     * Create a process using fork(2) and exec(2).
     * 
     * @param std_fds
     *            array of file descriptors. Indexes 0, 1, and 2 correspond to
     *            standard input, standard output and standard error,
     *            respectively. On input, a value of -1 means to create a pipe
     *            to connect child and parent processes. On output, a value
     *            which is not -1 is the parent pipe fd corresponding to the
     *            pipe which has been created. An element of this array is -1 on
     *            input if and only if it is <em>not</em> -1 on output.
     * @return the pid of the subprocess
     */
    private native int forkAndExec(byte[] prog, byte[] argBlock, int argc,
            byte[] envBlock, int envc, byte[] dir, int[] std_fds,
            boolean redirectErrorStream) throws IOException;

    /* In the process constructor we wait on this gate until the process */
    /* has been created. Then we return from the constructor. */
    /* fork() is called by the same thread which later waits for the process */
    /* to terminate */

    private static class Gate {

        private boolean exited = false;
        private IOException savedException;

        synchronized void exit() { /* Opens the gate */
            exited = true;
            this.notify();
        }

        synchronized void waitForExit() { /* wait until the gate is open */
            boolean interrupted = false;
            while (!exited) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }

        void setException(IOException e) {
            savedException = e;
        }

        IOException getException() {
            return savedException;
        }
    }

    UNIXJob(final byte[] prog, final byte[] argBlock, final int argc,
            final byte[] envBlock, final int envc, final byte[] dir,
            final int[] std_fds, final boolean redirectErrorStream)
            throws IOException {

        final Gate gate = new Gate();
        /*
         * For each subprocess forked a corresponding reaper thread is started.
         * That thread is the only thread which waits for the subprocess to
         * terminate and it doesn't hold any locks while doing so. This design
         * allows waitFor() and exitStatus() to be safely executed in parallel
         * (and they need no native code).
         */

        java.security.AccessController
                .doPrivileged(new java.security.PrivilegedAction<Void>() {
                    public Void run() {
                        Thread t = new Thread("process reaper") {
                            public void run() {
                                try {
                                    pid = forkAndExec(prog, argBlock, argc,
                                            envBlock, envc, dir, std_fds,
                                            redirectErrorStream);
                                } catch (IOException e) {
                                    gate.setException(e); /*
                                                           * remember to rethrow
                                                           * later
                                                           */
                                    gate.exit();
                                    return;
                                }
                                java.security.AccessController
                                        .doPrivileged(new java.security.PrivilegedAction<Void>() {
                                            public Void run() {
                                                if (std_fds[0] == -1)
                                                    stdin_stream = new JobBuilder.NullOutputStream();
                                                else {
                                                    FileDescriptor stdin_fd = new FileDescriptor();
                                                    fdAccess.set(stdin_fd,
                                                            std_fds[0]);
                                                    stdin_stream = new BufferedOutputStream(
                                                            new FileOutputStream(
                                                                    stdin_fd));
                                                }

                                                if (std_fds[1] == -1)
                                                    stdout_stream = new JobBuilder.NullInputStream();
                                                else {
                                                    FileDescriptor stdout_fd = new FileDescriptor();
                                                    fdAccess.set(stdout_fd,
                                                            std_fds[1]);
                                                    stdout_stream = new BufferedInputStream(
                                                            new FileInputStream(
                                                                    stdout_fd));
                                                }

                                                if (std_fds[2] == -1)
                                                    stderr_stream = new JobBuilder.NullInputStream();
                                                else {
                                                    FileDescriptor stderr_fd = new FileDescriptor();
                                                    fdAccess.set(stderr_fd,
                                                            std_fds[2]);
                                                    stderr_stream = new FileInputStream(
                                                            stderr_fd);
                                                }

                                                return null;
                                            }
                                        });
                                gate.exit(); /* exit from constructor */
                                int res = waitForProcessExit(pid);
                                synchronized (UNIXJob.this) {
                                    hasExited = true;
                                    exitcode = res;
                                    UNIXJob.this.notifyAll();
                                }
                            }
                        };
                        t.setDaemon(true);
                        t.start();
                        return null;
                    }
                });
        gate.waitForExit();
        IOException e = gate.getException();
        if (e != null)
            throw new IOException(e.toString());
    }

    public OutputStream getOutputStream() {
        return stdin_stream;
    }

    public InputStream getInputStream() {
        return stdout_stream;
    }

    public InputStream getErrorStream() {
        return stderr_stream;
    }

    public synchronized int waitFor() throws InterruptedException {
        while (!hasExited) {
            wait();
        }
        return exitcode;
    }

    public synchronized int exitValue() {
        if (!hasExited) {
            throw new IllegalThreadStateException("process hasn't exited");
        }
        return exitcode;
    }

    private FileFilter fileFilter = new FileFilter() {
        public boolean accept(File pathname) {
            int parsedPID = -1;
            try {
                parsedPID = Integer.parseInt(pathname.getName());
                if (parsedPID > 1 && parsedPID != pid) {
                    return true;
                }
                return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    };

    public File[] GetFiles(String pathname) {
        try {
            File root = new File(pathname);
            File[] files = root.listFiles(fileFilter);
            return files;
        } catch (Exception e) {
            return null;
        }
    }

    private void getAllChildren(int pid, ArrayList<Integer> list) {
        File[] procFiles = GetFiles("/proc");
        for (File file : procFiles) {
            try {
                File stat = new File(file, "stat");
                if (!stat.exists())
                    continue;
                BufferedReader br = new BufferedReader(new FileReader(stat));
                String input = br.readLine();
                if (input != null && !input.equals("")) {
                    String[] tokens = input.split(" ");
                    int id = Integer.parseInt(tokens[3]);
                    if (id == pid) {
                        list.add(Integer.parseInt(tokens[0]));
                    }
                }
                br.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * private void destroyJob(int pid) { ArrayList<Integer> queue=new
     * ArrayList<Integer>(); queue.add(pid);
     * 
     * while(queue.size()>0) { int cid=queue.remove(0); stopProcess(cid);
     * getAllChildren(cid,queue); destroyProcess(cid); } }
     */

    private static native void destroyProcess(int pid);

    private static native void stopProcess(int pid);

    public void destroy() {
        // There is a risk that pid will be recycled, causing us to
        // kill the wrong process! So we only terminate processes
        // that appear to still be running. Even with this check,
        // there is an unavoidable race condition here, but the window
        // is very small, and OSes try hard to not recycle pids too
        // soon, so this is quite safe.
        synchronized (this) {
            if (!hasExited) {
                destroyProcess(pid);
            }
        }
        try {
            stdin_stream.close();
            stdout_stream.close();
            stderr_stream.close();
        } catch (IOException e) {
            // ignore
        }
    }

    /* This routine initializes JNI field offsets for the class */
    private static native void initIDs();

    static {
        initIDs();
    }
}
