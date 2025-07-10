package me.staykwimp.jvd;


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
    public static void displayProgressBar(long bytesReceived, long fileSize) {
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
