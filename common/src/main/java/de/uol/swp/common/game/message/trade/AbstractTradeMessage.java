package de.uol.swp.common.game.message.trade;

import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.message.AbstractGameMessage;

import java.util.Objects;

/**
 * Base class of all server trade messages. Stores the session name and the trade offer
 */
public abstract class AbstractTradeMessage extends AbstractGameMessage {
    private final TradeOffer tradeOffer;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this trade has happened in
     * @param tradeOffer      the trade offer associated with the trade
     */
    protected AbstractTradeMessage(String gameSessionName, TradeOffer tradeOffer) {
        super(gameSessionName);
        this.tradeOffer = tradeOffer;
    }

    /**
     * Returns the trade offer associated with the trade
     *
     * @return the trade offer associated with the trade
     */
    public TradeOffer getTradeOffer() {
        return tradeOffer;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        AbstractTradeMessage that = (AbstractTradeMessage) obj;
        return Objects.equals(tradeOffer, that.tradeOffer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tradeOffer);
    }
}
