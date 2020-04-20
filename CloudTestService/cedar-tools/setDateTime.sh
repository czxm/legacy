#!/bin/sh

newTimeZone=$1
newDate=$2
newTime=$3

rm -f /etc/localtime
ln -sf /usr/share/zoneinfo/$newTimeZone /etc/localtime

hwclock --set --date="$newDate $newTime"
hwclock --hctosys
