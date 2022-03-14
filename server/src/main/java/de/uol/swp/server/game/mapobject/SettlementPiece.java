package de.uol.swp.server.game.mapobject;

import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.server.game.Player;

/**
 * Class representing the settlement board piece
 * <p>
 * This piece and the {@link CityPiece} piece are the only pieces that can be placed on a corner.
 */
public class SettlementPiece extends AbstractBuildablePiece implements CornerPiece {

    /**
     * Constructor
     *
     * @param player the player who owns the piece
     */
    public SettlementPiece(Player player) {
        super(player);
    }

    @Override
    public PieceType getPieceType() {
        return PieceType.SETTLEMENT;
    }
}
