package me.staykwimp.jvd;

import java.util.LinkedHashMap;
import java.util.Scanner;

import com.github.felipeucelli.javatube.*;
import com.github.felipeucelli.javatube.StreamQuery.Filter;

public class Main {
    public final static String saveDirectory = "";
    public static final String workingDirectory = System.getProperty("user.dir");
    public static final boolean deleteTempFiles = false;
    private static final LinkedHashMap<String, Filter> qualityMap = new LinkedHashMap<>();

    private static void initialiseQualityMap() {
        qualityMap.put("360p", StreamQuery.Filter.builder().res("360p").progressive(false).build());
        qualityMap.put("480p", StreamQuery.Filter.builder().res("480p").progressive(false).build());
        qualityMap.put("720p", StreamQuery.Filter.builder().res("720p").progressive(false).build());
        qualityMap.put("1080p", StreamQuery.Filter.builder().res("1080p").progressive(false).build());
        qualityMap.put("1440p", StreamQuery.Filter.builder().res("1440p").progressive(false).build());
        qualityMap.put("2160p", StreamQuery.Filter.builder().res("2160p").progressive(false).build());
    }


    public static void welcome() {
        System.out.println("Java Video Downloader\nBuild date 06.07.2025\nVersion 1.0.2\nBy StayKwimp_\n");
    }


    public static void main(String[] args) {
        initialiseQualityMap();
        welcome();
        
        Scanner scan = new Scanner(System.in);
        String input = "";


        while (true) {
            System.out.print("Enter Youtube URL (q to quit) > ");
            input = scan.nextLine().replaceAll("\s+", ""); // removes whitespace from the input


            if (!input.equals("q") && !input.equals("Q")) {
                
                if (input.contains("youtube.com") || input.contains("youtu.be")) {
                    if (input.contains("playlist?list=")) 
                        downloadYoutubePlaylist(input, scan);
                    
                    else
                        downloadYoutubeVideo(input, scan);
                }

                else
                    System.out.println("The URL you've entered is not a valid YouTube URL!");

            }
            else break;
        }

        scan.close();
    }



    public static void downloadYoutubeVideo(String url, Scanner scan) {
        YoutubeVideoDownloader downloader;
        try {
            downloader = new YoutubeVideoDownloader(url, saveDirectory);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        // print video information
        System.out.println(downloader.accept(new InfoVisitor()));
        System.out.println("\n");

        // print available streams
        System.out.println(downloader.accept(new AvailableQualityVisitor(qualityMap)));

        int itag = getItagInput("\nEnter itag ('audio' for only audio) > ", scan);

        downloader.accept(new DownloadVisitor(itag, saveDirectory, (i, d) -> {
            return getItagInput("\nItag " + i + " is invalid!\nPlease enter a new itag > ", scan);
        }));
    }



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

        playlist.accept(new DownloadVisitor(itag, saveDirectory, (i, d) -> {
            System.out.println(d.accept(new InfoVisitor()));
            System.out.println(d.accept(new AvailableQualityVisitor(qualityMap)));
            return getItagInput("\nItag " + i + " is not valid for video '" + d.getVideoTitle() + "'!\nPlease enter a new itag > ", scan);
        }));
    }



    // helper method to get a valid itag input from a user
    // returns -1 in case of audio only download
    private static int getItagInput(String msg, Scanner scan) {
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
                System.out.println("You must enter a number (<2^31 - 1) or 'audio'!");
            }
        }
        return itag;
    }
}
