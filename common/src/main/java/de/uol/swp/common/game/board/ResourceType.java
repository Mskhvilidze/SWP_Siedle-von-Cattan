package de.uol.swp.common.game.board;

import java.util.Random;

/**
 * The resources a player can have
 */
public enum ResourceType {
    LUMBER,
    WOOL,
    GRAIN,
    ORE,
    BRICK;

    private static final ResourceType[] VALUES = values();
    private static final Random RANDOM = new Random();

    /**
     * Returns a random resource type
     *
     * @return a random resource type
     */
    public static ResourceType getRandom() {
        return VALUES[RANDOM.nextInt(VALUES.length)];
    }
}
