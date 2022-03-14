package de.uol.swp.common.game.message;

import com.google.common.collect.Multimap;
import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.Port;
import de.uol.swp.common.game.board.PortCoord;
import de.uol.swp.common.game.board.ResourceTile;

import java.util.Map;
import java.util.Objects;

/**
 * Message containing Initial Settings for a Board
 */
public class InitialBoardMessage extends AbstractGameMessage {

    @SuppressWarnings("java:S1948")
    private final Multimap<Integer, Coord> hexNumbers;
    private final Map<Coord, ResourceTile> resourceTileMap;
    private final Map<PortCoord, Port> ports;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the gameSession
     * @param hexNumbers      the multimap containing the field value
     * @param resourceTileMap the Map containing the resource assignment
     * @param ports           the map containing the port coords
     */
    public InitialBoardMessage(String gameSessionName, Multimap<Integer, Coord> hexNumbers, Map<Coord, ResourceTile> resourceTileMap,
                               Map<PortCoord, Port> ports) {
        super(gameSessionName);
        this.hexNumbers = hexNumbers;
        this.resourceTileMap = resourceTileMap;
        this.ports = ports;
    }

    /**
     * Getter for the map containing field values
     *
     * @return Multimap containing numbers
     */
    public Multimap<Integer, Coord> getHexNumbers() {
        return hexNumbers;
    }

    /**
     * Getter for the Map containing the Resource tiles
     *
     * @return Map containing resource assignement
     */
    public Map<Coord, ResourceTile> getResourceTileMap() {
        return resourceTileMap;
    }

    /**
     * Returns a map of the board with the coordinates as keys and ports as values
     *
     * @return a map of the board with the coordinates as keys and ports as values
     */
    public Map<PortCoord, Port> getPorts() {
        return ports;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        InitialBoardMessage that = (InitialBoardMessage) object;
        return Objects.equals(hexNumbers, that.hexNumbers) && Objects.equals(resourceTileMap, that.resourceTileMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), hexNumbers, resourceTileMap);
    }
}



