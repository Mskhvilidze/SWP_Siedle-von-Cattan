package de.uol.swp.common.user;

import java.util.List;

/**
 * Interface for different kinds of user objects.
 * <p>
 * This interface is for unifying different kinds of user objects throughout the
 * project. With this being the base project it is currently only used for the UUIDSession
 * objects within the server.
 *
 * @author Marco Grawunder
 * @since 2019-08-05
 */
public interface Session {

    /**
     * Getter for the SessionID
     *
     * @return the ID of the session as String
     * @since 2019-08-05
     */
    String getSessionId();

    /**
     * Getter for the user that uses the session
     *
     * @return the user of the session as object implementing user
     * @see de.uol.swp.common.user.User
     * @since 2019-08-13
     */
    User getUser();

    /**
     * Getter for the updated user
     *
     * @param updatedUser User who is being updated
     * @return Updated user
     */
    User updateUser(User updatedUser);
    /**
     * Getter for the List of lobbies that the user is part of.
     *
     * @return the List of Lobbies
     */
    List<String> getLobbies();

    /**
     * Add one Lobby by name to the List of lobbies the user is part of.
     *
     * @param lobbyname the Lobby added to the list.
     */
    void addLobby(String lobbyname);

    /**
     * Remove one Lobby by name from the List of lobbies the user is part of.
     *
     * @param lobbyname The Lobby removed from the list.
     */
    void removeLobby(String lobbyname);
}
