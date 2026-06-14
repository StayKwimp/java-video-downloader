#!/usr/bin/env bash

export VERSION=1.3.0b1
export JVD=jvd-$(echo $VERSION).jar

./gradlew app:shadowJar
mv app/build/libs/app-all.jar $JVD

echo "Adding files to .tar.gz archive for release..."
tar -czf jvd-$(echo $VERSION).tar.gz $JVD LICENSE

echo "Creating videos directory and putting the built .jar in it."
mkdir -p videos
mv $JVD videos
echo -e "#!/usr/bin/env bash\njava -jar $JVD \$@" > videos/jvd
chmod +x videos/jvd
