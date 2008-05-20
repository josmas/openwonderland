#!/bin/bash
#
# A script to do nightly builds for a given system.
#
# The script works by doing a fresh cvs checkout of the entire project and
# then building everything from scratch.
#
# The script builds 4 type of bundles
#   1. ZIP bundle with audio
#   2. ZIP bundle without audio
#   3. Platform specific installable package with audio
#   4. Platform specific installable package without audio
#
# Once the bundles are built the script also copies ("ant pkg-copy") them to a specified
# target directory ("pkg-copy.basedir" property)
#

# The root dir where the checkout and build happens.
#
BUILDROOT=$HOME/wonderland/nightly
PKGSROOT=$BUILDROOT/pkgs

CVS_HOST=cvs.dev.java.net
CVS_ROOT=/cvs
CVS_USER=guest

voicebridge=0
voicebridge_url=

usage()
{
    echo
    echo "$0 [options...]"
    echo "    -br <buildroot>: Root dir where checkout/build is done [$BUILDROOT]"
    echo "    -pr <pkgsroot> : Root dir where the pkg bundles are saved [$PKGSROOT]"
    echo
    echo "    +vb/-vb        : enable/disable voicebridge"
    echo "    -au <url>      : URL of where the wonderland audio jars/data is"
    echo
    echo "    -ch <cvs-host> : Specify the CVS hostname [$CVS_HOST]"
    echo "    -cr <cvs-root> : Specify CVS root [$CVS_ROOT]"
    echo "    -cu <cvs-user> : Specify CVS username [$CVS_USER]"
    echo
    echo "    -help          : print this help"
    echo

    exit 0
}

while [ $# -gt 0 ]
do
    case "$1" in
    -br)	BUILDROOT=$2		; shift	;;
    -pr)	PKGSROOT=$2		; shift	;;
    -au)	voicebridge_url=$2	; shift ;;
    +vb)	voicebridge=1		;;
    -vb)	voicebridge=0		;;
    -ch)	CVS_HOST=$2		; shift	;;
    -cu)	CVS_USER=$2		; shift	;;
    -cr)	CVS_ROOT=$2		; shift	;;
    -*)		usage $0		;;
    esac

    shift
done

# We will now copy the built pkgs/bundles over to $PKGSROOT
if [ "`uname -s | cut -d_ -f1`" = "CYGWIN" ]; then
    PKGSROOT=`cygpath -m ${PKGSROOT}`
fi

mkdir -p $BUILDROOT
cd $BUILDROOT
/bin/rm -rf lg3d-wonderland

if [ -d lg3d-wonderland ]; then
   echo "*** Could not cleanly remove lg3d-wonderland"
   update=1
else
   update=0
fi

echo
echo "Checking out lg3d-wonderland..."

cvs -d :pserver:${CVS_USER}@${CVS_HOST}:${CVS_ROOT} checkout lg3d-wonderland
cd lg3d-wonderland

if [ $update -eq 1 ]; then
   echo
   echo "Updating lg3d-wonderland..."

   cvs -d :pserver:${CVS_USER}@${CVS_HOST}:${CVS_ROOT} update
fi

antdefopts="-Dbuild.level=opt -Dbuild.debug=true -Dpkg-copy.basedir=${PKGSROOT}"

if [ "$voicebridge_url" != "" ]; then
    antdefopts="${antdefopts} -Dvoicebridge.audio.url=${voicebridge_url}"
fi

#
# Always build the bundles without the voice bridge stuff
#
#ant -Dvoicebridge.enabled=false $antdefopts pkg-all pkg-copy

ant -Dvoicebridge.enabled=true $antdefopts pkg-all pkg-copy
