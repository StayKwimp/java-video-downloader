package me.staykwimp.jvd;

import java.util.concurrent.locks.ReentrantLock;

public class QueueDownloader implements Runnable {
    private Downloader currentDownloader = null;
    private DownloadVisitor currentVisitor = null;
    private ReentrantLock lock = new ReentrantLock();


    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DecoratedDownloader downloader = DownloadQueue.popQueue();
                lock.lock();
                try {
                    currentDownloader = downloader.downloader();
                    currentVisitor = downloader.visitor();
                } finally {
                    lock.unlock();
                }
                
                currentDownloader.accept(currentVisitor);
                currentDownloader = null;
            } catch (InterruptedException e) {
                return; // this code is reached when downloader thread is interrupted while waiting for downloadQueue
            }
        }
    }

    
    // gets the current downloader
    // throws a NoSuchFIeldException when the thread is not downloading anything
    public Downloader getCurrentDownloader() throws NoSuchFieldException {
        lock.lock();
        try {
            if (currentDownloader == null)
                throw new NoSuchFieldException("There is no current downloader (downloader idle)");
            return currentDownloader;
        } finally {
            lock.unlock();
        }
    }
}
