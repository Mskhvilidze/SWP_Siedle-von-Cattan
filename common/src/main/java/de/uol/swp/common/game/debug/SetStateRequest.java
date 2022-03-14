package de.uol.swp.common.game.debug;

import java.util.Objects;

/**
 * This is a debug request that allows players to change the state of a game session.
 * <p>
 * There are no checks on the server that validate the requesting user or game session
 */
public class SetStateRequest extends AbstractDebugRequest {
    private final String state;

    /**
     * Constructor
     *
     * @param sessionName the name of the game session that should be changed
     * @param state       a String representing the new state for the game session (eg "trade")
     */
    public SetStateRequest(String sessionName, String state) {
        super(sessionName);
        this.state = state;
    }

    /**
     * Returns a String representing the new state for the game session
     *
     * @return a String representing the new state for the game session
     */
    public String getState() {
        return state;
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
        SetStateRequest that = (SetStateRequest) object;
        return Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), state);
    }
}
