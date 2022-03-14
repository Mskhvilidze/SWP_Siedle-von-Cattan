package de.uol.swp.common.game.debug;

import de.uol.swp.common.game.message.AbstractGameMessage;

import java.util.Objects;

/**
 * Message sent to all players in a game session that contains the name of the current state
 */
public class StateMessage extends AbstractGameMessage {
    private final String state;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this message is sent to
     * @param state           the current state of the game session
     */
    public StateMessage(String gameSessionName, String state) {
        super(gameSessionName);
        this.state = state;
    }

    public String getState() {
        return state;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        StateMessage that = (StateMessage) obj;
        return Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), state);
    }
}
