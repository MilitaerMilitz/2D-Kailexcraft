package com.github.kailex;

import com.github.kailex.api.resourcepack.ResourcepackManager;
import com.github.kailex.api.util.LoggerUtil;
import com.github.kailex.fxWindow.KailexApp;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Launches application and does several things e.g. start loading
 * resources tasks before launching javaFx window.
 *
 * @author Alexander Ley
 * @version 1.0
 */
public class LaunchApplication {
    public static final Logger LOGGER = LoggerUtil.getLogger("KailexApp");

    public static void main(String[] args) {
        if (KailexApp.GAME_SETTINGS == null) return;

        try {
            //Loads active resourcepack, but not forced to improve performance.
            final CountDownLatch readyLatch = ResourcepackManager.applyPack(KailexApp.HOME_DIR
                    .resolve("resourcepack")
                    .resolve(KailexApp.GAME_SETTINGS.getActiveResourcepack()), false);

            //Waits until loading is ready and loads next scene.
            final Thread waiting = new Thread(() -> {
                try {
                    readyLatch.await();
                    Thread.sleep(2000);
                    ResourcepackManager.showProgress("Starting Game", -1);
                    //TODO: Load next scene
                }
                catch (InterruptedException e) {
                    throw new RuntimeException("Cannot apply Pack.");
                }
            });
            waiting.start();
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            LoggerUtil.showError("Cannot load resourcepack. Application will be closed.");
            return;
        }

        KailexApp.main(args);
    }

    /**
     * Creates home directory if it does not exists.
     * Default home directory is located at %appdata%/.kaliexcraft2D on Windows and
     * user.home/Library/Application Suppor/.kailexcraft2D on Mac and user.dir/.kaliexcraft2D on Linux.
     * @return Returns default home directory where all application needed files will be stored.
     */
    public static Path getHomeDir(){
        final String folderName = ".kailexcraft2D";
        final String FileFolder;

        final String os = System.getProperty("os.name").toUpperCase();

        if (os.contains("WIN")) {
            FileFolder = System.getenv("APPDATA") + "\\" + folderName;
            LOGGER.log(Level.INFO, "Found Windows");
        }
        else if (os.contains("MAC")) {
            FileFolder = System.getProperty("user.home") + "/Library/Application " + "Support"
                    + folderName;
            LOGGER.log(Level.INFO, "Found Mac Os");
        }
        else if (os.contains("NUX")) {
            FileFolder = System.getProperty("user.dir") + folderName;
            LOGGER.log(Level.INFO, "Found Linux");
        }
        else {
            LOGGER.log(Level.INFO, os + " is not supported");
            throw new RuntimeException(os + " is not supported");
        }

        final File directory = new File(FileFolder);

        if (directory.exists()) {
            System.out.println("Found folder");
        }

        if (!directory.exists()) {
            if (!directory.mkdir()){
                LOGGER.log(Level.SEVERE, directory + " cannot created.");
                throw new RuntimeException(directory + " cannot created.");
            }
        }
        return directory.toPath();
    }
}
