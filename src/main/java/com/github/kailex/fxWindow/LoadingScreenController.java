package com.github.kailex.fxWindow;

import com.github.kailex.api.util.IController;
import com.github.kailex.api.util.ImageUtil;
import com.github.kailex.api.util.LoggerUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * This class manages loading screen and manage incoming data.
 *
 * @author Kai Sturm
 * @version 1.1
 */
public class LoadingScreenController implements IController<LoadingScreenController>, Initializable {
    @FXML private ProgressBar prgbar;
    @FXML private Label lbl_prgbar;
    @FXML private ImageView imv_upper;
    @FXML private ImageView imv_lower;

    @Override
    public LoadingScreenController getInstance() {
        return this;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Image img = ImageUtil.loadImage("images/mojangstudios.png");
            int halfImageHeight = (int)(img.getHeight()/2);
            Image upperImage = ImageUtil.getImageSnippet(img, 0, 0, (int)img.getWidth(), halfImageHeight);
            Image lowerImage = ImageUtil.getImageSnippet(img, 0, halfImageHeight , (int)img.getWidth(), halfImageHeight);
            imv_upper.setImage(upperImage);
            imv_lower.setImage(lowerImage);

        } catch (IOException e) {
            LoggerUtil.getLogger("LoadingScreenController").log(Level.WARNING, "Can't load title picture");
        }
        setData("Starting Game", -10);
    }

    public void setData(String text, int progress){
        lbl_prgbar.setText(text);
        prgbar.setProgress(progress/100.0);
    }
}