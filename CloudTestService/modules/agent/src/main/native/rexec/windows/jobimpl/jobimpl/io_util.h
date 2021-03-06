/*
 * Copyright 1997-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

extern jfieldID IO_fd_fdID;
extern jfieldID IO_handle_fdID;

#if !defined(O_DSYNC) || !defined(O_SYNC)
#define O_SYNC  (0x0800)
#define O_DSYNC (0x2000)
#endif

/*
 * IO helper functions
 */

int readSingle(JNIEnv *env, jobject that, jfieldID fid);
int readBytes(JNIEnv *env, jobject that, jbyteArray bytes, jint off,
              jint len, jfieldID fid);
void writeSingle(JNIEnv *env, jobject that, jint byte, jfieldID fid);
void writeBytes(JNIEnv *env, jobject that, jbyteArray bytes, jint off,
                jint len, jfieldID fid);
void fileOpen(JNIEnv *env, jobject that, jstring path, jfieldID fid, int flags);
void throwFileNotFoundException(JNIEnv *env, jstring path);



/*
 * Macros for managing platform strings.  The typical usage pattern is:
 *
 *     WITH_PLATFORM_STRING(env, string, var) {
 *         doSomethingWith(var);
 *     } END_PLATFORM_STRING(env, var);
 *
 *  where  env      is the prevailing JNIEnv,
 *         string   is a JNI reference to a java.lang.String object, and
 *         var      is the char * variable that will point to the string,
 *                  after being converted into the platform encoding.
 *
 * The related macro WITH_FIELD_PLATFORM_STRING first extracts the string from
 * a given field of a given object:
 *
 *     WITH_FIELD_PLATFORM_STRING(env, object, id, var) {
 *         doSomethingWith(var);
 *     } END_PLATFORM_STRING(env, var);
 *
 *  where  env      is the prevailing JNIEnv,
 *         object   is a jobject,
 *         id       is the field ID of the String field to be extracted, and
 *         var      is the char * variable that will point to the string.
 *
 * Uses of these macros may be nested as long as each WITH_.._STRING macro
 * declares a unique variable.
 */

#define WITH_PLATFORM_STRING(env, strexp, var)                                \
    if (1) {                                                                  \
        const char *var;                                                      \
        jstring _##var##str = (strexp);                                       \
        if (_##var##str == NULL) {                                            \
            JNU_ThrowNullPointerException((env), NULL);                       \
            goto _##var##end;                                                 \
        }                                                                     \
        var = JNU_GetStringPlatformChars((env), _##var##str, NULL);           \
        if (var == NULL) goto _##var##end;

#define WITH_FIELD_PLATFORM_STRING(env, object, id, var)                      \
    WITH_PLATFORM_STRING(env,                                                 \
                         ((object == NULL)                                    \
                          ? NULL                                              \
                          : (*(env))->GetObjectField((env), (object), (id))), \
                         var)

#define END_PLATFORM_STRING(env, var)                                         \
        JNU_ReleaseStringPlatformChars(env, _##var##str, var);                \
    _##var##end: ;                                                            \
    } else ((void)NULL)

/* Macros for transforming Java Strings into native Unicode strings.
 * Works analogously to WITH_PLATFORM_STRING.
 */

#define WITH_UNICODE_STRING(env, strexp, var)                                 \
    if (1) {                                                                  \
        const jchar *var;                                                     \
        jstring _##var##str = (strexp);                                       \
        if (_##var##str == NULL) {                                            \
            JNU_ThrowNullPointerException((env), NULL);                       \
            goto _##var##end;                                                 \
        }                                                                     \
        var = ((env))->GetStringChars( _##var##str, NULL);             \
        if (var == NULL) goto _##var##end;

#define END_UNICODE_STRING(env, var)                                          \
        ((env))->ReleaseStringChars( _##var##str, var);                  \
    _##var##end: ;                                                            \
    } else ((void)NULL)
