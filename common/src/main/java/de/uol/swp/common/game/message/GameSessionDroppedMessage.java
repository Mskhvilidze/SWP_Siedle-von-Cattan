package de.uol.swp.common.game.message;

/**
 * a message that signals when a gameSession has been dropped
 */
public class GameSessionDroppedMessage extends AbstractGameMessage{

    /**
     * Constructor
     *
     * @param gameSessionName the name of the gameSession
     */
    public GameSessionDroppedMessage(String gameSessionName){
        super(gameSessionName);
    }
}
