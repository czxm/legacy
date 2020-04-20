#!/bin/sh
if [[ @${JAVA_HOME}@ == @@ ]] 
then
  echo "No JAVA_HOME defined!"
  exit 1
fi
export JAVA_CMD=${JAVA_HOME}/bin/java
export PATH=$JAVA_HOME/bin:$PATH

if [[ @${SOAK_HOME}@ == @@ ]] 
then
  export SOAK_HOME=`pwd`
fi

JARS=$SOAK_HOME/conf
for jar in `ls $SOAK_HOME/lib`
do
  JARS="$JARS:${SOAK_HOME}/lib/$jar"
done

export LD_LIBRARY_PATH="$EXT_LD:$LD_LIBRARY_PATH"
#SOAK_OPTS="-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8001"
$JAVA_CMD $SOAK_OPTS -Djava.library.path=${LD_LIBRARY_PATH} -Dsoak.home=${SOAK_HOME} -cp $JARS com.intel.bigdata.agent.Main $@
