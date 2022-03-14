package de.uol.swp.server.message;

import de.uol.swp.common.chat.SystemMessage;

import java.util.Objects;

/**
 * This message is used to tell the ChatService
 * to send a {@link SystemMessage} to a given lobby
 */
public class SendSystemMessage extends AbstractServerInternalMessage {
    private final String lobbyName;
    private final String content;

    /**
     * Constructor
     *
     * @param lobbyName the lobby name this message is sent to
     * @param content   the content of this message
     */
    public SendSystemMessage(String lobbyName, String content) {
        this.lobbyName = lobbyName;
        this.content = content;
    }

    /**
     * Returns the name of the lobby this message is sent to
     *
     * @return the name of the lobby this message is sent to
     */
    public String getLobbyName() {
        return lobbyName;
    }

    /**
     * Returns the content of this message
     *
     * @return the content of this message
     */
    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        SendSystemMessage that = (SendSystemMessage) obj;
        return Objects.equals(lobbyName, that.lobbyName) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobbyName, content);
    }
}
