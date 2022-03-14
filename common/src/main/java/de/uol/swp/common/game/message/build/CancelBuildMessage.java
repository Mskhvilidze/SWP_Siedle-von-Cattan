package de.uol.swp.common.game.message.build;

import de.uol.swp.common.game.message.AbstractGameMessage;

/**
 * Message sent to the user in a game session who wants to cancel their current build
 *
 * @see de.uol.swp.common.game.request.build.CancelBuildRequest
 */
public class CancelBuildMessage extends AbstractGameMessage {
    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this response is sent to
     */
    public CancelBuildMessage(String gameSessionName) {
        super(gameSessionName);
    }
}
