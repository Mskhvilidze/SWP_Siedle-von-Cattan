package de.uol.swp.common.user.message;

import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.message.AbstractServerMessage;

import java.util.Objects;

/**
 * A message indicating an updated user
 *
 * This message is used to notify clients that a user has been updated
 *
 * @see AbstractServerMessage
 */
public class ChangedUserInfoMessage extends AbstractServerMessage {

    private final UserDTO userDTO;
    private final String oldUsername;

    /**
     * Constructor
     *
     * @param oldUsername old username
     * @param userDTO updated user
     */
    public ChangedUserInfoMessage(String oldUsername, UserDTO userDTO) {
        this.userDTO = userDTO;
        this.oldUsername = oldUsername;
    }

    /**
     * Getter for the updated user
     *
     * @return updated user
     */
    public UserDTO getUserDTO() {
        return userDTO;
    }

    /**
     * Getter for the old username
     *
     * @return old username
     */
    public String getOldUsername() {
        return oldUsername;
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
        ChangedUserInfoMessage that = (ChangedUserInfoMessage) object;
        return Objects.equals(userDTO, that.userDTO) && Objects.equals(oldUsername, that.oldUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userDTO, oldUsername);
    }
}
