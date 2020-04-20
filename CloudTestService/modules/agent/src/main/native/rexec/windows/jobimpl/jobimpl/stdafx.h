// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently
//

#pragma once
#undef UNICODE
#define _WIN32_WINNT 0x0502
#define WIN32_LEAN_AND_MEAN		// Exclude rarely-used stuff from Windows headers
// Windows Header Files:
#include <windows.h>
#include <io.h>
#include <assert.h>
#include "jvm.h"
#include "jvm_md.h"
#include "jni.h"
#include "jni_util.h"
#include "io_util_md.h"
#include "io_util.h"
#include "jni_util.h"
#include <stdio.h>
#include <stdlib.h>
#include <wchar.h>
#include <io.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <limits.h>
#include <wincon.h>
#include "jobimpl.h"
#include "com_intel_xml_rss_util_rexec_WindowsJobImpl.h"
#include "com_intel_xml_rss_util_rexec_WindowsProcessEnvironment.h"

// TODO: reference additional headers your program requires here
