package de.uol.swp.client.chat;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.chat.SendChatMessageRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"UnstableApiUsage", "PMD.CommentRequired", "PMD.DefaultPackage"})
class ChatServiceTest {

    static final Logger LOG = LogManager.getLogger(ChatServiceTest.class);
    static final String DEFAULT_CONTENT = "String content to compare";
    final EventBus bus = new EventBus();
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
        LOG.info("Dead event {}", this.event);
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
     * Test for the send message function for the global chat
     * <p>
     * This Test creates a new ChatService object registered to the EventBus of
     * this test class. It then calls the sendChatMessage function of the object using
     * the defaultUser, the defaultContent and the "global" lobby as parameter and waits for it to post an updateMessageRequest
     * object on the EventBus.
     * If this happens within one second, it checks if the user in the request object
     * is the same as the default user, if the content is the same, if the lobby is the same and if the
     * timestamp in the request has a valid format ("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
     * If any of these checks fail or the method takes to long, this test is unsuccessful.
     *
     * @throws InterruptedException thrown by lock.await()
     */
    @Test
    void sendMessageGlobalTest() throws InterruptedException {
        String lobby = "global";
        event = null;

        ChatService chatService = new ChatService(bus);
        chatService.sendChatMessage(lobby, DEFAULT_CONTENT);

        lock.await(1000, TimeUnit.MILLISECONDS);

        assertTrue(event instanceof SendChatMessageRequest);

        SendChatMessageRequest request = (SendChatMessageRequest) event;

        assertEquals(DEFAULT_CONTENT, request.getContent());
        assertEquals(lobby, request.getLobbyName());
    }

}
