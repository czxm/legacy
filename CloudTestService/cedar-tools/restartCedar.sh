#!/bin/sh

/etc/init.d/CedarAgent stop
#this is only a workaround, a correct solution is to create an image configured for best performance
ulimit -n 353555
#below is also a workaround
[[ ! -e /etc/sudoers.changed ]] && touch /etc/sudoers.changed && perl -i -np -e 's/Defaults.+requiretty/#Defaults requiretty/g' /etc/sudoers
#again, workaround for SLES11
modprobe acpiphp 2>/dev/null
#make sure agent is terminated
if [[ -f /var/run/cedar_agent.pid ]]
then
  kill -9 `cat /var/run/cedar_agent.pid`
  rm -f /var/run/cedar_agent.pid
fi
/etc/init.d/CedarAgent start
