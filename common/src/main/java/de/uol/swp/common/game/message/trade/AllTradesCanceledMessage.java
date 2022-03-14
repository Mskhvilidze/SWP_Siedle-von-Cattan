package de.uol.swp.common.game.message.trade;

import de.uol.swp.common.game.message.AbstractGameMessage;

/**
 * Message sent to all players in a game session when the end of a turn canceled any open trade offers
 */
public class AllTradesCanceledMessage extends AbstractGameMessage {

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session
     */
    public AllTradesCanceledMessage(String gameSessionName) {
        super(gameSessionName);
    }
}
