package de.uol.swp.common.game.request.trade;

import de.uol.swp.common.game.TradeOffer;

/**
 * Request sent to the server when a user wants to start a trade in the gameSession
 *
 * @see AbstractTradeRequest
 * @since 2021-05-24
 */
public class StartBankTradeRequest extends AbstractTradeRequest {

    /**
     * Constructor
     *
     * @param gameSessionName name of the gameSession (lobby)
     * @param tradeOffer      offered and requested amount of resources for the trade
     * @since 2021-04-27
     */
    public StartBankTradeRequest(String gameSessionName, TradeOffer tradeOffer) {
        super(gameSessionName, tradeOffer);
    }
}