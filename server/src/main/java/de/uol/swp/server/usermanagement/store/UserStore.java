package de.uol.swp.server.usermanagement.store;

import de.uol.swp.common.user.User;
import de.uol.swp.common.user.player.PlayerProfile;

import java.util.List;
import java.util.Optional;

/**
 * Interface to unify different kinds of UserStores in order to able to exchange
 * them easily.
 *
 * @author Marco Grawunder
 * @since 2019-08-13
 */
public interface UserStore {

    /**
     * Find a user by username and password
     *
     * @param username username of the user to find
     * @param password password of the user to find
     * @return the User without password information, if found
     * @since 2019-08-13
     */
    Optional<User> findUser(String username, String password);

    /**
     * Find a user only by name
     *
     * @param username username of the user to find
     * @return the User without password information, if found
     * @since 2019-08-13
     */
    Optional<User> findUser(String username);

    /**
     * Create a new user
     *
     * @param username username of the new user
     * @param password password the user wants to use
     * @param eMail    email address of the new user
     * @return the User without password information
     * @since 2019-08-13
     */
    User createUser(String username, String password, String eMail);

    /**
     * Username is updated. Id cannot be updated
     *
     * @param username    the username is used to find and update users in the database
     * @param newUserName new username
     * @return String username
     */
    String updateUserName(String username, String newUserName);

    /**
     * E-Mail is updated, Id cannot be updated
     *
     * @param username the username is used to find and update users in the database
     * @param eMail    new E-Mail
     * @return String E-Mail
     */
    String updateUserEmail(String username, String eMail);

    /**
     * Password is updated
     *
     * @param username    this username is user to find and update users in the database
     * @param newPassword new password
     * @return if the password is changed successfully, it returns true
     */
    boolean updateUserPassword(String username, String newPassword);

    /**
     * Remove user from store
     *
     * @param username the username of the user to remove
     * @since 2019-10-10
     */
    void removeUser(String username);


    /**
     * Retrieves the list of all users.
     *
     * @return a list of all users without password information
     * @since 2019-08-13
     */
    List<User> getAllUsers();

    /**
     * Gets the name, wins and losses from all players.
     *
     * @return a list of all users with their wins and losses.
     * @see PlayerProfile
     */
    List<PlayerProfile> getAllPlayerStatistics();

    /**
     * Gets the info of player
     *
     * @param username which is compared in the database
     * @return info of player
     */
    PlayerProfile getPlayerInfoData(String username);

    /**
     * If the player wins, it is counted up
     * @param username of the player
     */
    boolean updateGamesWon(String username);

    /**
     * if the player loses, it counts down
     * @param username of the player
     */
    boolean updateGamesLost(String username);

    /**
     * it is determined how many times the player has won
     *
     * @param playerName of player
     *
     * @return Result of the wins
     */
    int getNoOfGamesWon(String playerName);

    /**
     * it is determined how many times the player has los
     *
     * @param playerName of player
     *
     * @return Result of loss
     */
    int getNoOfGamesLost(String playerName);
}
