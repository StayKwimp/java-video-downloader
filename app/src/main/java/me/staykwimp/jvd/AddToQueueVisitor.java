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

    public Void visit(YoutubeMusicPlaylistDownloader playlist) {
        ArrayList<YoutubeMusicDownloader> downloaders = playlist.getMusicDownloadersFromPlaylist(saveDirectory);

        downloaders.forEach(d -> {
            d.accept(this);
        });
        return null;
    }

    public Void visit(YoutubeMusicDownloader downloader) {
        int audioItag = defaultItag;
        while (!downloader.isValidItag(audioItag)) {
            audioItag = onInvalidItag.apply(audioItag, downloader);
        }
        DownloadQueue.addToQueue(new DecoratedDownloader(downloader, new DownloadVisitor(audioItag, saveDirectory)));
        return null;
    }
}
