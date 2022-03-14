package de.uol.swp.client.lobby;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.client.main.tab.SessionTab;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.game.request.RejoinGameSessionRequest;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.lobby.dto.LobbyOption;
import de.uol.swp.common.lobby.dto.LobbyOptions;
import de.uol.swp.common.lobby.message.LobbyUpdatedMessage;
import de.uol.swp.common.lobby.message.UserJoinedLobbyMessage;
import de.uol.swp.common.lobby.message.UserLeftLobbyMessage;
import de.uol.swp.common.lobby.message.UserReadyMessage;
import de.uol.swp.common.lobby.request.*;
import de.uol.swp.common.lobby.response.LobbyLeftSuccessfulResponse;
import de.uol.swp.common.user.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test for LobbyService
 */
@SuppressWarnings("UnstableApiUsage")
class LobbyServiceTest {
    static final Logger LOG = LogManager.getLogger(LobbyServiceTest.class);
    final User defaultUser = new UserDTO("Test", "test", "test@test.de");
    final Lobby defaultLobby = new LobbyDTO("TestLobby", defaultUser, true);
    final EventBus bus = new EventBus();
    final LobbyService lobbyService = new LobbyService(bus);
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
        LOG.info("Dead event: {}\n", this.event);
        lock.countDown();
    }

    /**
     * Helper method run before each test case
     * <p>
     * This method resets the variable event to null and registers the object of
     * this class to the EventBus.
     *
     * @since 2019-10-10
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
     *
     * @since 2019-10-10
     */
    @AfterEach
    void deregisterBus() {
        bus.unregister(this);
    }

    /**
     * Test for the addLobby method
     * <p>
     * This test adds a lobby to the lobbies map of the lobby service and checks if the lobby has been added.
     * <p>
     * The test fails if the lobby didn't get added to the map.
     */
    @Test
    void addLobbyToMap_ShouldAssertTrue() {
        lobbyService.addLobby(defaultLobby.getName(), mock(LobbyPresenter.class));
        assertTrue(lobbyService.getLobbies().containsKey(defaultLobby.getName()));
    }

    // -------------------------------------------------------------------------------
    // Subscribe Tests
    // -------------------------------------------------------------------------------

    /**
     * Test for the {@code UserJoinedLobbyMessage}
     * <p>
     * This test verifies that the {@link LobbyPresenter#addLobbyUser(User) addLobbyUser(User)} method has been called.
     * <p>
     * The test fails if the {@code addLobbyUser(User)} method didn't get called.
     */
    @Test
    @DisplayName("UserJoinedLobbyMessage should call addLobbyUser(User)")
    void onUserJoinedLobbyMessage_ShouldCallAddLobbyUser() {
        LobbyPresenter presenter = mock(LobbyPresenter.class);
        lobbyService.addLobby(defaultLobby.getName(), presenter);
        UserJoinedLobbyMessage message = new UserJoinedLobbyMessage(defaultLobby.getName(), (UserDTO) defaultUser);
        bus.post(message);
        verify(presenter, times(1)).addLobbyUser(message.getUser());
    }

    /**
     * Test for the {@code UserLeftLobbyMessage}
     * <p>
     * This test verifies that the {@link LobbyPresenter#removeLobbyUser(User) removeLobbyUser(User)} method has been called.
     * <p>
     * The test fails if the {@code removeLobbyUser(User)} method didn't get called.
     */
    @Test
    @DisplayName("UserLeftLobbyMessage should call removeLobbyUser(User)")
    void onUserLeftLobbyMessage_ShouldCallRemoveLobbyUser() {
        LobbyPresenter presenter = mock(LobbyPresenter.class);
        lobbyService.addLobby(defaultLobby.getName(), presenter);
        UserLeftLobbyMessage message = new UserLeftLobbyMessage(defaultLobby.getName(), (UserDTO) defaultUser);
        bus.post(message);
        verify(presenter, times(1)).removeLobbyUser(message.getUser());
    }

    /**
     * Test for the {@code UserJoinedLobbyMessage}
     * <p>
     * This test verifies that the {@link LobbyPresenter#setUserReady(User, boolean) setUserReady(User, boolean)} method
     * has been called.
     * <p>
     * The test fails if the {@code setUserReady(User, boolean)} method didn't get called.
     */
    @Test
    @DisplayName("UserReadyMessage should call setUserReady(User, boolean)")
    void onUserReadyMessage_ShouldCallSetUserReady() {
        LobbyPresenter presenter = mock(LobbyPresenter.class);
        lobbyService.addLobby(defaultLobby.getName(), presenter);
        UserReadyMessage message = new UserReadyMessage(defaultLobby.getName(), (UserDTO) defaultUser, true);
        bus.post(message);
        verify(presenter, times(1)).setUserReady(message.getUser(), true);
    }

    /**
     * Test for the {@code LobbyUpdatedMessage}
     * <p>
     * This test verifies that the {@link LobbyPresenter#updateLobby updateLobby} method has been called.
     * <p>
     * The test fails if the {@code updateLobby} method didn't get called.
     */
    @Test
    @DisplayName("LobbyUpdatedMessage should call updateLobby()")
    void onLobbyUpdatedMessage_ShouldCallUpdateLobby() {
        LobbyPresenter presenter = mock(LobbyPresenter.class);
        lobbyService.addLobby(defaultLobby.getName(), presenter);
        LobbyUpdatedMessage<Integer> message = new LobbyUpdatedMessage<>(defaultLobby.getName(), LobbyOptions.LOBBY_SIZE, 3);
        bus.post(message);
        verify(presenter, times(1)).updateLobby(message.getOption(), message.getNewValue());
    }

    /**
     * First test for the {@code LobbyLeftSuccessfulResponse}
     * <p>
     * This test first adds a lobby to the lobbies map of the lobby service. Afterwards it posts a
     * {@code LobbyLeftSuccessfulResponse} on the event bus and then checks if the lobby has been removed
     * from the lobbies map.
     * <p>
     * The test fails if the lobby didn't get removed from the map.
     */
    @Test
    @DisplayName("LobbyLeftSuccessfulResponse should remove lobby from LobbyService")
    void lobbyStillInMapAfterLeave_ShouldAssertFalse() {
        LobbyPresenter presenter = mock(LobbyPresenter.class);
        lobbyService.addLobby(defaultLobby.getName(), presenter);
        LobbyLeftSuccessfulResponse response = new LobbyLeftSuccessfulResponse(defaultLobby.getName());
        bus.post(response);

        assertFalse(lobbyService.getLobbies().containsKey(defaultLobby.getName()));
    }

    /**
     * Second test for the {@code LobbyLeftSuccessfulResponse}
     * <p>
     * This test verifies that the {@link SessionTab#closeTab()} method has been called.
     * <p>
     * The test fails if the {@code closeTab()} method didn't get called.
     */
    @Test
    @DisplayName("LobbyLeftSuccessfulResponse should call closeTab()")
    void onLobbyLeftSuccessfulResponse_ShouldCallCloseTab() {
        LobbyPresenter presenter = mock(LobbyPresenter.class);
        SessionTab tab = mock(SessionTab.class);
        LobbyContainer container = new LobbyContainer(presenter, tab);
        lobbyService.addLobby(defaultLobby.getName(), container);
        LobbyLeftSuccessfulResponse response = new LobbyLeftSuccessfulResponse(defaultLobby.getName());
        bus.post(response);

        verify(tab, times(1)).closeTab();
    }

    // -------------------------------------------------------------------------------
    // Request Tests
    // -------------------------------------------------------------------------------

    /**
     * Subroutine used for tests that need a created lobby
     * <p>
     * This subroutine creates a new LobbyService object registered to the EventBus
     * of this test class and class the objects createNewLobby method for the default lobby.
     *
     * @throws InterruptedException thrown by lock.await()
     * @since 2020-12-13
     */
    private void createLobby() throws InterruptedException {
        lobbyService.createNewLobby(defaultLobby.getName(), (UserDTO) defaultLobby.getOwner(), defaultLobby.isPrivateLobby());
        lock.await(1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Test for the createLobby routine
     * <p>
     * This test first calls the createLobby subroutine. Afterwards it checks if a
     * CreateLobbyRequest object got posted to the EventBus and if its content is the
     * default lobby information.
     * The test fails if any of the checks fail.
     *
     * @since 2020-12-13
     */
    @Test
    @DisplayName("CreateLobbyRequest should contain the correct lobby name, owner and private flag")
    void createLobbyRequest_ShouldContainCorrectInfo() throws InterruptedException {
        createLobby();
        assertTrue(event instanceof CreateLobbyRequest);

        CreateLobbyRequest request = (CreateLobbyRequest) event;
        assertEquals(request.getLobbyName(), defaultLobby.getName());
        assertEquals(request.getOwner(), defaultLobby.getOwner());
        assertEquals(request.isPrivateLobby(), defaultLobby.isPrivateLobby());
    }

    /**
     * Test for the joinLobby routine
     * <p>
     * This test creates a new LobbyService object registered to the EventBus of this test class. It then
     * calls the joinLobby function of the object using the defaultLobby and defaultUser as parameters
     * and waits for it to post an LobbyJoinUserRequest object on the EventBus. It then
     * checks if authorization is needed to join the lobby.
     * The test fails if any of the checks fail.
     *
     * @since 2020-12-13
     */
    @Test
    @DisplayName("JoinLobbyRequest should contain the correct lobby name")
    void joinLobbyRequest_ShouldContainCorrectInfo() {
        lobbyService.joinLobby(defaultLobby.getName());

        assertTrue(event instanceof JoinLobbyRequest);

        JoinLobbyRequest request = (JoinLobbyRequest) event;
        assertEquals(request.getLobbyName(), defaultLobby.getName());
    }

    /**
     * Test for the leaveLobby method
     * <p>
     * This test first calls the lobbyLeave subroutine. Afterwards it checks if a
     * LobbyLeaveUserRequest object got posted to the EventBus and if its content is the
     * default users information.
     * <p>
     * The test fails if any of the checks fail.
     */
    @Test
    @DisplayName("LeaveLobbyRequest should contain the correct lobby name")
    void leaveLobbyRequest_ShouldContainCorrectInfo() {
        lobbyService.leaveLobby(defaultLobby.getName());

        assertTrue(event instanceof LeaveLobbyRequest);
        LeaveLobbyRequest request = (LeaveLobbyRequest) event;
        assertEquals(request.getLobbyName(), defaultLobby.getName());
    }

    /**
     * Subroutine for tests. For users who are ready
     * <p>
     * This subroutine creates a new LobbyService object that is registered in the EventBus
     */
    private void setUserReady() throws InterruptedException {
        lobbyService.setUserReady(defaultLobby.getName(), true);
        lock.await(1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Test for the setUserReady method
     * <p>
     * This test first calls the setUserReady subroutine. Afterwards it checks if a
     * SetUserReadyRequest object got posted to the EventBus and if its content is the
     * default users information.
     * <p>
     * The test fails if any of the checks fail.
     */
    @Test
    @DisplayName("SetUserReadyRequest should contain the correct lobby name")
    void setUserReadyRequest_ShouldContainCorrectInfo() throws InterruptedException {
        setUserReady();
        assertTrue(event instanceof SetUserReadyRequest);
        SetUserReadyRequest request = (SetUserReadyRequest) event;
        assertEquals(request.getLobbyName(), defaultLobby.getName());
    }

    /**
     * Subroutine for tests. For users who need to be kicked out
     * <p>
     * This subroutine creates a new LobbyService object that is registered in the EventBus
     *
     * @throws InterruptedException thrown by lock.await()
     */
    private void kickPlayer() throws InterruptedException {
        lobbyService.kickPlayer(defaultLobby.getName(), defaultUser);
        lock.await(1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Test for kickPlayer method
     * <p>
     * <p>
     * This test first calls the kickPlayer subroutine. It is then checked whether a
     * The LobbyKickPlayer object was posted on the EventBus and if its content is the
     * Standard user information.
     *
     * @throws InterruptedException thrown by lock.await()
     */
    @Test
    @DisplayName("LobbyKickRequest should contain the correct lobby name and user")
    void lobbyKickRequest_ShouldContainCorrectInfo() throws InterruptedException {
        kickPlayer();
        assertTrue(event instanceof LobbyKickRequest);
        LobbyKickRequest request = (LobbyKickRequest) event;
        assertEquals(request.getLobbyName(), defaultLobby.getName());
        assertEquals(request.getKickUser(), defaultUser);
    }

    @Test
    @DisplayName("RejoinGameSessionRequest should contain the correct lobby name")
    void rejoinGameSessionRequest_ShouldContainCorrectInfo() {
        lobbyService.rejoinGameSession(defaultLobby.getName());
        assertTrue(event instanceof RejoinGameSessionRequest);
        RejoinGameSessionRequest request = (RejoinGameSessionRequest) event;
        assertEquals(defaultLobby.getName(), request.getGameSessionName());
    }

    /**
     * Tests for UpdateLobbyOption
     */
    @Nested
    class UpdateLobbyOption {
        /**
         * Helper method to update a lobby option
         */
        private <T extends Serializable> void updateLobby(LobbyOption<T> option, T newValue) {
            LobbyService lobbyService = new LobbyService(bus);
            lobbyService.updateLobby(defaultLobby.getName(), option, newValue);
        }

        /**
         * Tests for LobbySizeOption
         */
        @Nested
        class LobbySizeOption {
            private final int lobbySize = LobbyOptions.LOBBY_SIZE.getDefaultValue() - 1;

            @Test
            @DisplayName("UpdateLobbyRequest for the lobby size should contain the correct lobby name, lobby option and option value")
            void request_ShouldContainCorrectInfo() {
                UpdateLobbyOption.this.updateLobby(LobbyOptions.LOBBY_SIZE, lobbySize);
                assertAll(() -> assertTrue(event instanceof UpdateLobbyRequest),
                        () -> assertEquals(defaultLobby.getName(), ((UpdateLobbyRequest<?>) event).getLobbyName()),
                        () -> assertEquals(LobbyOptions.LOBBY_SIZE, ((UpdateLobbyRequest<?>) event).getOption()),
                        () -> assertEquals(lobbySize, ((UpdateLobbyRequest<?>) event).getNewValue())
                );
            }
        }

        /**
         * Tests for NumberOfVPOption
         */
        @Nested
        class NumberOfVPOption {
            private final int numOfVP = LobbyOptions.NUM_VICTORY_POINTS.getDefaultValue() - 1;

            @Test
            @DisplayName("UpdateLobbyRequest for the victory points should contain the correct lobby name, lobby option and option value")
            void request_ShouldContainCorrectInfo() {
                UpdateLobbyOption.this.updateLobby(LobbyOptions.NUM_VICTORY_POINTS, numOfVP);
                assertAll(() -> assertTrue(event instanceof UpdateLobbyRequest),
                        () -> assertEquals(defaultLobby.getName(), ((UpdateLobbyRequest<?>) event).getLobbyName()),
                        () -> assertEquals(LobbyOptions.NUM_VICTORY_POINTS, ((UpdateLobbyRequest<?>) event).getOption()),
                        () -> assertEquals(numOfVP, ((UpdateLobbyRequest<?>) event).getNewValue())
                );
            }
        }

        /**
         * Tests for TimerDurationOption
         */
        @Nested
        class TimerDurationOption {
            private final int timerDuration = LobbyOptions.TIMER_DURATION.getDefaultValue() - 1;

            @Test
            @DisplayName("UpdateLobbyRequest for the timer duration should contain the correct lobby name, lobby option and option value")
            void request_ShouldContainCorrectInfo() {
                UpdateLobbyOption.this.updateLobby(LobbyOptions.TIMER_DURATION, timerDuration);
                assertAll(() -> assertTrue(event instanceof UpdateLobbyRequest),
                        () -> assertEquals(defaultLobby.getName(), ((UpdateLobbyRequest<?>) event).getLobbyName()),
                        () -> assertEquals(LobbyOptions.TIMER_DURATION, ((UpdateLobbyRequest<?>) event).getOption()),
                        () -> assertEquals(timerDuration, ((UpdateLobbyRequest<?>) event).getNewValue())
                );
            }
        }

        /**
         * Tests for PrivacyOption
         */
        @Nested
        class PrivacyOption {
            private final boolean privateLobby = !LobbyOptions.PRIVATE_LOBBY.getDefaultValue();

            @Test
            @DisplayName("UpdateLobbyRequest for the privacy flag should contain the correct lobby name, lobby option and option value")
            void request_ShouldContainCorrectInfo() {
                UpdateLobbyOption.this.updateLobby(LobbyOptions.PRIVATE_LOBBY, privateLobby);
                assertAll(() -> assertTrue(event instanceof UpdateLobbyRequest),
                        () -> assertEquals(defaultLobby.getName(), ((UpdateLobbyRequest<?>) event).getLobbyName()),
                        () -> assertEquals(LobbyOptions.PRIVATE_LOBBY, ((UpdateLobbyRequest<?>) event).getOption()),
                        () -> assertEquals(privateLobby, ((UpdateLobbyRequest<?>) event).getNewValue())
                );
            }
        }
    }
}