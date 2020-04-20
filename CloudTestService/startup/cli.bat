ECHO OFF
IF ["%JAVA_HOME%"]==[""] set JAVA_HOME=c:\cedar\jre
set JAVA_CMD="%JAVA_HOME%\bin\java"
set PATH="%JAVA_HOME%\bin";%PATH%

IF ["%CEDAR_HOME%"]==[""] set CEDAR_HOME=c:\cedar
PUSHD "%CEDAR_HOME%\lib"
%JAVA_CMD% -Dcedar.home="%CEDAR_HOME%" -cp "%CEDAR_HOME%\conf";cedar-1.0.jar;apache-mime4j-0.6.jar;httpclient-4.1.jar;httpcore-4.1.jar;httpmime-4.1.jar;xstream-1.3.1.jar;commons-codec-1.4.jar;commons-fileupload-1.2.2.jar;org.apache.commons.lang_2.4.0.v20081016-1030.jar;log4j-1.2.16.jar;slf4j-api-1.6.1.jar;slf4j-log4j12-1.6.1.jar;commons-logging.jar;bcprov.jar com.intel.cedar.agent.impl.AgentTool %* 
POPD
