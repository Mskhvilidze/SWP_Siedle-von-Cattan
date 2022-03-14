package de.uol.swp.common.game.response.trade;

import de.uol.swp.common.game.response.AbstractGameResponse;

public class NotEnoughResourcesResponse extends AbstractGameResponse {
    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this response is sent to
     */
    public NotEnoughResourcesResponse(String gameSessionName) {
        super(gameSessionName);
    }
}
