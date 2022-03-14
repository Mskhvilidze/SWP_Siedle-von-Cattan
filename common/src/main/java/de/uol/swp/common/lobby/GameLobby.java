package de.uol.swp.common.lobby;

import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.lobby.dto.LobbyOption;
import de.uol.swp.common.lobby.dto.LobbyOptions;
import de.uol.swp.common.user.User;

import java.io.Serializable;
import java.util.*;

/**
 * Object to store and alter the information of a game lobby
 * <p>
 * This object is used to store the options set
 */
public final class GameLobby implements Lobby {

    private final String name;
    private final Set<User> users = new TreeSet<>();
    private final Set<User> leftUsers = new TreeSet<>();
    private final Map<User, Boolean> readyStatus = new HashMap<>();
    private User owner;
    private boolean privateLobby;
    private boolean debugEnabled;
    private int numVP;
    private int lobbySize;
    private int timerDurationInSeconds;
    private int noOfBots = 0;
    private boolean lobbyCanBeStarted;
    private boolean gameHasStarted = false;

    /**
     * Constructs a GameLobby with {@link LobbyOption#getDefaultValue() default option values}
     *
     * @param name         the name the lobby should have
     * @param creator      the user who created the lobby and is its owner
     * @param privateLobby if {@code true}, mark this lobby as private
     */
    public GameLobby(String name, User creator, boolean privateLobby) {
        this(name, creator, privateLobby, LobbyOptions.DEBUG.getDefaultValue(), LobbyOptions.NUM_VICTORY_POINTS.getDefaultValue(),
                LobbyOptions.LOBBY_SIZE.getDefaultValue(), LobbyOptions.TIMER_DURATION.getDefaultValue());
    }

    /**
     * Constructor
     *
     * @param name                   the name the lobby should have
     * @param creator                the user who created the lobby and is its owner
     * @param privateLobby           if {@code true}, mark this lobby as private
     * @param numVP                  the value of the lobby's {@link LobbyOptions#NUM_VICTORY_POINTS number of victory points} option
     * @param lobbySize              the value of the lobby's {@link LobbyOptions#LOBBY_SIZE lobby size} option
     * @param timerDurationInSeconds the value of the lobby's {@link LobbyOptions#TIMER_DURATION timer duration} option
     */
    private GameLobby(String name, User creator, boolean privateLobby, boolean debugEnabled, int numVP, int lobbySize,
                      int timerDurationInSeconds) {
        this.name = name;
        this.owner = creator;
        this.users.add(creator);
        this.readyStatus.put(creator, true);
        this.privateLobby = privateLobby;
        this.debugEnabled = debugEnabled;
        this.lobbyCanBeStarted = false;
        updateNumVP(numVP);
        updateLobbySize(lobbySize);
        updateTimer(timerDurationInSeconds);
    }

    /**
     * Copy constructor for a GameLobby object with the given lobby's option values
     *
     * @param lobby the {@code Lobby} object to copy the values of
     * @return the {@code GameLobby} copy of {@code Lobby} object
     */
    public static GameLobby create(Lobby lobby) {
        GameLobby gameLobby = new GameLobby(lobby.getName(), lobby.getOwner(), lobby.isPrivateLobby(), lobby.isDebugEnabled(), lobby.getNumVP(),
                lobby.getLobbySize(), lobby.getTimerDuration());
        lobby.getUsers().forEach(gameLobby::joinUser);
        gameLobby.setGameHasStarted(lobby.isGameStarted());
        for (User readyUser : lobby.getReadyUsers()) {
            for (User gameLobbyUser : gameLobby.getUsers()) {
                if (readyUser.getUsername().equals(gameLobbyUser.getUsername())) {
                    gameLobby.setUserReady(gameLobbyUser, true);
                }
            }
        }
        for (int i = 0; i < lobby.getNoOfBots(); i++) {
            gameLobby.addBot();
        }
        return gameLobby;
    }

    /**
     * Copy constructor for a GameLobby object with default options
     *
     * @param lobby {@code Lobby} object to copy the values of
     * @return the {@code GameLobby} copy of {@code Lobby} object
     */
    public static GameLobby createDefault(Lobby lobby) {
        GameLobby gameLobby = new GameLobby(lobby.getName(), lobby.getOwner(), lobby.isPrivateLobby());
        lobby.getUsers().forEach(gameLobby::joinUser);
        return gameLobby;
    }

    /**
     * Updates the correct lobby option based on the given {@code LobbyOption}
     *
     * @param option   the {@code LobbyOption} for the option that is being changed
     * @param newValue the new option value
     * @param <T>      the type of the option value
     */
    public <T extends Serializable> void updateLobby(LobbyOption<T> option, T newValue) {
        if (option.equals(LobbyOptions.PRIVATE_LOBBY)) {
            setPrivateLobby((Boolean) newValue);
        } else if (option.equals(LobbyOptions.DEBUG)) {
            setDebugEnabled((Boolean) newValue);
        } else if (option.equals(LobbyOptions.LOBBY_SIZE)) {
            updateLobbySize((Integer) newValue);
        } else if (option.equals(LobbyOptions.NUM_VICTORY_POINTS)) {
            updateNumVP((Integer) newValue);
        } else if (option.equals(LobbyOptions.TIMER_DURATION)) {
            updateTimer((Integer) newValue);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Checks if the given user object is in the lobby and changes it to the owner of the lobby
     *
     * @param user the user who should be the new owner
     */
    @Override
    public void updateOwner(User user) {
        if (!this.users.contains(user)) {
            throw new IllegalArgumentException(
                    "User " + user.getUsername() + "not found. Owner must be member of lobby!");
        }
        this.owner = user;
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
        }
        checkLobbyCanBeStarted();
    }

    @Override
    public User getOwner() {
        return owner;
    }

    /**
     * Getter for leftUsers
     *
     * @return leftUsers the Users that left after the game already started
     */
    public Set<User> getLeftUsers(){
        return leftUsers;
    }

    @Override
    public void joinUser(User user) {
        this.users.add(user);
        this.readyStatus.put(user, false);
        checkLobbyCanBeStarted();
    }

    /**
     * Removes an user from the lobby
     * <p>
     * Should that user be the owner then {@link #updateOwner(User)} gets called with the next user in the users {@code Set}.
     *
     * @param user The user to remove from the lobby
     * @throws IllegalArgumentException if there is only 1 user left in the lobby
     */
    @Override
    public void leaveUser(User user) {
        if (users.size() == 1) {
            throw new IllegalArgumentException("Lobby has to be dropped via LobbyManagement if last user wants to leave");
        }
        if (users.contains(user)) {
            this.users.remove(user);
            this.readyStatus.remove(user);
            if (this.owner.equals(user)) {
                updateOwner(users.iterator().next());
            }
            if(gameHasStarted){
                this.leftUsers.add(user);
            }
        }
        checkLobbyCanBeStarted();
    }

    /**
     * Getter for all users in the lobby
     *
     * @return An UnmodifiableSet containing all user in this lobby
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
        return gameHasStarted;
    }

    /**
     * Sets the boolean for if the game has started
     *
     * @param gameHasStarted the boolean that show if the game already started
     */
    @Override
    public void setGameHasStarted(boolean gameHasStarted) {
        this.gameHasStarted = gameHasStarted;
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
    public boolean isLobbyJoinable() {
        return getUsers().size() + getNoOfBots() < getLobbySize() && !isGameStarted();
    }

    /**
     * Sets whether the lobby is private and therefore cant be seen by other users
     *
     * @param privateLobby {@code true}, to mark this lobby as private
     */
    public void setPrivateLobby(boolean privateLobby) {
        this.privateLobby = privateLobby;
    }

    /**
     * Sets whether the debug menu is enabled
     *
     * @param debugEnabled {@code true}, to enable the debug mode
     */
    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
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

    /**
     * Sets the value of the lobby's {@link LobbyOptions#LOBBY_SIZE lobby size} option
     * <p>
     * The value must be between {@link LobbyOption#getMin() min} and {@link LobbyOption#getMax() max} otherwise it gets clamped.
     *
     * @param lobbySize the value to be set
     */
    public void updateLobbySize(int lobbySize) {
        this.lobbySize = LobbyOptions.clampInt(lobbySize, LobbyOptions.LOBBY_SIZE.getMin(),
                LobbyOptions.LOBBY_SIZE.getMax());
        checkLobbyCanBeStarted();
    }

    /**
     * Sets the value of the lobby's {@link LobbyOptions#NUM_VICTORY_POINTS number of victory points} option
     * <p>
     * The value must be between {@link LobbyOption#getMin() min} and {@link LobbyOption#getMax() max} otherwise it gets clamped.
     *
     * @param numVP the value to be set
     */
    public void updateNumVP(int numVP) {
        this.numVP = LobbyOptions.clampInt(numVP, LobbyOptions.NUM_VICTORY_POINTS.getMin(),
                LobbyOptions.NUM_VICTORY_POINTS.getMax());
    }

    /**
     * Sets the value of the lobby's {@link LobbyOptions#TIMER_DURATION timer duration} option
     * <p>
     * The value must be between {@link LobbyOption#getMin() min} and {@link LobbyOption#getMax() max} otherwise it gets clamped.
     *
     * @param duration the value to be set
     */
    public void updateTimer(int duration) {
        this.timerDurationInSeconds = LobbyOptions.clampInt(duration, LobbyOptions.TIMER_DURATION.getMin(),
                LobbyOptions.TIMER_DURATION.getMax());
    }

    /**
     * Checks if the lobbySize is equals to the amount of users which are ready and the number of bots,
     * so the game can't be started with 1 or 2 player.
     */
    public void checkLobbyCanBeStarted() {
        lobbyCanBeStarted = lobbySize == getReadyUsers().size() + noOfBots;
    }

    /**
     * Adds a bot to this lobby
     */
    public void addBot() {
        noOfBots++;
        checkLobbyCanBeStarted();
    }

    @Override
    public int getNoOfBots() {
        return noOfBots;
    }

    /**
     * Returns whether the number of users and bots is equal to or bigger than the lobby size
     *
     * @return {@code false} if the number of users and bots is smaller than the lobby size, otherwise {@code true}
     */
    public boolean isFull() {
        return noOfBots + users.size() >= lobbySize;
    }

    /**
     * Removes a bot from this lobby
     */
    public void removeBot() {
        noOfBots--;
        checkLobbyCanBeStarted();
    }
}
