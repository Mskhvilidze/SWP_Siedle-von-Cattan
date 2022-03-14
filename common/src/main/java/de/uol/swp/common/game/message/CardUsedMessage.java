package de.uol.swp.common.game.message;

import de.uol.swp.common.game.board.DevCard;
import de.uol.swp.common.game.dto.PlayerDTO;

import java.util.Objects;

/**
 * Message sent to all players in a game session when a dev card has been played
 */
public class CardUsedMessage extends AbstractGameMessage {
    private final PlayerDTO player;
    private final DevCard devCard;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this message is sent to
     * @param player          the player who played the dev card
     * @param devCard         the dev card that was played
     */
    public CardUsedMessage(String gameSessionName, PlayerDTO player, DevCard devCard) {
        super(gameSessionName);
        this.player = player;
        this.devCard = devCard;
    }

    /**
     * Returns the player who played the dev card
     *
     * @return the player who played the dev card
     */
    public PlayerDTO getPlayer() {
        return player;
    }

    /**
     * Returns the dev card that was played
     *
     * @return the dev card that was played
     */
    public DevCard getDevCard() {
        return devCard;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        CardUsedMessage that = (CardUsedMessage) obj;
        return Objects.equals(player, that.player) && devCard == that.devCard;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), player, devCard);
    }
}
