package de.uol.swp.common.game.board;

import de.uol.swp.common.game.dto.MapNode;

import java.io.Serializable;
import java.util.*;

/**
 * Objects of this class are used to store the coordinates of a board node
 */
@SuppressWarnings({"PMD.ShortVariable", "PMD.CyclomaticComplexity", "PMD.ControlStatementBraces"})
public class Coord implements Serializable {

    private static final Random RANDOM = new Random();
    private final int x;
    private final int y;
    private final MapNode nodeType;
    private Direction dir;

    /**
     * Constructor used for hexagon nodes
     *
     * @param x the horizontal coordinate of the hexagon
     * @param y the diagonal coordinate of the hexagon
     */
    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
        this.nodeType = MapNode.HEX;
    }

    /**
     * Constructor used for edge and corner nodes
     *
     * @param x        the horizontal coordinate of the parent hexagon
     * @param y        the diagonal coordinate of the parent hexagon
     * @param dir      the direction of the node relative to the parent hexagon
     * @param nodeType the type of node (either edge or corner)
     */
    public Coord(int x, int y, Direction dir, MapNode nodeType) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.nodeType = nodeType;
    }

    /**
     * Adds two board coordinates by adding their x and y values together, forwarding the direction of the second coordinate
     * and forwarding the node type of the first coordinate
     *
     * @param first  the first coordinate
     * @param second the second coordinate
     * @return the sum of the two coordinates
     */
    public static Coord add(Coord first, Coord second) {
        if (first.nodeType != second.nodeType) throw new IllegalArgumentException();
        return new Coord(first.x + second.x, first.y + second.y, second.dir, first.nodeType);
    }

    /**
     * Creates an Edge Coord instance
     *
     * @param x   the horizontal coordinate of the parent hexagon
     * @param y   the diagonal coordinate of the parent hexagon
     * @param dir the direction of the edge relative to the parent hexagon
     * @return an Edge Coord instance
     */
    public static Coord newEdge(int x, int y, Direction dir) {
        return new Coord(x, y, dir, MapNode.EDGE);
    }

    /**
     * Creates a Corner Coord instance
     *
     * @param x   the horizontal coordinate of the parent hexagon
     * @param y   the diagonal coordinate of the parent hexagon
     * @param dir the direction of the corner relative to the parent hexagon
     * @return a Corner Coord instance
     */
    public static Coord newCorner(int x, int y, Direction dir) {
        return new Coord(x, y, dir, MapNode.CORNER);
    }

    /**
     * Returns an array containing the corner coordinates for a given edge coordinate
     *
     * @param coord any <b>Edge</b> {@code Coord}
     * @return an array containing the corner coordinates for a given edge coordinate
     */
    public static Coord[] getCornersFromEdge(Coord coord) {
        if (!coord.nodeType.equals(MapNode.EDGE)) throw new IllegalArgumentException();
        Coord[] retCoords;
        switch (coord.dir) {
            case SOUTH:
                retCoords = new Coord[]{new Coord(coord.x - 1, coord.y + 1, Direction.NORTH, MapNode.CORNER), new Coord(coord.x, coord.y,
                        Direction.SOUTH, MapNode.CORNER)};
                break;
            case WEST:
                retCoords = new Coord[]{new Coord(coord.x, coord.y - 1, Direction.SOUTH, MapNode.CORNER), new Coord(coord.x - 1, coord.y + 1,
                        Direction.NORTH, MapNode.CORNER)};
                break;
            case NORTH:
                retCoords = new Coord[]{new Coord(coord.x, coord.y, Direction.NORTH, MapNode.CORNER), new Coord(coord.x, coord.y - 1,
                        Direction.SOUTH, MapNode.CORNER)};
                break;
            default:
                throw new IllegalArgumentException(coord.dir + " not a valid Edge Direction");
        }
        return retCoords;
    }

    /**
     * Returns an array containing the edge coordinates for a given hexagon coordinate
     *
     * @param coord any <b>Hexagon</b> {@code Coord}
     * @return an array containing the edge coordinates for a given hexagon coordinate
     */
    public static Coord[] getEdgesFromHex(Coord coord) {
        if (!coord.nodeType.equals(MapNode.HEX)) throw new IllegalArgumentException();
        return new Coord[]{
                new Coord(coord.x + 1, coord.y - 1, Direction.SOUTH, MapNode.EDGE), new Coord(coord.x + 1, coord.y,
                Direction.WEST, MapNode.EDGE),
                new Coord(coord.x, coord.y + 1, Direction.NORTH, MapNode.EDGE), new Coord(coord.x, coord.y, Direction.SOUTH,
                MapNode.EDGE),
                new Coord(coord.x, coord.y, Direction.WEST, MapNode.EDGE), new Coord(coord.x, coord.y, Direction.NORTH,
                MapNode.EDGE)
        };
    }

    /**
     * Returns an array containing the corner coordinates for a given hexagon coordinate
     *
     * @param coord any <b>Hexagon</b> {@code Coord}
     * @return an array containing the corner coordinates for a given hexagon coordinate
     */
    public static Coord[] getCornersFromHex(Coord coord) {
        if (!coord.nodeType.equals(MapNode.HEX)) throw new IllegalArgumentException();
        return new Coord[]{
                new Coord(coord.x, coord.y, Direction.NORTH, MapNode.CORNER), new Coord(coord.x + 1, coord.y - 1, Direction.SOUTH,
                MapNode.CORNER),
                new Coord(coord.x, coord.y + 1, Direction.NORTH, MapNode.CORNER), new Coord(coord.x, coord.y, Direction.SOUTH,
                MapNode.CORNER),
                new Coord(coord.x - 1, coord.y + 1, Direction.NORTH, MapNode.CORNER), new Coord(coord.x, coord.y - 1,
                Direction.SOUTH, MapNode.CORNER)
        };
    }

    /**
     * Returns a specific edge coordinate for a given hexagon coordinate and direction
     *
     * @param coord any <b>Hexagon</b> {@code Coord}
     * @param dir   the direction of the edge
     * @return a specific edge coordinate for a given hexagon coordinate and direction
     */
    public static Coord getEdgeFromHexWithDir(Coord coord, Direction dir) {
        if (!coord.nodeType.equals(MapNode.HEX)) throw new IllegalArgumentException();
        Coord retCoord;
        switch (dir) {
            case NORTHEAST:
                retCoord = new Coord(coord.x + 1, coord.y - 1, Direction.SOUTH, MapNode.EDGE);
                break;
            case EAST:
                retCoord = new Coord(coord.x + 1, coord.y, Direction.WEST, MapNode.EDGE);
                break;
            case SOUTHEAST:
                retCoord = new Coord(coord.x, coord.y + 1, Direction.NORTH, MapNode.EDGE);
                break;
            case SOUTHWEST:
                retCoord = new Coord(coord.x, coord.y, Direction.SOUTH, MapNode.EDGE);
                break;
            case WEST:
                retCoord = new Coord(coord.x, coord.y, Direction.WEST, MapNode.EDGE);
                break;
            case NORTHWEST:
                retCoord = new Coord(coord.x, coord.y, Direction.NORTH, MapNode.EDGE);
                break;
            default:
                throw new IllegalArgumentException(dir + " not legal");
        }
        return retCoord;
    }

    /**
     * Returns the {@code Coord} of the corner between two edges
     *
     * @param firstCoord  the {@code Coord} of the first <b>Edge</b>
     * @param secondCoord the {@code Coord} of the second <b>Edge</b>
     * @return the {@code Coord} of the corner between two edges
     */
    public static Coord getCornerFromEdges(Coord firstCoord, Coord secondCoord) {
        if (secondCoord == null) return null;
        List<Coord> firstList = new ArrayList<>(Arrays.asList(getCornersFromEdge(firstCoord)));
        List<Coord> secondList = new ArrayList<>(Arrays.asList(getCornersFromEdge(secondCoord)));
        if (firstList.retainAll(secondList)) {
            return firstList.get(0);
        } else {
            return null;
        }
    }

    /**
     * Get A random Valid Hex Coordinate
     *
     * @return a random valid Hex Coordinate
     */
    public static Coord getRandomHex() {
        int xCoord = -2 + RANDOM.nextInt(5);
        int yCoord;
        if (xCoord == -2) {
            yCoord = RANDOM.nextInt(3);
        } else if (xCoord == -1) {
            yCoord = -1 + RANDOM.nextInt(4);
        } else if (xCoord == 0) {
            yCoord = -2 + RANDOM.nextInt(5);
        } else if (xCoord == 1) {
            yCoord = -2 + RANDOM.nextInt(4);
        } else {
            yCoord = -2 + RANDOM.nextInt(3);
        }
        return new Coord(xCoord, yCoord);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Direction getDir() {
        return dir;
    }

    public MapNode getNodeType() {
        return nodeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, dir);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coord coord = (Coord) o;
        return x == coord.x && y == coord.y && dir == coord.dir && nodeType == coord.nodeType;
    }

    @Override
    public String toString() {
        return "EdgeCoord{" +
                "x=" + x +
                ", y=" + y +
                ", dir=" + dir +
                '}';
    }

}