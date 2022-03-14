package de.uol.swp.common.game.message.trade;

import de.uol.swp.common.game.TradeOffer;

/**
 * Message sent to all users in a game session when the offering player has canceled or all receiving player have declined a TradeOffer
 */
public class TradeOfferCanceledMessage extends AbstractTradeMessage {
    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this trade has happened in
     * @param tradeOffer      the trade offer that was canceled
     */
    public TradeOfferCanceledMessage(String gameSessionName, TradeOffer tradeOffer) {
        super(gameSessionName, tradeOffer);
    }
}
