#!/usr/bin/env bash

# Helper script for packing all related files,
# so it could faster to deploy new version to server (or test via docker infrastructure)
# Later this script should be replaced with SBT task

set -e

pushd `dirname $0` > /dev/null
ROOT_DIR=`dirname $(pwd -P)`

cd $ROOT_DIR
echo "Compiling..."
sbt "auginteEventSourcedJS/fullOptJS"
sbt "auginteEventSourcedJVM/pack"

echo "Getting version..."
VERSION=`cat project/Build.scala | grep "buildVersion" | awk '{print $4}' | sed 's/"//g'`
NAME="$(date +'%Y-%m-%dT%H-%M-%S')-v$VERSION"

echo "Copying..."
PATH="target/release/$NAME"
/bin/mkdir -p "$PATH/js"
JS_FILES=`/usr/bin/find js/target/scala-2.11/*.js | /bin/grep -v "fastopt"`
/bin/cp $JS_FILES "$PATH/js/"
/bin/cp -R jvm/target/pack $PATH/pack
/bin/cp -R jvm/target/web $PATH/web
/bin/rm -Rf $PATH/web/classes

REAL_PATH=`/usr/bin/realpath $PATH`
echo "Copied to: $REAL_PATH"