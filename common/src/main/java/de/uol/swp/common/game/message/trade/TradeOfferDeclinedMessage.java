package de.uol.swp.common.game.message.trade;

import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.dto.PlayerDTO;

import java.util.Objects;

/**
 * Message sent to the offering player in a game session when one of the receiving players has declined the trade offer
 */
public class TradeOfferDeclinedMessage extends AbstractTradeMessage {
    private final PlayerDTO declined;

    /**
     * @param gameSessionName the name of the game session this trade has happened in
     * @param tradeOffer      the trade offer that was declined by a receiving player
     * @param declined        the receiving player that has declined the trade offer
     */
    public TradeOfferDeclinedMessage(String gameSessionName, TradeOffer tradeOffer, PlayerDTO declined) {
        super(gameSessionName, tradeOffer);
        this.declined = declined;
    }

    /**
     * Returns the receiving player that has declined the trade offer
     *
     * @return the receiving player that has declined the trade offer
     */
    public PlayerDTO getDeclined() {
        return declined;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        TradeOfferDeclinedMessage message = (TradeOfferDeclinedMessage) obj;
        return Objects.equals(declined, message.declined);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), declined);
    }
}
