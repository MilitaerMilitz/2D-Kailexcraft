package com.github.kailex.api.util;

/**
 * This enum offers several rotations with degrees. This class is e.g. used in ImageManager.
 * North -> 0°
 * East -> 90°
 * South -> 180°
 * West -> 90°
 *
 * @author Alexander Ley
 * @version 1.0
 */
public enum Rotation {
    NORTH(0),
    WEST(270),
    SOUTH(180),
    EAST(90);

    private final int degrees;

    Rotation(int degrees){
        this.degrees = degrees;
    }

    public int getDegrees() {
        return degrees;
    }
}
