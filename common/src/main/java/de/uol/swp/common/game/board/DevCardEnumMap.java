package de.uol.swp.common.game.board;


import java.util.EnumMap;

/**
 * Custom implementation of EnumMap that allows for easier control over DevCards
 */
public class DevCardEnumMap extends EnumMap<DevCard, Integer> {

    /**
     * Constructs a DevCardEnumMap with all DevCard quantities set to 0
     */
    public DevCardEnumMap() {
        super(DevCard.class);
        putResources(0, 0, 0, 0, 0);
    }

    /**
     * Constructs a DevCardEnumMap with all DevCard quantities set to the given quantities
     *
     * @param victoryPointCard the victoryPointCard quantity
     * @param knight           the knight quantity
     * @param yearOfPlenty     the yearOfPlenty quantity
     * @param monopoly         the monopoly quantity
     * @param roadBuilding     the roadBuilding quantity
     */
    public DevCardEnumMap(int victoryPointCard, int knight, int yearOfPlenty, int monopoly, int roadBuilding) {
        super(DevCard.class);
        putResources(victoryPointCard, knight, yearOfPlenty, monopoly, roadBuilding);
    }

    /**
     * Puts the given amount of each DevCard into this map
     *
     * @param victoryPointCard the victoryPointCard quantity
     * @param knight           the knight quantity
     * @param yearOfPlenty     the yearOfPlenty quantity
     * @param monopoly         the monopoly quantity
     * @param roadBuilding     the roadBuilding quantity
     */
    private void putResources(int victoryPointCard, int knight, int yearOfPlenty, int monopoly, int roadBuilding) {
        put(DevCard.VP, victoryPointCard);
        put(DevCard.KNIGHT, knight);
        put(DevCard.YEAR_OF_PLENTY, yearOfPlenty);
        put(DevCard.MONOPOLY, monopoly);
        put(DevCard.ROAD_BUILDING, roadBuilding);
    }
}