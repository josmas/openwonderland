#!/bin/sh
#
which=`which $0`
dirname=`dirname $which`
basedir=`(cd $dirname/.. ; pwd)`

antdir=${basedir}/ant
antclasspath=${antdir}/lib/ant-launcher.jar

java -cp ${antclasspath} -Dant.home=${antdir} -Dwl.root.dir=${basedir} \
    org.apache.tools.ant.launch.Launcher -f ${antdir}/run.xml @TARGET@ $*
