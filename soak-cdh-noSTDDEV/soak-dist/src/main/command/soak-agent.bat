@echo off
IF NOT DEFINED JAVA_HOME echo "No JAVA_HOME found!" && exit /b
set JAVA_CMD="%JAVA_HOME%\bin\java"
set PATH="%JAVA_HOME%\bin";%PATH%
IF NOT DEFINED SOAK_HOME set SOAK_HOME=%CD%

setLocal EnableDelayedExpansion
SET CLASSPATH="conf"
FOR /R lib %%a in (*.jar) do (
   set CLASSPATH=!CLASSPATH!;lib\%%~na.jar
)
REM echo !CLASSPATH!

REM set SOAK_OPTS="-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8001"
%JAVA_CMD% %SOAK_OPTS% -Dsoak.home="%SOAK_HOME%" -cp %CLASSPATH% com.intel.bigdata.agent.Main %*