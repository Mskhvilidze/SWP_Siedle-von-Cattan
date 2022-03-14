package de.uol.swp.common.lobby.response;

import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.lobby.request.JoinLobbyRequest;
import de.uol.swp.common.message.AbstractResponseMessage;

import java.util.Objects;

/**
 * A response, that the user joined a lobby successful
 * <p>
 * This response is sent to the Client whose LobbyJoinUserRequest was successful
 * and contains the name of the lobby.
 *
 * @see JoinLobbyRequest
 */
public class LobbyJoinSuccessfulResponse extends AbstractResponseMessage {

    private final LobbyDTO lobby;

    /**
     * Constructor
     *
     * @param lobby the lobby that the user joined
     */
    public LobbyJoinSuccessfulResponse(LobbyDTO lobby) {
        this.lobby = lobby;
    }

    /**
     * Getter for the lobby that the user joined
     *
     * @return the lobby that the user joined
     */
    public LobbyDTO getLobby() {
        return lobby;
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
        LobbyJoinSuccessfulResponse that = (LobbyJoinSuccessfulResponse) object;
        return Objects.equals(lobby, that.lobby);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobby);
    }
}
