package de.uol.swp.common.lobby.message;

/**
 * The {@code LobbyNotificationMessage} interface should be implemented by any
 * message, upon receipt of which the user should receive a notification
 */
public interface LobbyNotificationMessage {

    /**
     * Returns the name of the lobby that has a new notification
     *
     * @return the name of the lobby that has a new notification
     */
    String getLobbyName();
}
