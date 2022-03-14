package de.uol.swp.server.usermanagement.store;


import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.player.PlayerProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for DataBaseUserStory
 */
@Tag("database")
class DataBaseUserStoreTest {

    static final List<UserDTO> USERS;

    static {
        USERS = new ArrayList<>();
        USERS.add(new UserDTO("Ronaldo", "ronaldo", "brazil@inter.it"));
        USERS.add(new UserDTO("Barella", "barella", "nicola@inter.it"));
        Collections.sort(USERS);
    }

    final User user = new UserDTO("Empty", "", "empty@yahoo.de");

    List<UserDTO> getDefaultUsers() {
        return Collections.unmodifiableList(USERS);
    }

    DataBaseUserStore getDefaultStore() {
        return new DataBaseUserStore(true);
    }

    @BeforeEach
    void resetDataBase() {
        getDefaultStore().clearTestDataBase();
    }

    @Test
    void createUser() {
        UserStore store = getDefaultStore();
        List<UserDTO> users = getDefaultUsers();
        users.forEach(u -> store.createUser(u.getUsername(), u.getPlainPassword(), u.getEMail()));

        //The user already exists in the database
        User userNotCreated = store.createUser(users.get(0).getUsername(), users.get(0).getPlainPassword(), users.get(0).getEMail());

        //assert
        Assertions.assertNull(userNotCreated);

        // IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> store.createUser("", "",
                ""));
    }

    @Test
    void findUserByName() {
        //arrange
        final UserStore store = getDefaultStore();
        User userToFind = store.createUser("Player", "player", "player@inter.de");
        //actual
        Optional<User> userFound = store.findUser(userToFind.getUsername());
        //assert
        Assertions.assertTrue(userFound.isPresent());
        Assertions.assertEquals(userToFind.getUsername(), userFound.get().getUsername());
    }

    @Test
    void findUserByNameNotFound() {
        final UserStore store = getDefaultStore();

        final Optional<User> userFound = store.findUser(user.getUsername());
        //assert
        Assertions.assertFalse(userFound.isPresent());
    }

    @Test
    void findUserByNameAndPassword() {
        final UserStore store = getDefaultStore();
        final User userToFind = store.createUser("Player1", "player1", "player1@inter.de");

        final Optional<User> userFound = store.findUser(userToFind.getUsername(), "player1");
        //assert
        Assertions.assertTrue(userFound.isPresent());
        Assertions.assertEquals(userToFind.getUsername(), userFound.get().getUsername());
    }

    @Test
    void findUserByNameAndPasswordNotFound() {
        final UserStore store = getDefaultStore();
        //User does not have to be found
        final Optional<User> userFound = store.findUser(user.getUsername(), user.getPlainPassword());
        //assert
        Assertions.assertFalse(userFound.isPresent());
    }

    @Test
    void updateUserName() {
        final UserStore store = getDefaultStore();
        final User userToUpdate = store.createUser("Player2", "player2", "player2@milan.de");
        //username updated
        String newUsername = store.updateUserName(userToUpdate.getUsername(), "Test");
        final Optional<User> userFound = store.findUser("Test");
        //assert
        Assertions.assertTrue(userFound.isPresent());
        Assertions.assertEquals(newUsername, userFound.get().getUsername());
    }

    @Test
    void updateUserEmail() {
        final UserStore store = getDefaultStore();
        final User userToUpdate = store.createUser("Player2", "player2", "player2@milan.de");
        //E-Mail updated
        String email = store.updateUserEmail(userToUpdate.getUsername(), "player2@yahoo.de");
        final Optional<User> userFound = store.findUser(userToUpdate.getUsername());
        //assert
        Assertions.assertTrue(userFound.isPresent());
        Assertions.assertEquals(email, userFound.get().getEMail());
    }

    @Test
    void updateUserPassword() {
        final UserStore store = getDefaultStore();
        final User userToUpdate = store.createUser("Player2", "player2", "player2@milan.de");
        //Password updated
        boolean updatedUser = store.updateUserPassword(userToUpdate.getUsername(), "test");
        final Optional<User> userFound = store.findUser(userToUpdate.getUsername());
        //assert
        Assertions.assertTrue(userFound.isPresent());
        Assertions.assertTrue(updatedUser);
    }

    @Test
    void removeUser() {
        final UserStore store = getDefaultStore();
        final User dropUser = store.createUser("Player3", "player3", "player@inter.de");
        //user deleted
        store.removeUser(dropUser.getUsername());
        final Optional<User> userFound = store.findUser(dropUser.getUsername());

        //assert
        Assertions.assertFalse(userFound.isPresent());

        //IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> store.removeUser(""));
    }

    @Test
    void shouldNotReturnPlainPasswords() {
        final UserStore store = getDefaultStore();

        final List<User> usersFromStore = store.getAllUsers();

        usersFromStore.forEach(u -> Assertions.assertNull(u.getPlainPassword()));
    }

    @Test
    void createEmptyUser() {
        final UserStore store = getDefaultStore();
        //IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> store.createUser("", "", ""));
    }

    @Test
    void allPlayerStatistics() {
        final UserStore store = getDefaultStore();

        final User user = store.createUser("Player", "player", "player@yahoo.de");

        List<PlayerProfile> statistic = store.getAllPlayerStatistics();
        statistic.forEach(i -> Assertions.assertEquals(i.getPlayerName(), user.getUsername()));
    }

    @Test
    void playerInfoData() {
        final UserStore store = getDefaultStore();

        final User user = store.createUser("Player", "player", "player@yahoo.de");
        final Optional<User> userToFind = store.findUser(user.getUsername());

        PlayerProfile profile = store.getPlayerInfoData(userToFind.get().getUsername());
        Assertions.assertEquals(user.getUsername(), profile.getPlayerName());
        Assertions.assertEquals(user.getEMail(), profile.getEmail());
    }

    @Test
    void updateGamesWon() {
        final UserStore store = getDefaultStore();

        final User user = store.createUser("Player", "player", "player@yahoo.de");
        String updateToUser = user.getUsername();
        boolean isUpdatedUser = store.updateGamesWon(updateToUser);
        int result = store.getNoOfGamesWon(updateToUser);
        Assertions.assertTrue(isUpdatedUser);
        Assertions.assertEquals(1, result);
    }
    @Test
    void updateGamesLost() {
        final UserStore store = getDefaultStore();

        final User user = store.createUser("Player", "player", "player@yahoo.de");
        String updateToUser = user.getUsername();
        boolean isUpdatedUser = store.updateGamesLost(updateToUser);
        int result = store.getNoOfGamesLost(updateToUser);
        Assertions.assertTrue(isUpdatedUser);
        Assertions.assertEquals(1, result);
    }
}