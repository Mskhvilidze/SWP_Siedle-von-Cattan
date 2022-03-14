package de.uol.swp.common.game.request.build;

import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.request.AbstractGameRequest;

import java.util.Objects;

/**
 * Request sent to the server when the current player wants to start building and needs their legal building locations
 *
 * @see de.uol.swp.common.game.response.build.StartBuildResponse
 */
public class StartBuildRequest extends AbstractGameRequest {

    private final PieceType pieceType;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this request is sent from
     * @param pieceType       the piece type the player wants to place
     */
    public StartBuildRequest(String gameSessionName, PieceType pieceType) {
        super(gameSessionName);
        this.pieceType = pieceType;
    }

    /**
     * Returns the piece type the player wants to place
     *
     * @return the piece type the player wants to place
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        StartBuildRequest that = (StartBuildRequest) obj;
        return pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pieceType);
    }
}
