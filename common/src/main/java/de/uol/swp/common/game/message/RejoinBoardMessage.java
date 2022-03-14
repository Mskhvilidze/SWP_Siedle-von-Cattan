package de.uol.swp.common.game.message;

import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.dto.PieceDTO;

import java.util.Map;
import java.util.Objects;

/**
 * Message sent to a user rejoining a game session that contains the placed pieces
 */
public class RejoinBoardMessage extends AbstractGameMessage {

    private final Map<Coord, PieceDTO> placedPieces;

    /**
     * @param gameSessionName the name of the game session the user joined
     * @param placedPieces    a map containing the already placed pieces
     */
    public RejoinBoardMessage(String gameSessionName, Map<Coord, PieceDTO> placedPieces) {
        super(gameSessionName);
        this.placedPieces = placedPieces;
    }

    public Map<Coord, PieceDTO> getPlacedPieces() {
        return placedPieces;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        RejoinBoardMessage message = (RejoinBoardMessage) obj;
        return Objects.equals(placedPieces, message.placedPieces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), placedPieces);
    }
}
