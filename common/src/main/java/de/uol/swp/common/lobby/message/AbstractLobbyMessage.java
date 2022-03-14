package de.uol.swp.common.lobby.message;

import de.uol.swp.common.message.AbstractServerMessage;

import java.util.Objects;

/**
 * Base class of all lobby messages. Basic handling of lobby data.
 *
 * @author Marco Grawunder
 * @see de.uol.swp.common.user.User
 * @see de.uol.swp.common.message.AbstractServerMessage
 * @since 2019-10-08
 */
public abstract class AbstractLobbyMessage extends AbstractServerMessage {

    private final String lobbyName;

    /**
     * Constructor
     *
     * @param lobbyName the name of the lobby this message is sent to
     * @since 2019-10-08
     */
    protected AbstractLobbyMessage(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    /**
     * Getter for the name variable
     *
     * @return a String containing the lobby's name
     * @since 2019-10-08
     */
    public String getLobbyName() {
        return lobbyName;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        AbstractLobbyMessage that = (AbstractLobbyMessage) object;
        return Objects.equals(lobbyName, that.lobbyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobbyName);
    }
}
