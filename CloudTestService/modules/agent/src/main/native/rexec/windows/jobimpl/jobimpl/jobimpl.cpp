// jobimpl.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"
#include "jobimpl.h"
	
LPFNDLLFUNC1 JVM_GetThreadInterruptEvent;    // Function pointer

BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
	switch (ul_reason_for_call)
	{
	case DLL_PROCESS_ATTACH:
		{
			HINSTANCE hDLL;               // Handle to DLL

			hDLL = LoadLibrary("jvm.dll");
			if (hDLL != NULL)
			{
#ifdef WIN64
				JVM_GetThreadInterruptEvent = (LPFNDLLFUNC1)GetProcAddress(hDLL,
														"JVM_GetThreadInterruptEvent");
#else
				JVM_GetThreadInterruptEvent = (LPFNDLLFUNC1)GetProcAddress(hDLL,
														"_JVM_GetThreadInterruptEvent@0");
#endif
				if (!JVM_GetThreadInterruptEvent)
				{
					// handle the error
					FreeLibrary(hDLL);
				}
				else
				{
				}
				printf("JVM_GetThreadInterruptEvent=%p\n",JVM_GetThreadInterruptEvent);
			}
		}
	case DLL_THREAD_ATTACH:
	case DLL_THREAD_DETACH:
	case DLL_PROCESS_DETACH:
		break;
	}
    return TRUE;
}

// This is an example of an exported variable
JOBIMPL_API int njobimpl=0;

// This is an example of an exported function.
JOBIMPL_API int fnjobimpl(void)
{
	return 42;
}




