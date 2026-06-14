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
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Arrays;

public class ArgHandler {
    public static final Integer PARSE_ERROR = -1;
    public static final Integer SHOW_HELP = 1;
    public static final Integer INTERACTIVE = 2;
    public static final Integer NO_UPDATE_CHECK = 3;
    

    // Category 1x: general program configuration
    public static final Integer QUEUE_LINK = 10;
    public static final Integer SAVE_DIR = 11;
    public static final Integer SAVE_FILENAME = 12;

    // Category 2x: FFMPEG options
    public static final Integer NO_FFMPEG = 20;
    public static final Integer FFMPEG_VIDEO_CODEC = 21;
    public static final Integer FFMPEG_AUDIO_CODEC = 22;
    public static final Integer FFMPEG_OBSCURE_METADATA = 23;

    // Category 3x: YouTube download options
    public static final Integer YT_SHOW_ITAGS = 30;
    public static final Integer YT_AUDIO_ONLY = 31;
    public static final Integer YT_VIDEO_ITAG = 32;
    public static final Integer YT_AUDIO_ITAG = 33;
    public static final Integer YT_MUSIC_NO_METADATA = 34;



    /*
        Class which contains a mapping from argument strings (such as -h or --help) to argument codes and argument counts (i.e. how many arguments are expected after it).
    */
    private static class CodeMap {
        private HashMap<String, Integer> codeMap = new HashMap<>();
        private HashMap<Integer, Integer> countMap = new HashMap<>();
        
        // Constructor method fills the mapping.
        // Decide between short or long arguments. The - or -- is handled elsewhere.
        public CodeMap(boolean short_arg) {
            if (short_arg) {
                codeMap.put("h", SHOW_HELP);
                codeMap.put("i", INTERACTIVE);
                codeMap.put("u", NO_UPDATE_CHECK);
                codeMap.put("q", QUEUE_LINK);
                codeMap.put("d", SAVE_DIR);
                codeMap.put("f", SAVE_FILENAME);
                
                codeMap.put("n", NO_FFMPEG);
                codeMap.put("v", FFMPEG_VIDEO_CODEC);
                codeMap.put("a", FFMPEG_AUDIO_CODEC);
                codeMap.put("o", FFMPEG_OBSCURE_METADATA);
            }
            else {
                // Warning: make sure these don't have a literal '--' in them.
                codeMap.put("help", SHOW_HELP);
                codeMap.put("interactive", INTERACTIVE);
                codeMap.put("no-update", NO_UPDATE_CHECK);
                codeMap.put("queue", QUEUE_LINK);
                codeMap.put("save-dir", SAVE_DIR);
                codeMap.put("save-name", SAVE_FILENAME);

                codeMap.put("no-ffmpeg", NO_FFMPEG);
                codeMap.put("video-codec", FFMPEG_VIDEO_CODEC);
                codeMap.put("audio-codec", FFMPEG_AUDIO_CODEC);
                codeMap.put("obscure-metadata", FFMPEG_OBSCURE_METADATA);

                codeMap.put("yt-show-itags", YT_SHOW_ITAGS);
                codeMap.put("yt-video-itag", YT_VIDEO_ITAG);
                codeMap.put("yt-audio-itag", YT_AUDIO_ITAG);
                codeMap.put("yt-music", YT_AUDIO_ONLY);
                codeMap.put("yt-no-metadata", YT_MUSIC_NO_METADATA);
            }
            
            // How many other arguments do we need?
            countMap.put(SHOW_HELP, 0);
            countMap.put(INTERACTIVE, 0);
            countMap.put(NO_UPDATE_CHECK, 0);
            countMap.put(QUEUE_LINK, 1);
            countMap.put(SAVE_DIR, 1);
            countMap.put(SAVE_FILENAME, 1);
            
            countMap.put(NO_FFMPEG, 0);
            countMap.put(FFMPEG_VIDEO_CODEC, 1);
            countMap.put(FFMPEG_AUDIO_CODEC, 1);
            countMap.put(FFMPEG_OBSCURE_METADATA, 0);
            
            countMap.put(YT_SHOW_ITAGS, 0);
            countMap.put(YT_VIDEO_ITAG, 1);
            countMap.put(YT_AUDIO_ITAG, 1);
            countMap.put(YT_AUDIO_ONLY, 0);
            countMap.put(YT_MUSIC_NO_METADATA, 0);
        }

        public Integer getCode(String s) {
            return codeMap.getOrDefault(s, PARSE_ERROR);
        }

        public Integer getArgCount(Integer code) {
            return countMap.getOrDefault(code, 0);
        }
    }



    // simple record which holds the result of an argument parsing.
    private record ArgHolder(Integer argCode, LinkedList<String> values) {}

    private static ArgHolder parseArgument(String arg, LinkedList<String> args, CodeMap map) {
        Integer code = map.getCode(arg);

        // print some error when code is PARSE_ERROR
        if (code == PARSE_ERROR) {
            System.err.println("Error while parsing: unknown argument '" + arg + "'. Use -h or --help for a list of arguments.");
        }


        Integer argCount = map.getArgCount(code);
        LinkedList<String> values = new LinkedList<String>();

        // Read 'values' amount of extra arguments from args.
        for (int i = 0; i < argCount; i++) {
            try {
                values.add(args.pop());
            } catch (NoSuchElementException err) {
                System.err.println("Not enough arguments for '" + arg + "': expected " + argCount + ", got " + i);
                return new ArgHolder(PARSE_ERROR, new LinkedList<>());
            }
        }

        return new ArgHolder(code, values);
    }


    /*
        Parses all arguments given by the first argument.
        Returns a mapping from Integer codes given by the ArgHandler class to argument values in String.
        The main program should decide what to do with the arguments.
    */
    public static HashMap<Integer, LinkedList<String>> handleArgs(String[] args) {
        // create a linkedlist from the arguments
        LinkedList<String> argsList = new LinkedList<String>(Arrays.asList(args));

        // create the arguments collector and 
        HashMap<Integer, LinkedList<String>> cmd_args = new HashMap<Integer, LinkedList<String>>();

        // two CodeMaps: one for short arguments such as -h or -i, other for long arguments such as --help.
        CodeMap shortMap = new CodeMap(true);
        CodeMap longMap = new CodeMap(false);

        while (argsList.size() > 0) {
            // retrieve argument
            String arg = argsList.pop();

            // check if argument is long enough
            if (arg.length() < 2) {
                System.err.println("Argument handler error: '" + arg + "' is not long enough.");
                cmd_args.put(PARSE_ERROR, new LinkedList<>());
            }
            else {
                // check whether we have a short or long argument, i.e. - or --.
                if (arg.charAt(0) == '-') {
                    ArgHolder argument;
                    if (arg.charAt(1) == '-') {
                        // we cannot chain long arguments the same as short ones, so this is a lot simpler :)
                        String argumentPart = arg.replace("--", "");
                        argument = parseArgument(argumentPart, argsList, longMap);
                        cmd_args.put(argument.argCode(), argument.values());
                    }
                    else {
                        // here we can chain different arguments as a convenience: -i -u can become -iu.
                        for (int i = 1; i < arg.length(); i++) {
                            char[] argumentPart = {arg.charAt(i)};
                            argument = parseArgument(new String(argumentPart), argsList, shortMap);
                            cmd_args.put(argument.argCode(), argument.values());
                        }
                    }

                    
                }
                else {
                    System.err.println("Argument handler error: '" + arg + "' does not start with a '-'.");
                    cmd_args.put(PARSE_ERROR, new LinkedList<>());
                }

                
            }
            
        }
        return cmd_args;
    }


    /*
        Returns the arguments help page.
    */
    public static String helpPage() {
        return  "Java Video Downloader command-line arguments help.\nProgram version: " + Main.VERSION + "\n\n" +
                "When ran with either no arguments, or only with -u, -d or -f, the program will enter interactive mode.\n" +
                "In any other case, the program will behave more like a command-line tool: \nit will do its best to execute the command-line arguments given, and then stop.\n" +
                "This behaviour can be turned off by passing the -i argument: execution of \ncommand-line arguments will be followed by entering interactive mode.\n\n" +  
                "Available arguments are: \n\n" + 
                "   -u  --no-update             Do not check for updates on startup.\n\n" + 
                "   -i  --interactive           Enter interactive mode after handling all other arguments.\n\n" +
                "   -q  --queue <URLs>          Queue URLs for downloading. Each URL ought to be separated by a ';' character.\n\n" +
                "   -d  --save-dir <DIR>        Save files to this directory.\n\n" +
                "   -f  --save-name <NAME>      Final output files will be named NAME.\n\n" +

                "\nFFMPEG-specific arguments are:\n\n" + 
                "   -n  --no-ffmpeg             Do not use ffmpeg.\n\n" + 
                "   -v  --video-codec <CODEC>   Re-encode video with CODEC. For a list of codecs, run `ffmpeg -encoders`.\n\n" + 
                "   -a  --audio-codec <CODEC>   Re-encode audio with CODEC. For a list of codecs, run `ffmpeg -encoders`.\n\n" + 
                "   -o  --obscure-metadata      Obscure final file metadata, such as encoder used and creation time.\n\n" + 

                "\nYouTube-specific arguments are:\n\n" + 
                "   --yt-show-itags         Show the values of known ITAGs, and then quit the application.\n\n" + 
                "   --yt-video-itag <ITAG>  Specify the video ITAG to download. Use --yt-music to download audio-only,\n" + 
                "                           instead of passing itag 251. Use --yt-show-itags to get a list of available ITAGs.\n" + 
                "                           When this flag is not set, the best video quality is downloaded.\n\n" + 
                "   --yt-audio-itag <ITAG>  Specify the audio ITAG to download. Use --yt-music to download audio-only,\n" + 
                "                           instead of passing itag 251. Use --yt-show-itags to get a list of available ITAGs.\n" + 
                "                           When this flag is not set, the best audio quality is downloaded.\n\n" + 
                "   --yt-music              Download audio-only (saves file as mp3).\n\n" + 
                "   --yt-no-metadata        Do not add additional metadata to the final file, such as title, author, and thumbnail.\n" + 
                "                           Only useful in combination with --yt-music.\n\n" + 

                "\n\nEXAMPLES\n\n" +
                "Download a YouTube video:\n" + 
                "   jvd -q https://www.youtube.com/watch?v=Ja_AOeWKMEs \n\n" +
                "\nFor convenience, arugments such as -i and -u (with a single -) can be combined into one as follows: -iu.\n" +
                "Single-letter arguments which take one or more arguments can be combined as follows: -us videos.\n" +
                "In particular, when combining multiple single-lettered arguments which take at least one argument, \n" +
                "one can combine them as follows: -udf videos filename. Or: -ufd filename videos. Both achieve the same result.";
    }
}
