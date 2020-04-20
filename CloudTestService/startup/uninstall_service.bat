ECHO OFF
IF ["%JAVA_HOME%"]==[""] (
  set JRE_HOME=c:\cedar\jre
) ELSE (
  set JRE_HOME=%JAVA_HOME%\jre
)

IF ["%CEDAR_HOME%"]==[""] set CEDAR_HOME=c:\cedar

IF ["%PROCESSOR_ARCHITECTURE%"]==["AMD64"] (
  set CEDAR="%CEDAR_HOME%\startup\cedar64.exe"
) ELSE (
  set CEDAR="%CEDAR_HOME%\startup\cedar.exe"
)
%CEDAR% //DS//CedarService
