package de.uol.swp.common.game.message;

import de.uol.swp.common.game.dto.PlayerDTO;

import java.util.Objects;

/**
 * Message send when a player is awarded the largest army bonus.
 * Used to display the current player with the most knights in client
 */
public class KnightUpdateMessage extends AbstractGameMessage {

    private final PlayerDTO playerWithArmyBonus;


    /**
     * Constructor
     *
     * @param gameSession         current gameSessionName
     * @param playerWithArmyBonus player with Army Bonus
     */
    public KnightUpdateMessage(String gameSession, PlayerDTO playerWithArmyBonus) {
        super(gameSession);
        this.playerWithArmyBonus = playerWithArmyBonus;
    }

    /**
     * Getter for Player with largest Army
     *
     * @return player with largest army
     */
    public PlayerDTO getPlayerWithArmyBonus() {
        return playerWithArmyBonus;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        KnightUpdateMessage that = (KnightUpdateMessage) obj;
        return Objects.equals(playerWithArmyBonus, that.playerWithArmyBonus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), playerWithArmyBonus);
    }
}
