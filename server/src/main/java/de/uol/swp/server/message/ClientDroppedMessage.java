package de.uol.swp.server.message;


import de.uol.swp.common.user.User;

import java.util.Objects;

/**
 * A message indicating that a user account has been deleted
 * This message is used to automatically update the user lists of all connected users
 * Client as soon as a user account is successfully deleted
 *
 * @see AbstractServerInternalMessage
 */
public class ClientDroppedMessage extends AbstractServerInternalMessage {
    private final User user;

    /**
     * Constructor
     *
     * @param user the username of the deleted account
     */
    public ClientDroppedMessage(User user) {
        this.user = user;
    }

    /**
     * getter for the username
     *
     * @return a user containing the user
     */
    public User getUser() {
        return user;
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
        ClientDroppedMessage that = (ClientDroppedMessage) object;
        return Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), user);
    }
}
