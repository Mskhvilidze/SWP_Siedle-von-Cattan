package de.uol.swp.common.lobby.request;

import de.uol.swp.common.game.dto.UserDTO;

import java.util.Objects;

/**
 * Request sent to the server when a lobby owner wants to kick a user
 */
public class LobbyKickRequest extends AbstractLobbyRequest {

    private final UserDTO kickUser;

    /**
     * Constructor
     *
     * @param lobbyName the name of the lobby the user should be kicked from
     * @param kickUser  the user who should be kicked
     */
    public LobbyKickRequest(String lobbyName, UserDTO kickUser) {
        super(lobbyName);
        this.kickUser = kickUser;
    }

    /**
     * Getter for the user who should be kicked
     *
     * @return the user who should be kicked
     */
    public UserDTO getKickUser() {
        return kickUser;
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
        LobbyKickRequest that = (LobbyKickRequest) object;
        return Objects.equals(kickUser, that.kickUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), kickUser);
    }
}
