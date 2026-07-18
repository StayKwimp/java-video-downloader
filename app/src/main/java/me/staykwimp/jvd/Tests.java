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

import java.util.ArrayList;
import com.github.felipeucelli.javatube.*;

/*
    Runs tests to see whether library stuff is working or not.
*/

public class Tests {
    private static void playlistTest() {
        try {
            Playlist p = new Playlist("https://www.youtube.com/playlist?list=PLOh2AUhKQzaNeE-vXiH1SMeJyTdRT84dr");
            ArrayList<String> videos = p.getVideos();
            System.out.println("Found " + videos.size() + " videos:");
            videos.forEach(s -> System.out.println("  URL: " + s));

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred during playlistTest. See stacktrace above for details.");
        }
    }

    public static void runTests() {
        System.out.println("Running playlist test. The output shouldn't be 0 videos.");
        playlistTest();
    }
}
