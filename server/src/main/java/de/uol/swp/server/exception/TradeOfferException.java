package de.uol.swp.server.exception;

import de.uol.swp.common.game.dto.PlayerDTO;

/**
 * Thrown to indicate that a player either doesn't have a trade offer but should have or does have a trade offer but shouldn't have
 */
public class TradeOfferException extends TradeException {
    private final PlayerDTO player;
    private final boolean available;

    /**
     * Constructor
     *
     * @param player    the player who should/shouldn't have a trade offer
     * @param available whether the player has the trade offer or not
     */
    public TradeOfferException(PlayerDTO player, boolean available) {
        this.player = player;
        this.available = available;
    }

    /**
     * Returns the player who should/shouldn't have a trade offer
     *
     * @return the player who should/shouldn't have a trade offer
     */
    public PlayerDTO getPlayer() {
        return player;
    }

    /**
     * Returns whether the player has the trade offer or not
     *
     * @return {@code true} if the player has the trade offer, otherwise {@code false}
     */
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String toString() {
        return "TradeOfferException{" +
                "player=" + player +
                ", available=" + available +
                '}';
    }
}
