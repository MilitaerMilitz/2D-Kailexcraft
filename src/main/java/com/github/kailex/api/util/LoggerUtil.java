package com.github.kailex.api.util;

import javax.swing.*;
import java.util.logging.Logger;

public final class LoggerUtil {
    public static Logger getLogger(Class<?> clazz){
        return Logger.getLogger(clazz.getName());
    }

    public static Logger getLogger(String name){
        return Logger.getLogger(name);
    }

    public static void showError(String msg){
        msg = msg.replace("ä", "\\u00e4")
                .replace("ß", "\\00df")
                .replace("ö", "\\00f6")
                .replace("ü", "\\00fc")
                .replace("Ä", "\\00c4")
                .replace("Ö", "\\00d6")
                .replace("Ü", "\\00dc");
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
