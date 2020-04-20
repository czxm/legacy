#!/bin/sh
if [[ @${JAVA_HOME}@ == @@ ]] 
then
  export JAVA_HOME=/opt/cedar/jre
fi
export JAVA_CMD=${JAVA_HOME}/bin/java

if [[ @${CEDAR_HOME}@ == @@ ]] 
then
  export CEDAR_HOME=/opt/cedar
fi
CEDAR_OPTS=
#CEDAR_OPTS="-Dsandbox.debug.port=8002 -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8001"
$JAVA_CMD $CEDAR_OPTS -Dcedar.home=$CEDAR_HOME -Djava.cmd=$JAVA_CMD -cp $CEDAR_HOME/startup/bootstrap.jar com.intel.cedar.util.CedarApplication $@

