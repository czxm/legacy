/*
 * Copyright 2003-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR that FILE HEADER.
 *
 * that code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates that
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied that code.
 *
 * that code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied that code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with that work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

#include "jni.h"
#include "jni_util.h"

/*
 * Prototypes for functions in io_util_md.c called from io_util,
 * FileDescriptor.c, FileInputStream.c, FileOutputStream.c
 */
WCHAR* pathToNTPath(JNIEnv *env, jstring path, jboolean throwFNFE);
WCHAR* fileToNTPath(JNIEnv *env, jobject file, jfieldID id);
WCHAR* getPrefixed(const WCHAR* path, int pathlen);
int currentDirLength(const WCHAR* path, int pathlen);
void fileOpen(JNIEnv *env, jobject that, jstring path, jfieldID fid, int flags);
int handleAvailable(jlong fd, jlong *pbytes);
JNIEXPORT int handleSync(jlong fd);
int handleSetLength(jlong fd, jlong length);
JNIEXPORT size_t handleRead(jlong fd, void *buf, jint len);
JNIEXPORT size_t handleWrite(jlong fd, const void *buf, jint len);
jint handleClose(JNIEnv *env, jobject that, jfieldID fid);
jlong handleLseek(jlong fd, jlong offset, jint whence);

/*
 * Returns an opaque handle to file named by "path".  If an error occurs,
 * returns -1 and an exception is pending.
 */
jlong winFileHandleOpen(JNIEnv *env, jstring path, int flags);

/*
 * Macros to use the right data type for file descriptors
 */
#define FD jlong

/*
 * Macros to set/get fd from the java.io.FileDescriptor.
 * If GetObjectField returns null, SET_FD will stop and GET_FD
 * will simply return -1 to avoid crashing VM.
 */
#define SET_FD(that, fd, fid) \
    if ((env)->GetObjectField( (that), (fid)) != NULL) \
        (env)->SetLongField( (env)->GetObjectField( (that), (fid)), IO_handle_fdID, (fd))

#define GET_FD(that, fid) \
    ((env)->GetObjectField((that), (fid)) == NULL) ? \
      -1 : (env)->GetLongField((env)->GetObjectField((that), (fid)), IO_handle_fdID)

/*
 * Macros to set/get fd when inside java.io.FileDescriptor
 */
#define this_FD(obj) (*env)->GetLongField(env, obj, IO_handle_fdID)

/*
 * Route the routines away from HPI layer
 */
#define IO_Write handleWrite
#define IO_Sync handleSync
#define IO_Read handleRead
#define IO_Lseek handleLseek
#define IO_Available handleAvailable
#define IO_SetLength handleSetLength

/*
 * Setting the handle field in Java_java_io_FileDescriptor_set for
 * standard handles stdIn, stdOut, stdErr
 */
#define SET_HANDLE(fd) \
if (fd == 0) { \
    return (jlong)GetStdHandle(STD_INPUT_HANDLE); \
} else if (fd == 1) { \
    return (jlong)GetStdHandle(STD_OUTPUT_HANDLE); \
} else if (fd == 2) { \
    return (jlong)GetStdHandle(STD_ERROR_HANDLE); \
} else { \
    return (jlong)-1; \
} \

/* INVALID_FILE_ATTRIBUTES is not defined in VC++6.0's header files but
 * in later release. Keep here just in case someone is still using VC++6.0
 */
#ifndef INVALID_FILE_ATTRIBUTES
#define INVALID_FILE_ATTRIBUTES ((DWORD)-1)
#endif
