package de.uol.swp.common.lobby.message;

import de.uol.swp.common.game.dto.UserDTO;

import java.util.Objects;

/**
 * Message sent to a lobby when a user successfully leaves that lobby
 *
 * @author Marco Grawunder
 * @see de.uol.swp.common.lobby.message.AbstractLobbyMessage
 * @see de.uol.swp.common.user.User
 * @since 2019-10-08
 */
public class UserLeftLobbyMessage extends AbstractLobbyMessage implements LobbyNotificationMessage {

    private final UserDTO user;

    /**
     * Constructor
     *
     * @param lobbyName name of the lobby
     * @param user      user who left the lobby
     * @since 2019-10-08
     */
    public UserLeftLobbyMessage(String lobbyName, UserDTO user) {
        super(lobbyName);
        this.user = user;
    }

    /**
     * Returns the user who left the lobby
     *
     * @return the user who left the lobby
     */
    public UserDTO getUser() {
        return user;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        UserLeftLobbyMessage that = (UserLeftLobbyMessage) obj;
        return Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), user);
    }
}
