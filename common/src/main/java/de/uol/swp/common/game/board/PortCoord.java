package de.uol.swp.common.game.board;

import java.io.Serializable;

/**
 * Objects of this class are used to store the coordinates of a port.
 * <p>
 * Hex Coord is the coordinate of the hexagon that the port is on
 * and Edge Coord is the coordinate of the edge whose corners are valid settlement locations
 */
public class PortCoord implements Serializable {
    private final Coord portHexCoord;
    private final Coord portEdgeCoord;

    /**
     * Constructor
     *
     * @param portHexCoord  the {@code Coord} of the <b>Hexagon</b> that the port is on
     * @param portEdgeCoord the <b>Edge</b> {@code Coord} of the edge whose corners are valid settlement locations
     */
    public PortCoord(Coord portHexCoord, Coord portEdgeCoord) {
        this.portHexCoord = portHexCoord;
        this.portEdgeCoord = portEdgeCoord;
    }

    /**
     * Returns the {@code Coord} of the <b>Hexagon</b> that the port is on
     *
     * @return the {@code Coord} of the <b>Hexagon</b> that the port is on
     */
    public Coord getPortHexCoord() {
        return portHexCoord;
    }

    /**
     * Returns the <b>Edge</b> {@code Coord} of the edge whose corners are valid settlement locations
     *
     * @return the <b>Edge</b> {@code Coord} of the edge whose corners are valid settlement locations
     */
    public Coord getPortEdgeCoord() {
        return portEdgeCoord;
    }
}
