#!/bin/sh
set -e

function print_usage() {
    echo "Usage: ./autobuild.sh [OPTION]...<RPM|ISO>"
    echo "Build IDH <RPM|ISO>"
    echo -e "\nValid options are:"
    echo -e "-l  <language>\trelease package language <CH|EN>, defaults to CH"
    echo -e "-n  <name>\trelease package name prefix, defaults to intelhadoop"
    echo -e "-v  <version>\trelease package major version, defaults to 2.0"
    echo -e "-o  <os>\trelease package OS version, defaults to el6.x86_64"
    echo -e "-r  <revision>\thadoop core svn revision, defaults to HEAD"
    echo -e "-d\t\tEnable the debugging mode, which will include source\n\t\tcode for intelcloudui"
    echo -e "-h\t\tdisplay this help and exit"
}


#init
build_type=RPM
iso_language=CH
iso_name_prefix=intelhadoop
iso_release_version=2.0
iso_os_version=el6.x86_64
core_svn_revision=HEAD
iso_debug_str=

while [ $# -gt 0 ]
do
    case "$1" in
      RPM|ISO) build_type="$1";;
      -l) iso_language="$2"; shift;;
      -n) iso_name_prefix="$2"; shift;;
      -v) iso_release_version="$2"; shift;;
      -o) iso_os_version="$2"; shift;;
      -r) core_svn_revision="$2"; shift;;
      -d) iso_debug_str="-d";; 
      -h)
          print_usage
          exit 0;;
      *)
          print_usage
          exit 0;;
    esac
    shift
done

echo "++build_type: $build_type"
echo "++iso_language: $iso_language"
echo "++iso_name_prefix: $iso_name_prefix"
echo "++iso_release_version: $iso_release_version"
echo "++iso_os_version: $iso_os_version"
echo "++core_svn_revision: $core_svn_revision"
echo "++iso_debug_str: $iso_debug_str"

#exit 0;
dir_IDH=IDH$iso_release_version
dir_BUILD_TOOLS=BUILD_TOOLS
dir_for_build_iso=gadget


ROOT_DIR=`pwd`
echo "++ROOT_DIR: $ROOT_DIR ++"

#make rpm
if [ ! -d $ROOT_DIR/$dir_IDH ]; then
	echo "IDH source directory $ROOT_DIR/$dir_IDH is not found! Please check out it first!"
	exit 1
fi

if [ ! -d $ROOT_DIR/$dir_BUILD_TOOLS ]; then
        echo "BUILD_TOOLS directory $ROOT_DIR/$dir_BUILD_TOOLS is not found! Please check out it first!"
        exit 1
fi
cd $ROOT_DIR/$dir_IDH
iso_svn_revision=`svn info |grep Revision | cut -d" " -f2`
rm -rf output
source $ROOT_DIR/$dir_BUILD_TOOLS/bashrc
make clean
if [ "$iso_language" != "CH" ]; then
	./mk_us_rpm.sh
else
	make rpm
fi

#install rpm
cd $ROOT_DIR
dir_RPMS=$ROOT_DIR/$dir_IDH/output
if [ ! -d $dir_RPMS ]; then
        echo "Output directory for RPMS $dir_RPMS is not found! Please check it first!"
        exit 1
fi
REQUIRED_PACKAGES="hadoop zookeeper hbase hive"
for pkg in $REQUIRED_PACKAGES
do
	rpm -ivh $dir_RPMS/$pkg/$pkg-* --force
done

#build iso
if [ "$build_type" != "RPM" ]; then
if [ ! -d $ROOT_DIR/$dir_for_build_iso ]; then
        echo "directory $ROOT_DIR/$dir_for_build_iso is not found! Please check out it first!"
        exit 1
fi
	cd $ROOT_DIR/$dir_for_build_iso
	rm -rf $iso_name_prefix
	rm -rf temp
	mkdir -p $iso_name_prefix
	./buildiso.sh $iso_language $iso_name_prefix -a $dir_RPMS -n $iso_name_prefix -v $iso_release_version -o $iso_os_version -r $iso_svn_revision -y $iso_debug_str
fi
