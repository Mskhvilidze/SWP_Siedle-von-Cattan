package de.uol.swp.common.game.message.inventory;

import de.uol.swp.common.game.board.PieceTypeEnumMap;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.message.AbstractGameMessage;

import java.util.Objects;

/**
 * Message that notifies the client about changes in a player's inventory.
 */
public class BuildableObjectMessage extends AbstractGameMessage {

    private final PlayerDTO player;
    private final PieceTypeEnumMap pieceType;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the session
     * @param player          the player who's affected
     * @param pieceType       the current inventory state
     */


    public BuildableObjectMessage(String gameSessionName, PlayerDTO player, PieceTypeEnumMap pieceType) {
        super(gameSessionName);
        this.pieceType = pieceType;
        this.player = player;
    }

    /**
     * Returns the pieceTypeEnumMap
     *
     * @return the pieceTypeEnumMap
     */
    public PieceTypeEnumMap getPieceType() {
        return pieceType;
    }

    /**
     * Returns the the player who's affected
     *
     * @return the the player who's affected
     */
    public PlayerDTO getPlayer() {
        return player;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        de.uol.swp.common.game.message.inventory.BuildableObjectMessage that = (de.uol.swp.common.game.message.inventory.BuildableObjectMessage) obj;
        return Objects.equals(player, that.player) && Objects.equals(pieceType, that.pieceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), player, pieceType);
    }
}

