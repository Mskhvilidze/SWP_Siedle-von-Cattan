package de.uol.swp.common.user.request;

import de.uol.swp.common.message.AbstractRequestMessage;

import java.util.Objects;

/**
 * A request send from client to server, trying to log in with
 * username and password
 *
 * @author Marco Grawunder
 * @since 2017-03-17
 */
public class LoginRequest extends AbstractRequestMessage {

    private static final long serialVersionUID = 7793454958390539421L;
    private final String username;
    private final String password;

    /**
     * Constructor
     *
     * @param username username the user tries to log in with
     * @param password password the user tries to log in with
     * @since 2017-03-17
     */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean authorizationNeeded() {
        return false;
    }

    /**
     * Getter for the username variable
     *
     * @return a String containing the username the user tries to log in with
     * @since 2017-03-17
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter for the password variable
     *
     * @return a String containing the password the user tries to log in with
     * @since 2017-03-17
     */
    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        LoginRequest that = (LoginRequest) object;
        return Objects.equals(username, that.username) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }
}
