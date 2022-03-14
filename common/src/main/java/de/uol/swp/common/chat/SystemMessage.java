package de.uol.swp.common.chat;

import de.uol.swp.common.message.AbstractServerMessage;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Message sent from server to connected players to signal a system message
 */
public class SystemMessage extends AbstractServerMessage {
    private final String lobbyName;
    private final String content;
    private final ZonedDateTime timestamp;

    /**
     * Constructor
     *
     * @param lobbyName the lobby name this message is sent to
     * @param content   the content of this message
     * @param timestamp the date-time at which the message was sent
     */
    public SystemMessage(String lobbyName, String content, ZonedDateTime timestamp) {
        this.lobbyName = lobbyName;
        this.content = content;
        this.timestamp = timestamp;
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

    /**
     * Returns the date-time at which the message was sent
     *
     * @return the date-time at which the message was sent
     */
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        SystemMessage that = (SystemMessage) obj;
        return Objects.equals(lobbyName, that.lobbyName) && Objects.equals(content, that.content) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobbyName, content, timestamp);
    }
}
