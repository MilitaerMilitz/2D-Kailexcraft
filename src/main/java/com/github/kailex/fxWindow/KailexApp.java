package com.github.kailex.fxWindow;

import com.github.kailex.LaunchApplication;
import com.github.kailex.api.game.GameSettings;
import com.github.kailex.api.util.FxUtils;
import com.github.kailex.api.util.IController;
import com.github.kailex.api.util.LoggerUtil;
import com.github.kailex.api.util.files.FileUtil;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class holds all information about and manages Kailex Application.
 *
 * @author Alexander Ley
 * @version 1.0
 */
public class KailexApp extends Application {

    public static Stage stage;
    public static IController<?> activeController;

    public static final Path HOME_DIR = LaunchApplication.getHomeDir();
    public static final GameSettings GAME_SETTINGS;

    /*Initialize GameSettings when class is loaded.
    * Loads settings from settings.json in home directory and creates new default settings if it does not exist.
     */
    static {
        final Logger LOGGER = LoggerUtil.getLogger("KailexApp");

        GameSettings tmpSettings;
        try {
            tmpSettings = FileUtil.loadFromJson(HOME_DIR.resolve("settings.json"), GameSettings.class);
        }
        catch (Exception e) {
            LOGGER.log(Level.INFO, "Cannot load settings. Creating new ...");
            try {
                tmpSettings = new GameSettings("default_pack.zip");
            }
            catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Cannot create settings. Application will be closed.");
                LoggerUtil.showError("Cannot create settings. Application will be closed.");
                tmpSettings = null;
            }
        }
        GAME_SETTINGS = tmpSettings;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setResizable(false);

        KailexApp.stage = stage;

        changeScene("/FXML/loadingScreen.fxml", "KaiLex Craft 2D", 856, 482, false);

        stage.show();

        if (activeController == null) stage.close();
    }

    @Override
    public void stop() throws IOException {
        //Save game settings to json
        final Path settingsPath = HOME_DIR.resolve("settings.json");
        FileUtil.saveToJson(settingsPath, GAME_SETTINGS);
    }

    public void changeScene(String fxmlFile, String title, int width, int height, boolean ignoreException) {
        activeController = FxUtils.changeScene(stage, fxmlFile, title, width, height, ignoreException);
    }
}
