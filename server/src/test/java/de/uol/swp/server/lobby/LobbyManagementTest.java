package de.uol.swp.server.lobby;


import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class LobbyManagementTest {
    static final UserDTO userDTO = new UserDTO("testUser", "test", "test@uol.de");
    static final LobbyDTO lobbyDTO = new LobbyDTO("testLobby", userDTO, false);
    static final List<LobbyDTO> lobbies;

    static {
        lobbies = new ArrayList<>();
        lobbies.add(lobbyDTO);
    }

    final LobbyManagement lobbyManagement = new LobbyManagement();

    @Test
    void createLobby() {
        Lobby lobby = lobbies.get(0);
        String lobbyName = lobby.getName();
        boolean privateLobby = lobby.isPrivateLobby();

        lobbyManagement.createLobby(lobbyName, userDTO, privateLobby);
        assertTrue(lobbyManagement.getLobby(lobbyName).isPresent());

        assertThrows(IllegalArgumentException.class, () -> lobbyManagement.createLobby(lobbyName, userDTO, privateLobby));
    }

    @Test
    void dropLobbyNotExisting() {
        Lobby lobby = lobbies.get(0);

        lobbyManagement.createLobby(lobby.getName(), userDTO, lobby.isPrivateLobby());

        assertThrows(IllegalArgumentException.class, () -> lobbyManagement.dropLobby("name"));

        lobbyManagement.dropLobby(lobby.getName());

        assertFalse(lobbyManagement.getLobby(lobby.getName()).isPresent());
    }

    @Test
    void getLobby() {
        lobbyManagement.createLobby("one", userDTO, false);
        lobbyManagement.createLobby("two", userDTO, true);

        assertTrue(lobbyManagement.getLobbies().containsKey("one"));
        assertTrue(lobbyManagement.getLobby("one").isPresent());

        assertTrue(lobbyManagement.getPrivateLobbies().containsKey("two"));
        assertTrue(lobbyManagement.getLobby("two").isPresent());
    }

    @Test
    void toggleLobbyPrivacy() {
        lobbyManagement.createLobby("lobby", userDTO, false);
        boolean containsLobby = lobbyManagement.toggleLobbyPrivacy("lobby");
        assertTrue(containsLobby && lobbyManagement.getPrivateLobbies().containsKey("lobby"));

        containsLobby = lobbyManagement.toggleLobbyPrivacy("lobby");
        assertTrue(containsLobby && lobbyManagement.getLobbies().containsKey("lobby"));

        containsLobby = lobbyManagement.toggleLobbyPrivacy("lobby2");
        assertFalse(containsLobby);
    }
}
