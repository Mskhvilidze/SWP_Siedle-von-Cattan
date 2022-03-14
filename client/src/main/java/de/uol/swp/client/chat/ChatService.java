package de.uol.swp.client.chat;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import de.uol.swp.common.chat.SendChatMessageRequest;

import java.time.ZonedDateTime;

/**
 * This class is used to hide the communication details
 * <p>
 * implements de.uol.swp.client.chat.ClientChatService
 *
 * @see ClientChatService
 */
@SuppressWarnings("UnstableApiUsage")
public class ChatService implements ClientChatService {

    private final EventBus bus;

    /**
     * Constructor
     *
     * @param bus the EventBus set in ClientModule
     * @see de.uol.swp.client.di.ClientModule
     */
    @Inject
    public ChatService(EventBus bus) {
        this.bus = bus;
    }

    /**
     * Sends a chat message to a specific lobby
     *
     * @param lobby   the name of the lobby
     * @param content the content from the chat message
     */
    @Override
    public void sendChatMessage(String lobby, String content) {
        ZonedDateTime now = ZonedDateTime.now();
        SendChatMessageRequest request = new SendChatMessageRequest(lobby, content, now);
        bus.post(request);
    }
}
