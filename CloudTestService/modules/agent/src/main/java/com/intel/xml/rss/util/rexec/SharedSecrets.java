/*
 * Copyright 2002-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.io.FileDescriptor;
import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * A repository of "shared secrets", which are a mechanism for calling
 * implementation-private methods in another package without using reflection. A
 * package-private class implements a public interface and provides the ability
 * to call package-private methods within that package; the object implementing
 * that interface is provided through a third package to which access is
 * restricted. This framework avoids the primary disadvantage of using
 * reflection for this purpose, namely the loss of compile-time checking.
 */

public class SharedSecrets {
    private static final Unsafe unsafe = SharedSecrets.getUnsafe();
    private static JavaIOFileDescriptorAccess javaIOFileDescriptorAccess;

    public static Unsafe getUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception ex) {
            throw new RuntimeException("can't get Unsafe instance", ex);
        }
    }

    public static void setJavaIOFileDescriptorAccess(
            JavaIOFileDescriptorAccess jiofda) {
        javaIOFileDescriptorAccess = jiofda;
    }

    public static JavaIOFileDescriptorAccess getJavaIOFileDescriptorAccess() {
        if (javaIOFileDescriptorAccess == null) {
            unsafe.ensureClassInitialized(FileDescriptor.class);
            setJavaIOFileDescriptorAccess(new JavaIOFileDescriptorAccess() {
                public void set(FileDescriptor obj, int fd) {
                    Field field;
                    try {
                        field = FileDescriptor.class.getDeclaredField("fd");
                        field.setAccessible(true);
                        field.set(obj, fd);
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SecurityException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (NoSuchFieldException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

                public int get(FileDescriptor obj) {
                    try {
                        Field field = FileDescriptor.class
                                .getDeclaredField("fd");
                        field.setAccessible(true);
                        return (Integer) field.get(obj);
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SecurityException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (NoSuchFieldException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } finally {
                        new Exception().printStackTrace();
                        return -1;
                    }
                }

                public void setHandle(FileDescriptor obj, long handle) {
                    Field field;
                    try {
                        field = FileDescriptor.class.getDeclaredField("handle");
                        field.setAccessible(true);
                        field.set(obj, handle);
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SecurityException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (NoSuchFieldException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

                public long getHandle(FileDescriptor obj) {
                    try {
                        Field field = FileDescriptor.class
                                .getDeclaredField("handle");
                        field.setAccessible(true);
                        return (Integer) field.get(obj);
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SecurityException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (NoSuchFieldException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } finally {
                        new Exception().printStackTrace();
                        return -1;
                    }
                }
            });

        }

        return javaIOFileDescriptorAccess;
    }

}
