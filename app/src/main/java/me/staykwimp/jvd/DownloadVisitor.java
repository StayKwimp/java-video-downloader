package me.staykwimp.jvd;

public class DownloadVisitor implements BaseVisitor<Void> {
    private final int itag;
    private final String saveDirectory;
    private final boolean onlyAudio;
    

    public DownloadVisitor(int itag, boolean onlyAudio, String saveDirectory) {
        this.itag = itag;
        this.onlyAudio = onlyAudio;
        this.saveDirectory = saveDirectory;
    }

    public Void visit(YoutubeVideoDownloader downloader) {
        if (onlyAudio) {
            downloader.downloadAudio("audio");
            downloader.convertAudioToMp3(downloader.getVideoTitle());
        }
        else {
            downloader.downloadVideo(this.itag, "video");
            downloader.downloadAudio("audio");
            downloader.mergeAudioAndVideoFile(downloader.getVideoTitle());
        }
        
        downloader.deleteTemporaryDownloadFiles();
        return null;
    }

    // downloads a whole playlist with a set itag
    public Void visit(YoutubePlaylistDownloader playlist) {        
        for (YoutubeVideoDownloader downloader: playlist.getDownloadersFromPlaylist(saveDirectory)) {
            downloader.accept(this);
        }
        return null;
    }
}
