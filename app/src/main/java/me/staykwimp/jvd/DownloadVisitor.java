package me.staykwimp.jvd;

import java.util.function.BiFunction;

public class DownloadVisitor implements BaseVisitor<Void> {
    private final int defaultItag;
    private final String saveDirectory;
    private final BiFunction<Integer, YoutubeVideoDownloader, Integer> onInvalidItag;


    public DownloadVisitor(int defaultItag, String saveDirectory, BiFunction<Integer, YoutubeVideoDownloader, Integer> onInvalidItag) {
        this.defaultItag = defaultItag;
        this.saveDirectory = saveDirectory;
        this.onInvalidItag = onInvalidItag;
    }


    public Void visit(YoutubePlaylistDownloader playlist) {
        for (YoutubeVideoDownloader downloader: playlist.getDownloadersFromPlaylist(saveDirectory)) {
            System.out.println("Downloading " + downloader.getVideoTitle());
            downloader.accept(this);
        }
        return null;
    }


    // can also be used to more safely
    public Void visit(YoutubeVideoDownloader downloader) {
        if (defaultItag == -1) {  // itag -1 has audio only
            try {
                downloader.downloadAudio("audio");
                downloader.convertAudioToMp3(downloader.getVideoTitle());
            } catch (NullPointerException e) {
                System.out.println("Video does not have audio!");
                return null;
            }
            
        } else {
            boolean successfulDownload = false;
            int tempItag = defaultItag;

            while (!successfulDownload) {
                try {
                    downloader.downloadVideo(tempItag, "video");
                    successfulDownload = true;
                } catch (NullPointerException e) {
                    tempItag = onInvalidItag.apply(defaultItag, downloader);
                }
            }

            downloader.downloadAudio("audio");
            downloader.mergeAudioAndVideoFile(downloader.getVideoTitle());
        }

        downloader.deleteTemporaryDownloadFiles();
        return null;
    }
}
