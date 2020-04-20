ECHO OFF
IF ["%JAVA_HOME%"]==[""] set JAVA_HOME=c:\cedar\jre
set JAVA_CMD="%JAVA_HOME%\bin\java"
set CEDAR_OPTS=
REM set CEDAR_OPTS=-Dsandbox.debug.port=8002 -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
IF ["%CEDAR_HOME%"]==[""] set CEDAR_HOME=c:\cedar
%JAVA_CMD% %CEDAR_OPTS% -Dcedar.home="%CEDAR_HOME%" -Djava.cmd=%JAVA_CMD% -cp "%CEDAR_HOME%\startup\bootstrap.jar" com.intel.cedar.util.CedarApplication %*
