package de.uol.swp.common.chat;

import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.message.AbstractServerMessage;

import java.time.ZonedDateTime;
import java.util.Objects;


/**
 * Message send from server to every connected player after receiving UpdateMessageRequest
 * Contains the content needed for updating the Chat window
 *
 * @see de.uol.swp.common.message.AbstractMessage
 */

public class ChatMessage extends AbstractServerMessage {

    private final String lobby;
    private final String content;
    private final UserDTO user;
    private final ZonedDateTime timestamp;

    /**
     * Constructor
     *
     * @param lobby     the lobby name this message is posted to
     * @param content   the content of the message
     * @param user      the author of the message
     * @param timestamp the date-time at which the message was sent
     */

    public ChatMessage(String lobby, String content, UserDTO user, ZonedDateTime timestamp) {
        this.lobby = lobby;
        this.content = content;
        this.user = user;
        this.timestamp = timestamp;
    }

    /**
     * Getter for the lobby name
     *
     * @return a String containing the lobby name
     */
    public String getLobby() {
        return lobby;
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
     * Getter for the User
     *
     * @return User user
     */
    public UserDTO getUser() {
        return user;
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
        if (this == object) { return true; }
        if (object == null || getClass() != object.getClass()){ return false;  }
        if (!super.equals(object)){ return false; }
        ChatMessage that = (ChatMessage) object;
        return Objects.equals(lobby, that.lobby) && Objects.equals(content,
                that.content) && Objects.equals(user, that.user) && Objects.equals(timestamp,
                that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobby, content, user, timestamp);
    }
}
