package de.uol.swp.common.user.request;

import de.uol.swp.common.message.AbstractRequestMessage;

import java.util.Objects;

/**
 * Response to delete a users account
 *
 * @see AbstractRequestMessage
 */
public class DropAccountUserRequest extends AbstractRequestMessage {

    private final String confirmPassword;

    /**
     * Constructor
     *
     * @param password password to confirm
     */
    public DropAccountUserRequest(String password) {
        this.confirmPassword = password;
    }

    /**
     * Getter for the password variable
     *
     * @return password to confirm
     */
    public String getPassword() {
        return confirmPassword;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        DropAccountUserRequest that = (DropAccountUserRequest) obj;
        return Objects.equals(confirmPassword, that.confirmPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), confirmPassword);
    }
}
