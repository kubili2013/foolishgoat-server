#!/bin/bash

server_pid=`ps -A |grep "java"| awk '{if($7 == "'com.foolishgoat.server.ServerMain'") {print $1}}'`

if [ "${server_pid}" = "" ]; then
  echo "no foolishgoat server is running."
  exit 1
fi

kill -9 ${server_pid}