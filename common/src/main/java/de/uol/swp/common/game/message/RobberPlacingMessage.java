package de.uol.swp.common.game.message;

/**
 * The player indicated can place the robber
 */
public class RobberPlacingMessage extends AbstractGameMessage {

    /**
     * Constructor
     *
     * @param gameSessionName the name of the session
     */
    public RobberPlacingMessage(String gameSessionName) {
        super(gameSessionName);
    }
}
