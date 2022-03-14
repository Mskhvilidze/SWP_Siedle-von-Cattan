package de.uol.swp.client.user;

import de.uol.swp.common.user.User;


/**
 * An interface for all methods of the client user service
 * <p>
 * As the communication with the server is based on events, the
 * returns of the call must be handled by events
 *
 * @author Marco Grawunder
 * @since 2017-03-17
 */

public interface ClientUserService {

    /**
     * Login with username and password
     *
     * @param username the name of the user
     * @param password the password of the user
     * @since 2017-03-17
     */
    void login(String username, String password);

    /**
     * Log out from server
     *
     * @param user User unused
     * @implNote param user is not used clientside
     * @since 2017-03-17
     */
    void logout(User user);

    /**
     * Create a new persistent user
     *
     * @param user the user to create
     * @since 2019-09-02
     */
    void createUser(User user);

    /**
     * Deletes the currently logged in user
     *
     * @param password the current password entered by the user
     */
    void dropUser(String password);

    /**
     * Update a users name
     *
     * @param newName the new username entered by the user
     * @implNote the user should be identified on the server
     */
    void changeUserName(String newName);

    /**
     * Update a users password
     *
     * @param currentPassword the current password entered by the user
     * @param newPassword     the new password entered by the user
     * @implNote the user should be identified on the server
     */
    void changeUserPassword(String currentPassword, String newPassword);

    /**
     * Update a users email
     *
     * @param newEmail the new email entered by the user
     * @implNote the user should be identified on the server
     */
    void changeUserEmail(String newEmail);

    /**
     * Retrieves the player profile of the currently logged in user
     */
    void retrievePlayerProfile();

    /**
     * Retrieve the list of all current logged in users
     *
     * @since 2017-03-17
     */
    void retrieveAllUsers();
}
