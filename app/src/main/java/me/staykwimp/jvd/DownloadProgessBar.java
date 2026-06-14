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


import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;


public class DownloadProgessBar {
    private static long timeSinceLastProgressbarCall = 0;
    private static long previousBytesReceived = 0;
    private static int cols = 120;
    private static final Map<Integer, String> conversionMap = new HashMap<>(5);
    private static boolean filledConversionMap = false;
    private static final int COOLDOWN = 200;

    // when true, displays no progress bar (but still updates the fields below)
    public static boolean silent = true;

    private static long latestFileSize = 0;
    private static long latestByteDownloadSpeed = 0;
    private static long latestBytesSaved = 0;


    private static void fillConversionMap() {
        if (!filledConversionMap) {
            conversionMap.put(0, "B");
            conversionMap.put(1, "KB");
            conversionMap.put(2, "MB");
            conversionMap.put(3, "GB");
            conversionMap.put(4, "TB");
            filledConversionMap = true;
        }
    }

    // resets field values
    public static void ready(String downloadInformation) {
        timeSinceLastProgressbarCall = System.currentTimeMillis();
        previousBytesReceived = 0;
        latestBytesSaved = 0;
        latestFileSize = 0;

        fillConversionMap();

        if (!silent)
            System.out.println("Currently downloading " + downloadInformation);
    }

    public static void end(String downloadInformation) {
        if (silent) return;

        PrintStream out = System.out;
        out.print("\n");
        out.flush();
        out.println("Finished downloading " + downloadInformation);
    }


    // called by thread that is downloading a video
    public static void displayProgressBar(Long bytesReceived, Long fileSize) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - timeSinceLastProgressbarCall + 1;  // +1 to prevent divison by zero

        if (timeDiff > COOLDOWN) {
            long bytesReceivedSinceLastCall = bytesReceived - previousBytesReceived;

            timeSinceLastProgressbarCall = currentTime;
            previousBytesReceived = bytesReceived;

            latestByteDownloadSpeed = bytesReceivedSinceLastCall * 1000 / timeDiff;
            latestFileSize = fileSize;
            latestBytesSaved = bytesReceived;

            
            if (silent) return;

            PrintStream out = System.out;

            out.print(getProgressBar(latestByteDownloadSpeed, fileSize, bytesReceived) + "\r");
            out.flush();
        }
    }


    private static String getProgressBar(long byteDownloadSpeed, long fileSize, long bytesSaved) {
        String downloadSpeedAndFilesize = " " + reduceSize(bytesSaved, 2) + " / " + reduceSize(fileSize, 2) + " (" + reduceSize(byteDownloadSpeed) + "/s)";

        int maxWidth = Math.max(40, cols - downloadSpeedAndFilesize.length() - 4);

        int filled = (int) Math.round(maxWidth * ((double) bytesSaved / fileSize));
        int remaining = maxWidth - filled;

        String bar = String.valueOf('#').repeat(Math.max(0, filled)) +
                ".".repeat(Math.max(0, remaining));

        return "[" + bar + "] " + downloadSpeedAndFilesize;
    }


    // Returns a progress bar string with the latest information regarding a download
    public static String getLatestProgressBar() {
        return getProgressBar(latestByteDownloadSpeed, latestFileSize, latestBytesSaved);
    }


    // returns a string with the reduced size
    // with input 10 000 000 this returns: 10.0 MB
    public static String reduceSize(long byteSize) {
        return reduceSize(byteSize, 5);
    }

    public static String reduceSize(long byteSize, int maxDivideCount) {
        fillConversionMap();

        double size = (double) byteSize * 10;

        int divideCount = 0;
        while (size > 15000 && divideCount < maxDivideCount) {
            size /= 1000;
            divideCount++;
        }

        size = Math.round(size) / 10;

        return size + " " + conversionMap.get(divideCount);
    }
}
