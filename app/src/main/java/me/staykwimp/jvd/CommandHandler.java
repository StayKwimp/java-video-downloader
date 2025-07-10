package me.staykwimp.jvd;

import java.util.InputMismatchException;
import java.util.Scanner;

public class CommandHandler {
    private static boolean assertEnoughArguments(String[] command, int expectedArguments) {
        if (command.length != expectedArguments) {
            System.out.println("Expected " + (expectedArguments - 1) + " argument(s), got " + (command.length - 1));
            return false;
        }
        return true;
    }

    public static void queueCommand(String[] command, Scanner scan) {
        if (!assertEnoughArguments(command, 2))
            return;

        // youtube video
        if (command[1].contains("youtube.com") || command[1].contains("youtu.be")) {
            Main.downloadYoutubeVideo(command[1], scan);
        }

        else
            System.out.println("The URL you've entered is not a valid YouTube URL!");
    }

    public static void removeFromQueue(String[] command) {
        if (!assertEnoughArguments(command, 2))
            return;
        
        try {
            int index = Integer.parseInt(command[1].replaceAll("\s+", ""));
            if (index <= 0) {
                System.out.println("Index must be larger than zero.");
                return;
            }

            DownloadQueue.removeFromQueue(index);
            
        } catch (NumberFormatException e) {
            System.out.println("The second argument should be a number");
        }
    }

    public static void helpCommand() {
        System.out.println("Available commands:");
        System.out.println("    queue <URL>     - adds a video to the download queue");
        System.out.println("    remove <index>  - removes a video at the specified index (run view command)");
        System.out.println("    playlist <URL>  - downloads a playlist");
        System.out.println("    view            - shows what's currently in the download queue");
        System.out.println("    show-progress   - shows the download progress bar (Q to quit this mode)");
        System.out.println("    quit            - closes the program (waits for last video to download)");
        System.out.println("    help            - view this help message");
        System.out.println("    debug           - shows some debug information");
    }

    public static void playlistCommand(String[] command, Scanner scan) {
        if (!assertEnoughArguments(command, 2))
            return;
        
        if (command[1].contains("youtube.com") && command[1].contains("playlist")) {
            Main.downloadYoutubePlaylist(command[1], scan);
        }
        else
            System.out.println("The URL you've entered is not a valid YouTube Playlist URL!");
    }

    public static void viewQueue() {
        printCurrentlyDownloadingProgressbar();
        System.out.println("\n" + DownloadQueue.getContents());
    }

    private static volatile boolean inputThreadRunning = false;

    public static void showProgress(Scanner scan) {
        try {
            System.out.println("\nEntering progress view mode. Press a letter key and hit Enter to quit this mode at any time.\n\n");
            System.out.println("Currently downloading " + Main.downloader.getCurrentDownloader().toString());
        } catch (NoSuchFieldException e) {
            System.out.println("Currently not downloading anything");
            return;
        }
        
        // run input thread to close the program in a separate thread
        Thread inputThread = new Thread(() -> {
            while (inputThreadRunning && !Thread.currentThread().isInterrupted()) {
                if (scan.hasNext()) {
                    inputThreadRunning = false;
                    scan.nextLine();
                }
            }
        });

        DownloadProgessBar.silent = false;
        inputThreadRunning = true;
        inputThread.start();

        try {
            while (inputThreadRunning) {
                Thread.sleep(200); // pause execution to avoid busy waiting
            }
        } catch (InputMismatchException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            DownloadProgessBar.silent = true;
            inputThread.interrupt();

        }
    }

    private static void printCurrentlyDownloadingProgressbar() {
        try {
            System.out.println("Currently downloading " + Main.downloader.getCurrentDownloader().toString());
            System.out.println(DownloadProgessBar.getLatestProgressBar());
        } catch (NoSuchFieldException e) {
            System.out.println("Currently not downloading anything");
        }
    }

    public static void debugCommand() {
        System.out.println("Version " + Main.VERSION + ", Build " + Main.BUILD_DATE);
        System.out.println("Java version: " + Runtime.version());
        System.out.println("Downloader Thread status: " + Main.downloaderThread.getState());
        System.out.println("Memory usage: " + DownloadProgessBar.reduceSize(Runtime.getRuntime().freeMemory()) + "/" + DownloadProgessBar.reduceSize(Runtime.getRuntime().maxMemory()));
    }
}
