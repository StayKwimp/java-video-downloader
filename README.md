# Java Video Downloader
This downloader allows anyone to locally download videos and music. Currently only YouTube is supported, but I'm planning on adding additional platforms in the future.

# Usage
The downloader requires Java 21 (newer versions are untested). It also requires an ffmpeg installation accessable in your PATH environment (if unsure, try to run `ffmpeg` from the command line: if it fails, you haven't set it up correctly).

# Beta
There exists a beta branch for this project which includes the latest development version. I guarantee that there are bugs in these builds, so proceed with caution when running those versions.

# Builds
Built .jar files are available in the [Releases](https://github.com/StayKwimp/java-video-downloader/releases) section of this repository. 

# Building
1. Clone the repository
2. Navigate to the root repository folder
3. On Unix: make sure `gradlew` is executable (`chmod +x ./gradlew`). Always make sure that what you're flagging as executable is safe!
4. Build the project with  `./gradlew app:shadowJar`. This will include any neccesary dependencies in the jar file.
5. The built jar is now available in the `app/build/libs/` directory

# Credits
To make downloading from YouTube possible, I've used the JavaTube library available on GitHub. You can find the source code at [https://github.com/felipeucelli/JavaTube](https://github.com/felipeucelli/JavaTube). Many thanks to them!

