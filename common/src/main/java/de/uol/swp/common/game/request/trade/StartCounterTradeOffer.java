package de.uol.swp.common.game.request.trade;

import de.uol.swp.common.game.TradeOffer;

import java.util.Objects;

/**
 * Request sent to the server when a receiving player wants to edit a TradeOffer and send a new one to the offering player
 */
public class StartCounterTradeOffer extends AbstractTradeRequest {

    private final TradeOffer newTradeOffer;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this trade has happened in
     * @param oldTradeOffer   the original trade offer
     * @param newTradeOffer   the edited trade offer that the receiving player wants to send to the offering player
     */
    public StartCounterTradeOffer(String gameSessionName, TradeOffer oldTradeOffer, TradeOffer newTradeOffer) {
        super(gameSessionName, oldTradeOffer);
        this.newTradeOffer = newTradeOffer;
    }

    /**
     * Returns the original trade offer
     *
     * @return the original trade offer
     */
    @Override
    public final TradeOffer getTradeOffer() {
        return super.getTradeOffer();
    }

    /**
     * Returns the edited trade offer that the receiving player wants to start
     *
     * @return the edited trade offer that the receiving player wants to start
     */
    public TradeOffer getNewTradeOffer() {
        return newTradeOffer;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        StartCounterTradeOffer that = (StartCounterTradeOffer) obj;
        return Objects.equals(newTradeOffer, that.newTradeOffer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), newTradeOffer);
    }
}
