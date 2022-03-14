package de.uol.swp.server.lobby;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.game.message.InitialBoardMessage;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.dto.LobbyOption;
import de.uol.swp.common.lobby.dto.LobbyOptions;
import de.uol.swp.common.lobby.message.UserReadyMessage;
import de.uol.swp.common.lobby.request.*;
import de.uol.swp.common.lobby.response.*;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.server.communication.UUIDSession;
import de.uol.swp.server.game.InventoryService;
import de.uol.swp.server.game.session.GameSession;
import de.uol.swp.server.game.session.GameSessionFactory;
import de.uol.swp.server.game.session.GameSessionManagement;
import de.uol.swp.server.game.session.GameSessionService;
import de.uol.swp.server.usermanagement.AuthenticationService;
import de.uol.swp.server.usermanagement.UserManagement;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import de.uol.swp.server.usermanagement.store.UserStore;
import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"UnstableApiUsage", "PMD.CommentRequired", "PMD.DefaultPackage", "PMD.MethodNamingConventions", "PMD.ClassNamingConventions", "PMD.FieldNamingConventions"})
class LobbyServiceTest {

    final static User USER = new UserDTO("name", "password", "email@test.de");
    final static User USER2 = new UserDTO("name2", "password2", "email@test.de2");
    final static User USER3 = new UserDTO("name3", "password3", "email@test.de3");
    final static User USER4 = new UserDTO("name4", "password4", "email@test.de4");
    final static User USER5 = new UserDTO("name5", "password5", "email@test.de5");
    final static Map<User, Session> usersSessions = new HashMap<>();
    final UserStore userStore = new MainMemoryBasedUserStore();
    final EventBus bus = new EventBus();
    final UserManagement userManagement = new UserManagement(userStore);
    final AuthenticationService authService = new AuthenticationService(bus, userManagement);
    final LobbyManagement lobbyManagement = new LobbyManagement();
    final GameSessionFactory gameSessionFactory = mock(GameSessionFactory.class);
    final GameSessionManagement gameSessionManagement = new GameSessionManagement(gameSessionFactory);
    final GameSessionService gameSessionService = new GameSessionService(gameSessionManagement, bus, authService);
    final InventoryService inventoryService = new InventoryService(bus, gameSessionService);
    final LobbyService lobbyService = new LobbyService(lobbyManagement, authService, gameSessionService, bus);
    final CountDownLatch lock = new CountDownLatch(1);
    Object event;

    @BeforeAll
    static void sessions() {
        usersSessions.put(USER, UUIDSession.create(USER));
        usersSessions.put(USER2, UUIDSession.create(USER2));
        usersSessions.put(USER3, UUIDSession.create(USER3));
        usersSessions.put(USER4, UUIDSession.create(USER4));
        usersSessions.put(USER5, UUIDSession.create(USER5));
    }

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

    @Test
    void onSetUserReadyRequest_ShouldUpdateLobby() {
        lobbyManagement.createLobby("name", USER, false);
        Lobby lobby = lobbyManagement.getLobby("name").get();
        lobby.joinUser(USER2);
        SetUserReadyRequest request = new SetUserReadyRequest("name", true);
        Session session = mock(Session.class);
        request.setSession(session);
        when(session.getUser()).thenReturn(USER2);

        bus.post(request);
        assertEquals(request.isUserReady(), lobby.getReadyUsers().contains(USER2));
        assertTrue(event instanceof UserReadyMessage);
    }

    /**
     * This test checks if the {@code LobbyService} update methods and the {@code GameLobby} update methods work correctly
     */
    @Test
    void updateLobby() throws InterruptedException {
        lobbyManagement.createLobby("name", USER, false);
        Lobby lobby = lobbyManagement.getLobby("name").get();
        assertEquals(LobbyOptions.LOBBY_SIZE.getDefaultValue(), lobby.getLobbySize());
        assertEquals(LobbyOptions.NUM_VICTORY_POINTS.getDefaultValue(), lobby.getNumVP());
        assertEquals(LobbyOptions.TIMER_DURATION.getDefaultValue(), lobby.getTimerDuration());

        updateLobby(lobby.getName(), LobbyOptions.LOBBY_SIZE, 3, lobby.getOwner());
        assertEquals(3, lobby.getLobbySize());

        updateLobby(lobby.getName(), LobbyOptions.NUM_VICTORY_POINTS, 5, lobby.getOwner());
        assertEquals(5, lobby.getNumVP());

        updateLobby(lobby.getName(), LobbyOptions.TIMER_DURATION, 58, lobby.getOwner());
        assertEquals(58, lobby.getTimerDuration());

        updateLobby(lobby.getName(), LobbyOptions.PRIVATE_LOBBY, true, lobby.getOwner());
        assertTrue(lobby.isPrivateLobby());
    }

    /**
     * Helper method to update a lobby option
     */
    private <T extends Serializable> void updateLobby(String lobbyName, LobbyOption<T> option, T newValue,
                                                      User sender) throws InterruptedException {
        final UpdateLobbyRequest<T> request = new UpdateLobbyRequest<>(lobbyName, option, newValue);
        request.setSession(usersSessions.get(sender));
        bus.post(request);
        lock.await(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    void retrieveLobbyList_ShouldReturnLobbySet() throws InterruptedException {
        lobbyManagement.createLobby("name", USER, false);
        RetrieveLobbyListRequest retrieveLobbyListRequest = new RetrieveLobbyListRequest();
        bus.post(retrieveLobbyListRequest);
        lock.await(1000, TimeUnit.MILLISECONDS);

        assertTrue(event instanceof RetrieveLobbyListResponse);
        assertEquals(new HashSet<>(lobbyManagement.getLobbies().values()), ((RetrieveLobbyListResponse) event).getLobbies());
    }

    @Test
    void onKickPlayerRequest_ShouldUpdateLobby() throws InterruptedException {
        lobbyManagement.createLobby("name", USER, false);
        Lobby lobby = lobbyManagement.getLobby("name").get();
        lobby.joinUser(USER2);
        LobbyKickRequest request = new LobbyKickRequest(lobby.getName(), UserDTO.create(USER2));
        authService.createSessionForTest(USER2);
        Session session = mock(Session.class);
        request.setSession(session);
        when(session.getUser()).thenReturn(USER);

        assertTrue(lobby.getUsers().contains(request.getKickUser()));
        lobbyService.onKickPlayerRequest(request);
        assertFalse(lobby.getUsers().contains(request.getKickUser()));
    }

    @Test
    void kickSenderNotOwner_ShouldNotUpdateLobby() throws InterruptedException {
        lobbyManagement.createLobby("name", USER, false);
        Lobby lobby = lobbyManagement.getLobby("name").get();
        lobby.joinUser(USER2);
        LobbyKickRequest request = new LobbyKickRequest(lobby.getName(), UserDTO.create(USER2));
        authService.createSessionForTest(USER2);
        Session session = mock(Session.class);
        request.setSession(session);
        when(session.getUser()).thenReturn(USER2);

        assertTrue(lobby.getUsers().contains(request.getKickUser()));
        lobbyService.onKickPlayerRequest(request);
        assertTrue(lobby.getUsers().contains(request.getKickUser()));
    }

    @Test
    void gameHasEnded_ShouldDropLobby() {
        lobbyManagement.createLobby("name", USER, false);
        assertTrue(lobbyManagement.getLobby("name").isPresent());
        lobbyManagement.getLobby("name").get().setDebugEnabled(true);
        bus.post(new GameEndedRequestLobby("name"));
        assertTrue(lobbyManagement.getLobby("name").isEmpty());
    }

    @Nested
    class createLobbyTests {
        @Test
        void nameAlreadyTaken_ShouldReturnError() throws InterruptedException {
            bus.post(new CreateLobbyRequest("name", (UserDTO) USER, false));
            lock.await(1000, TimeUnit.MILLISECONDS);

            assertEquals(USER, lobbyManagement.getLobby("name").get().getOwner());

            bus.post(new CreateLobbyRequest("name", (UserDTO) USER2, false));
            lock.await(1000, TimeUnit.MILLISECONDS);

            assertTrue(event instanceof LobbyNameAlreadyTakenResponse);
            assertEquals(USER, lobbyManagement.getLobby("name").get().getOwner());
        }

        @Test
        void successfullyCreated_ShouldExist() throws InterruptedException {
            bus.post(new CreateLobbyRequest("name", (UserDTO) USER, false));
            lock.await(1000, TimeUnit.MILLISECONDS);
            Lobby lobby = lobbyManagement.getLobby("name").get();
            assertAll(() -> {
                assertEquals("name", lobby.getName());
                assertEquals(USER, lobby.getOwner());
                assertTrue(lobby.getUsers().contains(USER));
                assertFalse(lobby.isPrivateLobby());
            });
        }

        @Test
        void successfullyCreated_ShouldReturnCorrectResponse() throws InterruptedException {
            bus.post(new CreateLobbyRequest("name", (UserDTO) USER, false));
            lock.await(1000, TimeUnit.MILLISECONDS);
            Lobby lobby = lobbyManagement.getLobby("name").get();

            if (!lobby.isPrivateLobby()) {
                //assertTrue(event instanceof LobbyCreatedMessage);
            }
            assertTrue(event instanceof LobbyJoinSuccessfulResponse);
        }
    }

    @Nested
    class JoinLobbyTests {
        @Test
        void successfullyJoined_ShouldContainUser() throws InterruptedException {

            lobbyManagement.createLobby("name", USER, false);

            joinLobby("name", USER2);
            assertTrue(lobbyManagement.getLobby("name").get().getUsers().contains(USER2));
            assertTrue(event instanceof LobbyJoinSuccessfulResponse);
        }

        @Test
        void fullLobbyJoined_ShouldReturnError() throws InterruptedException {

            lobbyManagement.createLobby("name", USER, false);

            joinLobby("name", USER2);
            assertTrue(lobbyManagement.getLobby("name").get().getUsers().contains(USER2));

            joinLobby("name", USER3);
            assertTrue(lobbyManagement.getLobby("name").get().getUsers().contains(USER3));

            joinLobby("name", USER4);
            assertTrue(lobbyManagement.getLobby("name").get().getUsers().contains(USER4));

            //lobby is full
            joinLobby("name", USER5);
            assertFalse(lobbyManagement.getLobby("name").get().getUsers().contains(USER5));
            assertTrue(event instanceof LobbyFullResponse);
        }

        @Test
        void lobbyNotFound_ShouldReturnError() throws InterruptedException {
            lobbyManagement.createLobby("name", USER, false);

            //Lobby not found
            joinLobby("fail", USER2);
            assertFalse(lobbyManagement.getLobby("name").get().getUsers().contains(USER2));
            assertTrue(event instanceof LobbyNotFoundResponse);
        }

        @Test
        void alreadyJoined_ShouldReturnError() throws InterruptedException {
            lobbyManagement.createLobby("name", USER, false);

            //Lobby not found
            joinLobby("name", USER);
            assertTrue(event instanceof LobbyAlreadyJoinedResponse);
        }

        @Test
        void canNotJoin_GameAlreadyStarted() throws InterruptedException {
            lobbyManagement.createLobby("name", USER, false);
            GameLobby lobby = lobbyManagement.getLobby("name").get();
            lobby.setGameHasStarted(true);
            joinLobby("name", USER2);
            assertTrue(event instanceof GameAlreadyStartedResponse);
        }

        //private method to join a lobby
        private void joinLobby(String lobbyName, User user) throws InterruptedException {
            JoinLobbyRequest joinLobbyRequest = new JoinLobbyRequest(lobbyName);
            Session session = mock(Session.class);
            joinLobbyRequest.setSession(session);
            when(session.getUser()).thenReturn(user);

            bus.post(joinLobbyRequest);
            lock.await(1000, TimeUnit.MILLISECONDS);
        }
    }

    @Nested
    class LeaveLobbyTests {
        @Test
        void lastUserInLobby_ShouldDropLobby() throws InterruptedException {
            lobbyManagement.createLobby("name", USER, false);
            leaveLobby("name", USER);
            assertTrue(lobbyManagement.getLobby("name").isEmpty());
            //assertTrue(event instanceof LobbyDroppedMessage);
            //TODO: Implement list of events instead of just last
        }

        @Test
        void successfullyLeft_ShouldRemoveUser() throws InterruptedException {
            lobbyManagement.createLobby("name", USER, false);
            Lobby lobby = lobbyManagement.getLobby("name").get();
            lobby.joinUser(USER2);
            leaveLobby("name", USER);
            assertFalse(lobby.getUsers().contains(USER));
        }

        private void leaveLobby(String lobbyName, User user) throws InterruptedException {
            LeaveLobbyRequest request = new LeaveLobbyRequest(lobbyName);
            Session session = mock(Session.class);
            request.setSession(session);
            when(session.getUser()).thenReturn(user);

            bus.post(request);
            lock.await(1000, TimeUnit.MILLISECONDS);
        }
    }

    @Nested
    class StartGameSessionTests {
        @Test
        void lobbyCanBeStarted_ShouldStartGameSession() throws InterruptedException {
            lobbyManagement.createLobby("name", USER, false);
            GameLobby gameLobby = lobbyManagement.getLobby("name").get();
            when(gameSessionFactory.create(gameLobby)).thenReturn(new GameSession(gameLobby, gameSessionService, inventoryService, bus));
            gameLobby.setUserReady(USER, true);
            gameLobby.joinUser(USER2);
            gameLobby.setUserReady(USER2, true);
            gameLobby.joinUser(USER3);
            gameLobby.setUserReady(USER3, true);
            gameLobby.joinUser(USER4);
            gameLobby.setUserReady(USER4, true);

            startGameSession(gameLobby.getName(), USER);
            lock.await(1000, TimeUnit.MILLISECONDS);
            assertEquals(InitialBoardMessage.class, event.getClass());
            GameSession gameSession = gameSessionManagement.getGameSession(gameLobby.getName()).get();
            assertEquals(gameLobby.getName(), gameSession.getGameSessionName());
        }

        @Test
        void lobbyCanNotBeStarted_ShouldNotStartGameSession() {
            lobbyManagement.createLobby("name", USER, false);
            startGameSession("name", USER);
            Optional<GameSession> gameSession = gameSessionManagement.getGameSession("name");
            assertThrows(NoSuchElementException.class, gameSession::get);
        }

        @Test
        void lobbyDoesNotExist_ShouldNotStartGameSession() {
            startGameSession("name", USER);
            Optional<GameSession> gameSession = gameSessionManagement.getGameSession("name");
            assertThrows(NoSuchElementException.class, gameSession::get);
        }

        @Test
        void senderNotOwner_ShouldNotStartGameSession() {
            lobbyManagement.createLobby("name", USER, false);
            GameLobby gameLobby = lobbyManagement.getLobby("name").get();
            when(gameSessionFactory.create(gameLobby)).thenReturn(new GameSession(gameLobby, gameSessionService, inventoryService, bus));
            gameLobby.setUserReady(USER, true);
            gameLobby.joinUser(USER2);
            gameLobby.setUserReady(USER2, true);
            gameLobby.joinUser(USER3);
            gameLobby.setUserReady(USER3, true);
            gameLobby.joinUser(USER4);
            gameLobby.setUserReady(USER4, true);

            startGameSession("name", USER2);
            Optional<GameSession> gameSession = gameSessionManagement.getGameSession("name");
            assertThrows(NoSuchElementException.class, gameSession::get);
        }

        private void startGameSession(String lobbyName, User user) {
            StartGameSessionRequest request = new StartGameSessionRequest(lobbyName);
            Session session = mock(Session.class);
            request.setSession(session);
            when(session.getUser()).thenReturn(user);
            bus.post(request);
        }
    }
}
