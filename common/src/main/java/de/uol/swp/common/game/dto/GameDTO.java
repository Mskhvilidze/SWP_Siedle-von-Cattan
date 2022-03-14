package de.uol.swp.common.game.dto;

import java.io.Serializable;

/**
 * Objects of this class are used to transfer game data between the server and the
 * clients.
 */
public class GameDTO implements Serializable {

    private final PlayerDTO[] players;
    private final String gameSessionName;
    private final boolean debugEnabled;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the lobby this game is running in
     * @param players         the players that are in the game
     */
    public GameDTO(String gameSessionName, boolean debugEnabled, PlayerDTO... players) {
        this.gameSessionName = gameSessionName;
        this.debugEnabled = debugEnabled;
        this.players = players.clone();
    }

    /**
     * Returns the players of a game
     *
     * @return the player of a game
     */
    public PlayerDTO[] getPlayers() {
        return players.clone();
    }

    /**
     * Getter for the name of the game session
     *
     * @return the name of the game session
     */
    public String getGameSessionName() {
        return gameSessionName;
    }

    /**
     * Getter for the debug
     *
     * @return if the debug mode is enabled
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}
