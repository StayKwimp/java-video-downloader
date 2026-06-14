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
import java.util.Random;

import com.github.felipeucelli.javatube.*;

public class YoutubePlaylistDownloader implements Downloader {
    public <R> R accept(BaseVisitor<R> visitor) {
        return visitor.visit(this);
    }


    protected final Playlist playlist;

    public YoutubePlaylistDownloader(String url) {
        playlist = new Playlist(url);
    }


    // Gets YoutubeVideoDownloaders from a playlist.
    public ArrayList<YoutubeVideoDownloader> getDownloadersFromPlaylist(String saveDirectory) {
        ArrayList<YoutubeVideoDownloader> downloaderArray = new ArrayList<>(getPlaylistSize());
        for (String url: this.getPlaylistUrls()) {
            try {
                // System.out.println("Found URL in playlist: " + url);
                YoutubeVideoDownloader yvd = new YoutubeVideoDownloader(url, saveDirectory);
                downloaderArray.add(yvd);
                System.out.println("Found video: " + yvd);
                Thread.sleep(new Random().nextInt(200) + 200); // random delay between adding videos
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
