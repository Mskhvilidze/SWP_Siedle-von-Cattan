package de.uol.swp.common.game.request.trade;

import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.message.trade.TradeOfferAcceptedMessage;

import java.util.Objects;

/**
 * Request sent to the server when the offering player wants to accept a TradeOffer
 * <p>
 * If this request was sent from the offering player in the trade offer then a new {@link TradeOfferAcceptedMessage}
 * will be sent to all players in the lobby indicating a successful trade.
 */
public class AcceptTradeOfferRequest extends AbstractTradeRequest {

    private final String tradeReceiver;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this trade has happened in
     * @param tradeOffer      the trade offer that the offering player wants to accept
     * @param tradeReceiver   the interested player that has been selected by the offering player to receive the resources
     */
    public AcceptTradeOfferRequest(String gameSessionName, TradeOffer tradeOffer, String tradeReceiver) {
        super(gameSessionName, tradeOffer);
        this.tradeReceiver = tradeReceiver;
    }

    /**
     * Returns the interested player that has been selected by the offering player to receive the resources
     *
     * @return the interested player that has been selected by the offering player to receive the resources
     */
    public String getTradeReceiver() {
        return tradeReceiver;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        AcceptTradeOfferRequest request = (AcceptTradeOfferRequest) obj;
        return Objects.equals(tradeReceiver, request.tradeReceiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tradeReceiver);
    }
}