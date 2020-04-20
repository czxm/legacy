#!/bin/sh

DEV=$1
DIR=$2
umount $DIR
#don't remove the mount point in case unmount failed
#rm -rf $DIR 2>/dev/null
