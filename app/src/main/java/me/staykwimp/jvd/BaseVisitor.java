package me.staykwimp.jvd;

public interface BaseVisitor<R> {
    default public R visit(Downloader visitable) throws ClassCastException {
        if (visitable instanceof YoutubePlaylistDownloader)
            return visit((YoutubePlaylistDownloader) visitable);
        else if (visitable instanceof YoutubeVideoDownloader)
            return visit((YoutubeVideoDownloader) visitable);
        
        throw new ClassCastException("Cannot visit class of type " + visitable.getClass().getName());
    }
    public R visit(YoutubeVideoDownloader visitable);
    public R visit(YoutubePlaylistDownloader visitable);
}
