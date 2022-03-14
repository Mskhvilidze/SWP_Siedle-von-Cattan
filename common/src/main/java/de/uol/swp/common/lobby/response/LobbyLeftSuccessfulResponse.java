package de.uol.swp.common.lobby.response;

import de.uol.swp.common.lobby.request.LeaveLobbyRequest;
import de.uol.swp.common.message.AbstractResponseMessage;

import java.util.Objects;

/**
 * A message, that the user left a lobby successful
 * <p>
 * This response is sent to the Client whose LobbyLeaveUserRequest was successful
 * and contains the name of the lobby
 *
 * @see LeaveLobbyRequest
 */
public class LobbyLeftSuccessfulResponse extends AbstractResponseMessage {

    private final String lobbyName;

    /**
     * Default constructor
     *
     * @param lobbyName the name of the lobby that was left
     */
    public LobbyLeftSuccessfulResponse(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    /**
     * Returns the name of the lobby that was left
     *
     * @return the name of the lobby that was left
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
        if (!super.equals(object)) {
            return false;
        }
        LobbyLeftSuccessfulResponse that = (LobbyLeftSuccessfulResponse) object;
        return Objects.equals(lobbyName, that.lobbyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobbyName);
    }
}
