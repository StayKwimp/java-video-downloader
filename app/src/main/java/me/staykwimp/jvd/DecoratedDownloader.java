package me.staykwimp.jvd;

// helper record which holds a downloader and a downloadVisitor, used for downloading queue

public record DecoratedDownloader(Downloader downloader, DownloadVisitor visitor) {
}
