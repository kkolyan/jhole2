#!/bin/sh
# resolve links - $0 may be a softlink
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`

PRGDIR="$PRGDIR/."

# build classpath
CP="$PRGDIR/classes"
CP="$CP:$PRGDIR/conf"
CP="$CP:$PRGDIR/bin"

for i in `ls $PRGDIR/lib/*.jar`
do
    CP=$CP:$i
done

JVM_ARGS=""
# JVM_ARGS="$JVM_ARGS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
# JVM_ARGS="$JVM_ARGS -ea"
JVM_ARGS="$JVM_ARGS -classpath $CP"

MAIN_CLASS=net.kkolyan.jhole2.JHoleClientJVM_ARGS $MAIN_CLASS