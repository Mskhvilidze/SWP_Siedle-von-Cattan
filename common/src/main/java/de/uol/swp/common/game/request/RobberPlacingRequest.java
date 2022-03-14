package de.uol.swp.common.game.request;

import de.uol.swp.common.game.board.Coord;

import java.util.Objects;

/**
 * Request to place the Robber at a position
 */
public class RobberPlacingRequest extends AbstractGameRequest {

    private final Coord robberPosition;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this request is sent from
     * @param robberPosition  the coordinates of the robber
     */
    public RobberPlacingRequest(String gameSessionName, Coord robberPosition) {
        super(gameSessionName);
        this.robberPosition = robberPosition;
    }

    /**
     * Getter For coordinates of the robber
     *
     * @return robberPosition coordinates
     */
    public Coord getRobberPosition() {
        return robberPosition;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        RobberPlacingRequest that = (RobberPlacingRequest) obj;
        return Objects.equals(robberPosition, that.robberPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), robberPosition);
    }
}
