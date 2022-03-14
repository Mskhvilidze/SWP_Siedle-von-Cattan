package de.uol.swp.common.game.request;

/**
 * Request sent to the server when the player has finished loading the game
 */
public class PlayerFinishedLoadingRequest extends AbstractGameRequest {
    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this request is sent from
     */
    public PlayerFinishedLoadingRequest(String gameSessionName) {
        super(gameSessionName);
    }
}
