package me.staykwimp.jvd;

import java.util.ArrayList;
import java.util.function.BiFunction;


public class AddToQueueVisitor implements BaseVisitor<Void> {
    private final int defaultItag;
    private final String saveDirectory;
    private final BiFunction<Integer, YoutubeVideoDownloader, Integer> onInvalidItag;

    public AddToQueueVisitor(int defualtItag, String saveDirectory, BiFunction<Integer, YoutubeVideoDownloader, Integer> onInvalidItag) {
        this.defaultItag = defualtItag;
        this.saveDirectory = saveDirectory;
        this.onInvalidItag = onInvalidItag;
    }

    public Void visit(YoutubeVideoDownloader downloader) {
        int videoItag = defaultItag;
        while (!downloader.isValidItag(videoItag)) {
            videoItag = onInvalidItag.apply(videoItag, downloader);
        }
        DownloadQueue.addToQueue(new DecoratedDownloader(downloader, new DownloadVisitor(videoItag, saveDirectory)));
        return null;
    }

    public Void visit(YoutubePlaylistDownloader playlist) {
        ArrayList<YoutubeVideoDownloader> downloaders = playlist.getDownloadersFromPlaylist(saveDirectory);

        downloaders.forEach(d -> {
            d.accept(this);
        });
        return null;
    }
}
