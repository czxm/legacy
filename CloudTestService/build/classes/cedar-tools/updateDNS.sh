#!/bin/sh

A=`host $1 | awk -F ' ' -- '{if ($4 != "found:") print substr($5,0,length($5)-1)}'`
PTR=`echo $1 | awk -F . -- '{printf "%s.%s.%s.%s.in-addr.arpa",$4,$3,$2,$1}'`
if [[ x$A != x ]]
then
expect <<EOF
set timeout 3600
spawn nsupdate
expect ">" {
send "update delete $A\r"
expect ">"
send "send\r"
expect ">"
send "update delete $PTR\r"
expect ">"
send "send\r"
expect ">"
send "quit\r"
}
EOF
fi
