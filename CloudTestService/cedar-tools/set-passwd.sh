#!/bin/sh

PASSWD=$1
expect <<EOF
set timeout 3600
spawn passwd
expect "New UNIX password:" {
send "$PASSWD\r"
expect "Retype new UNIX password:"
send "$PASSWD\r"
}
expect eof
spawn vncpasswd /root/.vnc/passwd
expect "Password:" {
send "$PASSWD\r"
expect "Verify:"
send "$PASSWD\r"
}
expect eof
EOF


