package de.uol.swp.common.user.response;

import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.message.AbstractResponseMessage;

import java.util.Objects;

/**
 * This response is sent to the client whose DropAccountUserRequest was successful
 *
 * @see AbstractResponseMessage
 */
public class DropAccountSuccessfulResponse extends AbstractResponseMessage {
    private final UserDTO userDTO;

    /**
     * Constructor
     *
     * @param user whose account will be deleted
     */
    public DropAccountSuccessfulResponse(UserDTO user) {
        this.userDTO = user;
    }

    /**
     * Getter for the user variable
     *
     * @return User whose account is deleted
     */
    public UserDTO getUser() {
        return userDTO;
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
        DropAccountSuccessfulResponse response = (DropAccountSuccessfulResponse) object;
        return Objects.equals(userDTO, response.userDTO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userDTO);
    }
}
