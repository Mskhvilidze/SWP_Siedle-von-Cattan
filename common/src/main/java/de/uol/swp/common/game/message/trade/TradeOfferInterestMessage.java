package de.uol.swp.common.game.message.trade;

import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.request.trade.AcceptTradeOfferRequest;

import java.util.Objects;

/**
 * Message sent to the offering player in a game session when one of the receiving players has accepted the trade offer on their end
 * <p>
 * The offering player can only accept trades with an {@link AcceptTradeOfferRequest} that a receiving player is interested in
 *
 * @see de.uol.swp.common.game.request.trade.InterestTradeOfferRequest
 */
public class TradeOfferInterestMessage extends AbstractTradeMessage {
    private final PlayerDTO interestedPlayer;

    /**
     * Constructor
     *
     * @param lobbyName        the name of the game session this trade has happened in
     * @param tradeOffer       the trade offer that was accepted by the receiving player
     * @param interestedPlayer the player that has accepted the trade offer on their end
     */
    public TradeOfferInterestMessage(String lobbyName, TradeOffer tradeOffer, PlayerDTO interestedPlayer) {
        super(lobbyName, tradeOffer);
        this.interestedPlayer = interestedPlayer;
    }

    /**
     * Returns the player that has accepted the trade offer on their end
     *
     * @return the player that has accepted the trade offer on their end
     */
    public PlayerDTO getInterestedPlayer() {
        return interestedPlayer;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        TradeOfferInterestMessage message = (TradeOfferInterestMessage) obj;
        return Objects.equals(interestedPlayer, message.interestedPlayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), interestedPlayer);
    }
}
