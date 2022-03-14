package de.uol.swp.server.usermanagement;

/**
 * Exception thrown in UserManagement
 * <p>
 * This exception is thrown if someone wants to register a with a username that
 * is already taken or someone tries to modify or remove a user that does not (yet)
 * exist within the UserStore.
 *
 * @author Marco Grawunder
 * @see de.uol.swp.server.usermanagement.UserManagement
 * @since 2019-07-08
 */
class UserManagementException extends RuntimeException {

    /**
     * Constructor
     *
     * @param cause a String containing the cause for the exception.
     * @since 2019-07-08
     */
    UserManagementException(String cause) {
        super(cause);
    }
}
