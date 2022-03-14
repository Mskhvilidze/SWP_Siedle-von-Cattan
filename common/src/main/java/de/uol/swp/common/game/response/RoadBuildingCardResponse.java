package de.uol.swp.common.game.response;

import java.util.Objects;

/**
 * Response sent to a player who used {@link de.uol.swp.common.game.board.DevCard#ROAD_BUILDING ROAD_BUILDING}
 */
public class RoadBuildingCardResponse extends AbstractGameResponse {

    private final int numOfFreeRoads;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this response is sent to
     * @param numOfFreeRoads  the amount of free roads the player has to place
     * @since 2021-06-13
     */
    public RoadBuildingCardResponse(String gameSessionName, int numOfFreeRoads) {
        super(gameSessionName);
        this.numOfFreeRoads = numOfFreeRoads;
    }

    /**
     * Returns the amount of free roads the player has to place
     *
     * @return the amount of free roads the player has to place
     */
    public int getNumOfFreeRoads() {
        return numOfFreeRoads;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        RoadBuildingCardResponse that = (RoadBuildingCardResponse) obj;
        return numOfFreeRoads == that.numOfFreeRoads;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), numOfFreeRoads);
    }
}
