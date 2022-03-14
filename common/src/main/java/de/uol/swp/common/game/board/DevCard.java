package de.uol.swp.common.game.board;


/**
 * An enumeration for the possible development cards a player can have
 */
public enum DevCard {
    /**
     * Victory Point Card. Gives 1 hidden victory point to a player
     */
    VP,
    /**
     * Knight Card. Allows a player to move the robber when played
     */
    KNIGHT,
    /**
     * One the Progress Cards. Allows a player to take any 2 resources from the bank
     */
    YEAR_OF_PLENTY,
    /**
     * One the Progress Cards. Allows a player to name 1 resource type.
     * All other players have to give all resources that they have of that type
     */
    MONOPOLY,
    /**
     * One the Progress Cards. Allows a player to immediately place 2 free roads (according to normal building rules)
     */
    ROAD_BUILDING;

    /**
     * Returns a filled {@code ResourceEnumMap} that represents the cost of a dev card
     *
     * @return a filled {@code ResourceEnumMap} that represents the cost of a dev card
     */
    public static ResourceEnumMap getCost() {
        return new ResourceEnumMap(0, 1, 1, 1, 0);
    }
}

