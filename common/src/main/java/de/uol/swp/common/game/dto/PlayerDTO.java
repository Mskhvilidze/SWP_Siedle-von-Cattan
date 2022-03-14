package de.uol.swp.common.game.dto;

import de.uol.swp.common.game.PlayerColor;
import de.uol.swp.common.game.board.Port;
import de.uol.swp.common.user.User;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Objects of this class are used to transfer player data between the server and the
 * clients.
 */
public class PlayerDTO implements Serializable {

    private final String playerName;
    private final int playerId;
    private final PlayerColor playerColor;
    private int victoryPoints; // public victory points, secret points are only saved on the server
    private Set<Port> ports = EnumSet.noneOf(Port.class);

    /**
     * public Constructor for setting ports
     *
     * @param playerName
     * @param playerId
     * @param color
     * @param numOfVP
     * @param ports
     */
    public PlayerDTO(String playerName, int playerId, PlayerColor color, int numOfVP, Set<Port> ports) {
        this(playerName, playerId, color, numOfVP);
        this.ports = ports;
    }

    /**
     * Constructor
     *
     * @param playerName the Player name
     * @param playerId   the playerId
     * @param color      the PlayerColor
     */
    public PlayerDTO(String playerName, int playerId, PlayerColor color, int victoryPoints) {
        this.playerName = playerName;
        this.playerId = playerId;
        this.playerColor = color;
        this.victoryPoints = victoryPoints;
    }

    /**
     * private Constructor used by create method
     *
     * @param username the username
     */
    private PlayerDTO(String username) {
        this.playerName = username;
        this.playerColor = PlayerColor.BLUE;
        this.playerId = -1;
    }

    /**
     * Copy Constructor
     *
     * @param user the user
     * @return PlayerDTO created from the user
     */
    public static PlayerDTO create(User user) {
        return new PlayerDTO(user.getUsername());
    }

    public Set<Port> getPorts() {
        return ports;
    }

    /**
     * Getter for PlayerName
     *
     * @return Player name as a String
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Getter for playerID
     *
     * @return the PlayerID
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Getter for the Color
     *
     * @return the player color
     */
    public PlayerColor getColor() {
        return playerColor;
    }

    /**
     * Getter for Victory Points
     *
     * @return points needed to win
     */
    public int getVictoryPoints() {
        return victoryPoints;
    }

    /**
     * Setter for Victory Points
     *
     * @param victoryPoints points needed to win
     */
    public void setVictoryPoints(int victoryPoints) {
        this.victoryPoints = victoryPoints;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, playerColor);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PlayerDTO playerDTO = (PlayerDTO) obj;
        return playerId == playerDTO.playerId && playerColor == playerDTO.playerColor;
    }
}
