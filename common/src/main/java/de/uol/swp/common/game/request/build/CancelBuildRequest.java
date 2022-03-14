package de.uol.swp.common.game.request.build;

import de.uol.swp.common.game.message.build.CancelBuildMessage;
import de.uol.swp.common.game.request.AbstractGameRequest;

/**
 * Request sent to the server when the current player wants to cancel building
 *
 * @see CancelBuildMessage
 */
public class CancelBuildRequest extends AbstractGameRequest {
    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this request is sent from
     */
    public CancelBuildRequest(String gameSessionName) {
        super(gameSessionName);
    }
}
