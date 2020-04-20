#!/bin/sh

if [[ @${CEDAR_HOME}@ == @@ ]] 
then
  export CEDAR_HOME=/opt/cedar
fi

DEV=/dev/`fdisk -l 2>/dev/null | grep Disk | grep vd | tail -n 1 | perl -n -e '$_=~s/.*(vd[a-z]).*/$1/;print $_'`

echo "creating $DEV"
DIR=$2
expect <<EOF
set timeout 3600
spawn fdisk $DEV
expect "Command (m for help):" {
send "d\r"
send "n\r"
expect "(1-4)"
send "p\r"
expect "(1-4)"
send "1\r"
expect "default 1):"
send "\r"
expect "Last cylinder"
send "\r"
expect "Command (m for help):"
send "w\r"
}
expect eof
EOF
mkfs.ext3 ${DEV}1
mkdir $DIR 2>/dev/null
mount ${DEV}1 $DIR
chmod 777 $DIR
