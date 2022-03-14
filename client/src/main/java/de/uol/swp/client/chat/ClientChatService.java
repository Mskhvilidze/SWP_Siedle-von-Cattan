package de.uol.swp.client.chat;

/**
 * An interface for all methods of the client chat service
 * <p>
 * As the communication with the server is based on events, the
 * returns of the call must be handled by events
 */
public interface ClientChatService {

    /**
     * send a chat message to a specific lobby
     */
    void sendChatMessage(String lobby, String content);
}
