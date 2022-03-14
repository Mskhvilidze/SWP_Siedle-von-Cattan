package de.uol.swp.common.game.message;

import java.util.Objects;

/**
 * Response to a all clients in a lobby, to close their DiscardPresenter if the turn has ended
 */
public class DiscardCardsMessage extends AbstractGameMessage {

    private final boolean successfulDiscard;
    private final String message;

    /**
     * Constructor
     *
     * @param gameSessionName   the name of the gameSession
     * @param successfulDiscard true if the cards were successfully discarded.
     * @param reason            a message to display to the player
     */
    public DiscardCardsMessage(String gameSessionName, boolean successfulDiscard, String reason) {
        super(gameSessionName);
        this.successfulDiscard = successfulDiscard;
        this.message = reason;
    }

    /**
     * Getter for the Reason
     *
     * @return gets the message of the Response
     */

    public String getMessage() {
        return message;
    }


    /**
     * Getter for the boolean
     *
     * @return true if a discard from the player inventory was successful
     */
    public boolean hasSuccessfullyDiscarded() {
        return successfulDiscard;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        DiscardCardsMessage that = (DiscardCardsMessage) obj;
        return successfulDiscard == that.successfulDiscard && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), successfulDiscard, message);
    }
}
