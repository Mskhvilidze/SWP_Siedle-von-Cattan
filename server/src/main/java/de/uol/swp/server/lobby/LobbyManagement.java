package de.uol.swp.server.lobby;

import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.user.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages creation, deletion and storing of lobbies
 *
 * @author Marco Grawunder
 * @see de.uol.swp.common.lobby.Lobby
 * @see de.uol.swp.common.lobby.dto.LobbyDTO
 * @since 2019-10-08
 */
public class LobbyManagement {

    private static final Logger LOG = LogManager.getLogger(LobbyManagement.class);
    private final Map<String, GameLobby> lobbies = new HashMap<>();
    private final Map<String, GameLobby> privateLobbies = new HashMap<>();

    /**
     * Creates a new lobby and adds it to the list.
     * Besides this method checks, if there are any spacial characters in the lobby name or
     * if the given name has more than 10 characters.
     *
     * @param name         the name of the lobby to create
     * @param owner        the user who wants to create a lobby
     * @param privateLobby boolean to create a private lobby
     * @throws IllegalArgumentException name already taken
     * @throws IllegalArgumentException name is illegal
     * @implNote the primary key of the lobbies is the name therefore the name has
     * to be unique
     * @see de.uol.swp.common.user.User
     * @since 2019-10-08
     */
    public void createLobby(String name, User owner, boolean privateLobby) {
        Pattern pattern = Pattern.compile("[a-z0-9 ]{1,10}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        if (lobbies.containsKey(name) || privateLobbies.containsKey(name)) {
            throw new IllegalArgumentException("Lobby name " + name + " already exists!");
        }
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Lobby name " + name + " is illegal!");
        }
        GameLobby lobby = new GameLobby(name, owner, privateLobby);
        if (privateLobby) {
            privateLobbies.put(name, lobby);
        } else {
            lobbies.put(name, lobby);
        }

    }

    /**
     * Searches for the lobby with the requested name
     *
     * @param name String containing the name of the lobby to search for
     * @return either empty Optional or Optional containing the lobby
     * @see Optional
     * @since 2019-10-08
     */
    public Optional<GameLobby> getLobby(String name) {
        GameLobby lobby = null;
        for (Map.Entry<String, GameLobby> entry : lobbies.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                lobby = entry.getValue();
            }
        }
        if (lobby == null) {
            for (Map.Entry<String, GameLobby> entry : privateLobbies.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) {
                    lobby = entry.getValue();
                }
            }
        }
        if (lobby != null) {
            return Optional.of(lobby);
        }
        return Optional.empty();
    }

    /**
     * Deletes lobby with requested name
     *
     * @param name String containing the name of the lobby to delete
     * @throws IllegalArgumentException there exists no lobby with the requested name
     * @since 2019-10-08
     */
    public void dropLobby(String name) {
        if (lobbies.containsKey(name)) {
            lobbies.remove(name);
        } else if (privateLobbies.containsKey(name)) {
            privateLobbies.remove(name);
        } else {
            throw new IllegalArgumentException("Lobby name " + name + " not found!");
        }
    }

    /**
     * Moves a lobby between the lobbies map and the privateLobbies map
     *
     * @param lobbyName the name of the lobby
     * @return {@code true} if the lobby was in one of the maps, otherwise {@code false}
     */
    public boolean toggleLobbyPrivacy(String lobbyName) {
        LOG.debug("{} is private: {}", lobbyName, lobbies.containsKey(lobbyName));
        if (lobbies.containsKey(lobbyName)) {
            privateLobbies.put(lobbyName, lobbies.remove(lobbyName));
            return true;
        } else if (privateLobbies.containsKey(lobbyName)) {
            lobbies.put(lobbyName, privateLobbies.remove(lobbyName));
            return true;
        }
        return false;
    }

    /**
     * Getter for all public lobbies on the server
     *
     * @return An UnmodifiableMap containing all public lobbies on the server
     */
    public Map<String, GameLobby> getLobbies() {
        return Collections.unmodifiableMap(lobbies);
    }

    /**
     * Getter for all private lobbies on the server
     *
     * @return An UnmodifiableMap containing all private lobbies on the server
     */
    public Map<String, GameLobby> getPrivateLobbies() {
        return Collections.unmodifiableMap(privateLobbies);
    }
}
