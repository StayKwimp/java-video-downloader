package me.staykwimp.jvd;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class DownloadQueue {
    private static final LinkedList<DecoratedDownloader> downloadQueue = new LinkedList<>();  // linkedlists can act as queues
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition queueNotEmpty = lock.newCondition();


    // adds a video to the queue (end of the line)
    // notifies any threads waiting for the queue to not be empty
    public static void addToQueue(DecoratedDownloader downloader) {
        lock.lock();
        try {
            downloadQueue.addLast(downloader);
            queueNotEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }


    // removes the head (first video in line) from the queue
    // if the queue is empty, waits for the queue to not be empty to continue downloading
    public static DecoratedDownloader popQueue() throws InterruptedException {
        lock.lock();
        try {
            while (downloadQueue.peek() == null) {
                queueNotEmpty.await();
            }

            return downloadQueue.poll();
        } finally {
            lock.unlock();
        }
    }


    public static DecoratedDownloader removeFromQueue(int index) {
        lock.lock();
        try {
            return downloadQueue.remove(index);
        } finally {
            lock.unlock();
        }
    }


    public static DecoratedDownloader peekHead() throws NoSuchElementException {
        DecoratedDownloader head = downloadQueue.peek();
        if (head == null)
            throw new NoSuchElementException("Download queue is empty");
        return head;
    }

    // returns a decorated string representation of the download queue
    public static String getContents() {
        lock.lock();
        try {
            if (downloadQueue.size() == 0)
                return "The download queue is currently empty.";
            
            StringBuilder builder = new StringBuilder();

            builder.append("The download queue currently contains " + downloadQueue.size() + " videos:");

            downloadQueue.forEach(dd -> {
                builder.append("\n    ");
                Downloader d = dd.downloader();
                if (d instanceof YoutubeVideoDownloader)
                    builder.append(((YoutubeVideoDownloader) d).toString(dd.visitor().getDefaultItag()));
                else
                    builder.append(d.toString());
            });


            return builder.toString();
        } finally {
            lock.unlock();
        }
    }
}
