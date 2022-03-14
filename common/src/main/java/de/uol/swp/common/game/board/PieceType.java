package de.uol.swp.common.game.board;

/**
 * The type of buildings that a piece can be
 */
public enum PieceType {
    ROAD(new ResourceEnumMap(1, 0, 0, 0, 1)),
    SETTLEMENT(new ResourceEnumMap(1, 1, 1, 0, 1)),
    CITY(new ResourceEnumMap(0, 0, 2, 3, 0));

    private final ResourceEnumMap costs;

    PieceType(ResourceEnumMap costs) {
        this.costs = costs;
    }

    /**
     * Returns a filled {@code ResourceEnumMap} that represents the cost of this piece
     *
     * @return a filled {@code ResourceEnumMap} that represents the cost of this piece
     */
    public ResourceEnumMap getCosts() {
        return costs;
    }
}
