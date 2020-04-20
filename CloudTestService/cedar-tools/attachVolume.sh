#!/bin/sh

DEV=/dev/`fdisk -l 2>/dev/null | grep Disk | grep vd | tail -n 1 | perl -n -e '$_=~s/.*(vd[a-z]).*/$1/;print $_'`
echo "attaching $DEV"
DIR=$2
mkdir $DIR 2>/dev/null
mount ${DEV}1 $DIR
chmod 777 $DIR