package me.staykwimp.jvd;

public class DownloadVisitor implements BaseVisitor<Void> {
    private final int defaultItag;
    private final String saveDirectory;
    // private final BiFunction<Integer, YoutubeVideoDownloader, Integer> onInvalidItag;


    public DownloadVisitor(int defaultItag, String saveDirectory) {
        this.defaultItag = defaultItag;
        this.saveDirectory = saveDirectory;
    }


    // Downloads a Youtube Playlist
    // Playlists are regarded as a list of YoutubeVideoDownloaders
    public Void visit(YoutubePlaylistDownloader playlist) {
        for (YoutubeVideoDownloader downloader: playlist.getDownloadersFromPlaylist(saveDirectory)) {
            if (!downloader.isValid()) {
                System.out.println("Skipping YouTube URL " + downloader.getUrl() + " because it is private or otherwise inaccessible.");
                continue;
            }
            System.out.println("Downloading " + downloader.getVideoTitle());
            downloader.accept(this);
        }
        return null;
    }

    
    // Downloads a youtube video
    public Void visit(YoutubeVideoDownloader downloader) {
        if (defaultItag == -1) {  // itag -1 has audio only
            try {
                downloader.downloadAudio("audio");
                downloader.convertAudioToMp3(downloader.getVideoTitle(), true);
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
                    // tempItag = onInvalidItag.apply(defaultItag, downloader);
                    break; // skip any invalid videos (invalid itags are handled by addToQueueVisitor)
                }
            }

            downloader.downloadAudio("audio");
            downloader.mergeAudioAndVideoFile(downloader.getVideoTitle());
        }

        downloader.deleteTemporaryDownloadFiles();
        return null;
    }


    public int getDefaultItag() {
        return defaultItag;
    }
}
