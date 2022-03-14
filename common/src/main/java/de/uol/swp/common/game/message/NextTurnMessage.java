package de.uol.swp.common.game.message;

import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.lobby.message.LobbyNotificationMessage;

import java.util.Objects;

/**
 * Message Send from Server to Clients, informing them about whose turn its next.
 */
public class NextTurnMessage extends AbstractGameMessage implements LobbyNotificationMessage {

    private final PlayerDTO player;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this message is sent to
     * @param player          the playerDTO of the player whose turn it is next
     */
    public NextTurnMessage(String gameSessionName, PlayerDTO player) {
        super(gameSessionName);
        this.player = player;
    }

    @Override
    public String getLobbyName() {
        return getGameSessionName();
    }

    /**
     * Returns the player whose turn it is next
     *
     * @return the player whose turn it is next
     */
    public PlayerDTO getPlayer() {
        return player;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        NextTurnMessage that = (NextTurnMessage) obj;
        return Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), player);
    }
}
