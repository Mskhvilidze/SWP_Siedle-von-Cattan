package de.uol.swp.server.game.mapobject;

import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.server.game.Player;

/**
 * Class representing the city board piece
 * <p>
 * This piece and the {@link SettlementPiece} piece are the only pieces that can be placed on a corner.
 */
public class CityPiece extends AbstractBuildablePiece implements CornerPiece {

    /**
     * Constructor
     *
     * @param player the player who owns the piece
     */
    public CityPiece(Player player) {
        super(player);
    }

    @Override
    public int getPlayerId() {
        return this.getPlayer().getPlayerId();
    }

    @Override
    public PieceType getPieceType() {
        return PieceType.CITY;
    }
}
