package de.uol.swp.common.game.request.trade;

import de.uol.swp.common.game.TradeOffer;

/**
 * Request sent to the server when a receiving player wants to accept a TradeOffer on their end
 * <p>
 * Once a receiving player has shown interest in a trade offer the offering player can accept the trade offer
 *
 * @see AcceptTradeOfferRequest
 */
public class InterestTradeOfferRequest extends AbstractTradeRequest {
    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this trade has happened in
     * @param tradeOffer      the trade offer that a receiving player wants to accept
     */
    public InterestTradeOfferRequest(String gameSessionName, TradeOffer tradeOffer) {
        super(gameSessionName, tradeOffer);
    }
}
