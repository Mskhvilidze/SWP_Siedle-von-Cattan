package de.uol.swp.server.game.mapobject;

import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.server.game.Player;

/**
 * Class representing the road board piece
 * <p>
 * It's the only piece that can be placed on an edge
 */
public class RoadPiece extends AbstractBuildablePiece implements EdgePiece {

    /**
     * Constructor
     *
     * @param player the player who owns the piece
     */
    public RoadPiece(Player player) {
        super(player);
    }

    @Override
    public PieceType getPieceType() {
        return PieceType.ROAD;
    }
}
