package de.uol.swp.server.game.board;

import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.dto.MapNode;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.mapnode.NodeOffsetHelper;
import de.uol.swp.server.game.mapobject.CornerPiece;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides useful helper methods for the board class
 */
public final class BoardUtils {

    private BoardUtils() {

    }

    /**
     * Returns an array with all hexes that are direct neighbours of a given hex
     * <p>
     * All coordinates that point to water are left out.
     *
     * @param board the board object this method is called from
     * @param coord any <b>Hex</b> {@code Coord} on the board
     * @return an array with all hexes that are direct neighbours of a given hex but not in water
     */
    public static Coord[] getHexNeighboursWithoutWater(Board board, Coord coord) {
        List<Coord> coords = new ArrayList<>();
        for (Coord offset : NodeOffsetHelper.getHexagonNeighbourOffset()) {
            Coord temp = Coord.add(coord, offset);
            if (board.hexagonIsOnMap(temp)) {
                coords.add(temp);
            }
        }
        return coords.toArray(new Coord[0]);
    }

    /**
     * Returns an array with all hexagons that are connected to a given corner
     * <p>
     * All coordinates that point to water are left out.
     *
     * @param board the board object this method is called from
     * @param coord any <b>Corner</b> {@code Coord} on the board
     * @return an array with all hexagons that are connected to a given corner but are not in water
     */
    public static Coord[] getCornerHexagonsWithoutWater(Board board, Coord coord) {
        List<Coord> coords = new ArrayList<>();
        Coord hex = new Coord(coord.getX(), coord.getY());
        for (Coord offset : NodeOffsetHelper.getHexagonOffsetFromCornerDirection(coord.getDir())) {
            Coord temp = Coord.add(hex, offset);
            if (board.hexagonIsOnMap(temp)) {
                coords.add(temp);
            }
        }
        return coords.toArray(new Coord[0]);
    }

    /**
     * Returns an array with all corners that are direct neighbours of a given corner
     * <p>
     * All coordinates that point to water are left out.
     *
     * @param board the board object this method is called from
     * @param coord any <b>Corner</b> {@code Coord} on the board
     * @return an array with all corners that are direct neighbours of a given corner but not in water
     */
    public static Coord[] getCornerNeighboursWithoutWater(Board board, Coord coord) {
        List<Coord> coords = new ArrayList<>();
        for (Coord offset : NodeOffsetHelper.getCornerNeighbourOffset(coord.getDir())) {
            Coord temp = Coord.add(coord, offset);
            if (board.cornerIsOnMap(temp)) {
                coords.add(temp);
            }
        }
        return coords.toArray(new Coord[0]);
    }

    /**
     * Returns an array with all edges that are direct neighbours of a given edge
     * <p>
     * All coordinates that point to water are left out.
     *
     * @param board the board object this method is called from
     * @param coord any <b>Edge</b> {@code Coord} on the board
     * @return an array with all edges that are direct neighbours of a given edge but not in water
     */
    public static Coord[] getEdgeNeighboursWithoutWater(Board board, Coord coord) {
        List<Coord> coords = new ArrayList<>();
        for (Coord offset : NodeOffsetHelper.getEdgeNeighbourOffset(coord.getDir())) {
            Coord temp = Coord.add(coord, offset);
            if (board.edgeIsOnMap(temp)) {
                coords.add(temp);
            }
        }
        return coords.toArray(new Coord[0]);
    }

    /**
     * Returns a {@code Set} with all edges that are direct neighbours of a given corner and on which the player has a road
     *
     * @param board  the board object this method is called from
     * @param coord  any <b>Corner</b> {@code Coord} on the board
     * @param player the player who the roads belong to
     * @return a {@code Set} containing the coordinates of all direct neighbour on which the player has a road
     */
    public static Set<Coord> getConnectedPlayerRoadsFromCorner(Board board, Coord coord, Player player) {
        return getEdgeNeighboursFromCornerWithoutWater(board, coord).stream()
                .filter(edgeCoord -> board.getRoadPiece(edgeCoord) != null && board.getRoadPiece(edgeCoord).getPlayer().equals(player))
                .collect(Collectors.toSet());
    }

    /**
     * Returns a {@code Set} with all edges that are direct neighbours of a given edge and on which the player has a road
     *
     * @param board    the board object this method is called from
     * @param coord    any <b>Edge</b> {@code Coord} on the board
     * @param playerId the player who the roads belong to
     * @return a {@code Set} containing the coordinates of all direct neighbour on which the player has a road
     */
    public static Set<Coord> getConnectedPlayerRoadsFromEdge(Board board, Coord coord, int playerId) {
        return Arrays.stream(getEdgeNeighboursWithoutWater(board, coord))
                .filter(edgeCoord -> board.getRoadPiece(edgeCoord) != null && board.getRoadPiece(edgeCoord).getPlayerId() == playerId)
                .collect(Collectors.toSet());
    }

    /**
     * Returns a {@code Set} with all edges that are direct neighbours of a given edge, on which the player has a road
     * and between which there is no other settlement
     *
     * @param board    the board object this method is called from
     * @param coord    any <b>Edge</b> {@code Coord} on the board
     * @param playerId the player who the roads belong to
     * @return a {@code Set} containing the coordinates of all direct neighbour on which the player has a road and between which there is no other settlement
     */
    public static Set<Coord> getConnectedPlayerRoadsWithoutBreak(Board board, Coord coord, int playerId) {
        return Arrays.stream(getEdgeNeighboursWithoutWater(board, coord))
                .filter(edgeCoord -> board.getRoadPiece(edgeCoord) != null && board.getRoadPiece(edgeCoord).getPlayerId() == playerId &&
                        !board.cornerHasOtherPlayerSettlement(Coord.getCornerFromEdges(coord, edgeCoord), playerId))
                .collect(Collectors.toSet());
    }

    /**
     * Returns a Set with all edges on the board that have a valid path (no settlement between) to the starting edge
     * and on which the player has a road
     * <p>
     * The returned set does not contain the startCoord
     *
     * @param board      the board object this method is called from
     * @param startCoord any <b>Edge</b> {@code Coord} on the board
     * @param playerId   the player who the roads belong to
     * @return a set containing the coordinates of all connected player roads (including the startCoord!)
     */
    public static Set<Coord> getAllConnectedPlayerRoads(Board board, Coord startCoord, int playerId) {
        Set<Coord> visited = new LinkedHashSet<>();
        Deque<Coord> stack = new ArrayDeque<>();
        stack.push(startCoord);
        while (!stack.isEmpty()) {
            Coord curCoord = stack.pop();
            Set<Coord> connectedRoads = getConnectedPlayerRoadsFromEdge(board, curCoord, playerId);
            for (Coord coord : connectedRoads) {
                Coord cornerCoord = Coord.getCornerFromEdges(coord, curCoord);
                if (!visited.contains(coord) && !board.cornerHasOtherPlayerSettlement(cornerCoord, playerId)) {
                    stack.push(coord);
                    visited.add(coord);
                }
            }
        }
        visited.add(startCoord);
        return visited;
    }

    /**
     * Returns a Set with all edges on the board that are connected to the given corner
     *
     * @param board the board object this method is called from
     * @param coord any <b>Corner</b> {@code Coord} on the board
     * @return a set with all edges on the board that are connected to the given corner
     */
    public static Set<Coord> getEdgeNeighboursFromCornerWithoutWater(Board board, Coord coord) {
        Set<Coord> edges = new HashSet<>();
        Coord[] offsets = NodeOffsetHelper.getEdgeOffsetFromCornerDirection(coord.getDir());
        for (Coord offset : offsets) {
            Coord edgeCoord = new Coord(coord.getX() + offset.getX(), coord.getY() + offset.getY(), offset.getDir(), MapNode.EDGE);
            if (board.edgeIsOnMap(edgeCoord)) {
                edges.add(edgeCoord);
            }
        }
        return edges;
    }

    /**
     * Return a Set of all Players who own buildings on one of the two corners of an edge.
     *
     * @param board the board object this method is called from
     * @param coord the coord of the edge
     */
    public static Set<Player> getPlayersFromEdge(Board board, Coord coord) {
        var corners = Coord.getCornersFromEdge(coord);
        return Arrays.stream(corners).filter(c -> board.getCornerPiece(c) != null).map(c -> board.getCornerPiece(c).getPlayer())
                .collect(Collectors.toSet());
    }

    /**
     * Return a Set of Players who own buildings on one of the corners of a hexagon.
     *
     * @param board the board object this method is called from
     * @param coord the coord of the hex
     */
    public static Set<Player> getPlayersAdjacentToHex(Board board, Coord coord) {
        var corners = Coord.getCornersFromHex(coord);
        return Arrays.stream(corners).filter(c -> board.getCornerPiece(c) != null).map(c -> board.getCornerPiece(c).getPlayer())
                .collect(Collectors.toSet());
    }

    /**
     * Returns a Set with all instances of CornerPiece that are adjacent to the given hex
     *
     * @param board the board object this method is called from
     * @param coord any <b>Hexagon</b> {@code Coord} on the board
     * @return a Set with all instances of CornerPiece that are adjacent to the given hex
     */
    public static Set<CornerPiece> getCornerPiecesAdjacentToHex(Board board, Coord coord) {
        var corners = Coord.getCornersFromHex(coord);
        return Arrays.stream(corners).map(board::getCornerPiece).filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
