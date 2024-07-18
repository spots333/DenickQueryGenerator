package org.example;

import javax.swing.*;
import java.io.*;

/*
 * This class gets the log file we should be watching.
 * First it checks if it is stored in a txt file called spotsDenickerLogFile.
 * If it doesnt exist, ask user for log file name and save it.
 */
public class LogFileFun {
    //TODO: make sure the user's input cant break program somehow
    public static void selectLogFile() {
        while (true) {
            // Create a panel with a text field and set its preferred size
            JPanel panel = new JPanel();
            JTextField textField = new JTextField(40);  // Increase the width by setting the column size
            panel.add(new JLabel("Enter the path to the logs file:"));
            panel.add(textField);

            // Show input dialog with the panel
            int result = JOptionPane.showConfirmDialog(null, panel, "Log File Path", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                JOptionPane.showMessageDialog(null, "Program will terminate.");
                System.exit(0);
            }

            String filePath = textField.getText().trim();

            if (filePath.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter a valid file path.");
                continue;
            }

            File logFileExact = new File(filePath);
            File logFileWithoutQuotes = new File(filePath.replace("\"", ""));

            if (logFileExact.exists() && logFileExact.isFile()) {
                JOptionPane.showMessageDialog(null, "File found: " + logFileExact.getAbsolutePath());
                Main.filepath = logFileExact;
                break;
            } else if (logFileWithoutQuotes.exists() && logFileWithoutQuotes.isFile()) {
                JOptionPane.showMessageDialog(null, "File found: " + logFileWithoutQuotes.getAbsolutePath());
                Main.filepath = logFileWithoutQuotes;
                break;
            } else {
                JOptionPane.showMessageDialog(null, "File does not exist. Please try again.");
            }
        }
    }

    public static File getLogFile() {
        try {
            // Determine the path of the current JAR file
            String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            File jarFile = new File(jarPath);

            // Define the log file
            File logFile = new File(jarFile.getParentFile(), "spotsDenickerLogFile.txt");

            // Check if the log file exists
            if (logFile.exists()) {
                // Read the file path from the log file
                try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                    String filePath = reader.readLine();
                    if (filePath != null && !filePath.trim().isEmpty()) {
                        if (checkIfFileWorks(filePath)) {
                            return new File(filePath.trim());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createLogFile(String filePath) {
        try {
            // Determine the path of the current JAR file
            String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            File jarFile = new File(jarPath);

            // Define the log file
            File logFile = new File(jarFile.getParentFile(), "spotsDenickerLogFile.txt");

            // Write the file path to the log file
            try (FileWriter writer = new FileWriter(logFile)) {
                writer.write(filePath);
                System.out.println("Log file created: " + logFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkIfFileWorks(String filePath) {
        //this if check was already checked above.
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Empty path saved in \"spotsDenickerLogFile.txt\"");
            return false;
        }

        File logFileExact = new File(filePath);
        if (logFileExact.exists() && logFileExact.isFile()) {
            //file found
            Main.filepath = logFileExact;
            return true;
        }
        else {
            JOptionPane.showMessageDialog(null, "Incorrect path saved in \"spotsDenickerLogFile.txt\". Please input a correct one.");
        }

        return false;
    }
}
