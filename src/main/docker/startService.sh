#!/bin/sh

cd /opt/app
if [ -z "${java_runtime_arguments}" ]; then
    java -Dlogging.config=config/logback.xml -Xms128m -Xmx512m -jar /opt/app/lib/pomba-network-discovery-context-builder.jar
else
    java -Dlogging.config=config/logback.xml $java_runtime_arguments -jar /opt/app/lib/pomba-network-discovery-context-builder.jar
fi