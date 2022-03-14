package de.uol.swp.server.usermanagement;

import de.uol.swp.common.user.User;

import java.util.List;

/**
 * An interface for all methods of the server user service
 *
 * @author Marco Grawunder
 * @since 2017-03-17
 */

public interface ServerUserService {

    /**
     * Login with username and password
     *
     * @param username the name of the user
     * @param password the password of the user
     * @return a new user object
     * @since 2017-03-17
     */
    User login(String username, String password);


    /**
     * Test, if given user is logged in
     *
     * @param user the user to check for
     * @return true if the User is logged in
     * @since 2019-09-04
     */
    boolean isLoggedIn(User user);

    /**
     * Log out from server
     *
     * @implNote the User Object has to contain an username identifier in order to
     * log out the correct user
     * @since 2017-03-17
     */
    void logout(User user);

    /**
     * Create a new persistent user
     *
     * @param user The user to create
     * @return the new created user
     * @since 2019-09-02
     */
    User createUser(User user);

    /**
     * Removes a user from the sore
     * <p>
     * Remove the User specified by the User object.
     *
     * @param user The user to remove
     * @implNote the User Object has to contain a unique identifier in order to
     * remove the correct user
     * @since 2019-10-10
     */
    void dropUser(User user);

    /**
     * Update a username
     *
     * @implNote the User Object has to contain a unique identifier in order to
     *      			 update the correct username
     *
     * @param user This user is identified in the database and then his name is updated
     * @param newUserName new name, the old one has to be replaced
     * @return the updated user object and new username
     * @since 2019-09-02
     */
    User updateUserName(User user, String newUserName);

    /**
     * Update a E-Mail
     *
     * @param user This user is identified in the database and then email is updated
     * @param newEmail new email, that has to be replaced whit the old one
     * @return the updated user object an new username
     */
    User updateUserEmail(User user, String newEmail);


    /**
     * update a password
     * @param user This user is identified in the database and then password is updated
     * @param currentPassword old password
     * @param newPassword new password
     * @return if the password is changed successfully, it returns false
     */
    boolean updateUserPassword(User user, String currentPassword, String newPassword);
    /**
     * Retrieve the list of all current logged in users
     *
     * @return a list of users
     * @since 2017-03-17
     */
    List<User> retrieveAllUsers();

}
