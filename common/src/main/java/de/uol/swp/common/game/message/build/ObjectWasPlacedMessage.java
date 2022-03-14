package de.uol.swp.common.game.message.build;

import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.message.AbstractGameMessage;

import java.util.Objects;

/**
 * Message notifying the players of a session that a new object was successfully placed by the player this
 * message refers to
 */
public class ObjectWasPlacedMessage extends AbstractGameMessage {

    private final PlayerDTO player;
    private final PieceType buildingType;
    private final Coord coord;


    /**
     * Constructor
     *
     * @param gameSessionName The name of the session
     * @param player          the player who placed the object
     * @param buildingType    the building type
     * @param coord           the coords at which the object as placed
     */
    public ObjectWasPlacedMessage(String gameSessionName, PlayerDTO player, PieceType buildingType, Coord coord) {
        super(gameSessionName);
        this.player = player;
        this.buildingType = buildingType;
        this.coord = coord;
    }

    /**
     * Returns the player who placed the object
     *
     * @return the player who placed the object
     */
    public PlayerDTO getPlayer() {
        return player;
    }

    public PieceType getBuildingType() {
        return buildingType;
    }

    public Coord getCoord() {
        return coord;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        ObjectWasPlacedMessage that = (ObjectWasPlacedMessage) obj;
        return Objects.equals(player, that.player) && buildingType == that.buildingType && Objects.equals(coord, that.coord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), player, buildingType, coord);
    }
}
