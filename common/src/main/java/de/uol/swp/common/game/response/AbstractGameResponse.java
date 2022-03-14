package de.uol.swp.common.game.response;

import de.uol.swp.common.message.AbstractResponseMessage;

import java.util.Objects;

/**
 * Base Game Response Class
 */
public abstract class AbstractGameResponse extends AbstractResponseMessage {
    private final String gameSessionName;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this response is sent to
     */
    protected AbstractGameResponse(String gameSessionName) {
        this.gameSessionName = gameSessionName;
    }

    /**
     * Returns the name of the game session this response is sent to
     *
     * @return the name of the game session this response is sent to
     */
    public String getGameSessionName() {
        return gameSessionName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        AbstractGameResponse that = (AbstractGameResponse) obj;
        return Objects.equals(gameSessionName, that.gameSessionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gameSessionName);
    }
}
