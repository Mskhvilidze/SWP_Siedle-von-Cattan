package de.uol.swp.common.user.response;

import de.uol.swp.common.message.AbstractResponseMessage;

import java.util.Objects;

/**
 * Message is used when password is changed successfully
 */
public class EditPasswordResponse extends AbstractResponseMessage {

    private final boolean correctPassword;

    /**
     * Constructor
     *
     * @param correctPassword
     */
    public EditPasswordResponse(boolean correctPassword ) {
        this.correctPassword = correctPassword ;
    }

    /**
     * Getter for the updated password
     *
     * @return wrongPassword
     */
    public boolean isCorrectPassword() {
        return correctPassword;
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
        EditPasswordResponse that = (EditPasswordResponse) object;
        return correctPassword == that.correctPassword;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), correctPassword);
    }
}
