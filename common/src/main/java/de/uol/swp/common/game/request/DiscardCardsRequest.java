package de.uol.swp.common.game.request;

import de.uol.swp.common.game.board.ResourceEnumMap;

import java.util.Objects;

/**
 * A player who sends this message asks the server to discard the amount of resources from their inventory
 */

public class DiscardCardsRequest extends AbstractGameRequest {
    private final ResourceEnumMap toDiscard;

    /**
     * Constructor
     *
     * @param gameSessionName game session name
     * @param toDiscard       Resources that will be discarded
     */
    public DiscardCardsRequest(String gameSessionName, ResourceEnumMap toDiscard) {
        super(gameSessionName);
        this.toDiscard = toDiscard;
    }


    /**
     * Getter for ResourcesToDiscard
     *
     * @return resources the player wants to discard
     */
    public ResourceEnumMap getToDiscard() {
        return toDiscard;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        DiscardCardsRequest that = (DiscardCardsRequest) object;
        return Objects.equals(toDiscard, that.toDiscard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), toDiscard);
    }
}
