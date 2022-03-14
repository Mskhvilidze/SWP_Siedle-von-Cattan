package de.uol.swp.common.user.message;

import de.uol.swp.common.message.AbstractServerMessage;

import java.util.Objects;

/**
 * A message indicating that a user account has been deleted
 * This message is used to automatically update the user lists of all connected users
 * as soon as a user account is successfully deleted
 *
 * @see AbstractServerMessage
 */

public class UserAccountDropMessage extends AbstractServerMessage {

    private final String username;

    /**
     * Constructor
     *
     * @param username the username of the deleted user
     */
    public UserAccountDropMessage(String username) {
        this.username = username;
    }

    /**
     * Getter for the username
     *
     * @return a String containing the username
     */
    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        final UserAccountDropMessage that = (UserAccountDropMessage) object;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username);
    }
}
