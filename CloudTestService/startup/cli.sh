#!/bin/sh
if [[ @${JAVA_HOME}@ == @@ ]] 
then
  export JAVA_HOME=/opt/cedar/jre
fi
export JAVA_CMD=${JAVA_HOME}/bin/java
export PATH=$JAVA_HOME/bin:$PATH

if [[ @${CEDAR_HOME}@ == @@ ]] 
then
  export CEDAR_HOME=/opt/cedar
fi

JARS=.:$CEDAR_HOME/conf
for jar in `ls $CEDAR_HOME/lib`
do
  JARS="$JARS:${CEDAR_HOME}/lib/$jar"
done

$JAVA_CMD -cp $JARS com.intel.cedar.agent.impl.AgentTool $@
