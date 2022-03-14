package de.uol.swp.client.game.mapobject;


import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.Direction;
import de.uol.swp.common.game.dto.MapNode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * A class which represents the hexagons of the board.
 */
public class Hexagon extends Polygon {

    private final Coord[] corners = new Coord[6];
    private final Coord[] edges = new Coord[6];
    private final Coord hexCoords;
    private final BooleanProperty active = new SimpleBooleanProperty();

    /**
     * Constructor
     *
     * @param x the x coordinates of the hexagon
     * @param y the y coordinates of the hexagon
     */
    public Hexagon(int x, int y) {//NOPMD
        hexCoords = new Coord(x, y);
        edges[0] = Coord.getEdgeFromHexWithDir(hexCoords, Direction.NORTHEAST);
        edges[1] = Coord.getEdgeFromHexWithDir(hexCoords, Direction.EAST);
        edges[2] = Coord.getEdgeFromHexWithDir(hexCoords, Direction.SOUTHEAST);
        edges[3] = Coord.getEdgeFromHexWithDir(hexCoords, Direction.SOUTHWEST);
        edges[4] = Coord.getEdgeFromHexWithDir(hexCoords, Direction.WEST);
        edges[5] = Coord.getEdgeFromHexWithDir(hexCoords, Direction.NORTHWEST);

        corners[0] = new Coord(x, y, Direction.NORTH, MapNode.CORNER);
        corners[1] = new Coord(x + 1, y - 1, Direction.SOUTH, MapNode.CORNER);
        corners[2] = new Coord(x, y + 1, Direction.NORTH, MapNode.CORNER);
        corners[3] = new Coord(x, y, Direction.SOUTH, MapNode.CORNER);
        corners[4] = new Coord(x - 1, y + 1, Direction.NORTH, MapNode.CORNER);
        corners[5] = new Coord(x, y - 1, Direction.SOUTH, MapNode.CORNER);

        setFill(Color.ANTIQUEWHITE);
        setStroke(Color.BLACK);
    }

    /**
     * Returns whether this hexagon is active, which means that its robber is highlighted
     * and clicking it should send a robber placing request
     *
     * @return {@code true} if this hexagon is active
     */
    public boolean isActive() {
        return active.get();
    }

    /**
     * Sets whether this hexagon is active, which means that its robber will be highlighted
     * and clicking it will send a robber placing request
     *
     * @param active whether the hexagon will be active
     */
    public void setActive(boolean active) {
        this.active.set(active);
    }

    /**
     * Return the edges of a hexagon
     *
     * @return the edges of a hexagon
     */
    public Coord[] getEdges() {
        return edges.clone();
    }

    /**
     * Returns the corners of a hexagon
     *
     * @return the corners of a hexagon
     */
    public Coord[] getCorners() {
        return corners.clone();
    }

    /**
     * Returns the x coordinates of the hexagon
     *
     * @return the x coordinates of the hexagon
     */
    public int getX() {
        return hexCoords.getX();
    }

    /**
     * Returns the y coordinates of the hexagon
     *
     * @return the y coordinates of the hexagon
     */
    public int getY() {
        return hexCoords.getY();
    }

    /**
     * Returns the coordinates of the hexagon
     *
     * @return the coordinates of the hexagon
     */
    public Coord getCoord() {
        return hexCoords;
    }
}

