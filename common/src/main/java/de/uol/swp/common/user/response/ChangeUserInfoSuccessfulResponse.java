package de.uol.swp.common.user.response;

import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.message.AbstractResponseMessage;

import java.util.Objects;

/**
 * This response is sent if userinfo is changed successfully
 *
 */
public class ChangeUserInfoSuccessfulResponse extends AbstractResponseMessage {

    private UserDTO newUserInfo;

    /**
     * Constructor
     *
     * @param newUserInfo user to update
     */
    public ChangeUserInfoSuccessfulResponse(UserDTO newUserInfo) {
        this.newUserInfo = newUserInfo;
    }

    /**
     * Getter for user
     *
     * @return user to update
     */
    public UserDTO getNewUserInfo() {
        return newUserInfo;
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
        ChangeUserInfoSuccessfulResponse response = (ChangeUserInfoSuccessfulResponse) object;
        return Objects.equals(newUserInfo, response.newUserInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), newUserInfo);
    }
}
