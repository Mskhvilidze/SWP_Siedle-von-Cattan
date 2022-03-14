package de.uol.swp.server.usermanagement.store;

import com.google.common.base.Strings;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.player.PlayerProfile;

import java.util.*;

/**
 * This is a user store.
 * <p>
 * This is the user store that is used for the start of the software project. The
 * user accounts in this user store only reside within the RAM of your computer
 * and only for as long as the server is running. Therefore the users have to be
 * added every time the server is started.
 *
 * @author Marco Grawunder
 * @implNote This store will never return the password of a user!
 * @see de.uol.swp.server.usermanagement.store.AbstractUserStore
 * @see de.uol.swp.server.usermanagement.store.UserStore
 * @since 2019-08-05
 * @deprecated replaced by {@link DataBaseUserStore}
 */
@Deprecated
public class MainMemoryBasedUserStore extends AbstractUserStore implements UserStore {
    private static final String DEPRECATED_MESSAGE = "Not supported in MainMemoryBasedUserStore";
    private final Map<String, User> users = new HashMap<>();

    @Override
    public Optional<User> findUser(String username, String password) {
        User usr = users.get(username);
        if (usr != null && validatePassword(password, usr.getPasswordHash(), usr.getPasswordSalt())) {
            return Optional.of(usr.getWithoutPassword());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findUser(String userId) {
        User usr = users.get(userId);
        if (usr != null) {
            return Optional.of(usr.getWithoutPassword());
        }
        return Optional.empty();
    }

    @Override
    public User createUser(String username, String password, String eMail) {
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalArgumentException("Username must not be null");
        }
        byte[][] passwordHashWithSalt = createHashWithSalt(password);
        User usr = new UserDTO("", username, passwordHashWithSalt[1], passwordHashWithSalt[0], eMail);
        users.put(username, usr);
        return usr;
    }

    @Override
    public String updateUserName(String username, String newUserName) {
        throw new UnsupportedOperationException(DEPRECATED_MESSAGE);
    }

    @Override
    public String updateUserEmail(String username, String eMail) {
        throw new UnsupportedOperationException(DEPRECATED_MESSAGE);
    }

    @Override
    public boolean updateUserPassword(String username, String newPassword) {
        throw new UnsupportedOperationException(DEPRECATED_MESSAGE);
    }

    @Override
    public void removeUser(String username) {
        users.remove(username);
    }

    @Override
    public List<User> getAllUsers() {
        List<User> retUsers = new ArrayList<>();
        users.values().forEach(u -> retUsers.add(u.getWithoutPassword()));
        return retUsers;
    }

    @Override
    public List<PlayerProfile> getAllPlayerStatistics() {
        throw new UnsupportedOperationException(DEPRECATED_MESSAGE);
    }

    @Override
    public PlayerProfile getPlayerInfoData(String username) {
        throw new UnsupportedOperationException(DEPRECATED_MESSAGE);
    }

    @Override
    public boolean updateGamesWon(String username) {
        throw new UnsupportedOperationException(DEPRECATED_MESSAGE);
    }

    @Override
    public boolean updateGamesLost(String username) {
        throw new UnsupportedOperationException(DEPRECATED_MESSAGE);
    }

    @Override
    public int getNoOfGamesWon(String playerName) {
        throw new UnsupportedOperationException(DEPRECATED_MESSAGE);
    }

    @Override
    public int getNoOfGamesLost(String playerName) {
        throw new UnsupportedOperationException(DEPRECATED_MESSAGE);
    }
}
