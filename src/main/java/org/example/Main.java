package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Main {
    public static File filepath;

    // wait time after the game end stats are printed before the click here to watch replay is.
    public static final long WAIT_TIME = 10L;

    public static void main(String[] args) throws IOException {
        System.out.println("Program starty :)");

        WebLogManager hey = new WebLogManager();
        hey.start();
    }
}
