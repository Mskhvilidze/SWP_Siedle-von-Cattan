package de.uol.swp.common.game;

import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration for the possible colors a player can be assigned to
 */
public enum PlayerColor {
    YELLOW(0),
    GREEN(1),
    BLUE(2),
    RED(3);

    static Map<Integer, PlayerColor> map = new HashMap<>();

    // this block maps int-values to the PlayerColors
    static {
        for (PlayerColor playerColor : PlayerColor.values()) {
            map.put(playerColor.value, playerColor);
        }
    }

    private final int value;

    PlayerColor(int value) {
        this.value = value;
    }

    /**
     * Fetches the PlayerColor for the given int-value (0-3)
     *
     * @param playerColorId the id of the player color
     * @return PlayerColor that is mapped with the int-value
     */
    public static PlayerColor valueOf(int playerColorId) {
        if (map.get(playerColorId) != null)
            return map.get(playerColorId);
        return null;
    }

}
