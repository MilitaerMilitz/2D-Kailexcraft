package com.github.kailex.api.game;

import com.github.kailex.api.resourcepack.ResourcepackManager;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class stores all relevant game settings.
 *
 * @author Alexander Ley
 * @version 1.0
 */
@Setter @Getter
public class GameSettings {

    private String activeResourcepack;

    /**
     * List of all Resourcepacks in resourcepack folder.
     */
    private List<String> availableResourcepacks;

    public GameSettings(String activeResourcepack) throws IOException {
        this.activeResourcepack = activeResourcepack;
        refreshAvailableResourcepacks();
    }

    /**
     * Refresh list of available resourcepacks.
     * @throws IOException if an I/O error occurs when opening the directory
     */
    public void refreshAvailableResourcepacks() throws IOException {
        availableResourcepacks = Files.list(ResourcepackManager.validate("resourcepack", false)).map(path -> path.toFile().getName()).collect(Collectors.toList());
    }

    /**
     * Checks if pack is currently chosen.
     */
    public boolean isPackActive(String packName){
        return getActiveResourcepack().equals(packName);
    }

    /**
     * Checks if pack is currently chosen.
     */
    public boolean isPackActive(Path pack){
        return isPackActive(pack.toFile().getName());
    }
}
