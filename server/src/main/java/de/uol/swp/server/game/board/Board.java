package de.uol.swp.server.game.board;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uol.swp.common.game.board.*;
import de.uol.swp.common.game.dto.MapNode;
import de.uol.swp.common.game.dto.PieceDTO;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.mapobject.*;
import de.uol.swp.server.game.session.GameSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * An object representing the catan board
 * <p>
 * No game logic will be run in this class and no game state altered.
 * The purpose of this class is to keep track of the board state and provide the {@link GameSession} class with necessary navigation methods.
 * <p>
 */
@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.CyclomaticComplexity"})
public class Board {

    private static final Logger LOG = LogManager.getLogger(Board.class);
    private static final Random RANDOM = new Random();

    private final int mapRadius;

    /**
     * This map is used to quickly check if a given coordinate is at a port
     */
    private final Map<Coord, Port> fastPortMap = new HashMap<>();
    private final Map<PortCoord, Port> ports = new HashMap<>();
    private final Map<Coord, CornerPiece> cornerMap = new HashMap<>();
    private final Map<Coord, RoadPiece> edgeMap = new HashMap<>();
    private final Map<Coord, ResourceTile> resourceTileMap = new HashMap<>();
    private final Robber robber = new Robber();
    private final Deque<ResourceTile> tiles = new ArrayDeque<>();
    private final Deque<Integer> numbers = new ArrayDeque<>();
    private final Map<Player, Path> playerRoadMap = new HashMap<>();
    private final Multimap<Integer, Coord> hexagonNumbers = HashMultimap.create();
    private Player playerWithLongestRoad;

    /**
     * Constructor
     *
     * @param mapRadius the radius of the board
     */
    public Board(int mapRadius) {
        this.mapRadius = mapRadius;
        initializeMapNodes();
        initializeTiles();
        initializeTileNumbers();
        initializePorts();
    }

    // -------------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------------

    private void initializeMapNodes() {
        for (int x = -mapRadius; x <= mapRadius; x++) {
            int radius1 = Math.max(-mapRadius, -x - mapRadius);
            int radius2 = Math.min(mapRadius, -x + mapRadius);
            for (int y = radius1; y <= radius2; y++) {
                resourceTileMap.put(new Coord(x, y), null);

                Coord[] edges = Coord.getEdgesFromHex(new Coord(x, y));
                for (Coord edgeCoord : edges) {
                    edgeMap.put(edgeCoord, null);
                }

                Coord[] corners = Coord.getCornersFromHex(new Coord(x, y));
                for (Coord cornerCoord : corners) {
                    cornerMap.put(cornerCoord, null);
                }
            }
        }
    }


    /**
     * Erstellt eine Deque mit den verfügbaren Werten für die Felder in der richtigen Reihenfolge.
     * A = 5 ,B = 2 ,C = 6 ,D = 3 ,E = 8 ,F = 10 ,G = 9 ,H = 12 ,I = 11 ,J = 4 ,K = 8 ,L = 10,
     * M = 9 ,N = 4 ,O = 5 ,P = 6 ,Q = 3 ,R = 11
     * Die Chips werden von außen nach innen nach alphabetischer Reihenfolge gegen den
     * Uhrzeiger gelegt. Dieses Muster wird durch die Reihenfolge hier widergespiegelt.
     */
    private void initialiseDiceNumbers() {
        numbers.add(5);
        numbers.add(2);
        numbers.add(6);
        numbers.add(3);
        numbers.add(8);
        numbers.add(10);
        numbers.add(9);
        numbers.add(12);
        numbers.add(11);
        numbers.add(4);
        numbers.add(8);
        numbers.add(10);
        numbers.add(9);
        numbers.add(4);
        numbers.add(5);
        numbers.add(6);
        numbers.add(3);
        numbers.add(11);
    }

    private void initializeTileNumbers() {
        initialiseDiceNumbers();
        for (int ring = mapRadius; ring >= 0; ring--) {
            for (int j = 0; j <= ring; j++) {
                Coord coord = new Coord(ring - j, -ring);
                putDiceNumberOnHexagon(coord);
            }
            for (int j = 1; j <= ring; j++) {
                Coord coord = new Coord(-j, -ring + j);
                putDiceNumberOnHexagon(coord);
            }
            for (int j = ring - 1; j >= 0; j--) {
                Coord coord = new Coord(-ring, ring - j);
                putDiceNumberOnHexagon(coord);
            }
            for (int j = 1; j <= ring; j++) {
                Coord coord = new Coord(-ring + j, ring);
                putDiceNumberOnHexagon(coord);
            }
            for (int j = 1, k = 1; j <= ring && k <= ring; j++, k++) {
                Coord coord = new Coord(j, ring - k);
                putDiceNumberOnHexagon(coord);
            }
            for (int j = 1; j <= ring - 1; j++) {
                Coord coord = new Coord(ring, -j);
                putDiceNumberOnHexagon(coord);
            }
        }
    }

    private void putDiceNumberOnHexagon(Coord coord) {
        if (!ResourceTile.isDesertTile(resourceTileMap.get(coord))) {
            hexagonNumbers.put(numbers.pop(), coord);
        }
    }

    /**
     * Initializes the resource on each tile in a random order
     */
    private void initializeTiles() {
        List<ResourceTile> tilesTemp = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            tilesTemp.add(ResourceTile.BRICK);
            tilesTemp.add(ResourceTile.ORE);
            tilesTemp.add(ResourceTile.GRAIN);
            tilesTemp.add(ResourceTile.WOOL);
            tilesTemp.add(ResourceTile.LUMBER);
        }
        tilesTemp.add(ResourceTile.GRAIN);
        tilesTemp.add(ResourceTile.WOOL);
        tilesTemp.add(ResourceTile.LUMBER);
        tilesTemp.add(ResourceTile.DESERT);
        Collections.shuffle(tilesTemp);
        tiles.addAll(tilesTemp);
        addResources();
    }

    /**
     * Adds the resource of each tile to the corresponding {@code Hexagon} object.
     * The coordinate of the desert tile gets added to the bandit.
     */
    private void addResources() {
        for (Coord hexCoord : resourceTileMap.keySet()) {
            if (ResourceTile.DESERT.equals(tiles.peek())) {
                robber.setCoord(hexCoord);
            }
            resourceTileMap.put(hexCoord, tiles.pop());
        }
    }

    /**
     * Getter for the initial values
     *
     * @return Map of Integers and Coords
     */
    public Multimap<Integer, Coord> getHexagonNumbers() {
        return hexagonNumbers;
    }

    /**
     * Getter ResourceTile Map
     *
     * @return a Map with the coordinates and ResourceTiles of a Game Board
     */
    public Map<Coord, ResourceTile> getResourceTileMap() {
        return resourceTileMap;
    }

    /**
     * Get a ResourceTile from a cooordinate
     *
     * @param coord coordinate
     * @return resourceTile
     */
    public ResourceTile getResourceTileFromCoord(Coord coord) {
        return resourceTileMap.get(coord);
    }

    /**
     * Getter for the robber
     *
     * @return the robber Object
     */
    public Robber getRobber() {
        return robber;
    }

    /**
     * Returns a map of this board with the coordinates as keys and ports as values
     *
     * @return a map of this board with the coordinates as keys and ports as values
     */
    public Map<PortCoord, Port> getPorts() {
        return ports;
    }

    /**
     * Initializes the ports on the board in a set clockwise order
     */
    private void initializePorts() {
        ports.put(new PortCoord(new Coord(2, -3), Coord.newEdge(2, -3, Direction.SOUTH)), Port.WOOL);
        ports.put(new PortCoord(new Coord(0, -3), Coord.newEdge(0, -2, Direction.NORTH)), Port.ANY);
        ports.put(new PortCoord(new Coord(-2, -1), Coord.newEdge(-1, -1, Direction.WEST)), Port.ORE);
        ports.put(new PortCoord(new Coord(-3, 1), Coord.newEdge(-2, 1, Direction.WEST)), Port.GRAIN);
        ports.put(new PortCoord(new Coord(-3, 3), Coord.newEdge(-2, 2, Direction.SOUTH)), Port.ANY);
        ports.put(new PortCoord(new Coord(-1, 3), Coord.newEdge(-1, 3, Direction.NORTH)), Port.LUMBER);
        ports.put(new PortCoord(new Coord(1, 2), Coord.newEdge(1, 2, Direction.NORTH)), Port.BRICK);
        ports.put(new PortCoord(new Coord(3, 0), Coord.newEdge(3, 0, Direction.WEST)), Port.ANY);
        ports.put(new PortCoord(new Coord(3, -2), Coord.newEdge(3, -2, Direction.SOUTH)), Port.ANY);
        ports.forEach((portCoord, port) -> {
            Coord[] corners = Coord.getCornersFromEdge(portCoord.getPortEdgeCoord());
            fastPortMap.put(corners[0], port);
            fastPortMap.put(corners[1], port);
        });
    }

    // -------------------------------------------------------------------------------
    // Board Management
    // -------------------------------------------------------------------------------

    /**
     * Places a piece on the board by putting it in the corresponding hashmap
     *
     * @param coord any {@code Coord} on the board
     * @param piece any {@code BuildablePiece}
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "java:S3824"})
    public void addPiece(Coord coord, BuildablePiece piece) {

        if (coord == null) {
            throw new IllegalArgumentException("coord must not be null");
        }

        switch (piece.getPieceType()) {
            case ROAD:
                if (edgeMap.get(coord) != null) {
                    throw new IllegalArgumentException("Coord for Road must be empty");
                }
                edgeMap.put(coord, (RoadPiece) piece);
                break;
            case SETTLEMENT:
                if (cornerMap.get(coord) != null) {
                    throw new IllegalArgumentException("Coord for settlement must be empty");
                }
                if (cornerIsAPort(coord)) {
                    piece.getPlayer().addPort(getPortFromCoord(coord));
                }
                cornerMap.put(coord, (SettlementPiece) piece);
                break;
            case CITY:
                if (cornerMap.get(coord) == null) {
                    throw new IllegalArgumentException("Coord for City must not be empty");
                }
                if (cornerMap.get(coord).getPieceType() != PieceType.SETTLEMENT) {
                    throw new IllegalArgumentException("Coord for City must contain settlement");
                }
                cornerMap.put(coord, (CityPiece) piece);
                break;
        }
    }

    /**
     * Return whenever a given coordinate is empty in the board map.
     * Empty coords are null
     *
     * @param coord coordinate to test
     * @return true if the coordinate is empty
     */
    public boolean isEmptyCoord(Coord coord) {
        switch (coord.getNodeType()) {
            case CORNER:
                return cornerMap.get(coord) == null;
            case EDGE:
                return edgeMap.get(coord) == null;
            case HEX:
            default:
                LOG.error("Coord type wrong");
        }
        return false;
    }

    /**
     * Return true when a given Player has a build a Settlement on the given coord
     *
     * @param coord  the {@code Coord} of the <b>Corner</b>
     * @param player owner
     * @return true when the player is the owner
     */
    public boolean hasPlayerSettlementOnCoord(Coord coord, Player player) {
        CornerPiece cornerPiece = getCornerPiece(coord);
        if (cornerPiece != null) {
            return cornerPiece.getPlayerId() == player.getPlayerId();
        }
        return false;
    }

    /**
     * Returns a random valid robber position
     *
     * @return a random valid robber position
     */
    public Coord getFreeRobberPos() {
        Coord[] coords = resourceTileMap.entrySet().stream()
                .filter(entry -> !ResourceTile.isDesertTile(entry.getValue()) && !robber.getCoord().equals(entry.getKey()))
                .map(Map.Entry::getKey).toArray(Coord[]::new);
        return coords[RANDOM.nextInt(coords.length)];
    }

    /**
     * Returns a random valid settlement position. Only useful for the setup state
     *
     * @return a random valid settlement position
     */
    public Coord getFreeSettlementPos() {
        Coord[] coords = cornerMap.entrySet().stream()
                .filter(entry -> entry.getValue() == null && !containsSettlementOnAdjacentCoords(entry.getKey()))
                .map(Map.Entry::getKey).toArray(Coord[]::new);
        return coords[RANDOM.nextInt(coords.length)];
    }

    /**
     * Method to update the current Position of the Robber
     */
    public void updateRobberPos(Coord newPosition) {
        robber.setCoord(newPosition);
    }

    /**
     * Returns true when a given corner has buildings on adjacent corner,
     * in that case the rules do not allow a new building to be placed here.
     *
     * @param coord the {@code Coord} of the <b>Corner</b>
     * @return true if this coord contains other settlements on adjacent coords.
     */
    public boolean containsSettlementOnAdjacentCoords(Coord coord) {
        Coord[] neighbors = BoardUtils.getCornerNeighboursWithoutWater(this, coord);

        for (Coord c : neighbors) {
            if (getCornerPiece(c) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true when When a corner Coord owns a Road owned by the player on one of the three
     * adjacent Edges. In this case the player is allowed to build a settlement here.
     *
     * @param coord  the {@code Coord} of the <b>Corner</b>
     * @param player the owner
     * @return true if the corner has a adjacent Road owned by player
     */
    public boolean containsPlayerRoadsAdjacentToCorner(Coord coord, Player player) {
        return !BoardUtils.getConnectedPlayerRoadsFromCorner(this, coord, player).isEmpty();
    }

    /**
     * Return true if a given Edge Coord has an adjacent Road owned by the player. In this case it
     * is allowed to place a new Road on this edge
     *
     * @param coord  the {@code Coord} of the <b>Edge</b>
     * @param player owner
     * @return true if the Edge contains an adjacent Road owned by the player.
     */
    public boolean containsPlayerRoadAdjacentToEdge(Coord coord, Player player) {
        Set<Coord> roads = BoardUtils.getConnectedPlayerRoadsFromEdge(this, coord, player.getPlayerId());
        if (roads.isEmpty()) {
            LOG.debug("Adjacent Edges Contain a PlayerRoad {}", false);
            return false;
        } else {
            LOG.debug("Adjacent Edges Contain a PlayerRoad {}", true);
            return true;
        }
    }


    /**
     * Checks if the given Edge has an adjacent settlement owned by the player, if it is true a road can be placed at this edge.
     *
     * @param coord  the {@code Coord} of the <b>Edge</b>
     * @param player the player to test
     * @return true if the given player owns a settlement or city at one of corners connected by the given edge
     */
    public boolean containsPlayerSettlementAdjacentToEdge(Coord coord, Player player) {
        var playersFromEdge = BoardUtils.getPlayersFromEdge(this, coord);
        for (Player edgePlayer : playersFromEdge) {
            if (edgePlayer.equals(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given Edge has at least one adjacent edge with a road from the given player on it
     * but no settlement from other players between them
     *
     * @param coord  the {@code Coord} of the <b>Edge</b>
     * @param player the owner of the road
     * @return {@code true} if the given Edge has at least one non broken connection to a players road
     */
    public boolean hasNoRoadBreakOnAtLeastOneSide(Coord coord, Player player) {
        Set<Coord> roads = BoardUtils.getConnectedPlayerRoadsFromEdge(this, coord, player.getPlayerId());
        return roads.stream().anyMatch(road -> !cornerHasOtherPlayerSettlement(Coord.getCornerFromEdges(coord, road), player.getPlayerId()));
    }

    /**
     * Calculates the longest road of a specific player on the entire board.
     * Should be used after the players longest has been broken.
     * <p>
     * The result will be put into {@link #playerRoadMap} a check is made
     * if that players longest road is the longest road on the whole board.
     * The result will be put into {@link #playerWithLongestRoad}
     *
     * @param player the player whose longest road is being searched
     * @implNote Uses a depth first search algorithm. Calls {@link #findLongestRoadFromCoord(Coord, Player)} for each road end
     */
    public void findLongestRoadAfterBreak(Player player) {
        playerRoadMap.remove(player);
        Set<Coord> allPlayerRoads = new HashSet<>();
        for (Map.Entry<Coord, RoadPiece> entry : edgeMap.entrySet()) {
            if (entry.getValue() != null && entry.getValue().getPlayer().equals(player)) {
                allPlayerRoads.add(entry.getKey());
            }
        }
        Set<Set<Coord>> roadSets = new HashSet<>();
        for (Coord roadCoord : allPlayerRoads) {
            if (roadSets.stream().noneMatch(roadSet -> roadSet.contains(roadCoord))) {
                roadSets.add(BoardUtils.getAllConnectedPlayerRoads(this, roadCoord, player.getPlayerId()));
            }
        }
        for (Set<Coord> roadSet : roadSets) {
            Optional<Coord> startCoord = roadSet.stream().findFirst();
            startCoord.ifPresent(coord -> findLongestRoad(coord, player, roadSet));
        }
        updateLongestRoad();
    }


    /**
     * Calculates the longest road of a specific player around the given edge
     * <p>
     * The result will be put into {@link #playerRoadMap} a check is made
     * if that players longest road is the longest road on the whole board.
     * The result will be put into {@link #playerWithLongestRoad}
     * <p>
     * The search will start from all possible road ends if the road is not circular.
     *
     * @param startCoord the <b>Edge</b> coordinate from which the search is started if the road is circular
     * @param player     the player whose longest road is being searched
     * @implNote Uses a depth first search algorithm. Calls {@link #findLongestRoadFromCoord(Coord, Player)} for each road end
     */
    public void findLongestRoadFromEdge(Coord startCoord, Player player) {
        if (edgeMap.get(startCoord) == null) throw new IllegalArgumentException("coord must not be null");
        int playerId = player.getPlayerId();
        Set<Coord> allConnectedRoads = BoardUtils.getAllConnectedPlayerRoads(this, startCoord, playerId);
        findLongestRoad(startCoord, player, allConnectedRoads);
        updateLongestRoad();
    }

    private void findLongestRoad(Coord startCoord, Player player, Set<Coord> allConnectedRoads) {
        Set<Coord> allRoadEnds = new HashSet<>();
        //Adds all obvious road ends(only 1 connected road) to a set
        for (Coord coord : allConnectedRoads) {
            if (BoardUtils.getConnectedPlayerRoadsWithoutBreak(this, coord, player.getPlayerId()).size() == 1) {
                allRoadEnds.add(coord);
            }
        }
        //If there were no obvious road ends then the road is either circular or only 1 long and so the initial coord is used
        if (allRoadEnds.isEmpty()) {
            allRoadEnds.add(startCoord);
        }
        for (Coord endCoord : allRoadEnds) {
            findLongestRoadFromCoord(endCoord, player);
        }
    }

    /**
     * Calculates the longest road of a specific player by using a depth first search algorithm
     * <p>
     * The result will be put into {@link #playerRoadMap} but no check is made
     * if that players longest road is the longest road on the whole board.
     *
     * @param startCoord the coordinate from which the search is started
     * @param player     the player whose longest road is being searched
     */
    private void findLongestRoadFromCoord(Coord startCoord, Player player) {
        int playerId = player.getPlayerId();
        Deque<Path> stack = new ArrayDeque<>();
        /*
        If a path forks into multiple roads their neighbours are being stored in this map
        to mark them as separate paths that shouldn't be followed.
        */
        Map<SplitInfo, Coord[]> splitPaths = new HashMap<>();
        //The startCoord will be pushed onto a stack encapsulated in a NodeInfo object
        stack.push(new Path(startCoord, null, 1, new LinkedHashSet<>()));
        while (!stack.isEmpty()) {
            Path curPath = stack.pop();
            Coord curCoord = curPath.getCurCoord();
            Coord lastCoord = curPath.getLastCoord();
            int curPathLength = curPath.getPathLength();
            //This set contains all the nodes that make up the current path
            Set<Coord> visited = curPath.getVisited();

            //Removes all nodes that have already been visited
            Set<Coord> directlyConnectedRoads = BoardUtils.getConnectedPlayerRoadsFromEdge(this, curCoord, playerId);
            directlyConnectedRoads.removeAll(visited);
            //The SplitInfo for the fork from the last node to the current node
            SplitInfo splitLookup = new SplitInfo(curCoord, lastCoord);
            //Check if there were other outgoing paths from the last node
            if (splitPaths.get(splitLookup) != null) {
                //Removes all neighbour nodes that are part of the outgoing paths
                directlyConnectedRoads.removeAll(Arrays.asList(splitPaths.get(splitLookup)));
            }

            visited.add(curCoord);
            //If there are still neighbours left they will be pushed onto the stack
            for (Coord neighbour : directlyConnectedRoads) {
                //Check if this road path is being blocked by another players settlement
                Coord cornerCoord = Coord.getCornerFromEdges(curCoord, neighbour);
                if (cornerHasOtherPlayerSettlement(cornerCoord, playerId)) {
                    continue;
                }
                //Check if there is a fork in the path
                if (directlyConnectedRoads.size() >= 2) {
                    splitPaths.put(new SplitInfo(neighbour, curCoord), directlyConnectedRoads.toArray(new Coord[0]));
                }
                stack.push(new Path(neighbour, curCoord, curPathLength + 1, new LinkedHashSet<>(visited)));
            }
            //Removes the SplitInfo to allow paths over the same nodes but started on different end nodes to function
            splitPaths.remove(splitLookup);

            //Check if this is the end of the current path & Update the length of the longest path
            if (directlyConnectedRoads.isEmpty() && curPathLength > playerRoadMap.getOrDefault(player, new Path()).getPathLength()) {
                playerRoadMap.put(player, curPath);
            }
        }
    }

    private void updateLongestRoad() {
        int longestRoad = 5;
        List<Player> newLongestRoads = new ArrayList<>();
        for (Map.Entry<Player, Path> entry : playerRoadMap.entrySet()) {
            if (entry.getValue().getPathLength() > longestRoad) {
                longestRoad = entry.getValue().getPathLength();
                newLongestRoads.clear();
                newLongestRoads.add(entry.getKey());
            } else if (longestRoad == entry.getValue().getPathLength()) {
                newLongestRoads.add(entry.getKey());
            }
        }
        if (newLongestRoads.isEmpty()) { //No one with road > 4
            playerWithLongestRoad = null;
        } else if (!newLongestRoads.contains(playerWithLongestRoad)) { //Player doesnt have longest road or isnt tied
            if (newLongestRoads.size() == 1) { //One other player has longest road
                playerWithLongestRoad = newLongestRoads.get(0);
            } else { //Multiple other players are tied
                playerWithLongestRoad = null;
            }
        }
    }

    // -------------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------------

    /**
     * Returns the player who has the longest road
     *
     * @return the player who has the longest road
     */
    public Player getPlayerWithLongestRoad() {
        return playerWithLongestRoad;
    }

    /**
     * Gets the {@code CornerPiece} object that is at the given coordinate
     *
     * @param cornerCoord any <b>Corner</b> {@code Coord}
     * @return the {@code CornerPiece} object that is at the given coordinate, or {@code null} if no corner piece has been placed there
     */
    public CornerPiece getCornerPiece(Coord cornerCoord) {
        return cornerMap.get(cornerCoord);
    }

    /**
     * Gets the {@code RoadPiece} object that is at the given coordinate
     *
     * @param coord any <b>Edge</b> {@code Coord}
     * @return the {@code RoadPiece} object that is at the given coordinate, or {@code null} if no road has been placed there
     */
    public RoadPiece getRoadPiece(Coord coord) {
        return edgeMap.get(coord);
    }

    /**
     * Checks if the corner has a settlement from a different player on it
     *
     * @param cornerCoord the {@code Coord} of the <b>Corner</b>
     * @param playerId    the player who has a road on both edges
     * @return {@code true} if there is a settlement from a different player on the corner, otherwise {@code false}
     */
    public boolean cornerHasOtherPlayerSettlement(Coord cornerCoord, int playerId) {
        return cornerCoord != null && getCornerPiece(cornerCoord) != null && getCornerPiece(cornerCoord).getPlayerId() != playerId;
    }

    /**
     * Check if a given edge is on the board/not in water
     *
     * @param coord any <b>Edge</b> {@code Coord}
     * @return {@code true} if the given edge is on the map, otherwise {@code false}
     */
    public boolean edgeIsOnMap(Coord coord) {
        if (!coord.getNodeType().equals(MapNode.EDGE)) throw new IllegalArgumentException();
        return edgeMap.containsKey(coord);
    }

    /**
     * Check if a given corner is on the board/not in water
     *
     * @param coord any <b>Corner</b> {@code Coord}
     * @return {@code true} if the given corner is on the map, otherwise {@code false}
     */
    public boolean cornerIsOnMap(Coord coord) {
        if (!coord.getNodeType().equals(MapNode.CORNER)) throw new IllegalArgumentException();
        return cornerMap.containsKey(coord);
    }

    /**
     * Check if a given hexagon is on the board/not in water
     *
     * @param coord any <b>Hexagon</b> {@code Coord}
     * @return {@code true} if the given hexagon is on the map, otherwise {@code false}
     */
    public boolean hexagonIsOnMap(Coord coord) {
        if (!coord.getNodeType().equals(MapNode.HEX)) throw new IllegalArgumentException();
        return resourceTileMap.containsKey(coord);
    }

    /**
     * Returns a map with all currently placed pieces
     *
     * @return a map with all currently placed pieces
     */
    public Map<Coord, PieceDTO> getAllPlacedPieces() {
        Map<Coord, PieceDTO> placed = new HashMap<>();
        cornerMap.entrySet().stream().filter(ent -> ent.getValue() != null).forEach(
                ent -> placed.put(ent.getKey(), new PieceDTO(ent.getValue().getPieceType(), ent.getValue().getPlayer().createDTO())));
        edgeMap.entrySet().stream().filter(ent -> ent.getValue() != null).forEach(
                ent -> placed.put(ent.getKey(), new PieceDTO(ent.getValue().getPieceType(), ent.getValue().getPlayer().createDTO())));
        return placed;
    }

    /**
     * Returns the port that is at the given coordinate
     *
     * @param coord any <b>Corner</b> {@code Coord}
     * @return the {@code Port} at the coordinate, or {@code null} if there is no port
     */
    public Port getPortFromCoord(Coord coord) {
        if (!cornerIsAPort(coord)) throw new IllegalArgumentException();
        return fastPortMap.get(coord);
    }

    /**
     * Check if there is a port at the given corner coordinate
     *
     * @param coord any <b>Corner</b> {@code Coord}
     * @return {@code true} if a port is at the given coordinate, otherwise {@code false}
     */
    public boolean cornerIsAPort(Coord coord) {
        if (!coord.getNodeType().equals(MapNode.CORNER)) throw new IllegalArgumentException();
        return fastPortMap.containsKey(coord);
    }

    /**
     * Returns the longest road of a player
     *
     * @param player any player actively playing
     * @return the longest road of a player
     */
    public Path getLongestPlayerRoad(Player player) {
        return playerRoadMap.getOrDefault(player, new Path());
    }

    /**
     * Returns a copy of the Set filled with all corner coords on this board
     *
     * @return a copy of the Set filled with all corner coords on this board
     */
    public Set<Coord> getAllCornerCoords() {
        return new HashSet<>(cornerMap.keySet());
    }

    /**
     * Returns a copy of the Set filled with all hexagon coords on this board
     *
     * @return a copy of the Set filled with all hexagon coords on this board
     */
    public Set<Coord> getAllHexagonCoords() {
        return new HashSet<>(resourceTileMap.keySet());
    }


// -------------------------------------------------------------------------------
// Helper Classes
// -------------------------------------------------------------------------------

    /**
     * If a path forks into multiple roads this class can be used to store that fork
     */
    private static class SplitInfo {
        private final Coord nextCoord;
        private final Coord curCoord;

        /**
         * Constructor
         *
         * @param nextCoord one of the coordinates that the path splits into
         * @param curCoord  the coordinate at which the fork is
         */
        public SplitInfo(Coord nextCoord, Coord curCoord) {
            this.nextCoord = nextCoord;
            this.curCoord = curCoord;
        }

        @Override
        public int hashCode() {
            return Objects.hash(nextCoord, curCoord);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            SplitInfo splitInfo = (SplitInfo) obj;
            return Objects.equals(nextCoord, splitInfo.nextCoord) && Objects.equals(curCoord, splitInfo.curCoord);
        }
    }

    /**
     * An immutable class used to store a path. Contains the current node and all previously visited nodes.
     * Also contains the current path length and the last visited node to reduce access time for common operations.
     */
    @SuppressWarnings("PMD.ShortClassName")
    public static class Path {
        private final Coord curCoord;
        private final Coord lastCoord;
        private final int pathLength;
        private final Set<Coord> visited;

        /**
         * Constructor for empty Path with null coords
         */
        public Path() {
            this(null, null, 0, new HashSet<>());
        }

        /**
         * Constructor
         *
         * @param curCoord   the newest node on this path
         * @param lastCoord  the node last visited on this path
         * @param pathLength the length of this path
         * @param visited    all previously visited nodes on this path
         */
        public Path(Coord curCoord, Coord lastCoord, int pathLength, Set<Coord> visited) {
            this.curCoord = curCoord;
            this.lastCoord = lastCoord;
            this.pathLength = pathLength;
            this.visited = visited;
        }

        /**
         * Returns the {@code Coord} of the newest node on this path
         *
         * @return the {@code Coord} of the newest node on this path
         */
        public Coord getCurCoord() {
            return curCoord;
        }

        /**
         * Returns the length of this path
         *
         * @return the length of this path
         */
        public int getPathLength() {
            return pathLength;
        }

        /**
         * Returns a {@code Set} with the {@code Coord} of all previously visited nodes on this path
         *
         * @return a {@code Set} with the {@code Coord} of all previously visited nodes on this path
         */
        public Set<Coord> getVisited() {
            return visited;
        }


        /**
         * Returns the {@code Coord} of the node last visited on this path
         *
         * @return the {@code Coord} of the node last visited on this path
         */
        public Coord getLastCoord() {
            return lastCoord;
        }

        @Override
        public int hashCode() {
            return Objects.hash(curCoord, lastCoord, pathLength, visited);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Path path = (Path) obj;
            return pathLength == path.pathLength && Objects.equals(curCoord, path.curCoord) && Objects.equals(lastCoord,
                    path.lastCoord) && Objects.equals(visited, path.visited);
        }

        @Override
        public String toString() {
            return "NodeInfo{" +
                    "curCoord=" + curCoord +
                    ", lastCoord=" + lastCoord +
                    ", pathLength=" + pathLength +
                    ", visited=" + visited +
                    '}';
        }

    }


}