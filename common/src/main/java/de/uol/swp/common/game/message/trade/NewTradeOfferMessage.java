package de.uol.swp.common.game.message.trade;

import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.lobby.message.LobbyNotificationMessage;

/**
 * Message sent to all receiving players in a game session when the offering player has started a TradeOffer
 */
public class NewTradeOfferMessage extends AbstractTradeMessage implements LobbyNotificationMessage {

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this trade has happened in
     * @param tradeOffer      the trade offer that has been started
     */
    public NewTradeOfferMessage(String gameSessionName, TradeOffer tradeOffer) {
        super(gameSessionName, tradeOffer);
    }

    @Override
    public String getLobbyName() {
        return getGameSessionName();
    }
}
