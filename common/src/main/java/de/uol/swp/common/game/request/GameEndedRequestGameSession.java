package de.uol.swp.common.game.request;

/**
 * Request used to signal that the game session should be force closed
 */
public class GameEndedRequestGameSession extends AbstractGameRequest{

    /**
     * Constructor
     *
     * @param gameSessionName of the referred gameSession
     */
    public GameEndedRequestGameSession(String gameSessionName){
        super(gameSessionName);
    }
}
