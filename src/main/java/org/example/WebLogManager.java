package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class WebLogManager {
    private static final int DEFAULT_PORT = 8080;
    private static final String CONFIG_FILE = "spotsDenickerLogFile.txt";
    private static final String LOCK_FILE = "spotsDenicker.lock"; //idek what this is even for jaaa
    private Set<String> customPaths = new HashSet<>();
    private String lunarPath;
    private String badlionPath;
    private String lastSearchResults = "";
    private boolean noResultsFound = false;
    private HttpServer server;
    private File lockFile;
    private int port;
    private String actualLunarPath; // To store the actual valid path
    private String actualBadlionPath; // To store the actual valid path

    public void start() throws IOException {
        if (isAlreadyRunning()) {
            System.out.println("Program is already running. Opening existing instance in browser...");
            openExistingInstance();
            System.exit(0);
        }

        port = findAvailablePort();
        createLockFile();

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new MainHandler());
            server.createContext("/addPath", new AddPathHandler());
            server.createContext("/search", new SearchHandler());
            server.createContext("/close", new CloseHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Server started on port " + port);
            openBrowser();
        } catch (IOException e) {
            deleteLockFile();
            throw e;
        }

        // Add shutdown hook to clean up lock file
        Runtime.getRuntime().addShutdownHook(new Thread(this::deleteLockFile));
    }

    private boolean isAlreadyRunning() {
        lockFile = new File(LOCK_FILE);
        if (lockFile.exists()) {
            try {
                // Read the port number from the lock file
                List<String> lines = Files.readAllLines(lockFile.toPath());
                if (!lines.isEmpty()) {
                    int existingPort = Integer.parseInt(lines.get(0));
                    // Try to connect to the port to verify the server is actually running
                    try (Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress("localhost", existingPort), 500);
                        return true;
                    } catch (IOException e) {
                        // If connection fails, assume the previous instance crashed
                        lockFile.delete();
                        return false;
                    }
                }
            } catch (IOException | NumberFormatException e) {
                // If there's any error reading the file, assume it's corrupt and delete it
                lockFile.delete();
            }
        }
        return false;
    }

    private void createLockFile() {
        try {
            // Write the port number to the lock file
            Files.write(lockFile.toPath(), String.valueOf(port).getBytes());
        } catch (IOException e) {
            System.err.println("Warning: Could not create lock file");
        }
    }

    private void deleteLockFile() {
        if (lockFile != null && lockFile.exists()) {
            lockFile.delete();
        }
    }

    private int findAvailablePort() {
        int port = DEFAULT_PORT;
        while (port < DEFAULT_PORT + 100) { // Try up to 100 ports
            try (ServerSocket socket = new ServerSocket(port)) {
                return port;
            } catch (IOException e) {
                port++;
            }
        }
        throw new RuntimeException("No available ports found");
    }

    private void openExistingInstance() {
        try {
            List<String> lines = Files.readAllLines(lockFile.toPath());
            if (!lines.isEmpty()) {
                int existingPort = Integer.parseInt(lines.get(0));
                openURL("http://localhost:" + existingPort);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error opening existing instance");
        }
    }

    private void openBrowser() {
        openURL("http://localhost:" + port);
    }

    private void openURL(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (IOException e) {
            System.out.println("Please open " + url + " in your browser");
        }
    }

    private class CloseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Shutting down...";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                shutdown();
            }).start();
        }
    }

    public void shutdown() {
        if (server != null) {
            server.stop(0);
        }
        deleteLockFile();
        System.exit(0);
    }

    private class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                String username = URLDecoder.decode(formData.split("=")[1], "UTF-8");

                if (isValidMinecraftUsername(username)) {
                    String[] logDirectories = customPaths.toArray(new String[0]);
                    String[][] results = findLastGames(username, logDirectories);

                    // Check if we found any results
                    boolean foundResults = false;
                    for (String[] result : results) {
                        if (result[0] != null) {
                            foundResults = true;
                            break;
                        }
                    }

                    if (!foundResults) {
                        noResultsFound = true;
                        lastSearchResults = "";
                    } else {
                        noResultsFound = false;
                        StringBuilder resultHtml = new StringBuilder();
                        resultHtml.append("<div class='results'>");

                        for (int i = 0; i < results.length; i++) {
                            if (results[i][0] != null) {
                                resultHtml.append("<div class='game-result'>");
                                resultHtml.append("<h4>Game ").append(i + 1).append("</h4>");
                                resultHtml.append("<p class='timestamp'>Timestamp: ").append(results[i][2]).append("</p>");
                                resultHtml.append("<div class='query'>Query 1:<br>").append(results[i][0]).append("</div>");
                                resultHtml.append("<div class='query'>Query 2:<br>").append(results[i][1]).append("</div>");
                                resultHtml.append("</div>");
                            }
                        }

                        resultHtml.append("</div>");
                        lastSearchResults = resultHtml.toString();
                    }
                }
            }
            exchange.getResponseHeaders().add("Location", "/");
            exchange.sendResponseHeaders(302, -1);
        }

        private boolean isValidMinecraftUsername(String username) {
            return username != null &&
                    username.length() >= 2 &&
                    username.length() <= 16 &&
                    username.matches("^[a-zA-Z0-9_]+$");
        }
    }

    private class MainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            initializePaths();
            String response = generateHtml();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private class AddPathHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                String path = formData.split("=")[1].replace("+", " ");
                path = URLDecoder.decode(path, "UTF-8");

                if (new File(path).exists()) {
                    customPaths.add(path);
                    savePathsToFile();
                }
            }
            exchange.getResponseHeaders().add("Location", "/");
            exchange.sendResponseHeaders(302, -1);
        }
    }

    private void initializePaths() {
        // Get current user
        String username = System.getProperty("user.name");

        // Set default paths with both C: and D: options
        String lunarPathC = "C:\\Users\\" + username + "\\.lunarclient\\offline\\multiver\\logs";
        String lunarPathD = "D:\\Users\\" + username + "\\.lunarclient\\offline\\multiver\\logs";
        String badlionPathC = "C:\\Users\\" + username + "\\AppData\\Roaming\\.minecraft\\logs\\blclient\\minecraft";
        String badlionPathD = "D:\\Users\\" + username + "\\AppData\\Roaming\\.minecraft\\logs\\blclient\\minecraft";

        // Check Lunar paths
        if (new File(lunarPathC).exists()) {
            actualLunarPath = lunarPathC;
        } else if (new File(lunarPathD).exists()) {
            actualLunarPath = lunarPathD;
        } else {
            actualLunarPath = lunarPathC; // Default to C: path for display
        }

        // Check Badlion paths
        if (new File(badlionPathC).exists()) {
            actualBadlionPath = badlionPathC;
        } else if (new File(badlionPathD).exists()) {
            actualBadlionPath = badlionPathD;
        } else {
            actualBadlionPath = badlionPathC; // Default to C: path for display
        }

        // Load existing paths from file
        loadPathsFromFile();

        // Check if paths exist and add to customPaths if they do
        if (new File(actualLunarPath).exists() && !customPaths.contains(actualLunarPath)) {
            customPaths.add(actualLunarPath);
            savePathsToFile();
        }
        if (new File(actualBadlionPath).exists() && !customPaths.contains(actualBadlionPath)) {
            customPaths.add(actualBadlionPath);
            savePathsToFile();
        }
    }

    private void loadPathsFromFile() {
        customPaths.clear();
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                List<String> lines = Files.readAllLines(Paths.get(CONFIG_FILE));
                customPaths.addAll(lines);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void savePathsToFile() {
        try {
            Files.write(Paths.get(CONFIG_FILE), customPaths);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><title>Log File Manager</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append(".status { font-weight: bold; }");
        html.append(".found { color: green; }");
        html.append(".not-found { color: red; }");
        html.append(".game-result { margin: 20px 0; padding: 10px; border: 1px solid #ccc; }");
        html.append(".timestamp { color: #666; }");
        html.append(".search-section { margin: 30px 0; padding: 20px; background: #f5f5f5; }");
        html.append(".query { margin: 10px 0; padding: 5px; background: #fff; }");
        html.append(".loading { display: none; margin: 20px 0; }");
        html.append(".loading:after { content: 'Searching logs...'; }");
        html.append(".no-results { color: #666; font-style: italic; margin: 20px 0; }");
        html.append(".close-button { position: fixed; top: 20px; right: 20px; ");
        html.append("background: #ff4444; color: white; border: none; padding: 10px 20px; ");
        html.append("cursor: pointer; border-radius: 5px; }");
        html.append(".close-button:hover { background: #ff0000; }");
        html.append("</style>");

        // Add JavaScript for loading indicator
        html.append("<script>");
        html.append("function showLoading() {");
        html.append("  document.getElementById('loading').style.display = 'block';");
        html.append("  document.getElementById('results').style.display = 'none';");
        html.append("  return true;");
        html.append("}");

        //Add js for shutting down
        html.append("function closeProgram() {");
        html.append("  if (confirm('Are you sure you want to close the program?')) {");
        html.append("    fetch('/close').then(() => window.close());");
        html.append("  }");
        html.append("}");
        html.append("</script>");
        html.append("</script>");

        html.append("</head><body>");

        // Close button
        html.append("<button onclick='closeProgram()' class='close-button'>Close</button>");

        // Log Paths Section
        html.append("<h2>Log File Paths</h2>");

        // Lunar Client section
        html.append("<h3>Lunar Logs file Path: ");
        html.append(actualLunarPath);
        if (new File(actualLunarPath).exists()) {
            html.append(" <span class='status found'>FOUND</span>");
        } else {
            html.append(" <span class='status not-found'>NOT FOUND</span>");
            html.append("<br><small style='color: #666; margin-left: 20px;'>");
            html.append("Checked both C: and D: drives</small>");
        }
        html.append("</h3>");

        // Badlion section
        html.append("<h3>Badlion Logs file Path: ");
        html.append(actualBadlionPath);
        if (new File(actualBadlionPath).exists()) {
            html.append(" <span class='status found'>FOUND</span>");
        } else {
            html.append(" <span class='status not-found'>NOT FOUND</span>");
            html.append("<br><small style='color: #666; margin-left: 20px;'>");
            html.append("Checked both C: and D: drives</small>");
        }
        html.append("</h3>");

        // Custom paths section - now filtering out Lunar and Badlion paths
        html.append("<h3>Custom Logs:</h3>");
        html.append("<ul>");
        boolean hasCustomPaths = false;
        for (String path : customPaths) {
            // Skip if path is either Lunar or Badlion path
            if (!path.equals(actualLunarPath) && !path.equals(actualBadlionPath)) {
                hasCustomPaths = true;
                html.append("<li>").append(path);
                html.append(" <span class='status ").append(isPathValid(path) ? "found'>FOUND" : "not-found'>NOT FOUND").append("</span>");
                html.append("</li>");
            }
        }
        if (!hasCustomPaths) {
            html.append("<li><i>No custom paths added</i></li>");
        }
        html.append("</ul>");

        // Add new path form
        html.append("<form action='/addPath' method='post'>");
        html.append("<input type='text' name='path' placeholder='Enter new path' size='50' autocomplete='off'>");
        html.append("<input type='submit' value='Add Path'>");
        html.append("</form>");

        // Query Generator Section
        html.append("<div class='search-section'>");
        html.append("<h2>Query Generator</h2>");
        html.append("<form action='/search' method='post' onsubmit='return showLoading();'>");
        html.append("<input type='text' name='username' placeholder='Enter Minecraft username' ");
        html.append("pattern='^[a-zA-Z0-9_]{2,16}$' autocomplete='off' ");
        html.append("title='Username must be 2-16 characters long and contain only letters, numbers, and underscores' required>");
        html.append("<input type='submit' value='Search'>");
        html.append("</form>");

        // Loading indicator
        html.append("<div id='loading' class='loading'></div>");

        // Results section
        html.append("<div id='results'>");
        if (noResultsFound) {
            html.append("<div class='no-results'>No games found for this username in the logs.</div>");
        } else if (!lastSearchResults.isEmpty()) {
            html.append("<h3>Search Results:</h3>");
            html.append(lastSearchResults);
        }
        html.append("</div>");

        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    private boolean isPathValid(String path) {
        return new File(path).exists();
    }

    // Placeholder for CombinedLogAnalyzer method - replace with actual implementation
    private String[][] findLastGames(String username, String[] logDirectories) {
        return CombinedLogAnalyzer.findLastGames(username, logDirectories);
    }
}