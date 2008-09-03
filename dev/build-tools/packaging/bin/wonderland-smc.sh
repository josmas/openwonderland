#!/bin/bash
#
which=`which $0`
dirname=`dirname $which`
basedir=`(cd $dirname/.. ; pwd)`

antdir=${basedir}/ant
antclasspath=${antdir}/lib/ant-launcher.jar

# start an Xvfb to run the server master client in
unset XAUTHORITY

# Check for Xvfb already running
set xvfb_proc = `ps -ef | fgrep "Xvfb :1" | fgrep -v grep`
if [ "$xvfb_proc" = "" ]; then
    if [ `uname` == SunOS ]; then
        ${basedir}/bin/Xvfb :1 &
    else
        ${basedir}/bin/Xvfb.xorg :1 &
    fi
    sleep 1
fi

export DISPLAY=:1

java -cp ${antclasspath} -Dant.home=${antdir} -Dwl.root.dir=${basedir} \
    org.apache.tools.ant.launch.Launcher -f ${antdir}/run.xml run-smc -Dj3d.rend=noop
