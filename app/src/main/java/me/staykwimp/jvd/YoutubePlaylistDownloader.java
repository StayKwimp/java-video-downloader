package me.staykwimp.jvd;

import java.util.ArrayList;

import com.github.felipeucelli.javatube.*;

public class YoutubePlaylistDownloader implements Downloader {
    public <R> R accept(BaseVisitor<R> visitor) {
        return visitor.visit(this);
    }


    private final Playlist playlist;

    public YoutubePlaylistDownloader(String url) {
        playlist = new Playlist(url);
    }


    public ArrayList<YoutubeVideoDownloader> getDownloadersFromPlaylist(String saveDirectory) {
        ArrayList<YoutubeVideoDownloader> downloaderArray = new ArrayList<>(getPlaylistSize());
        for (String url: this.getPlaylistUrls()) {
            try {
                downloaderArray.add(new YoutubeVideoDownloader(url, saveDirectory));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return downloaderArray;
    }


    public int getPlaylistSize() {
        return this.getPlaylistUrls().size();
    }

    public ArrayList<String> getPlaylistUrls() {
        try {
            return playlist.getVideos();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<String>(); // return empty list upon error
        }
    }

    public String getTitle() {
        try {
            return playlist.getTitle();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown title";
        }
    }

    public String getDescription() {
        try {
            return playlist.getDescription();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown description";
        }
    }

    public String getChannelName() {
        try {
            return playlist.getOwner();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown channel";
        }
    }
}
