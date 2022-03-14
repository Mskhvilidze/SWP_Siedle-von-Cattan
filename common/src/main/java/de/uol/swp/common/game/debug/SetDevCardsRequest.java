package de.uol.swp.common.game.debug;

import de.uol.swp.common.game.board.DevCardEnumMap;

import java.util.Objects;

/**
 * DEBUG message used to set the dev cards of a player
 */
public class SetDevCardsRequest extends AbstractDebugRequest {
    private final DevCardEnumMap devCards;
    private final String playerName;

    /**
     * @param sessionName the game session name
     * @param devCards    the dev cards
     * @param playerName  the player who will get the resources
     */
    public SetDevCardsRequest(String sessionName, DevCardEnumMap devCards, String playerName) {
        super(sessionName);
        this.devCards = devCards;
        this.playerName = playerName;
    }

    /**
     * Returns the dev cards
     *
     * @return the dev cards
     */
    public DevCardEnumMap getDevCards() {
        return devCards;
    }

    /**
     * Returns the player who will get the resources
     *
     * @return the player who will get the resources
     */
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        SetDevCardsRequest that = (SetDevCardsRequest) obj;
        return Objects.equals(devCards, that.devCards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), devCards);
    }
}
