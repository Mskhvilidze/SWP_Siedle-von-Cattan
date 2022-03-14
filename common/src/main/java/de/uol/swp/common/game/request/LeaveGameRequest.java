package de.uol.swp.common.game.request;

/**
 * Request used to signal that a user wants to leave the GameSession
 */
public class LeaveGameRequest extends AbstractGameRequest{

    /**
     * Constructor
     *
     * @param gameSessionName of the referred gameSession
     */
    public LeaveGameRequest(String gameSessionName){
        super(gameSessionName);
    }
}
