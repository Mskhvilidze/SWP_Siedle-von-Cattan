package de.uol.swp.common.lobby.request;

import java.util.Objects;

/**
 * Request sent to the server when a user in a lobby is ready
 *
 * @see AbstractLobbyRequest
 * @see de.uol.swp.common.user.User
 */

public class SetUserReadyRequest extends AbstractLobbyRequest {
    private final boolean ready;

    /**
     * Constructor
     *
     * @param lobbyName name of the lobby
     */
    public SetUserReadyRequest(String lobbyName, boolean ready) {
        super(lobbyName);
        this.ready = ready;
    }

    /**
     * Returns if the user is ready
     *
     * @return {@code true} if the user is ready, otherwise {@code false}
     */
    public boolean isUserReady() {
        return ready;
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
        SetUserReadyRequest that = (SetUserReadyRequest) object;
        return ready == that.ready;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ready);
    }
}
