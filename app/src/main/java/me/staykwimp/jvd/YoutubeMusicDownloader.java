package me.staykwimp.jvd;

/*
    Java Video Downloader
    Copyright (C) 2026  StayKwimp

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

import java.io.IOException;
import java.net.URI;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
// import java.lang.ProcessBuilder.Redirect;
import java.io.File;

/*
    Adds additional functionality to YoutubeVideoDownloader,
    such as saving to .mp3 files and adding metadata to them,
    including thumbnail, song title and artist.
*/
public class YoutubeMusicDownloader extends YoutubeVideoDownloader {
    protected String thumbnailFilename; // only set when saveThumbnail method returns true.

    public YoutubeMusicDownloader(String url, String saveDirectory) throws Exception {
        super(url, saveDirectory);
    }

    protected YoutubeMusicDownloader(YoutubeVideoDownloader downloader) {
        super();
        this.youtube = downloader.youtube;
        this.saveDirectory = downloader.saveDirectory;
        this.audioFilename = downloader.audioFilename;
        this.videoFilename = downloader.videoFilename;
        this.videoFileExtension = downloader.videoFileExtension;
    }

    public <R> R accept(BaseVisitor<R> visitor) {
        return visitor.visit(this);
    }

    // Gets the song title.
    // Sometimes titles have a title of the form "artist - song name", this method will only return that song name (or the whole title if the title doesn't follow that pattern)
    private String getSongTitle() {
        String[] videoTitle = this.getVideoTitle().split(" - ", 2);
        if (videoTitle.length == 1)
            return videoTitle[0];
        else
            return videoTitle[1];
    }

    // Gets the Thumbnail URL.
    public String getThumbnaillURL() throws Exception {
        return youtube.getThumbnailUrl();
    }

    // Cheesy way to download a specific audio stream is just to re-use the video download code.
    public void downloadAudio(int itag, String fileName) {
        if (audioFilename != null)
            throw new UnsupportedOperationException("downloadAudio can't be called more than once per object.");
        
        downloadVideo(itag, fileName);
        audioFilename = videoFilename;
    }

    // Saves the video thumbnail to a file.
    // Based off of https://stackoverflow.com/questions/10292792/getting-image-from-url-java
    public boolean saveThumbnail(String fileName) {
        try {
            String thumbnailURL = getThumbnaillURL();
            URI uri = new URI(thumbnailURL);
            InputStream is = uri.toURL().openStream();
            OutputStream os = new FileOutputStream(saveDirectory + fileName);

            byte[] buffer = new byte[2048];
            int bytesRead = 0;

            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            is.close();
            os.close();
            
            this.thumbnailFilename = fileName;
        }
        catch (Exception e) {
            System.err.println("Exception thrown while trying to save thumbnail:");
            e.printStackTrace();
            return false;
        }

        
        return true;
    }

    // Creates a process builder used for converting audio to mp3.
    // If addMetadata is set to true, we also add metadata to the mp3 file.
    // We only add album art if and only if addMetadata and addThumbnail are true.
    private ProcessBuilder createProcessBuilder(String outputFilename, boolean addMetadata, boolean addThumbnail) {
        String outputFile = saveDirectory + safeFileName(outputFilename) + ".mp3";
        if (addMetadata)
            if (addThumbnail)
                return new ProcessBuilder("ffmpeg", 
                                            "-i", (saveDirectory + audioFilename), 
                                            "-i", (saveDirectory + thumbnailFilename),
                                            "-c:a", "mp3", 
                                            "-y", 
                                            "-map", "0",
                                            "-map", "1",
                                            "-metadata", "title=" + this.getSongTitle(), 
                                            "-metadata", "artist=" + this.getChannelName().replace(" - Topic", ""), 
                                            outputFile);
            else
                return new ProcessBuilder("ffmpeg", 
                                            "-i", (saveDirectory + audioFilename), 
                                            "-c:a", "mp3", 
                                            "-y", 
                                            "-metadata", "title=" + this.getSongTitle(), 
                                            "-metadata", "artist=" + this.getChannelName().replace(" - Topic", ""), 
                                            outputFile);
        else
            return new ProcessBuilder("ffmpeg", "-i", (saveDirectory + audioFilename), "-c:a", "mp3", "-y", outputFile);
    }

    // Converts a downloaded Youtube stream to a .mp3 file.
    public void convertAudioToMp3(String outputFilename, boolean addMetadata, boolean addThumbnail) {
        System.out.println("audioFilename = " + audioFilename);
        if (audioFilename != null) {
            ProcessBuilder ffmpegProcessBuilder = createProcessBuilder(outputFilename, addMetadata, addThumbnail);
            File outputFile = new File("ffmpeg.latest.log");
            ffmpegProcessBuilder
                                // .redirectOutput(Redirect.INHERIT)
                                .redirectOutput(outputFile)
                                // .redirectError(Redirect.INHERIT);
                                .redirectError(outputFile);
            try {
                // ffmpegProcessBuilder.command().forEach(s -> System.out.println(s));
                Process ffmpegProcess = ffmpegProcessBuilder.start();
                ffmpegProcess.waitFor();
            } catch (InterruptedException e) {
                System.err.println("Got interrupted while ffmpeg is creating an mp3 file (how is this even possible?)");
            } catch (IOException e) {
                System.err.println("Error while starting ffmpeg process: " + e.toString());
                System.err.println("Output file name was: " + outputFilename);
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean deleteTemporaryDownloadFiles() {
        // only delete temp files if user wants to
        if (!Main.deleteTempFiles)
            return true;

        boolean success = super.deleteTemporaryDownloadFiles();
        
        if (thumbnailFilename != null) {
            return success & new File(saveDirectory + thumbnailFilename).delete();
        }
        else return success;
    }
}
