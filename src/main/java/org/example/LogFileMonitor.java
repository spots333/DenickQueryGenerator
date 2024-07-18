package org.example;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogFileMonitor {

    private static final String LOG_FILE_PATH = Main.filepath.getPath();
    private static final String LINE_REGEX = "^\\[\\d{2}:\\d{2}:\\d{2}\\] \\[Client thread/INFO\\]: \\[CHAT\\] .*";
    private static final Pattern PATTERN = Pattern.compile(LINE_REGEX);

    public static void run() {
        Path logFilePath = Paths.get(LOG_FILE_PATH);

        // Create a watch service
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            // Register the log file directory with the watch service
            logFilePath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            // Position to the end of the file
            try (RandomAccessFile file = new RandomAccessFile(LOG_FILE_PATH, "r")) {
                long filePointer = file.length();

                while (true) {
                    // Wait for a file change event with a timeout to periodically check the file
                    WatchKey key = watchService.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                    if (key != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                filePointer = readNewLines(file, filePointer);
                            }
                        }
                        // Reset the key and remove from the set if it is no longer valid
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }
                    } else {
                        // Periodically check the file for new lines
                        filePointer = readNewLines(file, filePointer);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static long readNewLines(RandomAccessFile file, long filePointer) throws IOException, InterruptedException {
        Pattern questionPattern = Pattern.compile(".*\\?{64}.*");
        //Pattern endGamePattern = Pattern.compile("Click to view [a-zA-Z0-9_]{2,16}'s Inventory"); //no worky for sum reason


        long newLength = file.length();
        if (newLength > filePointer) {
            file.seek(filePointer);
            String line;
            boolean insidePatternBlock = false;
            StringBuilder block = new StringBuilder();
            int blockLineCount = 0;

            while ((line = file.readLine()) != null) {
                if (questionPattern.matcher(line).matches()) {
                    if (insidePatternBlock) {
                        // Ending block
                        if (blockLineCount >= 3) {
                            if (block.lastIndexOf("Eliminate your opponents!") == -1) {
                                String[] queries = StringAnalyzer.getQuery(block);
                                if (queries != null) {
                                    Main.d.appendQueryMessage(queries[0], queries[1]);
                                    System.out.println(queries[1]);
                                }
                            }
                        }
                        insidePatternBlock = false;
                        block.setLength(0);
                        blockLineCount = 0;
                    } else {
                        // Starting block
                        insidePatternBlock = true;
                        //block.append(line).append(System.lineSeparator());
                        blockLineCount++;
                    }
                } else if (insidePatternBlock) {
                    block.append(line).append(System.lineSeparator());
                    blockLineCount++;
                }
            }
            filePointer = newLength;
        }
        return filePointer;
    }

    public static String removeNonAscii(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[^\\x00-\\x7F]", "");
    }

}
