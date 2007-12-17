#!/bin/sh

#
# resolve symlinks
#

PRG=$0

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
        PRG="$link"
    else
        PRG="`dirname "$PRG"`/$link"
    fi
done

progdir=`dirname "$PRG"`

# should be dynamic?
java_home=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home
export JAVA_HOME=$java_home

# mac os has built-in ant
ant_cmd="ant -lib \"$progdir/Contents/Resources/ant/lib\" -Dwl.root.dir=\"$progdir/Contents/Resources\" -f \"$progdir/Contents/Resources/ant/run.xml\" run-bridge"
echo "Executing: $ant_cmd"
$ant_cmd
