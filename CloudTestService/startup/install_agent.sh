#!/bin/bash

if [[ @${CEDAR_HOME}@ == @@ ]] 
then
  export CEDAR_HOME=/opt/cedar
fi
JSVC=${CEDAR_HOME}/startup/jsvc`uname -m`

cp $CEDAR_HOME/startup/CedarAgent /etc/init.d
chmod +x $CEDAR_HOME/lib/linux32/*.so $CEDAR_HOME/lib/linux64/*.so
chmod +x /etc/init.d/CedarAgent $JSVC $CEDAR_HOME/bin/*.sh $CEDAR_HOME/bin/*.pl 2>/dev/null
if [[ -x `which update-rc.d 2>/dev/null` ]]
then
  update-rc.d CedarAgent defaults
else
  chkconfig --add CedarAgent
  chkconfig CedarAgent on
fi
