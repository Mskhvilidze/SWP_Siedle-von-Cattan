package de.uol.swp.common.game.response;

import java.util.Objects;

/**
 * Response to a DiscardCardRequest, indicates whenever a discard was successful.
 */
public class DiscardCardsResponse extends AbstractGameResponse {

    private final boolean successfulDiscard;
    private final String message;

    /**
     * Constructor
     *
     * @param gameSessionName   name of the game session
     * @param successfulDiscard true if the cards were successfully discarded.
     * @param reason            a message to display to the player
     */
    public DiscardCardsResponse(String gameSessionName, boolean successfulDiscard, String reason) {
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
     * @return true if a discard from the player inventory was successfull
     */
    public boolean hasSuccessfullyDiscarded() {
        return successfulDiscard;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        DiscardCardsResponse that = (DiscardCardsResponse) obj;
        return successfulDiscard == that.successfulDiscard;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), successfulDiscard);
    }
}
