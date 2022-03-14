package de.uol.swp.common.game.request;

/**
 * Request from a player who wants to rejoin a game session
 */
public class RejoinGameSessionRequest extends AbstractGameRequest{

    /**
     * Constructor
     *
     * @param name of the referred game session
     */
    public RejoinGameSessionRequest(String name){
        super(name);
    }
}
