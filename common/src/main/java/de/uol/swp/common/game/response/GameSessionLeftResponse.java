package de.uol.swp.common.game.response;


/**
 * Response sent to client if he could leave the GameSession succesfully
 */
public class GameSessionLeftResponse extends AbstractGameResponse {

    /**
     * Constructor
     *
     * @param gameSessionName the name of the associated GameSession
     */
    public GameSessionLeftResponse (String gameSessionName){
        super(gameSessionName);
    }
}
