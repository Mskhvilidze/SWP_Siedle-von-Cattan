package de.uol.swp.server.game;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.game.message.AbstractGameMessage;
import de.uol.swp.common.game.message.NextTurnMessage;
import de.uol.swp.common.game.request.GameEndedRequestGameSession;
import de.uol.swp.common.game.request.LeaveGameRequest;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.server.exception.CanNotRejoinException;
import de.uol.swp.server.exception.UserIsNotPartOfGameSessionException;
import de.uol.swp.server.game.session.GameSession;
import de.uol.swp.server.game.session.GameSessionFactory;
import de.uol.swp.server.game.session.GameSessionManagement;
import de.uol.swp.server.game.session.GameSessionService;
import de.uol.swp.server.game.state.PlayState;
import de.uol.swp.server.usermanagement.AuthenticationService;
import de.uol.swp.server.usermanagement.UserManagement;
import de.uol.swp.server.usermanagement.store.DataBaseUserStore;
import de.uol.swp.server.usermanagement.store.UserStore;
import org.junit.jupiter.api.*;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test Class for the GameSessionService
 */

@SuppressWarnings({"PMD.DefaultPackage"})
class GameSessionServiceTest {
    static final User USER = new UserDTO("name", "password", "email@test.de");
    static final User USER2 = new UserDTO("name2", "password", "email@test.de");

    final UserStore userStore = new DataBaseUserStore(true);
    final EventBus bus = new EventBus();
    final UserManagement userManagement = new UserManagement(userStore);
    final AuthenticationService authenticationService = new AuthenticationService(bus, userManagement);
    final GameSessionFactory gameSessionFactory = mock(GameSessionFactory.class);
    final GameSessionManagement gameSessionManagement = new GameSessionManagement(gameSessionFactory);
    final GameSessionService gameSessionService = new GameSessionService(gameSessionManagement, bus, authenticationService);
    final InventoryService inventoryService = new InventoryService(bus, gameSessionService);
    final GameLobby gameLobby = new GameLobby("name", USER, false);
    final CountDownLatch lock = new CountDownLatch(1);
    Object event;

    /**
     * Handles DeadEvents detected on the EventBus
     * <p>
     * If a DeadEvent is detected the event variable of this class gets updated
     * to its event and its event is printed to the console output.
     *
     * @param event the DeadEvent detected on the EventBus
     * @since 2019-10-10
     */
    @Subscribe
    void handle(DeadEvent event) {
        this.event = event.getEvent();
        lock.countDown();
    }

    /**
     * Helper method run before each test case
     * <p>
     * This method resets the variable event to null and registers the object of
     * this class to the EventBus.
     */
    @BeforeEach
    void registerBus() {
        event = null;
        bus.register(this);
    }

    /**
     * Helper method run after each test case
     * <p>
     * This method only unregisters the object of this class from the EventBus.
     */
    @AfterEach
    void deregisterBus() {
        bus.unregister(this);
    }

    private void createGameSession(GameLobby lobby) {
        when(gameSessionFactory.create(lobby)).thenReturn(new GameSession(lobby, gameSessionService, inventoryService, bus));
        gameSessionService.createGameSession(gameLobby);
    }

    @Test
    void createGameSessionTest() {
        //TODO: Muss hier eig nur management call testen
        createGameSession(gameLobby);
        GameSession gameSession = gameSessionManagement.getGameSession("name").get();
        assertEquals("name", gameSession.getGameSessionName());
    }

    @Test
    void sendTAllInGameSession() {
        createGameSession(gameLobby);
        PlayerDTO player = PlayerDTO.create((gameLobby.getOwner()));
        NextTurnMessage message = new NextTurnMessage(gameLobby.getName(), player);
        gameSessionService.sendToAllInGameSession(gameLobby.getName(), message);
        assertTrue(event instanceof AbstractGameMessage);
        assertEquals(gameLobby.getName(), message.getGameSessionName());
        assertEquals(gameLobby.getOwner().getUsername(), message.getPlayer().getPlayerName());
        //TODO: Wird nicht benötigt weil private Methode die von anderen verwendet wird?
    }

    @Test
    void gameHasEnded_ShouldDropGameSession() {
        gameLobby.setDebugEnabled(true);
        createGameSession(gameLobby);
        bus.post(new GameEndedRequestGameSession(gameLobby.getName()));
        assertTrue(gameSessionManagement.getGameSession(gameLobby.getName()).isEmpty());
    }

    @Test
    void rejoinGameTest_ShouldBeAbleToRejoin() throws UserIsNotPartOfGameSessionException, CanNotRejoinException {
        gameLobby.joinUser(USER2);
        createGameSession(gameLobby);
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(gameLobby.getName());
        Session session = mock(Session.class);
        when(session.getUser()).thenReturn(USER2);
        gameSession.get().leaveGameSession(session);
        assertFalse(gameSession.get().getUsers().contains(USER2));
        gameSession.get().rejoinGameSession(USER2);
        assertTrue(gameSession.get().getUsers().contains(USER2));
    }

    @Test
    void rejoinGameTest_ShouldNotBeAbleToRejoin() {
        createGameSession(gameLobby);
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(gameLobby.getName());
        Assertions.assertThrows(CanNotRejoinException.class, () -> gameSession.get().rejoinGameSession(USER2));
    }

    @Nested
    class LeaveGameTests {
        @Test
        void lastRealUserInGameSession_ShouldDropGameSession() {
            gameLobby.joinUser(USER2);
            createGameSession(gameLobby);
            gameSessionManagement.getGameSession(gameLobby.getName()).get().setCurrentState(PlayState.INSTANCE);
            assertTrue(gameSessionManagement.getGameSession(gameLobby.getName()).get().getUsers().contains(USER));
            LeaveGameRequest request = new LeaveGameRequest(gameLobby.getName());
            Session session = mock(Session.class);
            request.setSession(session);
            when(session.getUser()).thenReturn(USER2);
            bus.post(request);
            when(session.getUser()).thenReturn(USER);
            bus.post(request);
            assertTrue(gameSessionManagement.getGameSession(gameLobby.getName()).isEmpty());
        }

        @Test
        void successfullyLeft_ShouldRemoveUser() {
            gameLobby.joinUser(USER2);
            createGameSession(gameLobby);
            assertTrue(gameSessionManagement.getGameSession(gameLobby.getName()).get().getUsers().contains(USER2));
            gameSessionManagement.getGameSession(gameLobby.getName()).get().setCurrentState(PlayState.INSTANCE);
            LeaveGameRequest request = new LeaveGameRequest(gameLobby.getName());
            Session session = mock(Session.class);
            request.setSession(session);
            when(session.getUser()).thenReturn(USER2);
            bus.post(request);
            assertTrue(gameSessionManagement.getGameSession(gameLobby.getName()).isPresent());
            assertFalse(gameSessionManagement.getGameSession(gameLobby.getName()).get().getUsers().contains(USER2));
        }
    }
}
