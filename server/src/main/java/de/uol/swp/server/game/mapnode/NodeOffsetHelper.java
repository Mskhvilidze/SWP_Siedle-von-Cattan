package de.uol.swp.server.game.mapnode;

import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.Direction;
import de.uol.swp.common.game.dto.MapNode;

/**
 * A class which stores the corner neighbour offsets.
 */
public final class NodeOffsetHelper {

    private static final Coord[] CORNER_NEIGHBOUR_OFFSET_NORTH = {
            new Coord(1, -2, Direction.SOUTH, MapNode.CORNER), new Coord(1, -1, Direction.SOUTH, MapNode.CORNER),
            new Coord(0, -1, Direction.SOUTH, MapNode.CORNER)
    };
    private static final Coord[] CORNER_NEIGHBOUR_OFFSET_SOUTH = {
            new Coord(-1, 1, Direction.NORTH, MapNode.CORNER), new Coord(0, 1, Direction.NORTH, MapNode.CORNER),
            new Coord(-1, 2, Direction.NORTH, MapNode.CORNER)
    };
    private static final Coord[] CORNER_HEXAGON_OFFSET_SOUTH = {
            new Coord(-1, 1), new Coord(0, 1), new Coord(0, 0)
    };
    private static final Coord[] CORNER_HEXAGON_OFFSET_NORTH = {
            new Coord(1, -1), new Coord(0, -1), new Coord(0, 0)
    };
    private static final Coord[] EDGE_NEIGHBOUR_OFFSET_NORTH = {
            new Coord(1, -1, Direction.WEST, MapNode.EDGE), new Coord(1, -1, Direction.SOUTH, MapNode.EDGE),
            new Coord(0, 0, Direction.WEST, MapNode.EDGE), new Coord(0, -1, Direction.SOUTH, MapNode.EDGE)
    };
    private static final Coord[] EDGE_NEIGHBOUR_OFFSET_WEST = {
            new Coord(0, -1, Direction.SOUTH, MapNode.EDGE), new Coord(0, 0, Direction.NORTH, MapNode.EDGE),
            new Coord(0, 0, Direction.SOUTH, MapNode.EDGE), new Coord(-1, 1, Direction.NORTH, MapNode.EDGE)
    };
    private static final Coord[] EDGE_NEIGHBOUR_OFFSET_SOUTH = {
            new Coord(-1, 1, Direction.NORTH, MapNode.EDGE), new Coord(0, 0, Direction.WEST, MapNode.EDGE),
            new Coord(0, 1, Direction.NORTH, MapNode.EDGE), new Coord(0, 1, Direction.WEST, MapNode.EDGE)
    };
    private static final Coord[] HEXAGON_NEIGHBOUR_OFFSET = {
            new Coord(1, -1), new Coord(1, 0), new Coord(0, 1),
            new Coord(-1, 1), new Coord(-1, 0), new Coord(0, -1)
    };
    private static final Coord[] EDGE_NEIGHBOUR_CORNER_NORTH = {
            new Coord(0, 0, Direction.NORTH, MapNode.EDGE),
            new Coord(1, -1, Direction.SOUTH, MapNode.EDGE),
            new Coord(1, -1, Direction.WEST, MapNode.EDGE)
    };

    private static final Coord[] EDGE_NEIGHBOUR_CORNER_SOUTH = {
            new Coord(0, 0, Direction.SOUTH, MapNode.EDGE),
            new Coord(0, 1, Direction.NORTH, MapNode.EDGE),
            new Coord(0, 1, Direction.WEST, MapNode.EDGE)
    };

    private NodeOffsetHelper() {

    }

    /**
     * Returns an array containing the offsets that are needed to calculate the neighbouring corners
     *
     * @param direction the direction of the corner from which you want to calculate the neighbours
     * @return an array containing the offsets that are needed to calculate the neighbouring corners
     * @implNote the order of the offsets is clockwise beginning at the highest leftmost neighbour
     */
    public static Coord[] getCornerNeighbourOffset(Direction direction) {
        Coord[] retCoords;
        switch (direction) {
            case NORTH:
                retCoords = CORNER_NEIGHBOUR_OFFSET_NORTH;
                break;
            case SOUTH:
                retCoords = CORNER_NEIGHBOUR_OFFSET_SOUTH;
                break;
            default:
                throw new IllegalArgumentException(direction + " not a valid Corner Direction");
        }
        return retCoords;
    }

    /**
     * Returns an array containing the offsets that are needed to calculate the connected hexagons
     *
     * @param direction the direction of the corner from which you want to calculate the connected hexagons
     * @return an array containing the offsets that are needed to calculate the connected hexagons
     */

    public static Coord[] getHexagonOffsetFromCornerDirection(Direction direction) {
        Coord[] retCoords;
        switch (direction) {
            case SOUTH:
                retCoords = CORNER_HEXAGON_OFFSET_SOUTH;
                break;
            case NORTH:
                retCoords = CORNER_HEXAGON_OFFSET_NORTH;
                break;
            default:
                throw new IllegalArgumentException(direction + " not a valid Corner Direction");
        }
        return retCoords;
    }

    /**
     * Returns an array containing the offsets that are needed to calculate the neighbouring edges
     *
     * @param direction the direction of the edge from which you want to calculate the neighbours
     * @return an array containing the offsets that are needed to calculate the neighbouring edges
     * @implNote the order of the offsets is clockwise beginning at the highest leftmost neighbour
     */
    public static Coord[] getEdgeNeighbourOffset(Direction direction) {
        Coord[] retCoords;
        switch (direction) {
            case NORTH:
                retCoords = EDGE_NEIGHBOUR_OFFSET_NORTH;
                break;
            case WEST:
                retCoords = EDGE_NEIGHBOUR_OFFSET_WEST;
                break;
            case SOUTH:
                retCoords = EDGE_NEIGHBOUR_OFFSET_SOUTH;
                break;
            default:
                throw new IllegalArgumentException(direction + " not a valid Edge Direction");
        }
        return retCoords;
    }

    /**
     * Returns an array containing the offsets that are needed to calculate the neighbouring hexes
     *
     * @return an array containing the offsets that are needed to calculate the neighbouring hexes
     * @implNote the order of the offsets is clockwise beginning at the top right neighbour
     */
    public static Coord[] getHexagonNeighbourOffset() {
        return HEXAGON_NEIGHBOUR_OFFSET.clone();
    }


    /**
     * Return an array containing the offsets needed for calculating the neighbouring edges.
     * Used to check for roads adjacent to a corner.
     *
     * @return an array containing the offsets that are needed to calculate the neighbouring edges from a corner
     */
    public static Coord[] getEdgeOffsetFromCornerDirection(Direction direction) {
        Coord[] retCoords;
        switch (direction) {
            case SOUTH:
                retCoords = EDGE_NEIGHBOUR_CORNER_SOUTH;
                break;
            case NORTH:
                retCoords = EDGE_NEIGHBOUR_CORNER_NORTH;
                break;
            default:
                throw new IllegalArgumentException(direction + " not a valid Corner Direction");
        }
        return retCoords;
    }
}
