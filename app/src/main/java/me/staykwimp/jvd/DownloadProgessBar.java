package me.staykwimp.jvd;


import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

// import org.jline.terminal.TerminalBuilder;
// import java.io.IOException;

public class DownloadProgessBar {
    private static long timeSinceLastProgressbarCall = 0;
    private static long previousBytesReceived = 0;
    private static int cols = 120;
    private static final Map<Integer, String> conversionMap = new HashMap<>(5);
    private static boolean filledConversionMap = false;
    private static final int COOLDOWN = 200;

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
    public static void ready() {
        timeSinceLastProgressbarCall = 0;
        previousBytesReceived = 0;

        fillConversionMap();

        // try {
        //     cols = TerminalBuilder.builder().dumb(true).build().getWidth();
        //     // cols = TerminalBuilder.terminal().getWidth();
        // } catch (IOException e) {
        //     e.printStackTrace();
        //     cols = 80; // default to 80 columns
        // }
    }

    public static void end() {
        PrintStream out = System.out;
        out.print("\n");
        out.flush();
    }

    public static void displayProgressBar(long bytesReceived, long fileSize) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - timeSinceLastProgressbarCall + 1;  // +1 to prevent divison by zero

        if (timeDiff > COOLDOWN) {
            long bytesReceivedSinceLastCall = bytesReceived - previousBytesReceived;

            timeSinceLastProgressbarCall = currentTime;
            previousBytesReceived = bytesReceived;

            long byteDownloadSpeed = bytesReceivedSinceLastCall * 1000 / timeDiff;

            displayProgressBar(byteDownloadSpeed, fileSize, bytesReceived);
        }
    }


    public static void displayProgressBar(long byteDownloadSpeed, long fileSize, long bytesSaved) {
        String downloadSpeedAndFilesize = " " + reduceSize(bytesSaved) + " / " + reduceSize(fileSize) + " (" + reduceSize(byteDownloadSpeed) + "/s)";

        int maxWidth = Math.max(40, cols - downloadSpeedAndFilesize.length() - 4);

        int filled = (int) Math.round(maxWidth * ((double) bytesSaved / fileSize));
        int remaining = maxWidth - filled;

        String bar = String.valueOf('#').repeat(Math.max(0, filled)) +
                ".".repeat(Math.max(0, remaining));
        
        
        PrintStream out = System.out;

        out.printf("[%s] %s\r", bar, downloadSpeedAndFilesize);
        out.flush();
    }


    // returns a string with the reduced size
    // with input 10 000 000 this returns: 10 MB
    public static String reduceSize(long byteSize) {
        return reduceSize(byteSize, 1000);
    }

    public static String reduceSize(long byteSize, int maxDivideCount) {
        fillConversionMap();

        double size = (double) byteSize * 10;

        int divideCount = 0;
        while (size > 10000 && divideCount < maxDivideCount) {
            size /= 1000;
            divideCount++;
        }

        size = Math.round(size) / 10;

        return size + " " + conversionMap.get(divideCount);
    }
}
