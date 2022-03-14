package de.uol.swp.common.game.response;

import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.message.AbstractResponseMessage;

import java.util.Objects;

/**
 * Response to a player if they could rejoin the lobby
 */
public class RejoinGameResponse extends AbstractResponseMessage {
    private final String lobbyName;
    private final GameDTO gameDTO;
    private final PlayerDTO playerWhoIsOn;

    /**
     * Constructor
     *
     * @param lobbyName the name of the referred lobby
     * @param gameDTO   the referred game
     * @param player    the player whose turn it is
     */
    public RejoinGameResponse(String lobbyName, GameDTO gameDTO, PlayerDTO player) {
        this.lobbyName = lobbyName;
        this.gameDTO = gameDTO;
        this.playerWhoIsOn = player;
    }

    /**
     * getter for lobbyName
     *
     * @return the name of the referred lobby
     */
    public String getLobbyName() {
        return lobbyName;
    }

    /**
     * getter for gameDTO
     *
     * @return the referred game
     */
    public GameDTO getGameDTO() {
        return gameDTO;
    }

    /**
     * Returns the player whose turn it is
     *
     * @return the player whose turn it is
     */
    public PlayerDTO getWhoseTurn() {
        return playerWhoIsOn;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        RejoinGameResponse that = (RejoinGameResponse) object;
        return Objects.equals(lobbyName, that.lobbyName) &&
                Objects.equals(gameDTO, that.gameDTO) &&
                Objects.equals(playerWhoIsOn, that.playerWhoIsOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobbyName, gameDTO, playerWhoIsOn);
    }
}
