#!/bin/sh

if [[ @${CEDAR_HOME}@ == @@ ]] 
then
  export CEDAR_HOME=/opt/cedar
fi

if [[ -x `which update-rd.c 2>/dev/null` ]]
then
  update-rc.d -f CedarService remove
  rm -f /etc/init.d/CedarService
else
  chkconfig CedarService off
  chkconfig --del CedarService
fi
