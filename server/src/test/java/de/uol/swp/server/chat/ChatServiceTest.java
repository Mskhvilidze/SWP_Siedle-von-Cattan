package de.uol.swp.server.chat;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.chat.ChatMessage;
import de.uol.swp.common.chat.SendChatMessageRequest;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.user.Session;
import de.uol.swp.server.game.session.GameSessionFactory;
import de.uol.swp.server.game.session.GameSessionManagement;
import de.uol.swp.server.game.session.GameSessionService;
import de.uol.swp.server.lobby.LobbyManagement;
import de.uol.swp.server.lobby.LobbyService;
import de.uol.swp.server.usermanagement.AuthenticationService;
import de.uol.swp.server.usermanagement.UserManagement;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import de.uol.swp.server.usermanagement.store.UserStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for the class uses to send ChatMessages to all clients
 *
 * @see de.uol.swp.server.chat.ChatService
 */
@SuppressWarnings({"UnstableApiUsage", "PMD.DefaultPackage", "PMD.MethodNamingConventions"})
class ChatServiceTest {

    static final Logger LOG = LogManager.getLogger(ChatServiceTest.class);
    static final String DEFAULT_CONTENT = "String content to compare";
    static final String DEFAULT_LOBBY = "global";
    static final String WRONG_TIMESTAMP = "1995-05-12T17:26:57.353+02:00[Europe/Berlin]";
    final UserDTO defaultUser = new UserDTO("test1", "test1", "test1@test.de");
    final EventBus bus = new EventBus();
    final UserStore userStore = new MainMemoryBasedUserStore();
    final UserManagement userManagement = new UserManagement(userStore);
    final AuthenticationService authService = new AuthenticationService(bus, userManagement);
    final LobbyManagement lobbyManagement = new LobbyManagement();
    final GameSessionFactory gameSessionFactory = mock(GameSessionFactory.class);
    final GameSessionManagement gameSessionManagement = new GameSessionManagement(gameSessionFactory);
    final GameSessionService gameSessionService = new GameSessionService(gameSessionManagement, bus, authService);
    final LobbyService lobbyService = new LobbyService(lobbyManagement, authService, gameSessionService, bus);
    final ChatService chatService = new ChatService(bus, lobbyService);
    final CountDownLatch lock = new CountDownLatch(1);
    Object event;

    /**
     * Handles DeadEvents detected on the EventBus
     * <p>
     * If a DeadEvent is detected the event variable of this class gets updated
     * to its event and its event is printed to the console output.
     *
     * @param event the DeadEvent detected on the EventBus
     */
    @Subscribe
    void handle(DeadEvent event) {
        this.event = event.getEvent();
        LOG.debug(event.getEvent());
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

    /**
     * Subroutine used for tests that need a chat message on the Event Bus
     *
     * @throws InterruptedException thrown by lock.await()
     */
    private void sendChatMessage(String lobby, ZonedDateTime zdt) throws InterruptedException {
        SendChatMessageRequest request = new SendChatMessageRequest(lobby, DEFAULT_CONTENT, zdt);
        Session session = mock(Session.class);
        when(session.getUser()).thenReturn(defaultUser);
        request.setSession(session);
        bus.post(request);
        lock.await(100, TimeUnit.MILLISECONDS);
    }

    @Test
    void messageTimeFail() throws InterruptedException {
        sendChatMessage("global", ZonedDateTime.parse(WRONG_TIMESTAMP, DateTimeFormatter.ISO_DATE_TIME));
        assertNull(event);
    }

    /**
     * Test for the onMessageReceived method
     * <p>
     * This test first calls the sendChatMessageGlobal subroutine. Afterwards it checks if a
     * ChatMessage object got posted to the EventBus and if its content is the
     * default chat message information.
     * The test fails if any of the checks fail.
     *
     * @throws InterruptedException thrown by sendChatMessageGlobal()
     */
    @Test
    void onMessageReceivedTest_Global() throws InterruptedException {
        ZonedDateTime now = ZonedDateTime.now();
        sendChatMessage("global", now);

        assertEquals(ChatMessage.class, event.getClass());

        ChatMessage chatMessage = (ChatMessage) event;
        assertEquals(DEFAULT_LOBBY, chatMessage.getLobby());
        assertEquals(defaultUser, chatMessage.getUser());
        assertEquals(DEFAULT_CONTENT, chatMessage.getContent());
        assertEquals(now.format(DateTimeFormatter.ISO_DATE_TIME), chatMessage.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));
    }

    /**
     * Test for the onMessageReceived method
     * <p>
     * Similar to the one onMessageReceivedTest_Global test, but this test
     * also creates a lobby.
     *
     * @throws InterruptedException thrown by sendChatMessageGlobal()
     */

    @Test
    void onMessageReceivedTest_Lobby() throws InterruptedException {
        final String lobbyName = "LobbyName";
        lobbyManagement.createLobby(lobbyName, defaultUser, false);

        sendChatMessage(lobbyName, ZonedDateTime.now());
        assertEquals(ChatMessage.class, event.getClass());
        ChatMessage chatMessage = (ChatMessage) event;
        assertEquals(lobbyName, chatMessage.getLobby());
    }
}
