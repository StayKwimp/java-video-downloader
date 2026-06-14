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
import java.util.LinkedHashMap;

import com.github.felipeucelli.javatube.StreamQuery;
import com.github.felipeucelli.javatube.StreamQuery.Filter;
import com.github.felipeucelli.javatube.Stream;


public class AvailableQualityVisitor implements BaseVisitor<String> {
    private final LinkedHashMap<String, Filter> filterMap, audioFilterMap;


    public AvailableQualityVisitor(LinkedHashMap<String, Filter> filterMap, LinkedHashMap<String, Filter> audioFilterMap) {
        this.filterMap = filterMap;
        this.audioFilterMap = audioFilterMap;
    }


    public String visit(YoutubeVideoDownloader downloader) {
        StringBuilder builder = new StringBuilder();
        
        LinkedHashMap<String, StreamQuery> queryMap = filterMapToStreamQueryMap(filterMap, downloader);

        queryMap.forEach((q, sq) -> {
            builder.append(q);
            builder.append(": ");
            builder.append(streamQueryListToString(sq));
            builder.append("\n");
        });

        return builder.toString();
    }

    public String visit(YoutubeMusicDownloader downloader) {
        StringBuilder builder = new StringBuilder();
        
        LinkedHashMap<String, StreamQuery> queryMap = filterMapToStreamQueryMap(audioFilterMap, downloader);

        queryMap.forEach((q, sq) -> {
            builder.append(q);
            builder.append(": ");
            builder.append(streamQueryListToString(sq));
            builder.append("\n");
        });

        return builder.toString();
    }



    // Returns a nice view of a stream query list.
    private String streamQueryListToString(StreamQuery streams) {
        StringBuilder builder = new StringBuilder();

        ArrayList<Stream> streamList = (ArrayList<Stream>) streams.getAll();

        streamList.forEach(s -> {
            String codec = s.getVideoCodec();
            boolean audioCodec = codec == null;
            if (audioCodec) {
                codec = s.getAudioCodec();
            }
            builder.append("itag: ");
            builder.append(s.getItag());
            builder.append(", codec: ");
            builder.append(getNiceCodec(codec));
            if (!audioCodec) {
                builder.append(", fps: ");
                builder.append(s.getFps());
            }
            else {
                builder.append(", bitrate: ");
                builder.append(s.getBitrate() / 1000);
                builder.append(" kbps");
            }
            builder.append(", est. size: ");
            builder.append(DownloadProgessBar.reduceSize(s.getFileSize(), 2));
            builder.append(" | ");
        });

        return builder.toString();
    }


    private String getNiceCodec(String videoCodec) {
        if (videoCodec.contains("vp9"))
            return "vp9";
        if (videoCodec.contains("av01"))
            return "av1";
        if (videoCodec.contains("avc"))
            return "h264";
        else return videoCodec;
    }


    private LinkedHashMap<String, StreamQuery> filterMapToStreamQueryMap(LinkedHashMap<String, Filter> filterMap, YoutubeVideoDownloader downloader) {
        LinkedHashMap<String, StreamQuery> queryMap = new LinkedHashMap<>();
        filterMap.forEach((s, f) -> {
            try {
                queryMap.put(s, downloader.filterStreams(f));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return queryMap;
    }

    
    // while theoretically possible by looping over every video in the playlist, you shouldn't bother with it
    public String visit(YoutubePlaylistDownloader playlist) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Can't get available quality of a Youtube playlist.");
    }

    // same here
    public String visit(YoutubeMusicPlaylistDownloader playlist) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Can't get available quality of a Youtube music playlist.");
    }
}
