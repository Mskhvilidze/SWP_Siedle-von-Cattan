package de.uol.swp.server.message;

import de.uol.swp.common.user.User;

import java.util.Objects;

/**
 * This message is used if a successful login occurred
 * <p>
 * This message is used to signalize all Services it is relevant to, that someone
 * just logged in successfully
 *
 * @author Marco Grawunder
 * @see de.uol.swp.server.message.AbstractServerInternalMessage
 * @see de.uol.swp.server.usermanagement.AuthenticationService
 * @since 2019-08-07
 */
public class ClientAuthorizedMessage extends AbstractServerInternalMessage {

    private final User user;

    /**
     * Constructor
     *
     * @param user user whose client authorized successfully
     * @see de.uol.swp.common.user.User
     * @since 2019-08-07
     */
    public ClientAuthorizedMessage(User user) {
        super();
        this.user = user;
    }

    /**
     * Getter for the user attribute
     *
     * @return the user whose client authorized successfully
     * @see de.uol.swp.common.user.User
     * @since 2019-08-07
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
        ClientAuthorizedMessage that = (ClientAuthorizedMessage) object;
        return Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), user);
    }
}
