package de.uol.swp.server.game.mapobject;

import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.server.game.Player;

/**
 * Base interface of all board pieces
 */
public interface BuildablePiece {

    /**
     * Returns the player who owns this piece
     *
     * @return the player who owns this piece
     */
    Player getPlayer();

    /**
     * Returns the id of the player who owns this piece
     *
     * @return the id of the player who owns this piece
     */
    int getPlayerId();

    /**
     * Returns the piece type of this piece
     *
     * @return the piece type of this piece
     */
    PieceType getPieceType();
}
