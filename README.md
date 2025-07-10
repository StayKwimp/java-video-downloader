# Java Video Downloader
This downloader allows anyone to locally download videos and music. Currently only YouTube is supported, but I'm planning on adding additional platforms in the future.

# Usage
The downloader requires Java 21 (newer versions are untested). It also requires an ffmpeg installation accessable in your PATH environment (if unsure, try to run `ffmpeg` from the command line: if it fails, you haven't set it up correctly). 

# Building
1. Clone the repository
2. Navigate to the root repository folder
3. On Unix: make sure `gradlew` is executable (`chmod +x ./gradlew`)
4. Build the project with  `./gradlew app:shadowJar`. This will include any neccesary dependencies in the jar file.
5. The built jar is now available in the `app/build/libs/` directory

# Credits
To make downloading from YouTube possible, I've used the JavaTube library available on GitHub. Many thanks to them!
