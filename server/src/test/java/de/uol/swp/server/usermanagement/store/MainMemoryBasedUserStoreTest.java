package de.uol.swp.server.usermanagement.store;

import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.user.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.CommentRequired", "PMD.LinguisticNaming", "PMD.MethodNamingConventions", "PMD.DefaultPackage"})
class MainMemoryBasedUserStoreTest {

    static final int NO_USERS = 10;
    static final List<UserDTO> USERS;

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

    MainMemoryBasedUserStore getDefaultStore() {
        MainMemoryBasedUserStore store = new MainMemoryBasedUserStore();
        List<UserDTO> users = getDefaultUsers();
        users.forEach(u -> store.createUser(u.getUsername(), u.getPlainPassword(), u.getEMail()));
        return store;
    }

    @Test
    void findUserByName() {
        // arrange
        UserStore store = getDefaultStore();
        User userToCreate = getDefaultUsers().get(0);

        // act
        Optional<User> userFound = store.findUser(userToCreate.getUsername());

        // assert
        assertTrue(userFound.isPresent());
        assertEquals(userToCreate, userFound.get());
        assertNull(userFound.get().getPlainPassword());
    }

    @Test
    void findUserByName_NotFound() {
        UserStore store = getDefaultStore();
        User userToFind = getDefaultUsers().get(0);

        Optional<User> userFound = store.findUser("öööö" + userToFind.getUsername());

        assertFalse(userFound.isPresent());
    }

    @Test
    void findUserByNameAndPassword() {
        UserStore store = getDefaultStore();
        User userToCreate = getDefaultUsers().get(1);
        store.createUser(userToCreate.getUsername(), userToCreate.getPlainPassword(), userToCreate.getEMail());

        Optional<User> userFound = store.findUser(userToCreate.getUsername(), userToCreate.getPlainPassword());

        assertTrue(userFound.isPresent());
        assertEquals(userToCreate, userFound.get());
        assertNull(userFound.get().getPlainPassword());
    }

    @Test
    void findUserByNameAndPassword_NotFound() {
        UserStore store = getDefaultStore();
        User userToFind = getDefaultUsers().get(0);

        Optional<User> userFound = store.findUser(userToFind.getUsername(), "");

        assertFalse(userFound.isPresent());
    }

    @Test
    void findUserByNameAndPassword_EmptyUser_NotFound() {
        UserStore store = getDefaultStore();

        Optional<User> userFound = store.findUser(null, "");

        assertFalse(userFound.isPresent());
    }


    @Test
    void overwriteUser() {
        UserStore store = getDefaultStore();
        User userToCreate = getDefaultUsers().get(1);
        store.createUser(userToCreate.getUsername(), userToCreate.getPlainPassword(), userToCreate.getEMail());
        store.createUser(userToCreate.getUsername(), userToCreate.getPlainPassword(), userToCreate.getEMail());

        Optional<User> userFound = store.findUser(userToCreate.getUsername(), userToCreate.getPlainPassword());

        assertEquals(NO_USERS, store.getAllUsers().size());
        assertTrue(userFound.isPresent());
        assertEquals(userToCreate, userFound.get());

    }


    @Test
    void updateUserName() {
        UserStore store = getDefaultStore();
        User userToUpdate = getDefaultUsers().get(2);
        String userID = userToUpdate.getUserId();
        assertThrows(UnsupportedOperationException.class, () -> store.updateUserName(userID, "Test"));
    }

    @Test
    void changePassword() {
        UserStore store = getDefaultStore();
        User userToUpdate = getDefaultUsers().get(2);
        String userName = userToUpdate.getUsername();
        assertThrows(UnsupportedOperationException.class, () -> store.updateUserPassword(userName, "test"));
    }

    @Test
    void dropUser() {
        UserStore store = getDefaultStore();
        User userToRemove = getDefaultUsers().get(3);

        store.removeUser(userToRemove.getUsername());

        Optional<User> userFound = store.findUser(userToRemove.getUsername());

        assertFalse(userFound.isPresent());
    }

    @Test
    void createEmptyUser(){
        UserStore store = getDefaultStore();

        assertThrows(IllegalArgumentException.class,
                () -> store.createUser("","","")
                );
    }

    @Test
    void getAllUsers() {
        UserStore store = getDefaultStore();
        List<UserDTO> allUsers = getDefaultUsers();

        List<User> allUsersFromStore = store.getAllUsers();

        allUsersFromStore.forEach(u -> assertNull(u.getPlainPassword()));
        Collections.sort(allUsersFromStore);
        assertEquals(allUsers, allUsersFromStore);
    }

    @Test
    void getAllPlayerStatistics() {
        UserStore store = getDefaultStore();
        User user = getDefaultUsers().get(1);

        assertThrows(UnsupportedOperationException.class, ()-> store.getAllPlayerStatistics());
    }

    @Test
    void getPlayerInfoData() {
        UserStore store = getDefaultStore();
        User user = getDefaultUsers().get(2);

        String userName = user.getUsername();
        assertThrows(UnsupportedOperationException.class, () -> store.getPlayerInfoData(userName));
    }

    @Test
    void updateGamesWon() {
        UserStore store = getDefaultStore();
        User user = getDefaultUsers().get(2);
        String userName = user.getUsername();

        assertThrows(UnsupportedOperationException.class, ()-> store.updateGamesWon(userName));
        assertThrows(UnsupportedOperationException.class, ()-> store.getNoOfGamesWon(userName));
    }

    @Test
    void updateGamesLost() {
        UserStore store = getDefaultStore();
        User user = getDefaultUsers().get(2);
        String userName = user.getUsername();

        assertThrows(UnsupportedOperationException.class, ()-> store.updateGamesLost(userName));
        assertThrows(UnsupportedOperationException.class, ()-> store.getNoOfGamesLost(userName));
    }

}