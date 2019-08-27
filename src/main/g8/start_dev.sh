#!/bin/sh

## Start application in dev mode with remote debugging
unset SBT_OPTS && javac -version && sbt clean compile
#export SBT_OPTS="-server -Xlog:gc -XX:+ExitOnOutOfMemoryError -XX:+CrashOnOutOfMemoryError -XX:+UseG1GC -Xms16m -Xmx1234m -XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=10 -XX:-ShrinkHeapInSteps"
#export SBT_OPTS="-server -Xlog:gc -XX:+ExitOnOutOfMemoryError -XX:+CrashOnOutOfMemoryError -XX:+UseParallelGC -Xms16m -Xmx1234m -XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=10"
sbt -jvm-debug 9999 \
    -Dlogback.configurationFile=conf/logback-dev.xml \
    run
