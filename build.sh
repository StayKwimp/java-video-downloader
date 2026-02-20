#!/bin/bash

export VERSION=1.2.1
export JVD=jvd-$(echo $VERSION).jar

./gradlew app:shadowJar
mv app/build/libs/app-all.jar $JVD

echo "Adding files to .tar.gz archive for release..."
tar -czf jvd-$(echo $VERSION).tar.gz $JVD LICENSE

echo "Creating videos directory and putting the built .jar in it."
mkdir videos
mv $JVD videos
