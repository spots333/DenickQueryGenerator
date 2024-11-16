package org.example;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class CombinedLogAnalyzer {
    private static class LogEntry {
        StringBuilder block;
        String filename;
        long timestamp;

        LogEntry(StringBuilder block, String filename, long timestamp) {
            this.block = block;
            this.filename = filename;
            this.timestamp = timestamp;
        }
    }

    public static String[][] findLastGames(String username, String[] logDirectories) {
        List<LogEntry> allGames = new ArrayList<>();
        Pattern borderPattern = Pattern.compile(".*\\?{64}.*");

        // Calculate the cutoff date (1 month ago)
        LocalDateTime oneMonthAgo = LocalDateTime.now().minus(1, ChronoUnit.MONTHS);
        long cutoffTimestamp = oneMonthAgo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        // Search through all directories
        for (String directoryPath : logDirectories) {
            File dir = new File(directoryPath);

            // First, check latest.log
            File latestLog = new File(dir, "latest.log");
            if (latestLog.exists() && latestLog.lastModified() >= cutoffTimestamp) {
                try {
                    allGames.addAll(searchFile(latestLog, username, borderPattern));
                } catch (IOException e) {
                    System.err.println("Error reading latest.log in " + directoryPath + ": " + e.getMessage());
                }
            }

            // Then, check latest.txt
            File latestTxt = new File(dir, "latest.txt");
            if (latestTxt.exists() && latestTxt.lastModified() >= cutoffTimestamp) {
                try {
                    allGames.addAll(searchFile(latestTxt, username, borderPattern));
                } catch (IOException e) {
                    System.err.println("Error reading latest.txt in " + directoryPath + ": " + e.getMessage());
                }
            }

            // Finally check all .gz files
            File[] gzFiles = dir.listFiles((d, name) -> {
                File f = new File(d, name);
                return name.endsWith(".log.gz") && f.lastModified() >= cutoffTimestamp;
            });

            if (gzFiles != null) {
                Arrays.sort(gzFiles, (f1, f2) -> f2.getName().compareTo(f1.getName()));
                for (File gzFile : gzFiles) {
                    try {
                        allGames.addAll(searchGzipFile(gzFile, username, borderPattern));
                    } catch (IOException e) {
                        System.err.println("Error reading " + gzFile.getName() + ": " + e.getMessage());
                        continue;
                    }
                }
            }
        }

        // Sort all games by timestamp (newest first)
        Collections.sort(allGames, (e1, e2) -> Long.compare(e2.timestamp, e1.timestamp));

        // Process the matches to find game blocks
        String[][] results = new String[3][3];
        int gameCount = 0;

        for (LogEntry game : allGames) {
            if (gameCount >= 3) break;

            // Skip blocks containing "Eliminate your opponents!"
            if (game.block.toString().contains("Eliminate your opponents!")) {
                continue;
            }

            String[] queries = StringAnalyzer.getQuery(game.block);
            //System.out.println(game.block); //DEBUGGING
            if (queries != null) {
                results[gameCount][0] = queries[0];
                results[gameCount][1] = queries[1];
                results[gameCount][2] = queries[2];
                gameCount++;
            }
        }

        return results;
    }

    private static List<LogEntry> searchFile(File file, String username, Pattern borderPattern) throws IOException {
        List<LogEntry> results = new ArrayList<>();
        List<StringBuilder> blocks = new ArrayList<>();
        List<Boolean> containsUsername = new ArrayList<>();

        // First pass: collect all blocks
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder currentBlock = null;
            boolean isCollecting = false;
            boolean hasUsername = false;

            String line;
            while ((line = reader.readLine()) != null) {
                if (borderPattern.matcher(line).matches()) {
                    if (isCollecting) {
                        // End of block
                        blocks.add(currentBlock);
                        containsUsername.add(hasUsername);
                    }
                    // Start new block
                    currentBlock = new StringBuilder();
                    currentBlock.append(line).append("\n");
                    isCollecting = true;
                    hasUsername = false;
                } else if (isCollecting) {
                    currentBlock.append(line).append("\n");
                    if (line.toLowerCase().contains(username.toLowerCase())) {
                        hasUsername = true;
                    }
                }
            }

            // Handle last block
            if (isCollecting) {
                blocks.add(currentBlock);
                containsUsername.add(hasUsername);
            }
        }

        // Process blocks in reverse order (most recent first)
        long baseTimestamp = file.lastModified();
        for (int i = blocks.size() - 1; i >= 0; i--) {
            if (containsUsername.get(i)) {
                // Use index to create descending timestamps within the file
                long timestamp = baseTimestamp + (i * 1000L);
                results.add(new LogEntry(blocks.get(i), file.getName(), timestamp));
            }
        }

        return results;
    }

    private static List<LogEntry> searchGzipFile(File file, String username, Pattern borderPattern) throws IOException {
        List<LogEntry> results = new ArrayList<>();
        List<StringBuilder> blocks = new ArrayList<>();
        List<Boolean> containsUsername = new ArrayList<>();

        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(file));
             BufferedReader reader = new BufferedReader(new InputStreamReader(gzis))) {

            StringBuilder currentBlock = null;
            boolean isCollecting = false;
            boolean hasUsername = false;

            String line;
            while ((line = reader.readLine()) != null) {
                if (borderPattern.matcher(line).matches()) {
                    if (isCollecting) {
                        // End of block
                        blocks.add(currentBlock);
                        containsUsername.add(hasUsername);
                    }
                    // Start new block
                    currentBlock = new StringBuilder();
                    currentBlock.append(line).append("\n");
                    isCollecting = true;
                    hasUsername = false;
                } else if (isCollecting) {
                    currentBlock.append(line).append("\n");
                    if (line.toLowerCase().contains(username.toLowerCase())) {
                        hasUsername = true;
                    }
                }
            }

            // Handle last block
            if (isCollecting) {
                blocks.add(currentBlock);
                containsUsername.add(hasUsername);
            }
        }

        // Process blocks in reverse order (most recent first)
        long baseTimestamp = file.lastModified();
        for (int i = blocks.size() - 1; i >= 0; i--) {
            if (containsUsername.get(i)) {
                // Use index to create descending timestamps within the file
                long timestamp = baseTimestamp + (i * 1000L);
                results.add(new LogEntry(blocks.get(i), file.getName(), timestamp));
            }
        }

        return results;
    }

    public static void main(String[] args) {
        String username = "bloodflows";
        String[] logDirectories = {
                "C:\\Users\\xxx\\.lunarclient\\offline\\multiver\\logs"
        };

        String[][] results = findLastGames(username, logDirectories);

        System.out.println("Searched files newer than: " +
                LocalDateTime.now().minus(1, ChronoUnit.MONTHS).toString());

        for (int i = 0; i < results.length; i++) {
            if (results[i][0] != null) {
                System.out.printf("Game %d:%n", i + 1);
                System.out.printf("Query 1: %s%n", results[i][0]);
                System.out.printf("Query 2: %s%n", results[i][1]);
                System.out.println("Timestamp: " + results[i][2]);
                System.out.println();
            }
        }
    }
}