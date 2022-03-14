package de.uol.swp.common.game.response.build;

import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.response.AbstractGameResponse;

import java.util.Objects;


/**
 * Response to a player from an PlaceObjectRequest, contains a boolean wherever the request was allowed or denied.
 */
public class PlaceObjectResponse extends AbstractGameResponse {
    private final PieceType buildingType;
    private final boolean canBuildObject;
    private final Coord coord;
    private final String message;

    /**
     * Constructor
     *
     * @param gameSessionName gameSessionName
     * @param buildingType    type of object
     * @param canBuildObject  is placing the object allowed
     * @param coord           the coordinates of the request.
     * @param message         a Message used to communicate why the build failed
     */
    public PlaceObjectResponse(String gameSessionName, PieceType buildingType, boolean canBuildObject, Coord coord, String message) {
        super(gameSessionName);
        this.buildingType = buildingType;
        this.canBuildObject = canBuildObject;
        this.coord = coord;
        this.message = message;
    }

    /**
     * getter for building Type
     *
     * @return buildingType
     */
    public PieceType getBuildingType() {
        return buildingType;
    }

    /**
     * Check whenever a request was allowed or denied
     *
     * @return canBuildObject
     */
    public boolean isPlayerAllowedToBuild() {
        return canBuildObject;
    }

    /**
     * Getter for coordinates
     *
     * @return coord
     */
    public Coord getCoord() {
        return coord;
    }

    /**
     * Getter for the message
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        PlaceObjectResponse that = (PlaceObjectResponse) obj;
        return canBuildObject == that.canBuildObject && buildingType == that.buildingType && Objects.equals(coord, that.coord) && Objects.equals(
                message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), buildingType, canBuildObject, coord, message);
    }
}
