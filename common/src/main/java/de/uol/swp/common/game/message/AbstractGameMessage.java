package de.uol.swp.common.game.message;

import de.uol.swp.common.message.AbstractServerMessage;

import java.util.Objects;

/**
 * Base Game Message Class
 */
public abstract class AbstractGameMessage extends AbstractServerMessage {

    private final String gameSessionName;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this message is sent to
     */
    protected AbstractGameMessage(String gameSessionName) {
        this.gameSessionName = gameSessionName;
    }

    /**
     * Returns the name of the game session this message is sent to
     *
     * @return the name of the game session this message is sent to
     */
    public String getGameSessionName() {
        return gameSessionName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        AbstractGameMessage that = (AbstractGameMessage) obj;
        return Objects.equals(gameSessionName, that.gameSessionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gameSessionName);
    }
}