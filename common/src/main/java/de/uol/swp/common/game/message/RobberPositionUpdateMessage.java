package de.uol.swp.common.game.message;

import de.uol.swp.common.game.board.Coord;

import java.util.Objects;

/**
 * Message informing the clients about an updated RobberPosition
 */
public class RobberPositionUpdateMessage extends AbstractGameMessage {
    private final Coord updatedPosition;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this message is sent to
     * @param updatedPosition the new position of the robber
     */
    public RobberPositionUpdateMessage(String gameSessionName, Coord updatedPosition) {
        super(gameSessionName);
        this.updatedPosition = updatedPosition;
    }

    /**
     * Returns the new position of the robber
     *
     * @return the new position of the robber
     */
    public Coord getUpdatedPosition() {
        return updatedPosition;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        RobberPositionUpdateMessage that = (RobberPositionUpdateMessage) obj;
        return Objects.equals(updatedPosition, that.updatedPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), updatedPosition);
    }
}
