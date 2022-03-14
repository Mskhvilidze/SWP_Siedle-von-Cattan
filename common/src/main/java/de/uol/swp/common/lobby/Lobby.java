package de.uol.swp.common.lobby;

import de.uol.swp.common.lobby.dto.LobbyOptions;
import de.uol.swp.common.user.User;

import java.io.Serializable;
import java.util.Set;

/**
 * Interface to unify lobby objects
 * <p>
 * This is an Interface to allow for multiple types of lobby objects since it is
 * possible that not every client has to have every information of the lobby.
 *
 * @author Marco Grawunder
 * @see de.uol.swp.common.lobby.dto.LobbyDTO
 * @since 2019-10-08
 */
public interface Lobby extends Serializable {

    /**
     * Getter for the lobby's name
     *
     * @return a String containing the name of the lobby
     * @since 2019-10-08
     */
    String getName();

    /**
     * Changes the owner of the lobby
     *
     * @param user the user who should be the new owner
     * @since 2019-10-08
     */
    void updateOwner(User user);

    /**
     * User is changed in the lobby
     *
     * @param user updated user
     * @param name old username
     */
    void updateUser(User user, String name);

    /**
     * Getter for the current owner of the lobby
     *
     * @return a User object containing the owner of the lobby
     * @since 2019-10-08
     */
    User getOwner();

    /**
     * Adds a new user to the lobby
     *
     * @param user the new user to add to the lobby
     * @since 2019-10-08
     */
    void joinUser(User user);

    /**
     * Removes an user from the lobby
     *
     * @param user The user to remove from the lobby
     * @since 2019-10-08
     */
    void leaveUser(User user);

    /**
     * Getter for all users in the lobby
     *
     * @return a Set containing all user in this lobby
     * @since 2019-10-08
     */
    Set<User> getUsers();

    /**
     * Returns if the lobby's session can be started
     *
     * @return {@code true} if the lobby's session can be started, otherwise {@code false}
     */
    boolean canLobbyBeStarted();

    /**
     * Returns if the game already started
     *
     * @return {@code true} if the game already started, otherwise {@code false}
     */
    boolean isGameStarted();

    /**
     * Sets the boolean for if the game has started
     *
     * @param gameHasStarted the boolean that show if the game already started
     */
    void setGameHasStarted(boolean gameHasStarted);

    /**
     * Returns if the lobby is visible to all users
     *
     * @return {@code true} if the lobby is private, otherwise {@code false}
     * @since 2020-11-26
     */
    boolean isPrivateLobby();

    /**
     * Returns if the debug mode is enabled
     *
     * @return {@code true} if the lobby enabled debug, otherwise {@code false}
     */
    boolean isDebugEnabled();

    /**
     * Getter for the {@link LobbyOptions#NUM_VICTORY_POINTS number of victory points} option's value
     *
     * @return the value of the {@link LobbyOptions#NUM_VICTORY_POINTS number of victory points} option
     */
    int getNumVP();

    /**
     * Getter for the {@link LobbyOptions#LOBBY_SIZE lobby size} option's value
     *
     * @return the value of the {@link LobbyOptions#LOBBY_SIZE lobby size} option
     */
    int getLobbySize();

    /**
     * Getter for the {@link LobbyOptions#TIMER_DURATION timer duration} option's value
     *
     * @return the value of the{@link LobbyOptions#TIMER_DURATION timer duration} option
     */
    int getTimerDuration();

    /**
     * Sets the value of the key(user) in the readyStatus-HashMap to the given boolean
     *
     * @param user the user who pressed the ready button
     */
    void setUserReady(User user, boolean ready);


    /**
     * Getter for all users, whose value in the Map readyStatus is true
     *
     * @return a set with all users, who pressed the readyButton
     */
    Set<User> getReadyUsers();

    /**
     * Getter for lobbyJoinable boolean
     *
     * @return true, if the lobby is joinable, false if not
     */
    boolean isLobbyJoinable();

    /**
     * Returns the number of bots in this lobby
     *
     * @return the number of bots in this lobby
     */
    int getNoOfBots();
}
