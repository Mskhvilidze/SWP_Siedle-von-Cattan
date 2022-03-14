package de.uol.swp.common.game.message.inventory;

import de.uol.swp.common.game.board.DevCardEnumMap;
import de.uol.swp.common.game.message.AbstractGameMessage;

import java.util.Objects;

/**
 * Message sent to a specific player to display the exact amount of development cards that player has
 */
public class DevCardDetailedCountMessage extends AbstractGameMessage {

    private final DevCardEnumMap devCards;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session
     * @param devCards        the map that contains the cards
     */
    public DevCardDetailedCountMessage(String gameSessionName, DevCardEnumMap devCards) {
        super(gameSessionName);
        this.devCards = devCards;
    }

    /**
     * Getter for devCardEnumMap
     *
     * @return devCardEnumMap
     */
    public DevCardEnumMap getDevCards() {
        return devCards;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        DevCardDetailedCountMessage that = (DevCardDetailedCountMessage) obj;
        return Objects.equals(devCards, that.devCards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), devCards);
    }
}