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

    public Void visit(YoutubeMusicPlaylistDownloader playlist) {
        for (YoutubeMusicDownloader downloader: playlist.getMusicDownloadersFromPlaylist(saveDirectory)) {
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

        downloader.deleteTemporaryDownloadFiles();
        return null;
    }

    
    // Not really different from YoutubeVideoDownloader, except that it also handles metadata.
    public Void visit(YoutubeMusicDownloader downloader) {
        int tempItag = defaultItag;
        boolean successfulDownload = false;

        while (!successfulDownload) {
            try {
                downloader.downloadAudio(tempItag, "audio");
                successfulDownload = true;
            } catch (NullPointerException e) {
                break;
            }
        }
        
        boolean thumbnailAvailable = downloader.saveThumbnail("album.png");
        downloader.convertAudioToMp3(downloader.getVideoTitle(), true, thumbnailAvailable);
        downloader.deleteTemporaryDownloadFiles();
        return null;
    }


    public int getDefaultItag() {
        return defaultItag;
    }
}
