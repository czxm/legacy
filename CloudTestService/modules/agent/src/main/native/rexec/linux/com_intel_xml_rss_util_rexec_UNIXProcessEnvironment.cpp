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
#include <stdio.h>
#include <string.h>
#include "com_intel_xml_rss_util_rexec_UNIXProcessEnvironment.h"
/*
 * Class:     com_intel_xml_rss_util_rexec_UNIXProcessEnvironment
 * Method:    environ
 * Signature: ()[[B
 */
  JNIEXPORT jobjectArray JNICALL Java_com_intel_xml_rss_util_rexec_UNIXProcessEnvironment_environ
(JNIEnv * env, jclass ign)
{
  /* This is one of the rare times it's more portable to declare an
   *      * external symbol explicitly, rather than via a system header.
   *           * The declaration is standardized as part of UNIX98, but there is
   *                * no standard (not even de-facto) header file where the
   *                     * declaration is to be found.  See:
   *                          * http://www.opengroup.org/onlinepubs/007908799/xbd/envvar.html */
  extern char ** environ; /* environ[i] looks like: VAR=VALUE\0 */

  jsize count = 0;
  jsize i, j;
  jobjectArray result;
  jclass byteArrCls = env->FindClass( "[B");

  for (i = 0; environ[i]; i++) {
    /* Ignore corrupted environment variables */
    if (strchr(environ[i], '=') != NULL)
      count++;
  }

  result = env->NewObjectArray( 2*count, byteArrCls, 0);
  if (result == NULL) return NULL;

  for (i = 0, j = 0; environ[i]; i++) {
    const char * varEnd = strchr(environ[i], '=');
    /* Ignore corrupted environment variables */
    if (varEnd != NULL) {
      jbyteArray var, val;
      const char * valBeg = varEnd + 1;
      jsize varLength = varEnd - environ[i];
      jsize valLength = strlen(valBeg);
      var = env->NewByteArray( varLength);
      if (var == NULL) return NULL;
      val = env->NewByteArray( valLength);
      if (val == NULL) return NULL;
      env->SetByteArrayRegion( var, 0, varLength,
                                 (jbyte*) environ[i]);
      env->SetByteArrayRegion( val, 0, valLength,
                                 (jbyte*) valBeg);
      env->SetObjectArrayElement( result, 2*j  , var);
      env->SetObjectArrayElement( result, 2*j+1, val);
      env->DeleteLocalRef( var);
      env->DeleteLocalRef( val);
      j++;
    }
  }

  return result;
}
