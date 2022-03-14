package de.uol.swp.common.lobby.dto;

import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.user.User;

import java.util.*;

/**
 * Object to transfer the information of a game lobby
 * <p>
 * This object is used to communicate the current state of game lobbies between
 * the server and clients. It contains information about the Name of the lobby,
 * who owns the lobby and who joined the lobby.
 *
 * @author Marco Grawunder
 * @since 2019-10-08
 */
public class LobbyDTO implements Lobby {

    private final String name;
    private final Set<User> users = new TreeSet<>();
    private final boolean privateLobby;
    private final boolean debugEnabled;
    private final int numVP;
    private final int lobbySize;
    private final int timerDurationInSeconds;
    private final Map<User, Boolean> readyStatus = new HashMap<>();
    private User owner;
    private int noOfBots = 0;
    private boolean lobbyCanBeStarted;
    private boolean gameStarted;

    /**
     * Constructs a LobbyDTO with {@link LobbyOption#getDefaultValue() default option values}
     *
     * @param name         the name the lobby should have
     * @param creator      the user who created the lobby and is its owner
     * @param privateLobby if {@code true}, marks this lobby as private
     */
    public LobbyDTO(String name, User creator, boolean privateLobby) {
        this(name, creator, privateLobby, LobbyOptions.DEBUG.getDefaultValue(), LobbyOptions.NUM_VICTORY_POINTS.getDefaultValue(),
                LobbyOptions.LOBBY_SIZE.getDefaultValue(), LobbyOptions.TIMER_DURATION.getDefaultValue());
    }

    /**
     * Constructor
     *
     * @param name                   the name the lobby should have
     * @param creator                the user who created the lobby and is its owner
     * @param privateLobby           if {@code true}, mark this lobby as private
     * @param debugEnabled           the value of the lobby's {@link LobbyOptions#DEBUG debug mode activated} option
     * @param numVP                  the value of the lobby's {@link LobbyOptions#NUM_VICTORY_POINTS number of victory points} option
     * @param lobbySize              the value of the lobby's {@link LobbyOptions#LOBBY_SIZE lobby size} option
     * @param timerDurationInSeconds the value of the lobby's {@link LobbyOptions#TIMER_DURATION timer duration} option
     */
    public LobbyDTO(String name, User creator, boolean privateLobby, boolean debugEnabled, int numVP, int lobbySize,
                    int timerDurationInSeconds) {
        this.name = name;
        this.owner = creator;
        this.users.add(creator);
        this.readyStatus.put(creator, true);
        this.privateLobby = privateLobby;
        this.debugEnabled = debugEnabled;
        this.lobbyCanBeStarted = false;
        this.numVP = numVP;
        this.lobbySize = lobbySize;
        this.timerDurationInSeconds = timerDurationInSeconds;
    }

    /**
     * Copy constructor for a LobbyDTO object with the given lobby's option values
     *
     * @param lobby the {@code Lobby} object to copy the values of
     * @return the {@code LobbyDTO} copy of the {@code Lobby} object
     */
    public static LobbyDTO create(Lobby lobby) {
        LobbyDTO lobbyDTO = new LobbyDTO(lobby.getName(), lobby.getOwner(), lobby.isPrivateLobby(), lobby.isDebugEnabled(), lobby.getNumVP(),
                lobby.getLobbySize(), lobby.getTimerDuration());
        lobby.getUsers().forEach(lobbyDTO::joinUser);
        Set<User> readyUsers = lobby.getReadyUsers();
        lobbyDTO.setGameHasStarted(lobby.isGameStarted());
        for (User user : readyUsers) {
            lobbyDTO.setUserReady(user, true);
        }
        lobbyDTO.setNoOfBots(lobby.getNoOfBots());
        return lobbyDTO;
    }

    /**
     * Getter for the lobby's name
     *
     * @return A String containing the name of the lobby
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Checks if the given user is in the lobby and changes it to the owner of the lobby
     *
     * @param user The user who should be the new owner
     */
    @Override
    public void updateOwner(User user) {
        if (!this.users.contains(user)) {
            throw new IllegalArgumentException("User " + user.getUsername() + "not found. Owner must be member of lobby!");
        }
        this.owner = user;
        setUserReady(owner, true);
        checkLobbyCanBeStarted();
    }

    /**
     * Checks that the specified user object is in the lobby and inserts updated and deletes old
     *
     * @param user updated user
     * @param name old username
     */
    @Override
    public void updateUser(User user, String name) {
        UserDTO oldUser = new UserDTO(name, "", "");
        if (!this.users.contains(user)) {
            this.users.add(user);
            this.users.remove(oldUser);
            setUserReady(user, true);
        }
        checkLobbyCanBeStarted();
    }

    /**
     * Getter for the current owner of the lobby
     *
     * @return A User object containing the owner of the lobby
     */
    @Override
    public User getOwner() {
        return owner;
    }

    /**
     * Adds a new user to the lobby by adding it to the user list
     *
     * @param user The new user to add to the lobby
     */
    @Override
    public void joinUser(User user) {
        this.users.add(user);
        this.readyStatus.put(user, false);
        checkLobbyCanBeStarted();
    }

    /**
     * Removes an user from the lobby by removing it from the user list
     *
     * @param user The user to remove from the lobby
     */
    @Override
    public void leaveUser(User user) {
        if (users.contains(user)) {
            users.remove(user);
            this.readyStatus.remove(user);
            if (users.size() != 1 && owner.equals(user)) {
                updateOwner(users.iterator().next());
            }
        }
        checkLobbyCanBeStarted();
    }

    /**
     * Getter for all users in the lobby
     *
     * @return An UnmodifiableSet containing all users in this lobby
     */
    @Override
    public Set<User> getUsers() {
        return Collections.unmodifiableSet(users);
    }

    @Override
    public boolean canLobbyBeStarted() {
        return lobbyCanBeStarted;
    }

    /**
     * Returns if the game already started
     *
     * @return {@code true} if the game already started, otherwise {@code false}
     */
    @Override
    public boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * Sets the boolean for if the game has started
     *
     * @param gameStarted the boolean that show if the game already started
     */
    @Override
    public void setGameHasStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    @Override
    public boolean isPrivateLobby() {
        return privateLobby;
    }

    @Override
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    @Override
    public int getNumVP() {
        return numVP;
    }

    @Override
    public int getLobbySize() {
        return lobbySize;
    }

    @Override
    public int getTimerDuration() {
        return timerDurationInSeconds;
    }

    @Override
    public void setUserReady(User user, boolean ready) {
        this.readyStatus.replace(user, ready);
        checkLobbyCanBeStarted();
    }

    @Override
    public Set<User> getReadyUsers() {
        Map<User, Boolean> filteredReadyUsers = new HashMap<>();
        for (Map.Entry<User, Boolean> map : readyStatus.entrySet()) {
            if (Boolean.TRUE.equals(map.getValue())) {
                filteredReadyUsers.put(map.getKey(), true);
            }
        }
        return filteredReadyUsers.keySet();
    }

    @Override
    public boolean isLobbyJoinable() {
        return getUsers().size() + getNoOfBots() < getLobbySize() && !isGameStarted();
    }

    // if the amount of users that are ready is equal to the users in the lobby, set lobbyCanBeStarted to true
    private void checkLobbyCanBeStarted() {
        lobbyCanBeStarted = users.size() == getReadyUsers().size();
    }

    @Override
    public int getNoOfBots() {
        return noOfBots;
    }

    private void setNoOfBots(int noOfBots) {
        this.noOfBots = noOfBots;
    }
}
