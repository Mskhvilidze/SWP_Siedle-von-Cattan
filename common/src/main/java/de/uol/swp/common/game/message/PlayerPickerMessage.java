package de.uol.swp.common.game.message;

import de.uol.swp.common.game.dto.PlayerDTO;

import java.util.List;
import java.util.Objects;

/**
 * Message sent to a player game session indicating that they can rob adjacent PLayers of an Hex after placing the robber.
 */
public class PlayerPickerMessage extends AbstractGameMessage {

    private final List<PlayerDTO> playersAdjacentToHex;

    /**
     * Constructor
     *
     * @param gameSessionName      the name of the game session this message is sent to
     * @param playersAdjacentToHex the list of player that can be robbed
     */
    public PlayerPickerMessage(String gameSessionName, List<PlayerDTO> playersAdjacentToHex) {
        super(gameSessionName);
        this.playersAdjacentToHex = playersAdjacentToHex;
    }

    /**
     * Returns the list of player that can be robbed
     *
     * @return the list of player that can be robbed
     */
    public List<PlayerDTO> getPlayersGettingRobbed() {
        return playersAdjacentToHex;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        PlayerPickerMessage that = (PlayerPickerMessage) obj;
        return Objects.equals(playersAdjacentToHex, that.playersAdjacentToHex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), playersAdjacentToHex);
    }
}
