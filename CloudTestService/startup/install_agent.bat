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
set JVM="%JRE_HOME%\bin\server\jvm.dll"

%CEDAR% //IS//CedarAgent --DisplayName="Cedar Agent" --Description="Agent Runtime for Cloud Test Service" --Install=%CEDAR% --Startup=auto --Jvm=%JVM% --Classpath="%CEDAR_HOME%\startup\bootstrap.jar" --StartMode=jvm --StartClass=com.intel.cedar.util.CedarApplication --StartMethod=serviceMain --StartParams=agent ++StartParams=start --StopMode=jvm --StopClass=com.intel.cedar.util.CedarApplication --StopMethod=serviceMain --StopParams=agent ++StopParams=stop --LogPath="%CEDAR_HOME%\log" --StdOutput=auto --JvmOptions=-Dcedar.home="%CEDAR_HOME%" ++JvmOptions=-Djava.cmd="%JRE_HOME%\bin\java.exe"
