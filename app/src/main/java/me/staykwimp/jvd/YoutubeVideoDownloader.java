package me.staykwimp.jvd;

import java.io.IOException;
import java.util.ArrayList;

import java.io.File;

import com.github.felipeucelli.javatube.*;
import com.github.felipeucelli.javatube.StreamQuery.Filter;

public class YoutubeVideoDownloader implements Downloader {
    private final Youtube youtube;
    private String saveDirectory;
    private String audioFilename;
    private String videoFilename;
    private String videoFileExtension;

    public YoutubeVideoDownloader(String url, String saveDirectory) throws Exception {
        youtube = new Youtube(url);
        this.saveDirectory = saveDirectory;
    }

    public <R> R accept(BaseVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public ArrayList<Stream> getVideoStreams(Filter filters) {
        try {
            return (ArrayList<Stream>) youtube.streams().filter(filters).getAll();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Stream>(); // returns empty streams list
        }
    }

    public Stream getStream(int itag) {
        try {
            return youtube.streams().getByItag(itag);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public StreamQuery filterStreams(Filter filters) throws Exception {
        return youtube.streams().filter(filters);
    }

    public void downloadVideo(int itag, String fileName) {
        if (videoFilename == null) {
            boolean validStream = false;
            label: try {
                Stream stream = youtube.streams().getByItag(itag);
                if (stream == null)
                    break label;
                
                DownloadProgessBar.ready(this.toString(stream));
                
                validStream = true;
                stream.download("", fileName, DownloadProgessBar::displayProgressBar);
                DownloadProgessBar.end(this.toString(stream));
                

                this.videoFilename = fileName + this.getFileExtension(itag);
                // this.videoFileExtension = this.getFileExtension(itag);
                this.videoFileExtension = ".mp4";
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (!validStream)
                throw new NullPointerException("Itag " + itag + " is not available for Youtube video");
        }
        else throw new NoSuchMethodError("downloadVideo can't be successfully called more than once per object.");
    }


    // downloads the audio of a youtube video
    // throws a NullPointerException when no audio stream is present
    public void downloadAudio(String fileName) throws NullPointerException {
        if (audioFilename == null) {
            boolean validStream = false;
            label: try {
                Stream stream = youtube.streams().getOnlyAudio();
                if (stream == null)
                    break label;


                DownloadProgessBar.ready(this.toString(stream));
                
                validStream = true;
                stream.download("", fileName, DownloadProgessBar::displayProgressBar);
                DownloadProgessBar.end(this.toString(stream));

                this.audioFilename = fileName + getFileExtension(youtube.streams().getOnlyAudio());
            } catch (org.json.JSONException e) {
                System.err.println("JSONException occured in JavaTube while trying to download " + this.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (!validStream)
                throw new NullPointerException("This Youtube video doesn't contain audio");
        }
        else throw new NoSuchMethodError("downloadAudio can't be successfully called more than once per object.");
    }

    public String getFileExtension(int itag) {
        try {
            return "." + youtube.streams().getByItag(itag).getSubType();  // getSubType returns the file extension
        } catch (Exception e) {
            e.printStackTrace();
            return ".mp4";
        }
    }

    public static String getFileExtension(Stream stream) {
        try {
            return "." + stream.getSubType();  // getSubType returns the file extension
        } catch (Exception e) {
            e.printStackTrace();
            return ".mp4";
        }
    }

    public String getChannelName() {
        try {
            String author = youtube.getAuthor().replaceAll("\\p{C}", "");

            // Edge case where the author is an empty string
            if (author.strip().isEmpty())
                return "No author";
            return author;
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown channel";
        }
    }

    public String getVideoTitle() {
        try {
            String title = youtube.getVidInfo().getJSONObject("videoDetails").getString("title").replaceAll("\\p{C}", ""); // remove control characters from the string.

            // Edge case where the video title is an empty string, or otherwise a collection of spaces:
            if (title.isEmpty())
                return "No title";
            return title;
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown video name";
        }
    }

    public long getVideoViews() {
        try {
            return youtube.getVidInfo().getJSONObject("videoDetails").getLong("viewCount");
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public long getVideoLikes() {
        throw new UnsupportedOperationException("Downloader can't get video likes (yet?)");
    }

    public String getUrl() {
        return youtube.getUrl().replaceAll("\\p{C}", "");
    }

    public String getSaveDirectory() {
        if (saveDirectory != null)
            return saveDirectory;
        return "";
    }

    public String getAudioFilename() {
        if (audioFilename != null)
            return audioFilename;
        return "";
    }

    public String getVideoFilename() {
        if (videoFilename != null)
            return videoFilename;
        return "";
    }

    public void mergeAudioAndVideoFile(String outputFilename) {
        if (videoFilename != null && audioFilename != null && videoFileExtension != null) {
            YoutubeVideoDownloader.mergeTwoFFmpegFiles(saveDirectory + videoFilename, saveDirectory + audioFilename, saveDirectory + safeFileName(outputFilename) + videoFileExtension);
        }
    }

    public boolean deleteTemporaryDownloadFiles() {
        // only delete temp files if user wants to
        if (!Main.deleteTempFiles)
            return true;
        
        
        boolean videoFileDeletion = false;
        boolean audioFileDeletion = false;

        if (saveDirectory != null) {
            if (videoFilename != null)
                videoFileDeletion = new File(saveDirectory + videoFilename).delete();
            if (audioFilename != null)
                audioFileDeletion = new File(saveDirectory + audioFilename).delete();
            
            return (videoFilename == null || videoFileDeletion) && (audioFilename == null || audioFileDeletion);
        }
        return false;
    }


    // Gets the song title.
    // Sometimes titles have a title of the form "artist - song name", this method will only return that song name (or the whole title if the title doesn't follow that pattern)
    private String getSongTitle() {
        String[] videoTitle = this.getVideoTitle().split(" - ");
        if (videoTitle.length == 1)
            return videoTitle[0];
        else
            return videoTitle[1];
    }

    // Creates a process builder used for converting audio to mp3.
    // If addMetadata is set to true, we also add metadata to the mp3 file.
    private ProcessBuilder createProcessBuilder(String outputFilename, boolean addMetadata) {
        String outputFile = saveDirectory + safeFileName(outputFilename) + ".mp3";
        if (addMetadata)
            return new ProcessBuilder("ffmpeg", "-i", (saveDirectory + audioFilename), "-c:a", "mp3", "-y", "-metadata", "title=" + this.getSongTitle(), "-metadata", "artist=" + this.getChannelName().replace(" - Topic", ""), outputFile);
        else
            return new ProcessBuilder("ffmpeg", "-i", (saveDirectory + audioFilename), "-c:a", "mp3", "-y", outputFile);
    }


    // Converts a downloaded Youtube stream to a .mp3 file.
    public void convertAudioToMp3(String outputFilename, boolean addMetadata) {
        if (audioFilename != null) {
            ProcessBuilder ffmpegProcessBuilder = createProcessBuilder(outputFilename, addMetadata);
            ffmpegProcessBuilder.redirectOutput(new File("ffmpeg.latest.log"))
                                // .redirectError(Redirect.INHERIT);
                                .redirectError(new File("ffmpeg.latest.log"));
            try {
                // ffmpegProcessBuilder.command().forEach(s -> System.out.println(s));
                Process ffmpegProcess = ffmpegProcessBuilder.start();
                ffmpegProcess.waitFor();
            } catch (InterruptedException e) {
                System.out.println("Got interrupted while ffmpeg is creating an mp3 file (how is this even possible?)");
            } catch (IOException e) {
                System.err.println("Error while starting ffmpeg process: " + e.toString());
                System.err.println("Output file name was: " + outputFilename);
                e.printStackTrace();
            }
        }
    }


    // Merges two downloaded Youtube streams into one file.
    // YouTube stores its audio track as a separate itag, so we must download it separately.
    // The idea behind this is to merge the video and audio file into one so it actually becomes watchable.
    public static void mergeTwoFFmpegFiles(String file1, String file2, String outputFile) {

        ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder("ffmpeg", "-i", file1, "-i", file2, "-c", "copy", "-y", outputFile);
        ffmpegProcessBuilder.redirectOutput(new File("ffmpeg.latest.log"))
                            // .redirectError(Redirect.INHERIT)
                            .redirectError(new File("ffmpeg.latest.log"));
        try {
            Process ffmpegProcess = ffmpegProcessBuilder.start();
            // System.out.println(ffmpegProcess.info());
            ffmpegProcess.waitFor();
        } catch (InterruptedException e) {
            System.out.println("Got interrupted while ffmpeg is merging files (how is this even possible?)");
        } catch (IOException e) {
            System.err.println("Error while starting ffmpeg process: " + e.toString());
            e.printStackTrace();
        }
    }

    // yoinked form JavaTube, removed the . replacement
    private static String safeFileName(String s){
        return s.replaceAll("[\"'#$%*,:;<>?\\\\^|~/]", " ");
    }

    public String toString() {
        return "YouTube video: " + this.getVideoTitle();
    }

    public String toString(Stream stream) {
        if (stream == null) {
            return this.toString();
        } else if (stream.getResolution() == null) {
            return this.toString() + "  |  audio  " + DownloadProgessBar.reduceSize(stream.getFileSize(), 2); 
        }
            
        return this.toString() + "  |  " + stream.getResolution() + "  " + DownloadProgessBar.reduceSize(stream.getFileSize(), 2);
    }

    public String toString(int itag) {
        return this.toString(this.getStream(itag));
    }

    /* 
     * Returns true if and only if the video can actually be accessed (is not private or age rescritected or member-only)
     */
    public boolean isValid() {
        try {
            youtube.getAuthor();
            youtube.streams();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
     * Returns true if and only if the itag supplied contains at least one stream
     */
    public boolean isValidItag(int itag) {
        try {
            return youtube.streams().getByItag(itag) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
