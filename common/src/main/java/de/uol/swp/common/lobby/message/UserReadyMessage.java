package de.uol.swp.common.lobby.message;

import de.uol.swp.common.game.dto.UserDTO;

import java.util.Objects;

/**
 * Message sent to a lobby when a user in that lobby is ready
 */
public class UserReadyMessage extends AbstractLobbyMessage {

    private final UserDTO user;
    private final boolean ready;

    /**
     * Constructor
     *
     * @param lobbyName name of the lobby
     * @param user      user who is or isn't ready
     */
    public UserReadyMessage(String lobbyName, UserDTO user, boolean ready) {
        super(lobbyName);
        this.user = user;
        this.ready = ready;
    }

    /**
     * Returns the user who is or isn't ready
     *
     * @return the user who is or isn't ready
     */
    public UserDTO getUser() {
        return user;
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
        UserReadyMessage that = (UserReadyMessage) object;
        return ready == that.ready;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ready);
    }
}
