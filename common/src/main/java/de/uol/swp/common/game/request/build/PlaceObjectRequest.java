package de.uol.swp.common.game.request.build;

import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.request.AbstractGameRequest;

import java.util.Objects;


/**
 * Request from a player who wants to place a new object at the specified location.
 */
public class PlaceObjectRequest extends AbstractGameRequest {
    private final PieceType objectToPlace;
    private final Coord coord;

    /**
     * Constructor
     *
     * @param gameSessionName the gameSessionname
     * @param objectToPlace   the object Type
     * @param coord           the coordinates
     */
    public PlaceObjectRequest(String gameSessionName, PieceType objectToPlace, Coord coord) {
        super(gameSessionName);
        this.objectToPlace = objectToPlace;
        this.coord = coord;
    }

    /**
     * Getter for the object
     *
     * @return objectToPlace
     */
    public PieceType getObjectToPlace() {
        return objectToPlace;
    }

    /**
     * Getter For coordinates
     *
     * @return coord
     */
    public Coord getCoord() {
        return coord;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        PlaceObjectRequest that = (PlaceObjectRequest) obj;
        return Objects.equals(objectToPlace, that.objectToPlace) && Objects.equals(coord, that.coord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), objectToPlace, coord);
    }
}
