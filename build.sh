#!/bin/bash

export VERSION=1.2.1
export JVD=jvd-$(echo $VERSION).jar

./gradlew app:shadowJar
mv app/build/libs/app-all.jar $JVD
tar -czvf jvd-$(echo $VERSION).tar.gz $JVD LICENSE
mv $JVD videos
