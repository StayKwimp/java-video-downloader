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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

    public static final String VERSION = "v1.3.0";
    public static final String BUILD_DATE = getBuildDate();

    private static final LinkedHashMap<String, Filter> qualityMap = new LinkedHashMap<>();
    private static final LinkedHashMap<String, Filter> audioQualityMap = new LinkedHashMap<>();

    // to initialise the quality map, cannot be done when creating it.
    private static void initialiseQualityMap() {
        qualityMap.put("360p", StreamQuery.Filter.builder().res("360p").progressive(false).build());
        qualityMap.put("480p", StreamQuery.Filter.builder().res("480p").progressive(false).build());
        qualityMap.put("720p", StreamQuery.Filter.builder().res("720p").progressive(false).build());
        qualityMap.put("1080p", StreamQuery.Filter.builder().res("1080p").progressive(false).build());
        qualityMap.put("1440p", StreamQuery.Filter.builder().res("1440p").progressive(false).build());
        qualityMap.put("2160p", StreamQuery.Filter.builder().res("2160p").progressive(false).build());

        audioQualityMap.put("audio", StreamQuery.Filter.builder().onlyAudio(true).build());
    }

    // Gets the build date from the .jar manifest
    public static String getBuildDate() {
        try {
            Enumeration<URL> resources = Main.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            Manifest mf = new Manifest(resources.nextElement().openStream());
            Attributes att = mf.getMainAttributes();
            String buildDate = att.getValue("Build-Date");
            if (buildDate == null)
                return "date not set";
            return buildDate;
        } catch (IOException e) {
            return "unknown";
        }
    }

    // Prints a nice welcome message on startup
    public static void welcome(boolean check_updates) {
        System.out.println("Java Video Downloader");
        System.out.print("Version " + VERSION);

        try {
            if (check_updates) {
                UpdateChecker updateChecker = new UpdateChecker("StayKwimp", "java-video-downloader", VERSION);
                updateChecker.check();
                if (updateChecker.isUpdateAvailable())
                    System.out.println(" (new version available: v" + updateChecker.getLatestVersion() + ")");
                else
                    // print new line, since we do not print one when printing the version.
                    System.out.println();
            }
            else
                System.out.println(" (update check disabled)"); // for the same reason, we print a new line here.
        } catch (Exception e) {
            System.out.println("\nCould not check for updates");
        } finally {
            System.out.println("Build " + BUILD_DATE + "\nBy StayKwimp_   Copyright (C) 2026\n");
            System.out.println( "    This program comes with ABSOLUTELY NO WARRANTY.\n" + 
                                "    In particular, the author(s) of this program are not liable\n" + 
                                "    for any damages caused by the use of this program.\n" + 
                                "    Please consult the LICENSE file for more details.\n" + 
                                "    This is free software, and you are welcome to redistribute it\n" + 
                                "    under certain conditions.\n");
        }
    }


    public static void main(String[] args) {
        HashMap<Integer, LinkedList<String>> cmd_args = ArgHandler.handleArgs(args);
        
        
        // abort if there are errors while parsing arguments.
        if (cmd_args.containsKey(ArgHandler.PARSE_ERROR)) {
            System.err.println("An error occured while parsing arguments.");
            System.exit(-2);
            return;
        }

        // Show help page, then exit.
        if (cmd_args.containsKey(ArgHandler.SHOW_HELP)) {
            System.err.println(ArgHandler.helpPage());
            System.exit(0);
            return;
        }

        // Show youtube itags, then exit.
        if (cmd_args.containsKey(ArgHandler.YT_SHOW_ITAGS)) {
            System.err.println("--yt-show-itags is not implemented yet.");
            System.exit(0);
            return;
        }

        initialiseQualityMap();


        /*
            This parses all command-line arguments into useful values.
            Strings are empty if that argument is not supplied (same is true with arrays).
        */
        LinkedList<String> emptyArg = new LinkedList<String>();
        emptyArg.add("");

        boolean no_update_check =   cmd_args.containsKey(ArgHandler.NO_UPDATE_CHECK);
        String saveDirectory =      cmd_args.getOrDefault(ArgHandler.SAVE_DIR, emptyArg).get(0);
        String saveFilename  =      cmd_args.getOrDefault(ArgHandler.SAVE_FILENAME, emptyArg).get(0);

        // Only enter interactive mode if -i was passed, or if only -u, -d or -f were passed.
        boolean interactive =       cmd_args.containsKey(ArgHandler.INTERACTIVE) 
                                        || ((no_update_check ? 1 : 0) + (saveDirectory.length() == 0 ? 0 : 1) + (saveFilename.length() == 0 ? 0 : 1) == cmd_args.size());
        
        // Handle download queue
        String[] downloadQueue = cmd_args.getOrDefault(ArgHandler.QUEUE_LINK, emptyArg).get(0).split(";");

        // Handle FFMPEG options
        boolean disableFFmpeg = cmd_args.containsKey(ArgHandler.NO_FFMPEG);
        boolean obscureMetadata = cmd_args.containsKey(ArgHandler.FFMPEG_OBSCURE_METADATA);
        String ffmpegVideoCodec = cmd_args.getOrDefault(ArgHandler.FFMPEG_VIDEO_CODEC, emptyArg).get(0);
        String ffmpegAudioCodec = cmd_args.getOrDefault(ArgHandler.FFMPEG_AUDIO_CODEC, emptyArg).get(0);

        // Handle YouTube options
        boolean ytAudioOnly = cmd_args.containsKey(ArgHandler.YT_AUDIO_ONLY);
        boolean ytMusicNoMetadata = cmd_args.containsKey(ArgHandler.YT_MUSIC_NO_METADATA);
        String ytVideoItag = cmd_args.getOrDefault(ArgHandler.YT_VIDEO_ITAG, emptyArg).get(0);
        String ytAudioItag = cmd_args.getOrDefault(ArgHandler.YT_AUDIO_ITAG, emptyArg).get(0);


        if (!interactive) {
            // TODO: add non-interactive mode

            System.err.println("Program is running in non-interactive mode. Stopping.");
            System.exit(0);
            return;
        }

        

        welcome(!no_update_check);
        
        
        

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
                System.err.println("The YouTube URL you've entered is invalid.");
            else {
                e.printStackTrace();
            }
            return;
        }

        if (!downloader.isValid()) {
            System.err.println("The YouTube Video you've entered is inaccessible (because it is private or otherwise restricted)!");
            return;
        }
        
        // print video information
        System.out.println(downloader.accept(new InfoVisitor()));
        System.out.println("\n");

        // print available streams
        System.out.println(downloader.accept(new AvailableQualityVisitor(qualityMap, audioQualityMap)));

        int itag = getItagInput("\nEnter itag ('audio' for only audio) > ", scan, true);

        // itag -1 means audio
        if (itag == -1) {
            YoutubeMusicDownloader musicDownloader = downloader.toYoutubeMusic();
            
            System.out.println("Selected audio-only download, please select an audio itag to download.");
            System.out.println(musicDownloader.accept(new AvailableQualityVisitor(qualityMap, audioQualityMap)));

            itag = getItagInput("\nEnter itag > ", scan, false);

            musicDownloader.accept(new AddToQueueVisitor(itag, saveDirectory, (i, d) -> {
                return getItagInput("\nItag " + i + " is invalid!\nPlease enter a new itag > ", scan, false);
            }));
        }
        else {
            downloader.accept(new AddToQueueVisitor(itag, saveDirectory, (i, d) -> {
                return getItagInput("\nItag " + i + " is invalid!\nPlease enter a new itag > ", scan, true);
            }));
        }

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


        int itag = getItagInput("\nEnter default itag for whole playlist ('audio' for only audio) > ", scan, true);

        // itag -1 means audio-only
        boolean audioOnly = itag == -1;
        if (audioOnly) {
            System.out.println("Selected audio-only download, please select a default audio itag for the whole playlist.");

            itag = getItagInput("\nEnter itag > ", scan, false);
        }

        

        playlist.accept(new AddToQueueVisitor(itag, saveDirectory, (i, d) -> {
            System.out.println(d.accept(new InfoVisitor()));
            System.out.println(d.accept(new AvailableQualityVisitor(qualityMap, audioQualityMap)));
            return getItagInput("\nItag " + i + " is not valid for video '" + d.getVideoTitle() + "'!\nPlease enter a new itag > ", scan, audioOnly);
        }));

        System.out.println("Playlist added to queue. \nUse command 'view' to view the current queue, and use 'show-progress' to view the download progress.");
    }



    // helper method to get a valid itag input from a user
    // returns -1 in case of audio only download
    protected static int getItagInput(String msg, Scanner scan, boolean allowAudioOnly) {
        int itag = 0;
        while (itag == 0) {
            System.out.print(msg);
            try {
                String input = scan.nextLine().replaceAll("\s+", "");  // removes all spaces
                if (allowAudioOnly && input.contains("audio"))
                    itag = -1;
                
                else 
                    itag = Integer.parseInt(input);  // throws NumberFormatException in case of an input that isn't a number
            } catch (NumberFormatException e) {
                if (allowAudioOnly) {
                    System.err.println("You must enter a valid integer or 'audio'!");
                }
                else
                    System.err.println("You must enter a valid integer!");
            }
        }
        return itag;
    }
}
