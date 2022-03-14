package de.uol.swp.server.game;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.server.game.session.GameSession;
import de.uol.swp.server.game.session.GameSessionFactory;
import de.uol.swp.server.game.session.GameSessionManagement;
import de.uol.swp.server.game.session.GameSessionService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test Class for GamesSessionManagement
 */
@SuppressWarnings({"PMD.DefaultPackage"})
class GameSessionManagementTest {
    static final UserDTO USER_DTO = new UserDTO("testUser", "test", "test@uol.de");
    static final GameLobby GAME_LOBBY = new GameLobby("testLobby", USER_DTO, false);


    final GameSessionFactory gameSessionFactory = mock(GameSessionFactory.class);
    final GameSessionManagement gameSessionManagement = new GameSessionManagement(gameSessionFactory);

    private void createGameSession(GameLobby lobby) {
        when(gameSessionFactory.create(lobby)).thenReturn(
                new GameSession(lobby, mock(GameSessionService.class), mock(InventoryService.class), mock(EventBus.class)));
        gameSessionManagement.createGameSession(lobby);
    }

    @Test
    void createGameSession() {
        createGameSession(GAME_LOBBY);
        assertTrue(gameSessionManagement.getGameSession(GAME_LOBBY.getName()).isPresent());
        assertThrows(IllegalArgumentException.class, () -> gameSessionManagement.createGameSession(GAME_LOBBY));
    }

    @Test
    void dropGameSessionNotExisting() {
        createGameSession(GAME_LOBBY);

        assertThrows(IllegalArgumentException.class, () -> gameSessionManagement.dropGameSession("name"));

        gameSessionManagement.dropGameSession(GAME_LOBBY.getName());

        assertFalse(gameSessionManagement.getGameSession(GAME_LOBBY.getName()).isPresent());
    }

    @Test
    void fetchLobby() {
        createGameSession(GAME_LOBBY);
        assertTrue(gameSessionManagement.getGameSession(GAME_LOBBY.getName()).isPresent());

    }
}
