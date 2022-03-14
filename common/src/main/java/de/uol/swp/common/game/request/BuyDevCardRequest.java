package de.uol.swp.common.game.request;

/**
 * Request sent to the server when a player wants to buy a development card
 */
public class BuyDevCardRequest extends AbstractGameRequest {

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this request is sent from
     */
    public BuyDevCardRequest(String gameSessionName) {
        super(gameSessionName);
    }
}
