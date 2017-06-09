#!/bin/bash

if [ "$JAVA_HOME" = "" ]; then
  echo "Error: JAVA_HOME is not set."
  exit 1
fi

bin=`dirname "$0"`

export FOOLISHGOAT_SERVER_HOME=`cd $bin/../; pwd`  

FOOLISHGOAT_CONF_DIR=$FOOLISHGOAT_SERVER_HOME/conf
CLASSPATH="${FOOLISHGOAT_CONF_DIR}"

for f in $FOOLISHGOAT_SERVER_HOME/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

LOG_DIR=${FOOLISHGOAT_SERVER_HOME}/logs

CLASS=com.foolishgoat.server.ServerMain
nohup ${JAVA_HOME}/bin/java -classpath "$CLASSPATH" $CLASS > ${LOG_DIR}/foolishgoat.$(date +%y-%m-%d).log 2>&1 < /dev/null &