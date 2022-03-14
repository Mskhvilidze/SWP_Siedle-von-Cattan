package de.uol.swp.common.game.message.inventory;

import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.message.AbstractGameMessage;

import java.util.Objects;

/**
 * Message sent to all users in a game session to display the total amount of development cards a player has
 */
public class DevCardCountMessage extends AbstractGameMessage {

    private final PlayerDTO player;
    private final int count;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session
     * @param player          the player who is affected
     * @param count           the amount of dev cards the player has
     */
    public DevCardCountMessage(String gameSessionName, PlayerDTO player, int count) {
        super(gameSessionName);
        this.player = player;
        this.count = count;
    }

    /**
     * Returns the player who is affected
     *
     * @return the player who is affected
     */
    public PlayerDTO getPlayer() {
        return player;
    }

    /**
     * Returns how many development cards the player has
     *
     * @return how many development cards the player has
     */
    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        DevCardCountMessage that = (DevCardCountMessage) obj;
        return count == that.count && Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), player, count);
    }
}

