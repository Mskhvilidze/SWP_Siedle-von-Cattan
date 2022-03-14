package de.uol.swp.common.game.message.trade;

import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.dto.PlayerDTO;

import java.util.Objects;

/**
 * Message sent to all users in a game session when the offering player has accepted a TradeOffer
 */
public class TradeOfferAcceptedMessage extends AbstractTradeMessage {
    private final PlayerDTO tradeReceiver;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this trade has happened in
     * @param tradeOffer      the trade offer that was accepted by the offering player
     * @param tradeReceiver   the interested player that has been selected by the offering player to receive the resources
     */
    public TradeOfferAcceptedMessage(String gameSessionName, TradeOffer tradeOffer, PlayerDTO tradeReceiver) {
        super(gameSessionName, tradeOffer);
        this.tradeReceiver = tradeReceiver;
    }

    /**
     * Returns the player that has been selected by the offering player to receive the resources
     *
     * @return the player that has been selected by the offering player to receive the resources
     */
    public PlayerDTO getTradeReceiver() {
        return tradeReceiver;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        TradeOfferAcceptedMessage message = (TradeOfferAcceptedMessage) obj;
        return Objects.equals(tradeReceiver, message.tradeReceiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tradeReceiver);
    }
}
