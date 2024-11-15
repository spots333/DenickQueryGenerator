package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringAnalyzer {

    //returns null if this is a false call
    //Returns a string array of length 3. The first two are queries, and the last is the timestamp.
    public static String[] getQuery(StringBuilder input) {
        //TODO: Create better confirmations, this is just a prototype.
        //There is also a better way to do this, but it will be more complex, less readable,
        //and require way more regex.
        //This way is very easy to fix if something goes wrong or a gamemode gets updated.


        //TODO: figure out how bridge damage is stored, if it is even stored, or whatever
        if (input.lastIndexOf("UHC Duel") != -1) {
            if (input.lastIndexOf("Health Regenerated") != -1) {
                String[][] stats = parseDuelStats(input.toString());
                if (stats == null) {return null;} // error checking :D
                long unixTime = (System.currentTimeMillis() / 1000L) + Main.WAIT_TIME;
                String query1 = stats[0][0] + ":" + unixTime + ":" + stats[0][1] + ":" + stats[0][6] + ":UHC:" + stats[0][7];
                String query2 = stats[1][0] + ":" + unixTime + ":" + stats[1][1] + ":" + stats[1][6] + ":UHC:" + stats[1][7];
                String timestamp = stats[0][stats[0].length-1]; //last part of data
                return new String[]{query1, query2, timestamp};
            }
        }
        else if (input.lastIndexOf("SkyWars Duel") != -1) {
            if (input.lastIndexOf("Health Regenerated") != -1) {
                String[][] stats = parseDuelStats(input.toString());
                if (stats == null) {return null;}
                long unixTime = (System.currentTimeMillis() / 1000L) + Main.WAIT_TIME;
                String query1 = stats[0][0] + ":" + unixTime + ":" + stats[0][1] + ":" + stats[0][5] + ":SW:" + stats[0][7];
                String query2 = stats[1][0] + ":" + unixTime + ":" + stats[1][1] + ":" + stats[1][5] + ":SW:" + stats[1][7];
                String timestamp = stats[0][stats[0].length-1]; //last part of data
                return new String[]{query1, query2, timestamp};
            }
        }
        else if (input.lastIndexOf("Sumo Duel") != -1) {
            if (input.lastIndexOf("Melee Accuracy") != -1) {
                String[][] stats = parseDuelStats(input.toString());
                if (stats == null) {return null;}
                long unixTime = (System.currentTimeMillis() / 1000L) + Main.WAIT_TIME;
                String query1 = stats[0][0] + ":" + unixTime + ":0:0:Sumo:" + stats[0][1];
                String query2 = stats[1][0] + ":" + unixTime + ":0:0:Sumo:" + stats[1][1];
                String timestamp = stats[0][stats[0].length-1]; //last part of data
                return new String[]{query1, query2, timestamp};
            }
        }
        else if (input.lastIndexOf("MegaWalls Duel") != -1) {
            if (input.lastIndexOf("Health Regenerated") != -1) {
                String[][] stats = parseDuelStats(input.toString());
                if (stats == null) {return null;}
                long unixTime = (System.currentTimeMillis() / 1000L) + Main.WAIT_TIME;
                String query1 = stats[0][0] + ":" + unixTime + ":" + stats[0][1] + ":" + stats[0][5] + ":MW:" + stats[0][7];
                String query2 = stats[1][0] + ":" + unixTime + ":" + stats[1][1] + ":" + stats[1][5] + ":MW:" + stats[1][7];
                String timestamp = stats[0][stats[0].length-1]; //last part of data
                return new String[]{query1, query2, timestamp};
            }
        }
        else if (input.lastIndexOf("Blitz Duel") != -1) {
            if (input.lastIndexOf("Health Regenerated") != -1) {
                String[][] stats = parseDuelStats(input.toString());
                if (stats == null) {return null;}
                long unixTime = (System.currentTimeMillis() / 1000L) + Main.WAIT_TIME;
                String query1 = stats[0][0] + ":" + unixTime + ":" + stats[0][1] + ":" + stats[0][5] + ":Blitz:" + stats[0][7];
                String query2 = stats[1][0] + ":" + unixTime + ":" + stats[1][1] + ":" + stats[1][5] + ":Blitz:" + stats[1][7];
                String timestamp = stats[0][stats[0].length-1]; //last part of data
                return new String[]{query1, query2, timestamp};
            }
        }
        else if (input.lastIndexOf("Bow Duel") != -1) {
            if (input.lastIndexOf("Health Regenerated") != -1) {
                String[][] stats = parseDuelStats(input.toString());
                if (stats == null) {return null;}
                long unixTime = (System.currentTimeMillis() / 1000L) + Main.WAIT_TIME;
                String query1 = stats[0][0] + ":" + unixTime + ":" + stats[0][1] + ":" + stats[0][3] + ":Bow:" + stats[0][4];
                String query2 = stats[1][0] + ":" + unixTime + ":" + stats[1][1] + ":" + stats[1][3] + ":Bow:" + stats[1][4];
                String timestamp = stats[0][stats[0].length-1]; //last part of data
                return new String[]{query1, query2, timestamp};
            }
        }
        else if (input.lastIndexOf("Bridge Duel") != -1) {
            if (input.lastIndexOf("Blocks Placed") != -1) {
                String[][] stats = parseDuelStats(input.toString());
                if (stats == null) {return null;}
                long unixTime = (System.currentTimeMillis() / 1000L) + Main.WAIT_TIME;
                String query1 = stats[0][0] + ":" + unixTime + ":" + stats[0][2] + ":" + "0" + ":Bridge:" + stats[0][7];
                String query2 = stats[1][0] + ":" + unixTime + ":" + stats[1][2] + ":" + "0" + ":Bridge:" + stats[1][7];
                String timestamp = stats[0][stats[0].length-1]; //last part of data
                return new String[]{query1, query2, timestamp};
            }
        }
        else if (input.lastIndexOf("OP Duel") != -1) {
            if (input.lastIndexOf("Health Regenerated") != -1) {
                String[][] stats = parseDuelStats(input.toString());
                if (stats == null) {return null;}
                long unixTime = (System.currentTimeMillis() / 1000L) + Main.WAIT_TIME;
                String query1 = stats[0][0] + ":" + unixTime + ":" + stats[0][1] + ":" + stats[0][3] + ":OP:" + stats[0][4];
                String query2 = stats[1][0] + ":" + unixTime + ":" + stats[1][1] + ":" + stats[1][3] + ":OP:" + stats[1][4];
                String timestamp = stats[0][stats[0].length-1]; //last part of data
                return new String[]{query1, query2, timestamp};
            }
        }
        else if (input.lastIndexOf("Classic Duel") != -1) {
            if (input.lastIndexOf("Health Regenerated") != -1) {
                String[][] stats = parseDuelStats(input.toString());
                if (stats == null) {return null;}
                long unixTime = (System.currentTimeMillis() / 1000L) + Main.WAIT_TIME;
                String query1 = stats[0][0] + ":" + unixTime + ":" + stats[0][1] + ":" + stats[0][4] + ":Classic:" + stats[0][5];
                String query2 = stats[1][0] + ":" + unixTime + ":" + stats[1][1] + ":" + stats[1][4] + ":Classic:" + stats[1][5];
                String timestamp = stats[0][stats[0].length-1]; //last part of data
                return new String[]{query1, query2, timestamp};
            }
        }
        else if (input.lastIndexOf("NoDebuff Duel") != -1) {
            if (input.lastIndexOf("Health Regenerated") != -1) {
                String[][] stats = parseDuelStats(input.toString());
                if (stats == null) {return null;}
                long unixTime = (System.currentTimeMillis() / 1000L) + Main.WAIT_TIME;
                String query1 = stats[0][0] + ":" + unixTime + ":" + stats[0][1] + ":" + stats[0][4] + ":NDB:" + stats[0][5];
                String query2 = stats[1][0] + ":" + unixTime + ":" + stats[1][1] + ":" + stats[1][4] + ":NDB:" + stats[1][5];
                String timestamp = stats[0][stats[0].length-1]; //last part of data
                return new String[]{query1, query2, timestamp};
            }
        }
        else if (input.lastIndexOf("Combo Duel") != -1) {
            if (input.lastIndexOf("Health Regenerated") != -1) {
                String[][] stats = parseDuelStats(input.toString());
                if (stats == null) {return null;}
                long unixTime = (System.currentTimeMillis() / 1000L) + Main.WAIT_TIME;
                String query1 = stats[0][0] + ":" + unixTime + ":" + stats[0][1] + ":" + stats[0][4] + ":Combo:" + stats[0][5];
                String query2 = stats[1][0] + ":" + unixTime + ":" + stats[1][1] + ":" + stats[1][4] + ":Combo:" + stats[1][5];
                String timestamp = stats[0][stats[0].length-1]; //last part of data
                return new String[]{query1, query2, timestamp};
            }
        }
        else if (input.lastIndexOf("Bow Spleef Duel") != -1) {
            if (input.lastIndexOf("Shots Taken") != -1) {
                String[][] stats = parseDuelStats(input.toString());
                if (stats == null) {return null;}
                long unixTime = (System.currentTimeMillis() / 1000L) + Main.WAIT_TIME;
                String query1 = stats[0][0] + ":" + unixTime + ":0:0:Tnt:" + stats[0][2];
                String query2 = stats[1][0] + ":" + unixTime + ":0:0:Tnt:" + stats[1][2];
                String timestamp = stats[0][stats[0].length-1]; //last part of data
                return new String[]{query1, query2, timestamp};
            }
        }
        else if (input.lastIndexOf("Boxing Duel") != -1) {
            if (input.lastIndexOf("Melee Accuracy") != -1) {
                String[][] stats = parseDuelStats(input.toString());
                if (stats == null) {return null;}
                long unixTime = (System.currentTimeMillis() / 1000L) + Main.WAIT_TIME;
                String query1 = stats[0][0] + ":" + unixTime + ":0:0:Boxing:" + stats[0][2];
                String query2 = stats[1][0] + ":" + unixTime + ":0:0:Boxing:" + stats[1][2];
                String timestamp = stats[0][stats[0].length-1]; //last part of data
                return new String[]{query1, query2, timestamp};
            }
        }


        return null; //No end duel stats found in given input
    }


    /*
     * The format this gives is two columns for the 2 players.
     * First column has their usernames, each next column is a stat
     * that is shown at the end of the game, even ones we cant use.
     * Last row is either a W or L based on who won that game.
     */
    public static String[][] parseDuelStats(String input) {
        String[] lines = input.split("\n");
        List<String> firstColumn = new ArrayList<>();
        List<String> secondColumn = new ArrayList<>();
        String winner = null;
        String loser = null;
        boolean didFirstGuyWin = false; //initialization required :(
        String timestamp = "";

        // Patterns for both cases
        //Pattern playerPattern1 = Pattern.compile(".*?(\\[?\\w+\\+?\\]?\\s*)?(\\S+)\\s+WINNER!\\s+(\\[?\\w+\\+?\\]?\\s*)?(\\S+)");
        //Pattern playerPattern2 = Pattern.compile(".*?(\\[?\\w+\\+?\\]?\\s*)?(\\S+)\\s+(\\[?\\w+\\+?\\]?\\s*)?(\\S+)\\s+WINNER!");
        Pattern playerPattern1 = Pattern.compile(".*?(\\[.*?\\]\\s*)?([\\w+]+)\\s+WINNER!\\s+(\\[.*?\\]\\s*)?([\\w+]+)");
        Pattern playerPattern2 = Pattern.compile(".*?(\\[.*?\\]\\s*)?([\\w+]+)\\s+(\\[.*?\\]\\s*)?([\\w+]+)\\s+WINNER!");

        for (String line : lines) {
            Matcher matcher1 = playerPattern1.matcher(line);
            Matcher matcher2 = playerPattern2.matcher(line);

            if (matcher1.find()) {
                winner = extractUsername(matcher1.group(1), matcher1.group(2));
                loser = extractUsername(matcher1.group(3), matcher1.group(4));
                didFirstGuyWin = true;
                timestamp = extractTimestamp(line);
                break;
            } else if (matcher2.find()) {
                winner = extractUsername(matcher2.group(1), matcher2.group(2));
                loser = extractUsername(matcher2.group(3), matcher2.group(4));
                didFirstGuyWin = false;
                timestamp = extractTimestamp(line);
                break;
            }
        }

        // If winner is not found, return null
        if (winner == null) return null;

        // Add usernames to the columns
        firstColumn.add(winner);
        secondColumn.add(loser);

        // Parse stat lines
        Pattern statPattern = Pattern.compile("\\s*(\\S+)\\s*-\\s*([^-]+)\\s*-\\s*(\\S+)");
        for (String line : lines) {
            Matcher matcher = statPattern.matcher(line);
            if (matcher.find()) {
                firstColumn.add(matcher.group(1));
                secondColumn.add(matcher.group(3));
            }
        }

        //we can figure out who won/lost, but we cannot figure out
        //who is who, so we cannot determine if our user won or lost
        //that game, since we don't know who our user is.
        //(which is what that value represents)
        //This can be fixed if we can 100% determine if the first player in the stats column is the user.
        if (didFirstGuyWin) {
            firstColumn.add(""); //"W"
            secondColumn.add(""); //"L"
        }
        else {
            firstColumn.add(""); //"L"
            secondColumn.add(""); //"W"
        }

        firstColumn.add(timestamp);
        secondColumn.add(timestamp);

        // Convert to String arrays
        String[] winnerArray = firstColumn.toArray(new String[0]);
        String[] loserArray = secondColumn.toArray(new String[0]);

        // Remove question marks
        return removeQuestionMarks(new String[][]{winnerArray, loserArray});
    }

    /*private static String extractUsername(String rank, String name) {
        String fullName = (rank != null ? rank.trim() + " " : "") + name;
        return fullName.replaceAll("\\[.*?\\]\\s*", "").trim();
    }*/

    private static String extractUsername(String rank, String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Placeholder";
        }

        // Remove any brackets and their contents from the name first
        String cleanName = name.replaceAll("\\[.*?\\]", "").trim();

        // If name is empty after cleaning, return original name
        if (cleanName.isEmpty()) {
            return name.trim();
        }

        return cleanName;
    }

    public static String[][] removeQuestionMarks(String[][] input) {
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                input[i][j] = input[i][j].replace("?", "");
            }
        }

        //the following is just sometimes cuz nick is empty. most likely something with MVP++ rank breaking it.
        if (input[0][0] == null || input[0][0].isEmpty()) {
            input[0][0] = "Placeholder";
        }
        if (input[1][0] == null || input[0][1].isEmpty()) {
            input[1][0] = "Placeholder";
        }

        //the following is just sometimes cuz it adds a space between the username and the last character of username
        if (input[0][0].contains(" ")) {
            input[0][0] = input[0][0].replaceAll("\\s", "");
        }
        if (input[1][0].contains(" ")) {
            input[1][0] = input[1][0].replaceAll("\\s", "");
        }

        return input;
    }

    //debugging purposes only
    private static void printStats(String[][] stats) {
        System.out.println("Left column: " + String.join(", ", stats[0]));
        System.out.println("Right column: " + String.join(", ", stats[1]));
    }

    /**
     * Extracts timestamp from a log line format like "[HH:mm:ss]"
     * @param logLine Single line of log containing timestamp in brackets
     * @return Extracted timestamp string without brackets, or empty if no valid timestamp found
     */
    public static String extractTimestamp(String logLine) {
        // Check if line is null or empty
        if (logLine == null || logLine.isEmpty()) {
            return "";
        }

        // Find the timestamp pattern [XX:XX:XX]
        int startBracket = logLine.indexOf('[');
        int endBracket = logLine.indexOf(']');

        // Verify we found both brackets and they're in the correct position
        if (startBracket != -1 && endBracket != -1 && endBracket > startBracket) {
            // Extract the content between brackets
            String timestamp = logLine.substring(startBracket + 1, endBracket);

            // Verify the timestamp format (XX:XX:XX)
            if (timestamp.matches("\\d{2}:\\d{2}:\\d{2}")) {
                return timestamp;
            }
        }

        return "";
    }
}
