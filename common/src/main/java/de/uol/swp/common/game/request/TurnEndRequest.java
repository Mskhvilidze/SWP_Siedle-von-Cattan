package de.uol.swp.common.game.request;

/**
 * Request used to signal that a player wants to end his turn
 */
public class TurnEndRequest extends AbstractGameRequest {

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this request is sent from
     */
    public TurnEndRequest(String gameSessionName) {
        super(gameSessionName);
    }
}
