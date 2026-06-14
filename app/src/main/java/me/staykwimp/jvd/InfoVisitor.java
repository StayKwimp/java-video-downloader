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

public class InfoVisitor implements BaseVisitor<String> {
    public String visit(YoutubeVideoDownloader downloader) {
        StringBuilder builder = new StringBuilder();
    
        builder.append("=======================================================\n");
        builder.append("Video: ");
        builder.append(downloader.getVideoTitle());
        builder.append("\n");
        builder.append(downloader.getVideoViews());
        builder.append(" views\nBy: ");
        builder.append(downloader.getChannelName());
        builder.append("\n=======================================================");
        
        
        return builder.toString();
    }

    public String visit(YoutubePlaylistDownloader playlist) {
        StringBuilder builder = new StringBuilder();

        builder.append("=======================================================\n");
        builder.append("Playlist: ");
        builder.append(playlist.getTitle());
        builder.append("\n");
        builder.append(playlist.getPlaylistSize());
        builder.append(" videos\nBy: ");
        builder.append(playlist.getChannelName());
        builder.append("\n=======================================================");

        return builder.toString();
    }

    public String visit(YoutubeMusicDownloader downloader) {
        return this.visit((YoutubeVideoDownloader) downloader);
    }

    public String visit(YoutubeMusicPlaylistDownloader downloader) {
        return this.visit((YoutubePlaylistDownloader) downloader);
    }
}
