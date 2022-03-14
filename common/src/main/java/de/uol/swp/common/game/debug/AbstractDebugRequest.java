package de.uol.swp.common.game.debug;

import de.uol.swp.common.message.AbstractRequestMessage;

import java.util.Objects;

/**
 * Base class of all debug requests. Currently only used for game sessions
 */
public abstract class AbstractDebugRequest extends AbstractRequestMessage {
    private final String sessionName;

    /**
     * Constructor
     *
     * @param sessionName the name of the game session that should be changed
     */
    protected AbstractDebugRequest(String sessionName) {
        this.sessionName = sessionName;
    }

    /**
     * Returns the name of the game session that should be changed
     *
     * @return the name of the game session that should be changed
     */
    public String getSessionName() {
        return sessionName;
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
        AbstractDebugRequest that = (AbstractDebugRequest) object;
        return Objects.equals(sessionName, that.sessionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sessionName);
    }
}
