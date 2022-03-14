package de.uol.swp.common.lobby.request;

import de.uol.swp.common.message.AbstractRequestMessage;

import java.util.Objects;

/**
 * Base class of all lobby request messages. Basic handling of lobby data.
 *
 * @author Marco Grawunder
 * @see de.uol.swp.common.user.User
 * @see de.uol.swp.common.message.AbstractRequestMessage
 * @since 2019-10-08
 */
public abstract class AbstractLobbyRequest extends AbstractRequestMessage {

    private final String lobbyName;

    /**
     * Constructor
     *
     * @param lobbyName name of the lobby
     * @since 2019-10-08
     */
    protected AbstractLobbyRequest(String lobbyName) {
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
        AbstractLobbyRequest that = (AbstractLobbyRequest) object;
        return Objects.equals(lobbyName, that.lobbyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lobbyName);
    }
}
