package de.uol.swp.common.game.message;

/**
 * Message Indicating that the setup Phase has begun.
 */
public class SetupPhaseStartMessage extends AbstractGameMessage {
    /**
     * Constructor
     *
     * @param gameSessionName
     */
    public SetupPhaseStartMessage(String gameSessionName) {
        super(gameSessionName);
    }
}
