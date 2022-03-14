package de.uol.swp.common.game.message;

import de.uol.swp.common.game.dto.PlayerDTO;

import java.util.Objects;

/**
 * Message sent to all players in a game session when a players victory points have been updated
 */
public class VPUpdateMessage extends AbstractGameMessage {

    private final PlayerDTO player;
    private final int vps;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the gameSession
     * @param player          the player whose vps will be updated
     * @param vps             the amount of vps
     */
    public VPUpdateMessage(String gameSessionName, PlayerDTO player, int vps) {
        super(gameSessionName);
        this.player = player;
        this.vps = vps;
    }

    /**
     * getter for the victory points
     *
     * @return the amount of vps
     */
    public int getVps() {
        return vps;
    }

    /**
     * getter for the player
     *
     * @return the player whose vps will be updated
     */
    public PlayerDTO getPlayer() {
        return player;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        VPUpdateMessage message = (VPUpdateMessage) object;
        return vps == message.vps && Objects.equals(player, message.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), player, vps);
    }
}
