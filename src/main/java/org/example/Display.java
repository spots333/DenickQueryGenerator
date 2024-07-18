package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Display {
    private JFrame frame;
    private JTextPane outputArea;
    private final String DOWN = "--&#8681;--&#8681;--&#8681;--&#8681;--&#8681;--&#8681;--&#8681;--"; //are supposed to be arrows but unicode didnt work
    private final String UP = "--&#8679;--&#8679;--&#8679;--&#8679;--&#8679;--&#8679;--&#8679;--"; //so i had to do this.

    public Display() {
        //LogFileFun.selectLogFile();
        File logFileSaver = LogFileFun.getLogFile();
        if (logFileSaver == null) {
            //no log file found, ask for one
            LogFileFun.selectLogFile();
            LogFileFun.createLogFile(Main.filepath.getPath());
        }
        else {
            Main.filepath = logFileSaver;
        }

        createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("Query Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setSize(500, 400);
        frame.setSize(500, 150);

        outputArea = new JTextPane();
        outputArea.setContentType("text/html; charset=UTF-8");
        outputArea.setEditable(false);
        outputArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        outputArea.setFont(new Font("Times New Roman", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(outputArea);

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    //ai generated method idk how much of it is really needed
    public void appendQueryMessage(String query1, String query2) {
        SwingUtilities.invokeLater(() -> {
            String newContent = String.format(
                    "<div style='font-family: Times New Roman; font-size: 14px;'>" +
                            "<font color='red'>%s</font><br>%s<br>%s<br><font color='red'>%s</font><br>" +
                            "</div>",
                    this.DOWN,
                    escapeHtml(query1),
                    escapeHtml(query2),
                    this.UP
            );

            String currentContent = outputArea.getText();
            String updatedContent;
            if (currentContent.contains("</body></html>")) {
                updatedContent = currentContent.replace("</body></html>", newContent + "</body></html>");
            } else {
                updatedContent = "<html><body>" + newContent + "</body></html>";
            }

            outputArea.setText(updatedContent);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
    }


    public void close() {
        System.out.println("Program endy :)");
        frame.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Display display = new Display();
            // Test output
            display.appendQueryMessage("Test Query 1", "Test Query 2");
        });
    }
}
