package de.uol.swp.common.game.message;

import java.util.Objects;

/**
 * When a player receives this message they are notified that they have to discard half of their cards.
 */
public class DiscardNotificationMessage extends AbstractGameMessage {
    private final int amount;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this message is sent to
     * @param amount          the amount the player needs to discard
     */
    public DiscardNotificationMessage(String gameSessionName, int amount) {
        super(gameSessionName);
        this.amount = amount;
    }

    /**
     * Returns the amount the player needs to discard
     *
     * @return the amount the player needs to discard
     */
    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        DiscardNotificationMessage that = (DiscardNotificationMessage) obj;
        return amount == that.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), amount);
    }
}
