package de.uol.swp.common.game.request.trade;

import de.uol.swp.common.game.TradeOffer;

/**
 * Request sent to the server when the offering player wants to cancel a TradeOffer
 */
public class CancelTradeOfferRequest extends AbstractTradeRequest {
    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this trade has happened in
     * @param tradeOffer      the trade offer that the offering player wants to cancel
     */
    public CancelTradeOfferRequest(String gameSessionName, TradeOffer tradeOffer) {
        super(gameSessionName, tradeOffer);
    }
}
