package com.github.kailex.fxWindow;

import com.github.kailex.api.util.IController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.net.URL;
import java.util.ResourceBundle;

public class LoadingScreenController implements IController<LoadingScreenController>, Initializable {
    @FXML private ProgressBar prgbar;
    @FXML private Label lbl_prgbar;

    @Override
    public LoadingScreenController getInstance() {
        return null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prgbar.setProgress(0.64);
        setTextInPrgbar();
    }

    public void setTextInPrgbar() {
        double progress = prgbar.getProgress() * 100;
        lbl_prgbar.setText(progress + "%");
    }
}