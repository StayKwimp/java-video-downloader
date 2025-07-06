package me.staykwimp.jvd;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.github.felipeucelli.javatube.StreamQuery;
import com.github.felipeucelli.javatube.StreamQuery.Filter;
import com.github.felipeucelli.javatube.Stream;


public class AvailableQualityVisitor implements BaseVisitor<String> {
    private final LinkedHashMap<String, Filter> filterMap;


    public AvailableQualityVisitor(LinkedHashMap<String, Filter> filterMap) {
        this.filterMap = filterMap;
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



    // 
    private String streamQueryListToString(StreamQuery streams) {
        StringBuilder builder = new StringBuilder();

        ArrayList<Stream> streamList = (ArrayList<Stream>) streams.getAll();

        streamList.forEach(s -> {
            builder.append("itag: ");
            builder.append(s.getItag());
            builder.append(", codec: ");
            builder.append(getNiceCodec(s.getVideoCodec()));
            builder.append(", fps: ");
            builder.append(s.getFps());
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
        throw new UnsupportedOperationException("Can't get available quality of a playlist.");
    }
}
