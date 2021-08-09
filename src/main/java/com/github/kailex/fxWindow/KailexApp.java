package com.github.kailex.fxWindow;

import com.github.kailex.api.util.FxUtils;
import com.github.kailex.api.util.IController;
import javafx.application.Application;
import javafx.stage.Stage;

public class KailexApp extends Application {

    public static Stage stage;
    public static IController<?> activeController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setResizable(false);

        KailexApp.stage = stage;

        //loadImages();

        changeScene("/FXML/example.fxml", "Beispiel", 600, 400, false);

        stage.show();

        if (activeController == null) stage.close();

    }

    public void changeScene(String fxmlFile, String title, int width, int height, boolean ignoreException) {
        activeController = FxUtils.changeScene(stage, fxmlFile, title, width, height, ignoreException);
    }
}
