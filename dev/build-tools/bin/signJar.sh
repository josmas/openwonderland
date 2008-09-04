#!/bin/bash -f

# parameters  <file> <keystore> <passwd>

pack200 -J-Xmx256m --repack $1
jarsigner -keystore $2 -storepass $3 $1  lgSig
pack200 -J-Xmx256m $1.pack.gz $1
