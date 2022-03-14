package de.uol.swp.common.game.response;

/**
 * Response sent to a player to signal that they can close their PlayerPickerView
 */
public class PlayerPickerResponse extends AbstractGameResponse {
    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this response is sent to
     */
    public PlayerPickerResponse(String gameSessionName) {
        super(gameSessionName);
    }
}
