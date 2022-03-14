package de.uol.swp.server.message;

import de.uol.swp.common.game.dto.UserDTO;

import java.util.List;
import java.util.Objects;

/**
 * A message indicating that user information has been changed
 *
 * This message is used to notify clients of the updated user
 *
 */
public class ClientChangedUserInfoMessage extends AbstractServerInternalMessage{

    private final UserDTO user;
    private final String oldUsername;
    private final List<String> lobbies;

    /**
     * Constructor
     * @param oldUsername old username, which will be replaced by a new one
     * @param user  User who is being updated
     * @param lobbies lobbies, in which user is
     */
    public ClientChangedUserInfoMessage(String oldUsername, UserDTO user, List<String> lobbies) {
        this.user = user;
        this.oldUsername = oldUsername;
        this.lobbies = lobbies;
    }

    /**
     * Getter for the updated user
     *
     * @return updated users
     */
    public UserDTO getUser() {
        return user;
    }

    /**
     * Getter for the oldUsername
     *
     * @return oldUsername
     */
    public String getOldUsername() {
        return oldUsername;
    }

    /**
     * Getter for lobbyList
     *
     * @return list for Lobby
     */
    public List<String> getLobbies() {
        return lobbies;
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
        ClientChangedUserInfoMessage that = (ClientChangedUserInfoMessage) object;
        return Objects.equals(user, that.user) && Objects.equals(oldUsername, that.oldUsername) && Objects.equals(lobbies, that.lobbies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), user, oldUsername, lobbies);
    }
}
