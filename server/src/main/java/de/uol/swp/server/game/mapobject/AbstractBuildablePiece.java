package de.uol.swp.server.game.mapobject;

import de.uol.swp.server.game.Player;

/**
 * Immutable base class of all board pieces. Stores basic player info.
 *
 * @implNote subclasses should never override the equals method. All comparisons should be done in the Board class via the position of the pieces
 */
public abstract class AbstractBuildablePiece implements BuildablePiece {
    private final Player player;

    /**
     * Constructor
     *
     * @param player the player who owns the piece
     */
    protected AbstractBuildablePiece(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public int getPlayerId() {
        return player.getPlayerId();
    }
}
