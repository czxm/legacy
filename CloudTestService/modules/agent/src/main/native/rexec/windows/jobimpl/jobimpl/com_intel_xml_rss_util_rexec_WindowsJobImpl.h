/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_intel_xml_rss_util_rexec_WindowsJobImpl */

#ifndef _Included_com_intel_xml_rss_util_rexec_WindowsJobImpl
#define _Included_com_intel_xml_rss_util_rexec_WindowsJobImpl
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_intel_xml_rss_util_rexec_WindowsJobImpl
 * Method:    getStillActive
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_intel_xml_rss_util_rexec_WindowsJobImpl_getStillActive
  (JNIEnv *, jclass);

/*
 * Class:     com_intel_xml_rss_util_rexec_WindowsJobImpl
 * Method:    getExitCodeProcess
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_intel_xml_rss_util_rexec_WindowsJobImpl_getExitCodeProcess
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_intel_xml_rss_util_rexec_WindowsJobImpl
 * Method:    waitForInterruptibly
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_intel_xml_rss_util_rexec_WindowsJobImpl_waitForInterruptibly
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_intel_xml_rss_util_rexec_WindowsJobImpl
 * Method:    terminateProcess
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_intel_xml_rss_util_rexec_WindowsJobImpl_terminateProcess
  (JNIEnv *, jclass, jlong);
JNIEXPORT void JNICALL Java_com_intel_xml_rss_util_rexec_WindowsJobImpl_terminateJob
  (JNIEnv *, jclass, jstring);
/*
 * Class:     com_intel_xml_rss_util_rexec_WindowsJobImpl
 * Method:    create
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[JZ)J
 */
JNIEXPORT jlong JNICALL Java_com_intel_xml_rss_util_rexec_WindowsJobImpl_create
  (JNIEnv *, jclass, jstring, jstring, jstring, jstring, jlongArray, jboolean);

/*
 * Class:     com_intel_xml_rss_util_rexec_WindowsJobImpl
 * Method:    closeHandle
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_intel_xml_rss_util_rexec_WindowsJobImpl_closeHandle
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
