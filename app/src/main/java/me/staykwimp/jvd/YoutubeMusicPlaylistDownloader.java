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

import org.json.JSONException;

public class YoutubeMusicPlaylistDownloader extends YoutubePlaylistDownloader {
    public <R> R accept(BaseVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public YoutubeMusicPlaylistDownloader(String url) throws JSONException, NoSuchFieldException, IllegalAccessException {
        super(url);
    }

    protected YoutubeMusicPlaylistDownloader(YoutubePlaylistDownloader p) {
        super();
        this.playlist = p.playlist;
    }


    // Return error upon trying to create a list of YoutubeVideoDownloaders from a YoutubeMusicPlaylist...
    @Override
    public ArrayList<YoutubeVideoDownloader> getDownloadersFromPlaylist(String saveDirectory) {
        throw new UnsupportedOperationException("Cannot get YoutubeVideoDownloaders from a YoutubeMusicPlaylist.");
    }

    // Gets YoutubeMusicDownloaders from a playlist.
    public ArrayList<YoutubeMusicDownloader> getMusicDownloadersFromPlaylist(String saveDirectory) {
        ArrayList<YoutubeMusicDownloader> downloaderArray = new ArrayList<>(getPlaylistSize());
        Random randomGen = new Random();
        for (String url: getPlaylistUrls()) {
            try {
                YoutubeMusicDownloader yvd = new YoutubeMusicDownloader(url, saveDirectory);
                downloaderArray.add(yvd);
                System.out.println("Found video: " + yvd);
                Thread.sleep(randomGen.nextInt(300) + 300); // random delay between adding videos
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return downloaderArray;
    }
}
