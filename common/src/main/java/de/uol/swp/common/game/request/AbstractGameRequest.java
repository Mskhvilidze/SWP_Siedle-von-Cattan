package de.uol.swp.common.game.request;

import de.uol.swp.common.message.AbstractRequestMessage;

import java.util.Objects;

/**
 * Base Game Request Class
 */
public abstract class AbstractGameRequest extends AbstractRequestMessage {

    private final String gameSessionName;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this request is sent from
     */
    public AbstractGameRequest(String gameSessionName) {
        this.gameSessionName = gameSessionName;
    }


    /**
     * Returns the name of the game session this request is sent from
     *
     * @return the name of the game session this request is sent from
     */
    public String getGameSessionName() {
        return gameSessionName;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        AbstractGameRequest that = (AbstractGameRequest) object;
        return Objects.equals(gameSessionName, that.gameSessionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gameSessionName);
    }
}