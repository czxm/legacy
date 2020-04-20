// The following ifdef block is the standard way of creating macros which make exporting 
// from a DLL simpler. All files within this DLL are compiled with the JOBIMPL_EXPORTS
// symbol defined on the command line. this symbol should not be defined on any project
// that uses this DLL. This way any other project whose source files include this file see 
// JOBIMPL_API functions as being imported from a DLL, whereas this DLL sees symbols
// defined with this macro as being exported.
#ifdef JOBIMPL_EXPORTS
#define JOBIMPL_API __declspec(dllexport)
#else
#define JOBIMPL_API __declspec(dllimport)
#endif


extern JOBIMPL_API int njobimpl;

JOBIMPL_API int fnjobimpl(void);

typedef void* (CALLBACK* LPFNDLLFUNC1)();		
extern LPFNDLLFUNC1 JVM_GetThreadInterruptEvent;    // Function pointer