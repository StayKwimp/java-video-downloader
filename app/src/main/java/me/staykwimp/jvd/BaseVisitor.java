package me.staykwimp.jvd;

public interface BaseVisitor<R> {
    public R visit(YoutubeVideoDownloader visitable);
    public R visit(YoutubePlaylistDownloader visitable);
}
