#!/bin/bash

export VERSION=1.2.0

./gradlew app:shadowJar
mv app/build/libs/app-all.jar videos/jvd-$(echo $VERSION).jar
