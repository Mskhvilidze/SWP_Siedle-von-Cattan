package de.uol.swp.server.usermanagement;

import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.player.PlayerProfile;
import de.uol.swp.server.usermanagement.store.DataBaseUserStore;
import de.uol.swp.server.usermanagement.store.UserStore;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"PMD.AccessorMethodGeneration", "PMD.AvoidInstantiatingObjectsInLoops", "PMD.CommentRequired", "PMD.MethodNamingConventions", "PMD.DefaultPackage"})
@Tag("database")
class UserManagementTest {

    static final int NO_USERS = 10;
    static final List<UserDTO> USERS;
    static final User USER_NOT_IN_STORE = new UserDTO("marco" + NO_USERS, "marco" + NO_USERS, "marco" + NO_USERS + "@grawunder.de");

    static {
        USERS = new ArrayList<>();
        for (int i = 0; i < NO_USERS; i++) {
            USERS.add(new UserDTO("marco" + i, "marco" + i, "marco" + i + "@grawunder.de"));
        }
        Collections.sort(USERS);
    }

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

    UserManagement getDefaultManagement() {
        DataBaseUserStore store = getDefaultStore();
        List<UserDTO> users = getDefaultUsers();
        store.createUsers(users);
        return new UserManagement(store);
    }

    /**
     * Test for user profile
     */
    @Test
    void playerInfoData() {
        UserManagement management = getDefaultManagement();
        User user = USERS.get(0);
        UserStore store = getDefaultStore();

        Optional<User> userToFind = store.findUser(user.getUsername());
        PlayerProfile profile = management.getPlayerInfoData(userToFind.get());
        Assertions.assertEquals(user.getUsername(), profile.getPlayerName());
        Assertions.assertEquals(user.getEMail(), profile.getEmail());
    }

    /**
     * Test for all player statistics
     */
    @Test
    void allPlayerStatistics() {
        UserManagement management = getDefaultManagement();
        UserStore store = getDefaultStore();
        User user = USERS.get(0);

        Optional<User> userToFind = store.findUser(user.getUsername());

        List<PlayerProfile> list = management.getAllPlayerStatistics();
        Assertions.assertEquals(list.get(0).getPlayerName(), userToFind.get().getUsername());
    }

    @Test
    void loggedInUser_ShouldLogoutUser() {
        UserManagement management = getDefaultManagement();
        User userToLogin = USERS.get(0);

        management.login(userToLogin.getUsername(), userToLogin.getPlainPassword());

        assertTrue(management.isLoggedIn(userToLogin));

        management.logout(userToLogin);

        assertFalse(management.isLoggedIn(userToLogin));
    }

    @Test
    void retrieveAllUsers_ShouldNotContainPasswords() {
        UserManagement management = getDefaultManagement();

        List<User> allUsers = management.retrieveAllUsers();

        Collections.sort(allUsers);

        allUsers.forEach(u -> assertTrue(u.getPasswordHash().length == 0 && u.getPasswordSalt().length == 0));
    }

    /**
     * Test subclass for all login related functions in {@code UserManagement}
     */
    @Nested
    class Login {
        @Test
        void correctPassword_ShouldLoginUser() {
            UserManagement management = getDefaultManagement();
            User userToLogIn = USERS.get(0);

            management.login(userToLogIn.getUsername(), userToLogIn.getPlainPassword());

            assertTrue(management.isLoggedIn(userToLogIn));
        }

        @Test
        void emptyPassword_ShouldNotLoginUser() {
            UserManagement management = getDefaultManagement();
            User userToLogIn = USERS.get(0);
            String username = userToLogIn.getUsername();
            assertThrows(SecurityException.class, () -> management.login(username, ""));

            assertFalse(management.isLoggedIn(userToLogIn));
        }

        @Test
        void wrongPassword_ShouldNotLoginUser() {
            UserManagement management = getDefaultManagement();
            User userToLogIn = USERS.get(0);
            String username = userToLogIn.getUsername();
            String wrongPassword = userToLogIn.getPlainPassword() + "1";
            assertThrows(SecurityException.class, () -> management.login(username, wrongPassword));

            assertFalse(management.isLoggedIn(userToLogIn));
        }
    }

    /**
     * Test subclass for all functions related to creating a user in {@code UserManagement}
     */
    @Nested
    class CreateUser {
        @Test
        void notAlreadyExisting_ShouldCreateUser() {
            UserManagement management = getDefaultManagement();
            String username = USER_NOT_IN_STORE.getUsername();
            String password = USER_NOT_IN_STORE.getPlainPassword();
            assertFalse(management.isLoggedIn(USER_NOT_IN_STORE));
            management.createUser(USER_NOT_IN_STORE);

            // Creation leads not to log in
            assertFalse(management.isLoggedIn(USER_NOT_IN_STORE));
            // Only way to test, if user is stored
            management.login(username, password);
            assertTrue(management.isLoggedIn(USER_NOT_IN_STORE));
        }

        @Test
        void alreadyExisting_ShouldThrowException() {
            UserManagement management = getDefaultManagement();
            User userToCreate = USERS.get(0);

            assertThrows(UserManagementException.class, () -> management.createUser(userToCreate));
        }
    }

    /**
     * Test subclass for all functions related to deleting a user in {@code UserManagement}
     */
    @Nested
    class DropUser {
        @Test
        void notExisting_ShouldThrowException() {
            UserManagement management = getDefaultManagement();
            assertThrows(UserManagementException.class, () -> management.dropUser(USER_NOT_IN_STORE));
        }

        @Test
        void existing_ShouldRemoveUser() {
            UserManagement management = getDefaultManagement();
            management.createUser(USER_NOT_IN_STORE);
            management.dropUser(USER_NOT_IN_STORE);

            String username = USER_NOT_IN_STORE.getUsername();
            String password = USER_NOT_IN_STORE.getPlainPassword();
            assertThrows(SecurityException.class, () -> management.login(username, password));
        }
    }

    /**
     * Test subclass for all functions related to updating a user in {@code UserManagement}
     */
    @Nested
    class UpdateUser {
        @Test
        void newPassword_WhenNotLoggedIn_ShouldUpdatePassword() {
            UserManagement management = getDefaultManagement();
            User userToUpdate = USERS.get(0);
            User updatedUser = new UserDTO(userToUpdate.getUsername(), "newPassword", null);

            assertFalse(management.isLoggedIn(userToUpdate));
            management.updateUserPassword(updatedUser, userToUpdate.getPlainPassword(), updatedUser.getPlainPassword());

            management.login(updatedUser.getUsername(), updatedUser.getPlainPassword());
            assertTrue(management.isLoggedIn(updatedUser));
        }

        /**
         * This test checks if the {@code UserManagement} updates the password of a logged in user
         * without forcing the user to log out
         */
        @Test
        void newPassword_WhenLoggedIn_ShouldUpdatePassword() {
            UserManagement management = getDefaultManagement();
            User userToUpdate = USERS.get(0);
            User updatedUser = new UserDTO(userToUpdate.getUsername(), "newPassword", null);

            management.login(userToUpdate.getUsername(), userToUpdate.getPlainPassword());
            assertTrue(management.isLoggedIn(userToUpdate));

            management.updateUserPassword(updatedUser, userToUpdate.getPlainPassword(), updatedUser.getPlainPassword());
            assertTrue(management.isLoggedIn(updatedUser));

            management.logout(updatedUser);
            assertFalse(management.isLoggedIn(updatedUser));

            management.login(updatedUser.getUsername(), updatedUser.getPlainPassword());
            assertTrue(management.isLoggedIn(updatedUser));

        }

        @Test
        void newEmail_ShouldUpdateEmailAddress() {
            UserManagement management = getDefaultManagement();
            User userToUpdate = USERS.get(0);
            User updatedUser = new UserDTO(userToUpdate.getUsername(), "", "newMail@mail.com");

            management.updateUserEmail(updatedUser, updatedUser.getEMail());

            User user = management.login(updatedUser.getUsername(), userToUpdate.getPlainPassword());
            assertTrue(management.isLoggedIn(updatedUser));
            assertEquals(user.getEMail(), updatedUser.getEMail());
        }

        @Test
        void notExisting_ShouldThrowException() {
            UserManagement management = getDefaultManagement();
            assertThrows(UserManagementException.class, () -> management.updateUserName(USER_NOT_IN_STORE, "newName"));
        }
    }
}