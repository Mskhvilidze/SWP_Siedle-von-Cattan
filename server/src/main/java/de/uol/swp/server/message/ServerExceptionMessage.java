package de.uol.swp.server.message;

import java.util.Objects;

/**
 * This message is used if something went wrong
 * <p>
 * This ServerMessage is used if something went wrong e.g. in the login process
 *
 * @author Marco Grawunder
 * @see de.uol.swp.server.message.AbstractServerInternalMessage
 * @see de.uol.swp.server.usermanagement.AuthenticationService#onLoginRequest
 * @since 2019-08-07
 */
public class ServerExceptionMessage extends AbstractServerInternalMessage {

    private final Exception exception;

    /**
     * Constructor
     *
     * @param exception the Exception that is the reason for the creation of this
     * @since 2019-08-07
     */
    public ServerExceptionMessage(Exception exception) {
        super();
        this.exception = exception;
    }

    /**
     * Getter for the Exception
     *
     * @return the Exception passed in the constructor
     * @since 2019-08-07
     */
    public Exception getException() {
        return exception;
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
        ServerExceptionMessage that = (ServerExceptionMessage) object;
        return Objects.equals(exception, that.exception);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), exception);
    }
}
