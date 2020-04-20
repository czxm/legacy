#!/bin/sh

ARCH=-m32
#ARCH=

g++ $ARCH -fPIC -g -c -Wall *.cpp -I$JAVA_HOME/include -I$JAVA_HOME/include/linux
g++ $ARCH -shared -Wl,-soname,libjobimpl.so -o libjobimpl.so *.o -lc
cp libjobimpl.so ../..

