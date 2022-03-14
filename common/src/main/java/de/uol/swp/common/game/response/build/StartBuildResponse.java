package de.uol.swp.common.game.response.build;

import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.response.AbstractGameResponse;

import java.util.Objects;
import java.util.Set;

/**
 * Response sent to the user in a game session who wants to start placing pieces
 *
 * @see de.uol.swp.common.game.request.build.StartBuildRequest
 */
public class StartBuildResponse extends AbstractGameResponse {
    private final Set<Coord> legalNodes;
    private final PieceType pieceType;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this response is sent to
     * @param legalNodes      the positions where the player can place the piece
     * @param pieceType       the piece type the player wants to place
     */
    public StartBuildResponse(String gameSessionName, Set<Coord> legalNodes, PieceType pieceType) {
        super(gameSessionName);
        this.legalNodes = legalNodes;
        this.pieceType = pieceType;
    }

    /**
     * Returns the positions where the player can place the piece
     *
     * @return the positions where the player can place the piece
     */
    public Set<Coord> getLegalNodes() {
        return legalNodes;
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
        StartBuildResponse that = (StartBuildResponse) obj;
        return Objects.equals(legalNodes, that.legalNodes) && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), legalNodes, pieceType);
    }
}