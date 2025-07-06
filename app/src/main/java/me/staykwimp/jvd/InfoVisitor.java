package me.staykwimp.jvd;

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
}
