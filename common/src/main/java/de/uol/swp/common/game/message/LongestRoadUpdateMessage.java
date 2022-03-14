package de.uol.swp.common.game.message;

import de.uol.swp.common.game.dto.PlayerDTO;

import java.util.Objects;

/**
 * Message send to update the clients info about the player with the longest road.
 */
public class LongestRoadUpdateMessage extends AbstractGameMessage {

    private final PlayerDTO playerWithLongestRoad;

    /**
     * Constructor
     *
     * @param gameSessionName       game Session Name
     * @param playerWithLongestRoad the player with the longest road or {@code null} if no player has the longest road
     */
    public LongestRoadUpdateMessage(String gameSessionName, PlayerDTO playerWithLongestRoad) {
        super(gameSessionName);
        this.playerWithLongestRoad = playerWithLongestRoad;
    }

    /**
     * Returns the player with the longest road or {@code null} if no player has the longest road
     *
     * @return the player with the longest road or {@code null} if no player has the longest road
     */
    public PlayerDTO getPlayerWithLongestRoad() {
        return playerWithLongestRoad;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        LongestRoadUpdateMessage that = (LongestRoadUpdateMessage) obj;
        return Objects.equals(playerWithLongestRoad, that.playerWithLongestRoad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), playerWithLongestRoad);
    }
}
