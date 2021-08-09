package com.github.kailex.api.util;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * This Interface wraps all Controller into one Reference Type and Returns the actual object.
 *
 * @author Alexander Ley
 * @version 1.0
 */
public interface IController<T> {
    T getInstance();
}
