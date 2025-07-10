# Java Video Downloader
This downloader allows anyone to locally download videos and music. It is a Java CLI program which supports commands. See [Usage](#usage) for more information. Currently only YouTube (music) is supported, but I'm planning on adding additional platforms in the future.

# A fair warning
Usage of this video downloader without prior authorisation violates YouTube's Terms of Serivce, and the downloading of certain videos may be an infringement of the creator's copyright and/or intellectual property. Use at your own discretion. Risks may include IP or account bans if you're not careful (such as with any YouTube downloader). It is provided to GitHub for educational purposes only and must strictly not be used in any commercial setting. I am not to be held responsible for any damages resulting from usage of this program.

# Usage
The downloader requires Java 21 (newer versions are untested). It also requires an ffmpeg installation accessable in your PATH environment (if unsure, try to run `ffmpeg` from the command line: if it fails, you haven't set it up correctly).
Running the .jar is as simple as `java -jar <jar file name>`, or double clicking it in a file browser.
Supported commands inside the program are:
```
    help            - views a help page
    queue <URL>     - queues a video from a given url
    playlist <URL>  - adds a whole playlist to the queue
    view            - views the download queue and what's currently downloading
    show-progress   - enters a special mode where the download progress is continuosly displayed
    remove <index>  - removes a video at a given index (to know which video is on which index, run the view command)
    quit            - quits the program, but waits for the video that's currently downloading to finish (Ctrl+C forcefully terminates the program)
    debug           - shows some debug information
```

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

