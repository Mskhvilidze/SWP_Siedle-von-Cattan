package de.uol.swp.common.game.request.trade;

import de.uol.swp.common.game.TradeOffer;

/**
 * Request sent to the server when a receiving player wants to decline a TradeOffer
 */
public class DeclineTradeOfferRequest extends AbstractTradeRequest {
    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this trade has happened in
     * @param tradeOffer      the trade offer that a receiving player wants to decline
     */
    public DeclineTradeOfferRequest(String gameSessionName, TradeOffer tradeOffer) {
        super(gameSessionName, tradeOffer);
    }
}
