package me.staykwimp.jvd;

import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.jar.Manifest;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;

import com.github.felipeucelli.javatube.*;
import com.github.felipeucelli.javatube.StreamQuery.Filter;
import com.github.felipeucelli.javatube.exceptions.*;

import com.technicjelle.UpdateChecker;

public class Main {
    public final static String saveDirectory = "";
    public static final String workingDirectory = System.getProperty("user.dir");
    public static final boolean deleteTempFiles = false;
    public static final QueueDownloader downloader = new QueueDownloader();
    public static final Thread downloaderThread = new Thread(downloader);

    public static final String VERSION = "v1.2.1";
    public static final String BUILD_DATE = getBuildDate();

    private static final LinkedHashMap<String, Filter> qualityMap = new LinkedHashMap<>();

    // to initialise the quality map, cannot be done when creating it.
    private static void initialiseQualityMap() {
        qualityMap.put("360p", StreamQuery.Filter.builder().res("360p").progressive(false).build());
        qualityMap.put("480p", StreamQuery.Filter.builder().res("480p").progressive(false).build());
        qualityMap.put("720p", StreamQuery.Filter.builder().res("720p").progressive(false).build());
        qualityMap.put("1080p", StreamQuery.Filter.builder().res("1080p").progressive(false).build());
        qualityMap.put("1440p", StreamQuery.Filter.builder().res("1440p").progressive(false).build());
        qualityMap.put("2160p", StreamQuery.Filter.builder().res("2160p").progressive(false).build());
    }

    // Gets the build date from the .jar manifest
    public static String getBuildDate() {
        try {
            Enumeration<URL> resources = Main.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            try {
                Manifest mf = new Manifest(resources.nextElement().openStream());
                Attributes att = mf.getMainAttributes();
                String buildDate = att.getValue("Build-Date");
                if (buildDate == null)
                    return "date not set";
                return buildDate;
            } catch (IOException e) {
                return "unknown";
            }
        } catch (IOException e) {
            return "unknown";
        }
    }

    // Prints a nice welcome message on startup
    public static void welcome() {
        System.out.println("Java Video Downloader");
        System.out.print("Version " + VERSION);

        try {
            UpdateChecker updateChecker = new UpdateChecker("StayKwimp", "java-video-downloader", VERSION);
            updateChecker.check();
            if (updateChecker.isUpdateAvailable())
                System.out.println(" (new version available: v" + updateChecker.getLatestVersion() + ")");
            else
                System.out.println();
        } catch (Exception e) {
            System.out.println("\nCould not check for updates");
        } finally {
            System.out.println("Build " + BUILD_DATE + "\nBy StayKwimp_\n");
        }
    }


    public static void main(String[] args) {
        initialiseQualityMap();
        welcome();
        downloaderThread.start();
        
        Scanner scan = new Scanner(System.in);
        String input = "";

        
        // Main loop, reads commands.
        while (true) {
            System.out.print("java-video-downloader@" + VERSION + " > ");
            input = scan.nextLine();
            String[] command = input.split("\s+");
            
            if (command[0].equals("queue"))
                CommandHandler.queueCommand(command, scan);
            else if (command[0].equals("remove"))
                CommandHandler.removeFromQueue(command);
            else if (command[0].equals("playlist"))
                CommandHandler.playlistCommand(command, scan);
            else if (command[0].equals("view"))
                CommandHandler.viewQueue();
            else if (command[0].equals("show-progress"))
                CommandHandler.showProgress(scan);
            else if (command[0].equals("help"))
                CommandHandler.helpCommand();
            else if (command[0].toLowerCase().equals("q") || command[0].equals("quit") || command[0].equals("exit"))
                break;
            else if (command[0].equals("debug"))
                CommandHandler.debugCommand();
            else { // if no known command is entered
                System.out.println(command[0] + " is not recognised as a command.");
                CommandHandler.helpCommand();
            }
        }

        scan.close();

        // stop downloader thread
        downloaderThread.interrupt();
        try {
            downloaderThread.join();
        } catch (InterruptedException e) {
            return;
        }
    }



    // Downloads a YouTube video
    public static void downloadYoutubeVideo(String url, Scanner scan) {
        YoutubeVideoDownloader downloader;
        try {
            downloader = new YoutubeVideoDownloader(url, saveDirectory);
        } catch (Exception e) {
            if (e instanceof RegexMatchError)
                System.out.println("The YouTube URL you've entered is invalid.");
            else
                e.printStackTrace();
            
            
            return;
        }

        if (!downloader.isValid()) {
            System.out.println("The YouTube Video you've entered is inaccessible (because it is private or otherwise restricted)!");
            return;
        }
        
        // print video information
        System.out.println(downloader.accept(new InfoVisitor()));
        System.out.println("\n");

        // print available streams
        System.out.println(downloader.accept(new AvailableQualityVisitor(qualityMap)));

        int itag = getItagInput("\nEnter itag ('audio' for only audio) > ", scan);



        downloader.accept(new AddToQueueVisitor(itag, saveDirectory, (i, d) -> {
            return getItagInput("\nItag " + i + " is invalid!\nPlease enter a new itag > ", scan);
        }));

        System.out.println("Video added to queue. \nUse command 'view' to view the current queue, and use 'show-progress' to view the download progress.");
    }


    // Downloads a Youtube playlist
    public static void downloadYoutubePlaylist(String url, Scanner scan) {
        YoutubePlaylistDownloader playlist;


        try {
            playlist = new YoutubePlaylistDownloader(url);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // print playlist information
        System.out.println(playlist.accept(new InfoVisitor()));
        System.out.println("\n");


        int itag = getItagInput("\nEnter default itag for whole playlist ('audio' for only audio) > ", scan);

        

        playlist.accept(new AddToQueueVisitor(itag, saveDirectory, (i, d) -> {
            System.out.println(d.accept(new InfoVisitor()));
            System.out.println(d.accept(new AvailableQualityVisitor(qualityMap)));
            return getItagInput("\nItag " + i + " is not valid for video '" + d.getVideoTitle() + "'!\nPlease enter a new itag > ", scan);
        }));

        System.out.println("Playlist added to queue. \nUse command 'view' to view the current queue, and use 'show-progress' to view the download progress.");
    }



    // helper method to get a valid itag input from a user
    // returns -1 in case of audio only download
    protected static int getItagInput(String msg, Scanner scan) {
        int itag = 0;
        while (itag == 0) {
            System.out.print(msg);
            try {
                String input = scan.nextLine().replaceAll("\s+", "");  // removes all spaces
                if (input.contains("audio"))
                    itag = -1;
                
                else 
                    itag = Integer.parseInt(input);  // throws NumberFormatException in case of an input that isn't a number
            } catch (NumberFormatException e) {
                System.out.println("You must enter a number (<2^31) or 'audio'!");
            }
        }
        return itag;
    }
}
