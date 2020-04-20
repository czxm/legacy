#!/bin/sh
#workaroud to fix the blocking issue caused by RHEL5.4's virtio driver

DEV=$1
while [[ ! -e $DEV ]]
do
  sleep 1
done
rmmod virtio_net
modprobe virtio_net
