package de.uol.swp.common.game.message;

import java.util.Objects;

/**
 * Message sent to all players in a game session containing the total amount of remaining development cards in the bank
 */
public class DevCardRemainingMessage extends AbstractGameMessage {

    private final int amountOfDevCards;

    /**
     * Constructor
     *
     * @param gameSessionName  the name of the game session this message is sent to
     * @param amountOfDevCards the amount of dev cards that the bank still has
     */
    public DevCardRemainingMessage(String gameSessionName, int amountOfDevCards) {
        super(gameSessionName);
        this.amountOfDevCards = amountOfDevCards;
    }


    /**
     * Returns the amount of dev cards that the bank still has
     *
     * @return the amount of dev cardsthe amount of dev cards that the bank still has
     */
    public int getAmountOfDevCards() {
        return amountOfDevCards;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        DevCardRemainingMessage that = (DevCardRemainingMessage) obj;
        return amountOfDevCards == that.amountOfDevCards;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), amountOfDevCards);
    }
}
