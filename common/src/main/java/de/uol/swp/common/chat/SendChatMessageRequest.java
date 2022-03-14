package de.uol.swp.common.chat;

import de.uol.swp.common.message.AbstractRequestMessage;

import java.time.ZonedDateTime;
import java.util.Objects;


/**
 * Message send from client to Server asking to update the global messages with the message content sent.
 *
 * @see de.uol.swp.common.message.AbstractRequestMessage
 */

public class SendChatMessageRequest extends AbstractRequestMessage {

    private final String lobbyName;
    private final String content;
    private final ZonedDateTime timestamp;

    /**
     * Constructor
     *
     * @param lobbyName the lobby name this message is posted to
     * @param content   the content of the message
     * @param timestamp the date-time at which the message was sent
     */
    public SendChatMessageRequest(String lobbyName, String content, ZonedDateTime timestamp) {
        this.lobbyName = lobbyName;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * Getter for the Lobbyname
     *
     * @return a String containing the lobby name
     */
    public String getLobbyName() {
        return lobbyName;
    }


    /**
     * Getter for the content of the message
     *
     * @return a String with the content of the message
     */
    public String getContent() {
        return content;
    }


    /**
     * Returns the date-time stored at which the message was sent
     *
     * @return the date-time stored at which the message was sent
     */
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        SendChatMessageRequest that = (SendChatMessageRequest) object;
        return Objects.equals(lobbyName, that.lobbyName) && Objects.equals(content,
                that.content) && Objects.equals(timestamp,
                that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobbyName, content, timestamp);
    }
}
