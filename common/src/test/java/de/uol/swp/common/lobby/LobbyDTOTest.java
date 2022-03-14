package de.uol.swp.common.lobby;

import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.user.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Class for the UserDTO
 *
 * @author Marco Grawunder
 * @since 2019-10-08
 */

@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops"})
class LobbyDTOTest {

    static final User DEFAULT_USER = new UserDTO("marco", "marco", "marco@grawunder.de");
    static final User NOT_IN_LOBBY_USER = new UserDTO("no", "marco", "no@grawunder.de");

    static final int NO_USERS = 10;
    static final List<User> USERS;

    static {
        USERS = new ArrayList<>();
        for (int i = 0; i < NO_USERS; i++) {
            USERS.add(new UserDTO("marco" + i, "marco" + i, "marco" + i + "@grawunder.de"));
        }
        Collections.sort(USERS);
    }

    /**
     * This test checks whether a lobby is created correctly
     * <p>
     * If the variables are not set correctly the test fails
     *
     * @since 2019-10-08
     */
    @Test
    void createLobbyTest() {
        Lobby lobby = new LobbyDTO("test", DEFAULT_USER, true);

        assertEquals("test", lobby.getName());
        assertEquals(1, lobby.getUsers().size());
        assertEquals(DEFAULT_USER, lobby.getUsers().iterator().next());
        assertTrue(lobby.isPrivateLobby());
    }

    /**
     * This test checks whether a user can join a lobby
     * <p>
     * The test fails if the size of the user list of the lobby does not get bigger
     * or a user who joined is not in the list.
     *
     * @since 2019-10-08
     */
    @Test
    void joinUserLobbyTest() {
        Lobby lobby = new LobbyDTO("test", DEFAULT_USER, true);

        lobby.joinUser(USERS.get(0));
        assertEquals(2, lobby.getUsers().size());
        assertTrue(lobby.getUsers().contains(USERS.get(0)));

        lobby.joinUser(USERS.get(0));
        assertEquals(2, lobby.getUsers().size());

        lobby.joinUser(USERS.get(1));
        assertEquals(3, lobby.getUsers().size());
        assertTrue(lobby.getUsers().contains(USERS.get(1)));
    }

    /**
     * This test checks whether a user can leave a lobby
     * <p>
     * The test fails if the size of the user list of the lobby does not get smaller
     * or the user who left is still in the list.
     *
     * @since 2019-10-08
     */
    @Test
    void leaveUserLobbyTest() {
        Lobby lobby = new LobbyDTO("test", DEFAULT_USER, true);
        USERS.forEach(lobby::joinUser);

        assertEquals(lobby.getUsers().size(), USERS.size() + 1);
        lobby.leaveUser(USERS.get(5));

        assertEquals(lobby.getUsers().size(), USERS.size() + 1 - 1);
        assertFalse(lobby.getUsers().contains(USERS.get(5)));
    }

    /**
     * Test to check if the owner can leave the Lobby correctly
     * <p>
     * This test fails if the owner field is not updated if the owner leaves the
     * lobby or if he still is in the user list of the lobby.
     *
     * @since 2019-10-08
     */
    @Test
    void removeOwnerFromLobbyTest() {
        Lobby lobby = new LobbyDTO("test", DEFAULT_USER, true);
        USERS.forEach(lobby::joinUser);

        lobby.leaveUser(DEFAULT_USER);

        assertNotEquals(DEFAULT_USER, lobby.getOwner());
        assertTrue(USERS.contains(lobby.getOwner()));

    }

    /**
     * This checks if the owner of a lobby can be updated and if he has joined the lobby
     * <p>
     * This test fails if the owner cannot be updated or does not have to be joined
     *
     * @since 2019-10-08
     */
    @Test
    void updateOwnerTest() {
        Lobby lobby = new LobbyDTO("test", DEFAULT_USER, true);
        USERS.forEach(lobby::joinUser);

        lobby.updateOwner(USERS.get(6));
        assertEquals(lobby.getOwner(), USERS.get(6));

        assertThrows(IllegalArgumentException.class, () -> lobby.updateOwner(NOT_IN_LOBBY_USER));
    }

    /**
     * This checks if a user of a lobby can be set to ready
     * <p>
     * This test fails if a user cannot be set to ready
     */
    @Test
    void setUserReadyTest() {
        Lobby lobby = new LobbyDTO("test", DEFAULT_USER, true);
        int readyCount = 1;
        assertEquals(readyCount, lobby.getReadyUsers().size());
        USERS.forEach(lobby::joinUser);
        assertEquals(readyCount, lobby.getReadyUsers().size());
        for (User user : USERS) {
            lobby.setUserReady(user, true);
            readyCount++;
        }
        assertEquals(readyCount, lobby.getReadyUsers().size());
    }

    /**
     * This checks if a user of a lobby can be set to not ready
     * <p>
     * This test fails if a user cannot be set to not ready
     */
    @Test
    void setUserNotReadyTest() {
        Lobby lobby = new LobbyDTO("test", DEFAULT_USER, true);
        int readyCount = 1;
        USERS.forEach(lobby::joinUser);
        for (User user : USERS) {
            lobby.setUserReady(user, true);
            readyCount++;
        }
        assertEquals(readyCount, lobby.getReadyUsers().size());

        lobby.setUserReady(USERS.get(7), false);
        readyCount--;
        assertEquals(readyCount, lobby.getReadyUsers().size());

        lobby.setUserReady(USERS.get(7), true);
        readyCount++;
        for (User user : USERS) {
            lobby.setUserReady(user, false);
            readyCount--;
        }
        assertEquals(readyCount, lobby.getReadyUsers().size());
    }

    /**
     * This checks if a lobby can be started if every user in the lobby is ready
     * <p>
     * This test fails if not every user in the lobby is ready
     */
    @Test
    void checkLobbyCanBeStartedTest() {
        Lobby lobby = new LobbyDTO("test", DEFAULT_USER, true);
        assertFalse(lobby.canLobbyBeStarted());
        lobby.setUserReady(DEFAULT_USER, true);
        assertTrue(lobby.canLobbyBeStarted());

        USERS.forEach(lobby::joinUser);
        assertFalse(lobby.canLobbyBeStarted());
        for (User user : USERS) {
            lobby.setUserReady(user, true);
        }
        assertTrue(lobby.canLobbyBeStarted());
    }
}
