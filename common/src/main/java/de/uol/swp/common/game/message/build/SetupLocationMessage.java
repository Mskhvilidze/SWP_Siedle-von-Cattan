package de.uol.swp.common.game.message.build;

import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.message.AbstractGameMessage;

import java.util.Objects;
import java.util.Set;

/**
 * Message sent to the user in a game session who is currently setting up their pieces
 * <p>
 * Contains the legal coordinates for a piece type
 */
public class SetupLocationMessage extends AbstractGameMessage {
    private final Set<Coord> legalNodes;
    private final PieceType pieceType;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this message is sent to
     * @param legalNodes      the legal coordinates for the given piece type
     * @param pieceType       the piece type the player wants to place
     */
    public SetupLocationMessage(String gameSessionName, Set<Coord> legalNodes, PieceType pieceType) {
        super(gameSessionName);
        this.legalNodes = legalNodes;
        this.pieceType = pieceType;
    }

    /**
     * Returns the legal coordinates for the given piece type
     *
     * @return the legal coordinates for the given piece type
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
        SetupLocationMessage that = (SetupLocationMessage) obj;
        return Objects.equals(legalNodes, that.legalNodes) && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), legalNodes, pieceType);
    }
}
