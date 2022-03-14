package de.uol.swp.common.game.board;

import java.util.EnumMap;

/**
 * Custom implementation of EnumMap that allows for easier control over buildableObjects
 */
public class PieceTypeEnumMap extends EnumMap<PieceType, Integer> {

    /**
     * Constructs a PieceTypeEnumMap with all buildableObjects quantities set to 0
     */
    public PieceTypeEnumMap() {
        super(PieceType.class);
        putObjects(0, 0, 0);
    }

    /**
     * Constructs a PieceTypeEnumMap with all buildableObjects quantities set to the given quantities
     *
     * @param road       the road quantity
     * @param city       the city quantity
     * @param settlement the settlement quantity
     */
    public PieceTypeEnumMap(int road, int city, int settlement) {
        super(PieceType.class);
        putObjects(road, city, settlement);
    }

    protected final void putObjects(int road, int city, int settlement) {
        put(PieceType.ROAD, road);
        put(PieceType.CITY, city);
        put(PieceType.SETTLEMENT, settlement);
    }
}
