package de.uol.swp.server.usermanagement;

import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.player.PlayerProfile;
import de.uol.swp.server.usermanagement.store.UserStore;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Handles most user related issues e.g. login/logout
 *
 * @author Marco Grawunder
 * @see de.uol.swp.server.usermanagement.AbstractUserManagement
 * @since 2019-08-05
 */
public class UserManagement extends AbstractUserManagement {

    private final UserStore userStore;
    private final SortedMap<String, User> loggedInUsers = new TreeMap<>();

    /**
     * Constructor
     *
     * @param userStore object of the UserStore to be used
     * @see de.uol.swp.server.usermanagement.store.UserStore
     * @since 2019-08-05
     */
    @Inject
    public UserManagement(UserStore userStore) {
        this.userStore = userStore;
    }

    @Override
    public User login(String username, String password) {
        Optional<User> user = userStore.findUser(username, password);
        if (user.isPresent()) {
            if (!loggedInUsers.containsKey(username)) {
                this.loggedInUsers.put(username, user.get());
                return user.get();
            } else {
                throw new UserManagementException("User already logged in " + username);
            }
        } else {
            throw new SecurityException("Cannot auth user " + username);
        }
    }

    @Override
    public boolean isLoggedIn(User username) {
        return loggedInUsers.containsKey(username.getUsername());
    }

    @Override
    public void logout(User user) {
        loggedInUsers.remove(user.getUsername());
    }

    @Override
    public User createUser(User userToCreate) {
        Optional<User> user = userStore.findUser(userToCreate.getUsername());
        if (user.isPresent()) {
            throw new UserManagementException("Username already used!");
        }
        return userStore.createUser(userToCreate.getUsername(), userToCreate.getPlainPassword(), userToCreate.getEMail());
    }

    @Override
    public void dropUser(User userToDrop) {
        Optional<User> user = userStore.findUser(userToDrop.getUsername());
        if (user.isEmpty()) {
            throw new UserManagementException("Username unknown!");
        }
        logout(userToDrop);
        userStore.removeUser(userToDrop.getUsername());
    }

    @Override
    public User updateUserName(User user, String newUserName) {
        Optional<User> userToUpdate = userStore.findUser(user.getUsername());
        if (userToUpdate.isEmpty()) {
            throw new UserManagementException("Username unknown!");
        }
        //Update only if new username is available
        String username = userToUpdate.get().getUsername();
        String updatedUserName = userStore.updateUserName(username, newUserName);
        return UserDTO.createWithoutPassword(userToUpdate.get().getUserId(), updatedUserName, userToUpdate.get().getEMail());

    }

    @Override
    public User updateUserEmail(User user, String newEmail) {
        Optional<User> userToUpdate = userStore.findUser(user.getUsername());
        if (userToUpdate.isEmpty()) {
            throw new UserManagementException("E-Mail unknown!");
        }

        String username = userToUpdate.get().getUsername();
        String updatedUserEmail = userStore.updateUserEmail(username, newEmail);
        return UserDTO.createWithoutPassword(username, userToUpdate.get().getUsername(), updatedUserEmail);
    }

    @Override
    public boolean updateUserPassword(User user, String currentPassword, String newPassword) {
        Optional<User> userToUpdate = userStore.findUser(user.getUsername(), currentPassword);
        if (userToUpdate.isEmpty()) {
            return false;
        } else {
            String username = userToUpdate.get().getUsername();
            return userStore.updateUserPassword(username, newPassword);
        }
    }

    @Override
    public List<User> retrieveAllUsers() {
        return userStore.getAllUsers();
    }

    /**
     * If the user is found in the database, his account will be deleted
     *
     * @param userName     Name of the user
     * @param userPassword confirm password
     * @return boolean value
     */
    public boolean isValidateUser(String userName, String userPassword) {
        Optional<User> user = userStore.findUser(userName, userPassword);
        return user.isPresent();
    }

    /**
     * This method returns the list of players with name, wins and losses
     *
     * @return list with playerStatistics
     */
    public List<PlayerProfile> getAllPlayerStatistics() {
        return userStore.getAllPlayerStatistics();
    }

    /**
     * This method returns the info of player
     *
     * @param user User, who is searched for in the database
     * @return player info
     */
    public PlayerProfile getPlayerInfoData(User user) {
        Optional<User> userToFind = userStore.findUser(user.getUsername());
        if (userToFind.isEmpty()) {
            throw new UserManagementException("Username unknown");
        }

        String username = userToFind.get().getUsername();
        return userStore.getPlayerInfoData(username);
    }

    /**
     * This method saves player winners via DataBaseUserStory
     * in the database
     *
     * @param playerWon, who won
     */
    public void gameWon(String playerWon){
        Optional<User> userToFind = userStore.findUser(playerWon);

        if (userToFind.isEmpty()) {
            throw new UserManagementException("Username unknown");
        }

        userStore.updateGamesWon(userToFind.get().getUsername());
    }

    /**
     * This method saves player losers via DataBaseUserStory
     * in the database
     *
     * @param playerLoss contains the list of player losers
     */
    public void gameLoss(String playerLoss){
        Optional<User> userToFind = userStore.findUser(playerLoss);

        if (userToFind.isEmpty()) {
            throw new UserManagementException("Username unknown");
        }

        userStore.updateGamesLost(playerLoss);
    }
}
