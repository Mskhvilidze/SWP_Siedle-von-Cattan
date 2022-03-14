package de.uol.swp.common.game.request;

/**
 * Requests the Server to Roll the Dice
 */
public class RollDiceRequest extends AbstractGameRequest {

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this message is sent to
     */
    public RollDiceRequest(String gameSessionName) {
        super(gameSessionName);
    }

}
